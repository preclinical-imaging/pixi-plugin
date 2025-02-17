package org.nrg.xnatx.plugins.pixi.inveon.importer;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.fileExtraction.Format;
import org.nrg.xnat.helpers.ZipEntryFileWriterWrapper;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnatx.plugins.pixi.inveon.factories.InveonImageRepresentationFactory;
import org.nrg.xnatx.plugins.pixi.inveon.models.InveonImageRepresentation;
import org.nrg.xnatx.plugins.pixi.inveon.models.InveonSessionFiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.nrg.xnat.archive.Operation.Rebuild;

@Slf4j
@ImporterHandler(handler = InveonImporter.IMPORTER_HANDLER)
public class InveonImporter extends ImporterHandlerA {

    public static final String IMPORTER_HANDLER = "INVEON";

    public static final String SUBJECT_LABELING_OPTION_PARAM = "subjectLabelOption";
    public static final String SESSION_LABELING_OPTION_PARAM = "sessionLabelOption";
    public static final String SUBJECT_LABEL_REGEX_PARAM = "subjectLabelRegex";
    public static final String SESSION_LABEL_REGEX_PARAM = "sessionLabelRegex";

    private static final String UNKNOWN_SESSION_LABEL = "inveon_unknown";

    private final FileWriterWrapperI fw;
    private final InputStream in;
    private final UserI user;
    private final Map<String, Object> params;
    private final Format format;
    private final Date uploadDate;
    private final Set<String> uris;
    private final Set<Path> timestampDirectories;
    private InveonImageRepresentationFactory factory = new InveonImageRepresentationFactory();

    private final Set<SessionData> sessions;

    // Key in the inveonSessionFilesMap is the session label
    private final Map<String, InveonSessionFiles> inveonSessionFilesMap = new HashMap<>();
    private final Map<String, InveonImageRepresentation> inveonScanFilesMap = new HashMap<>();

    private int inveonImageIndex = 0;

    public InveonImporter(final Object listenerControl,
                          final UserI user,
                          final FileWriterWrapperI fw,
                          final Map<String, Object> params) throws IOException {
        super(listenerControl, user);
        this.user = user;
        this.params = params;
        this.in = fw.getInputStream();
        this.fw = fw;
        this.format = Format.getFormat(fw.getName());
        this.uploadDate = new Date();
        this.uris = Sets.newLinkedHashSet();
        this.timestampDirectories = Sets.newLinkedHashSet();
        this.sessions = Sets.newLinkedHashSet();
    }


    @Override
    public List<String> call() throws ClientException, ServerException {
        log.info("Importing Inveon data for user {}, upload date {}, file name {}",
                 user.getLogin(), uploadDate, fw.getName());
        log.debug("Importing Inveon data with parameters: {}", params);

        // Project ID is required
        if (!params.containsKey(URIManager.PROJECT_ID)) {
            ClientException e = new ClientException("PROJECT_ID is a required parameter for Inveon session uploads.");
            log.error("Project ID required for Inveon session uploads", e);
            throw e;
        }

        String projectId = (String) params.get(URIManager.PROJECT_ID);

        // Only accepting ZIP format
        if (format == Format.ZIP) {
            try (final ZipInputStream zin = new ZipInputStream(in)) {
                log.debug("Zip file received by Inveon importer.");

                ZipEntry ze = zin.getNextEntry();
                while (null != ze) {
                    // Make no assumptions about folder structure.
                    // Stage and organize files based on parameters entered along with upload request.
                    // These are normally made in the Web UI by a user.
                    if (!ze.isDirectory()) {
                        ze = stageInveonFile(projectId, ze, zin);
                    }
                    ze = zin.getNextEntry();
                }
            } catch (IOException | ServerException | ClientException e) {
                log.error("Error uploading Inveon session.", e);
                cleanupOnImportFailure();
                throw new ServerException(e);
            } catch (Exception e) {
                log.error("Error uploading Inveon session.", e);
                cleanupOnImportFailure();
                throw e;
            }
        } else {
            ClientException e = new ClientException("Unsupported file format " + format);
            log.error("Unsupported file format: {}; upload only supports ZIP files", format);
            throw e;
        }

        // Work through all files that were staged during the unzip process above.
        // The call the processSessionMap walks through everything and creates
        // prearchive entries that will be the input to the Archive process.
        try {
            processScanMap(projectId);
            processSessionMap(projectId);
        } catch (IOException e) {
            log.error("Error uploading Inveon session(s).", e);
            cleanupOnImportFailure();
            throw new ServerException(e);
        }

        // Files have been staged and placed in folders in the prearchive.
        // Make queue entries to trigger session building.

        // Send a build request after all sessions have been imported
        sessions.forEach(this::sendSessionBuildRequest);

        return Lists.newArrayList(uris);
    }

    // Review the entries in the map of scan files
    // 1. Throw an error if there are any entries that are missing the .hdr or .img file.
    // 2. Determine session information and create session files

    private void processScanMap(String projectId) throws ServerException, IOException {
        // Make a pass through the scan files and throw an exception if we encounter an error
        log.debug("InveonImporter::processScanMap");
        for (Map.Entry<String, InveonImageRepresentation> entry: inveonScanFilesMap.entrySet()) {
            String key = entry.getKey();
            InveonImageRepresentation inveonImageRepresentation = entry.getValue();
            if ((inveonImageRepresentation.getHeaderFileName() == null) || inveonImageRepresentation.getHeaderFileName().isEmpty()) {
                throw new ServerException("No header file specified for this path: " + key);
            } else if ((inveonImageRepresentation.getPixelFileName() == null) || inveonImageRepresentation.getPixelFileName().isEmpty()) {
                throw new ServerException("No pixel file specified for this path: " + key);
            }
        }

        // If the user specified a session label, then ignore session label options
        if (params.containsKey(URIManager.EXPT_LABEL)) {
            constructSessions();
            return;
        }

        // We believe the scan files are consistent. Create sessions from these.
        String sessionLabelOption = (String) params.get("sessionLabelOption");
        log.debug("sessionLabelOption: {}", sessionLabelOption);
        if (sessionLabelOption == null) {
            throw new ServerException("No value passed for sessionLabelOption");
        }
        if (sessionLabelOption.equals("pcif")) {
            constructSessions();
        } else if (sessionLabelOption.equals("study_identifier")) {
            constructSessions();
        } else if (sessionLabelOption.equals("datetime")) {
            constructSessions();
        } else if (sessionLabelOption.equals("file_name")) {
            constructSessions();
        } else {
            throw new ServerException("Unrecognized value for sessionLabelOption: " + sessionLabelOption);
        }
    }

    // This method assumes that the caller has already determined that the scan files are consistent.
    // That is, there are no cases where we have a .img file and no .img.hdr file and no cases where
    // the .img file is missing.
    private void  constructSessions() throws ServerException, IOException {
        log.debug("InveonImporter::constructSessions");
        for (Map.Entry<String, InveonImageRepresentation> entry: inveonScanFilesMap.entrySet()) {
            InveonImageRepresentation inveonImageRepresentation = entry.getValue();

            // We know we have both the .img and .img.hdr files
            // Parse the header file and extract the metadata so we can build the scan and session objects
            factory.fillInveonHeaderMap(inveonImageRepresentation);

            String sessionLabel = extractSessionLabel(inveonImageRepresentation);
            InveonSessionFiles inveonSessionFiles = inveonSessionFilesMap.get(sessionLabel);
            if (inveonSessionFiles == null) {
                inveonSessionFiles = new InveonSessionFiles();
                inveonSessionFiles.setSessionLabel(sessionLabel);
                inveonSessionFiles.setPrearchiveTimestampPath(inveonImageRepresentation.getPrearchiveTimestampPath());

                // The timestamp will be used as part of a directory path
                inveonSessionFiles.setTimeStamp(inveonImageRepresentation.getTimestamp());
                log.debug("New Session Object with timestamp {}", inveonSessionFiles.getTimeStamp());
            } else {
                // This is the second (or third, ...) scan in this session
                // Because of code later in XNAT core, we want to move these files
                // into the same timestamp folder as the first scan in this session

                Set<String> keySet = inveonSessionFiles.getInveonImageMap().keySet();
                String randomKey = keySet.iterator().next();
                consolidateScanFolders(inveonSessionFiles.getInveonImageRepresentation(randomKey), inveonImageRepresentation);

            }
            String imageName = inveonImageRepresentation.getName();
            inveonSessionFiles.putInveonImageRepresentation(imageName, inveonImageRepresentation);
            inveonSessionFilesMap.put(sessionLabel, inveonSessionFiles);

            log.debug("PUT {} {} {}", sessionLabel, imageName, inveonImageRepresentation.getPixelFileName());
        }
    }

    private void consolidateScanFolders(InveonImageRepresentation existingScan, InveonImageRepresentation scanToConsolidate) throws ServerException, IOException {
        // Grab the subfolder of the existing path. This should be named with the session label.
        // We know at this stage that string is most likely 'inveon_unknown'.
        File targetFolder = extractSingleSubFolder(existingScan.getPrearchiveTimestampPath());

        // Grab the subfolder of the scan to be consolidated. Then grab the subfolder beneath that.
        File toConsolidateSessionFolder = extractSingleSubFolder(scanToConsolidate.getPrearchiveTimestampPath());
        File toConsolidateScanFolder    = extractSingleSubFolder(toConsolidateSessionFolder.getAbsolutePath());
        String scanFolderName = toConsolidateScanFolder.getName();

        log.debug("Target: {} To Consolidate: {}", targetFolder.getAbsolutePath(), toConsolidateScanFolder.getAbsolutePath());
        String toConsolidateTarget = targetFolder.getPath() + "/" + scanFolderName;
        if (toConsolidateTarget.equals(scanToConsolidate.getPrearchiveTempFolder())) {
            // This means the scan to be consolidated is already under the target folder.
            log.debug("No need to move scan to target folder. It is already in the target folder.");
        } else {
            log.debug("Final consolidated path: {}", toConsolidateTarget);
            File sessionFolderToDelete   = toConsolidateScanFolder.getParentFile();
            File timestampFolderToDelete = sessionFolderToDelete.getParentFile();

            //FileUtils.moveDirectory(toConsolidateScanFolder, toConsolidateSessionFolder);
            FileUtils.moveDirectoryToDirectory(toConsolidateScanFolder, targetFolder, false);
            scanToConsolidate.setPrearchiveTempFolder(toConsolidateTarget);

            FileUtils.deleteDirectory(sessionFolderToDelete);
            FileUtils.deleteDirectory(timestampFolderToDelete);
        }
    }

    private File extractSingleSubFolder(String path) throws ServerException {
        File[] subfolders = new File(path).listFiles();

        if (subfolders == null || subfolders.length == 0) {
            throw new ServerException("Folder " + path + " has no subfolders. We expected exactly one.");
        } else if (subfolders.length > 1) {
            throw new ServerException("Folder " + path + " has " + subfolders.length + " subfolders. We expected exactly one.");
        }
        // We know there is one item in the collection. Grab that single item as the target folder.
        return subfolders[0];
    }

    private void processSessionMap(String projectId) throws ServerException, IOException {
        log.debug("Inveon Sessions Found: {}", inveonSessionFilesMap.size());
        if (params.containsKey(URIManager.EXPT_LABEL) && inveonSessionFilesMap.size() > 1) {
            // If the user specified a session label, then we cannot handle a zip where the user
            // specified multiple sessions.
            log.error("Cannot handle a zip file with {} sessions where the user specified one label: {}",
                    inveonSessionFilesMap.size(),
                    params.get(URIManager.EXPT_LABEL));
            throw new ServerException(
                    "Cannot handle a zip file with "
                    + inveonSessionFilesMap.size()
                    + " sessions where the user specified one label: "
                    + params.get(URIManager.EXPT_LABEL));

        }

        // The loop below is executed one time for each of the sessions that were identified
        // during the initial staging process.
        // Each iteration corresponds to one session.
        // The inveonSessionFiles instance contains a list of the PET and CT header and pixel files
        // that were discovered during the staging process.
        Iterator<InveonSessionFiles> inveonSessionFilesIterator = inveonSessionFilesMap.values().iterator();
        while (inveonSessionFilesIterator.hasNext()) {
            InveonSessionFiles inveonSessionFiles = inveonSessionFilesIterator.next();
            log.debug("Session label: {}", inveonSessionFiles.getSessionLabel());
            Map<String, InveonImageRepresentation> map = inveonSessionFiles.getInveonImageMap();
            Set<String> imageKeys = inveonSessionFiles.getInveonImageMap().keySet();
            Iterator<String> keyIterator = imageKeys.iterator();
            Set<String> modalitySet = new HashSet<>();

            // Loop through the (potential) PET and CT files
            // Build out the representation of the image (add metadata).
            // Especially, determine the modality (PET or CT) because we need that
            // in the next step to create the correct database entries based on the session type.
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                InveonImageRepresentation inveonImageRepresentation = inveonSessionFiles.getInveonImageRepresentation(key);
                modalitySet.add(map.get(key).getModality());
            }

            // The step above sets the value for modality in the inveonImageRepresentation instances
            // and created a set that lists the modalities that are included.
            // Process the session depending on the type.
            if (modalitySet.contains("PET")) {
                processPETSession(inveonSessionFiles, projectId);
            } else {
                processCTSession(inveonSessionFiles, projectId);
            }
        }
    }

    // Call this method for each file referenced in the zip file that was uploaded.
    // Stage an Inveon native format file by creating or updating an entry in the inveonScanFilesMap.
    // We will process the files in that map later, after we have staged all of the files.
    protected ZipEntry stageInveonFile(final String projectId, ZipEntry ze, ZipInputStream zin) throws IOException, ServerException, ClientException {
        final String pathName = ze.getName();

        String extension = FilenameUtils.getExtension(pathName);
        String fileName  = FilenameUtils.getName(pathName);

        // Skip mac metadata files/directories
        if (fileName.startsWith("__MACOSX") || fileName.startsWith("._") || fileName.startsWith(".DS_Store")) {
            return ze;
        }

        String name = "";
        String scanFileKey = "";
        if (extension.equals("hdr")) {
            //  xxx.img.hdr becomes xxx.img
            name = FilenameUtils.getBaseName(fileName);
            scanFileKey = FilenameUtils.getFullPath(pathName) + FilenameUtils.getBaseName(pathName);
        } else if (extension.equals("img")) {
            //  xxx.img
            name = fileName;
            scanFileKey = FilenameUtils.getFullPath(pathName) + FilenameUtils.getName(pathName);
        } else {
            log.error("Unrecognized file extension: {}; file is not imported into the system.", extension);
        }

        log.debug("Importing file: {} with Scan File Key {}", pathName, scanFileKey);

        // TODO It would be better to extract a scan label from something in the header itself
        String imageName = FilenameUtils.getBaseName(name);
        String scanLabel = imageName;
        log.debug("File Name {} Name {} Image Name {} Scan Label {}", fileName, name, imageName, scanLabel);

        boolean keepFile = (extension.equals("hdr") || extension.equals("img"));

        if (keepFile) {
            // Find existing instance of InveonImageRepresentation based on scanFileKey or create a new one.
            // We are creating a map with one entry for each scan (.img file + .img.hdr file)

            InveonImageRepresentation inveonScan = inveonScanFilesMap.get(scanFileKey);
            if (inveonScan == null) {
                inveonScan = new InveonImageRepresentation();
                inveonScan.setName(imageName);
                inveonScan.setIndex(inveonImageIndex++);

                // The timestamp will be used as part of a directory path
                inveonScan.setTimestamp(PrearcUtils.makeTimestamp());

                Path prearchiveTimestampPath = Paths.get(ArcSpecManager.GetInstance().getGlobalPrearchivePath(), projectId, inveonScan.getTimestamp());
                Path prearchiveTempDirectoryPath = prearchiveTimestampPath.resolve(UNKNOWN_SESSION_LABEL).resolve(scanLabel);
                timestampDirectories.add(prearchiveTimestampPath); // Keep track of timestamp paths, will delete these folders in case of error
                Files.createDirectories(prearchiveTempDirectoryPath);
                inveonScan.setPrearchiveTempFolder(prearchiveTempDirectoryPath.toString());
                inveonScan.setPrearchiveTimestampPath(prearchiveTimestampPath.toString());
            }
            if (extension.equals("hdr")) {
                inveonScan.setHeaderFileName(fileName);
            } else if (extension.equals("img")) {
                inveonScan.setPixelFileName(fileName);
            } else {
                // This should not happen
                throw new ServerException(
                    "Severe coding error. The file extension is not .hdr nor .img, but we should have already tested for this " +
                    fileName);
            }
            Path prearchiveFile = Paths.get(inveonScan.getPrearchiveTempFolder(), fileName);
            ZipEntryFileWriterWrapper zipEntryFileWriterWrapper = new ZipEntryFileWriterWrapper(ze, zin);
            zipEntryFileWriterWrapper.write(prearchiveFile.toFile());
            log.debug("Wrote file: {}", prearchiveFile.toString());

            inveonScanFilesMap.put(scanFileKey, inveonScan);
        } else {
            // TODO Determine if any explicit cleanup is needed for a file we are not keeping
        }
        return ze;
    }

    private void processCTSession(InveonSessionFiles inveonSessionFiles, String projectId) throws ServerException, IOException {
        SessionData session = new SessionData();
        Iterator<InveonImageRepresentation> iterator = inveonSessionFiles.getInveonImageMap().values().iterator();
        boolean firstScan = true;
        Optional<String> subjectId = Optional.of("");
        Optional<String> sessionLabel = Optional.of("");
        Path sessionFolder = null;

        while (iterator.hasNext()) {
            // This outer loop will fill in the data needed for each scan.
            // It will also fill in session level data if this is the first image that is processed
            InveonImageRepresentation inveonImageRepresentation = iterator.next();
            Optional<String> headerFileName = Optional.ofNullable(inveonImageRepresentation.getHeaderFileName());
            Optional<String> pixelFileName = Optional.ofNullable(inveonImageRepresentation.getPixelFileName());
            if (! headerFileName.isPresent()) {
                log.error("Inveon file with name {} does not have a header file", inveonImageRepresentation.getName());
                //TODO fix
                return;
            }
            if (! pixelFileName.isPresent()) {
                log.error("Inveon file with name {} does not have a pixel file", inveonImageRepresentation.getName());
                //TODO fix
                return;
            }
            log.debug("Header file: {}", headerFileName);
            log.debug("Pixel file:  {}", pixelFileName);

            String scanDateString = inveonImageRepresentation.getHeaderValue("SCAN_DATE");
            String scanTimeString = inveonImageRepresentation.getHeaderValue("SCAN_TIME");

            log.debug("Scan Date from scan_time field: {}", scanDateString);
            log.debug("Scan Time from scan_time field: {}", scanTimeString);

            Optional<String> scanLabel = Optional.empty();
            Optional<String> uid = Optional.empty();

            Optional<Date> scanDate = Optional.ofNullable(factory.transformScanDate(inveonImageRepresentation));

            log.debug("Transformed Scan Date: {}", scanDate.get().toString());

            if (firstScan) {
                // Override the subject ID if it is provided in the URI parameters
                if (params.containsKey(URIManager.SUBJECT_ID)) {
                    subjectId = Optional.of((String) params.get(URIManager.SUBJECT_ID));
                } else {
                    subjectId = Optional.ofNullable(extractSubjectId(inveonImageRepresentation));
                }

                // Override the session label if it is provided in the URI parameters
                if (params.containsKey(URIManager.EXPT_LABEL)) {
                    sessionLabel = Optional.of((String) params.get(URIManager.EXPT_LABEL));
                } else {
                    sessionLabel = Optional.ofNullable(extractSessionLabel(inveonImageRepresentation));
                }

                sessionFolder = Paths.get(inveonImageRepresentation.getPrearchiveTimestampPath(), sessionLabel.orElse(UNKNOWN_SESSION_LABEL));

                session.setFolderName(sessionLabel.orElse(UNKNOWN_SESSION_LABEL));
                session.setName(sessionLabel.orElse(UNKNOWN_SESSION_LABEL));
                session.setProject(projectId);
                session.setScan_date(scanDate.orElse(null));
                session.setUploadDate(uploadDate);
                session.setTimestamp(scanDateString);
                session.setTimestamp(inveonImageRepresentation.getTimestamp());

                session.setStatus(PrearcUtils.PrearcStatus.RECEIVING);
                session.setLastBuiltDate(Calendar.getInstance().getTime());
                session.setSubject(subjectId.orElse(""));
                session.setSource(params.get(URIManager.SOURCE));
                session.setPreventAnon(Boolean.valueOf((String) params.get(URIManager.PREVENT_ANON)));
                session.setPreventAutoCommit(Boolean.valueOf((String) params.get(URIManager.PREVENT_AUTO_COMMIT)));
                session.setAutoArchive(shouldAutoArchive(projectId));

                firstScan = false;
            }
            log.debug("Subject ID: {}", subjectId.get());
            log.debug("Session Label: {}", sessionLabel.get());

            if (!scanLabel.isPresent()) {
                if (uid.isPresent()) {
                    scanLabel = uid;
                } else {
                    //scanLabel = Optional.of(UUID.randomUUID().toString());
                    scanLabel = Optional.of(extractScanLabel(inveonImageRepresentation));
                }
            }

            log.debug("Scan Label extracted from InveonImageRepresentation {}", scanLabel);
            Path scanFolder = Paths.get(sessionFolder.toString(), "SCANS", scanLabel.get(), "INVEON");

            // TODO Merge code
            // TODO


            Path prearchiveTempDirectoryPath = Paths.get(inveonImageRepresentation.getPrearchiveTempFolder());
            transferFiles(prearchiveTempDirectoryPath, scanFolder);
            session.setUrl(sessionFolder.toString());
        }
        try {
            PrearcDatabase.addSession(session);
            log.debug("Added session to prearchive database. Project: {} Subject: {} Session: {}", projectId, subjectId, sessionLabel);
        } catch (Exception e) {
            log.error("Unable to add Inveon session", e);
            throw new ServerException(e);
        }
        uris.add(sessionFolder.toString());
        sessions.add(session);
    }

    private void processPETSession(InveonSessionFiles inveonSessionFiles, String projectId) throws ServerException, IOException {
        SessionData session = new SessionData();
        Iterator<InveonImageRepresentation> iterator = inveonSessionFiles.getInveonImageMap().values().iterator();
        boolean firstScan = true;
        Optional<String> subjectId = Optional.of("");
        Optional<String> sessionLabel = Optional.of("");
        Path sessionFolder = null;
        log.debug("Process PET session with subject ID {} and session label {}", subjectId.get(), sessionLabel.get());

        while (iterator.hasNext()) {
            // This outer loop will fill in the data needed for each scan.
            // It will also fill in session level data if this is the first image that is processed
            InveonImageRepresentation inveonImageRepresentation = iterator.next();
            Optional<String> headerFileName = Optional.ofNullable(inveonImageRepresentation.getHeaderFileName());
            Optional<String> pixelFileName = Optional.ofNullable(inveonImageRepresentation.getPixelFileName());
            if (! headerFileName.isPresent()) {
                log.error("Inveon file with name {} does not have a header file", inveonImageRepresentation.getName());
                //TODO fix
                return;
            }
            if (! pixelFileName.isPresent()) {
                log.error("Inveon file with name {} does not have a pixel file", inveonImageRepresentation.getName());
                //TODO fix
                return;
            }
            log.debug("Header file: {}", headerFileName);
            log.debug("Pixel file:  {}", pixelFileName);

            String scanDateString = inveonImageRepresentation.getHeaderValue("SCAN_DATE");
            String scanTimeString = inveonImageRepresentation.getHeaderValue("SCAN_TIME");

            log.debug("Scan Date from scan_time field: {}", scanDateString);
            log.debug("Scan Time from scan_time field: {}", scanTimeString);

            Optional<String> scanLabel = Optional.empty();
            Optional<String> uid = Optional.empty();

            Optional<Date> scanDate = Optional.ofNullable(factory.transformScanDate(inveonImageRepresentation));

            log.debug("Transformed Scan Date: {}", scanDate.get().toString());

            if (firstScan) {
                // Override the subject ID if it is provided in the URI parameters
                if (params.containsKey(URIManager.SUBJECT_ID)) {
                    subjectId = Optional.of((String) params.get(URIManager.SUBJECT_ID));
                } else {
                    subjectId = Optional.ofNullable(extractSubjectId(inveonImageRepresentation));
                }

                // Override the session label if it is provided in the URI parameters
                if (params.containsKey(URIManager.EXPT_LABEL)) {
                    sessionLabel = Optional.of((String) params.get(URIManager.EXPT_LABEL));
                } else {
                    sessionLabel = Optional.ofNullable(extractSessionLabel(inveonImageRepresentation));
                }

                sessionFolder = Paths.get(inveonImageRepresentation.getPrearchiveTimestampPath(), sessionLabel.orElse(UNKNOWN_SESSION_LABEL));

                session.setFolderName(sessionLabel.orElse(UNKNOWN_SESSION_LABEL));
                session.setName(sessionLabel.orElse(UNKNOWN_SESSION_LABEL));
                session.setProject(projectId);
                session.setScan_date(scanDate.orElse(null));
                session.setUploadDate(uploadDate);
                session.setTimestamp(scanDateString);
                session.setTimestamp(inveonImageRepresentation.getTimestamp());

                session.setStatus(PrearcUtils.PrearcStatus.RECEIVING);
                session.setLastBuiltDate(Calendar.getInstance().getTime());
                session.setSubject(subjectId.orElse(""));
                session.setSource(params.get(URIManager.SOURCE));
                session.setPreventAnon(Boolean.valueOf((String) params.get(URIManager.PREVENT_ANON)));
                session.setPreventAutoCommit(Boolean.valueOf((String) params.get(URIManager.PREVENT_AUTO_COMMIT)));
                session.setAutoArchive(shouldAutoArchive(projectId));

                firstScan = false;
            }
            log.debug("Subject ID: {}", subjectId.get());
            log.debug("Session Label: {}", sessionLabel.get());

            if (!scanLabel.isPresent()) {
                if (uid.isPresent()) {
                    scanLabel = uid;
                } else {
                    //scanLabel = Optional.of(UUID.randomUUID().toString());
                    scanLabel = Optional.of(extractScanLabel(inveonImageRepresentation));
                }
            }

            log.debug("Scan Label extracted from InveonImageRepresentation {}", scanLabel);
            Path scanFolder = Paths.get(sessionFolder.toString(), "SCANS", scanLabel.get(), "INVEON");

            // TODO Merge code
            // TODO


            Path prearchiveTempDirectoryPath = Paths.get(inveonImageRepresentation.getPrearchiveTempFolder());
            transferFiles(prearchiveTempDirectoryPath, scanFolder);
            session.setUrl(sessionFolder.toString());
        }
        try {
            PrearcDatabase.addSession(session);
            log.debug("Added session to prearchive database. Project: {} Subject: {} Session: {}", projectId, subjectId, sessionLabel);
        } catch (Exception e) {
            log.error("Unable to add Inveon session", e);
            throw new ServerException(e);
        }
        uris.add(sessionFolder.toString());
        sessions.add(session);
    }

    // Extract a value for Subject ID for this instance of InveonImageRepresentation
    // TODO:  Need a better scheme than assume the PCIF naming convention
    private String extractSubjectId(InveonImageRepresentation inveonImageRepresentation) {
        if (params.containsKey(URIManager.SUBJECT_ID)) {
            return (String) params.get(URIManager.SUBJECT_ID);
        }

        String subjectLabelingOption = (String) params.getOrDefault(SUBJECT_LABELING_OPTION_PARAM, "subject_identifier");
        String subjectLabelRegex = (String) params.getOrDefault(SUBJECT_LABEL_REGEX_PARAM, "(.*)");

        if (subjectLabelingOption.toLowerCase().contains("hotel")) {
            return "Hotel";
        }

        String subjectLabel = inveonImageRepresentation.getHeaderValue(subjectLabelingOption);
        if (StringUtils.isBlank(subjectLabel)) {
            return "Unknown";
        }

        try {
            // Apply the regex to extract the subject label
            Pattern pattern = Pattern.compile(subjectLabelRegex);
            Matcher matcher = pattern.matcher(subjectLabel);

            if (matcher.find()) {
                subjectLabel = matcher.group(1);
            } else {
                log.warn("No match found for subject label regex: '{}'", subjectLabelRegex);
                subjectLabel = "Unknown";
            }

            if (StringUtils.isBlank(subjectLabel)) {
                subjectLabel = "Unknown";
            }
        } catch (Exception e) {
            // Not best practice to catch Exception, but we shouldn't fail the upload if regex fails
            log.error("Error applying subject label regex '{}' to subject label '{}'. Error: {}",
                      subjectLabelRegex, subjectLabel, e.getMessage());
            subjectLabel = "Unknown";
        }

        return subjectLabel;
    }

    private String extractSessionLabel(InveonImageRepresentation inveonImageRepresentation) {
        if (params.containsKey(URIManager.EXPT_LABEL)) {
            return (String) params.get(URIManager.EXPT_LABEL);
        }

        String sessionLabelingOption = (String) params.getOrDefault(SESSION_LABELING_OPTION_PARAM, "study_identifier");
        String sessionLabelRegex = (String) params.getOrDefault(SESSION_LABEL_REGEX_PARAM, "(.*)");

        String sessionLabel = null;
        log.debug("In extractSessionLabel, option = {}", sessionLabelingOption);

        switch (sessionLabelingOption) {
            case "study_identifier":
                sessionLabel = inveonImageRepresentation.getHeaderValue("study_identifier");
                break;
            case "datetime":
                sessionLabel =
                        inveonImageRepresentation.getHeaderValue("SCAN_DATE") + "_" +
                        inveonImageRepresentation.getHeaderValue("SCAN_TIME");
                break;
            case "file_name":
                sessionLabel = inveonImageRepresentation.getPixelFileName();

                if (sessionLabel.endsWith(".img")) {
                    sessionLabel = sessionLabel.substring(0, sessionLabel.length() - 4);
                }
                break;

            case "pcif":
                // Special case for internal WUSTL PCIF lab
                sessionLabel = inveonImageRepresentation.getPixelFileName();

                if (StringUtils.isBlank(sessionLabel)) {
                    sessionLabel = inveonImageRepresentation.getHeaderFileName();
                }

                if (StringUtils.isBlank(sessionLabel)) {
                    sessionLabel = UNKNOWN_SESSION_LABEL;
                } else {
                    // File name: mpet5013c_ct1_v1.ct.img -> Session label: mpet5013c_1
                    // File name: mpet5013c_em1_v1.pet.img -> Session label: mpet5013c_1
                    // File name: mpet5013c_ct2_v1.ct.img -> Session label: mpet5013c_2
                    // File name: mpet5013c_em2_v1.pet.img -> Session label: mpet5013c_2
                    // The 1 or 2 is the timepoint

                    String regex = "(.*)_(ct|em)(\\d+)_v(\\d+).(.*)";
                    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(sessionLabel);

                    if (matcher.matches() && matcher.groupCount() >= 3) {
                        String prefix = matcher.group(1);
                        String timepoint = matcher.group(3);
                        sessionLabel = prefix + "_" + timepoint;
                    } else if (sessionLabel.length() > 9) {
                        sessionLabel = sessionLabel.substring(0, 9);
                    }
                }

                break;
            default:
                sessionLabel = UNKNOWN_SESSION_LABEL;
                break;
        }

        sessionLabel = sessionLabel.replaceAll("[^a-zA-Z0-9]", "_");

        try {
            // Apply the regex to extract the session label
            Pattern pattern = Pattern.compile(sessionLabelRegex);
            Matcher matcher = pattern.matcher(sessionLabel);

            if (matcher.find()) {
                sessionLabel = matcher.group(1);
            } else {
                log.warn("No match found for session label regex: '{}'", sessionLabelRegex);
                sessionLabel = UNKNOWN_SESSION_LABEL;
            }

            if (StringUtils.isBlank(sessionLabel)) {
                sessionLabel = UNKNOWN_SESSION_LABEL;
            }
        } catch (Exception e) {
            // Not best practice to catch Exception, but we shouldn't fail the upload if regex fails
            log.error("Error applying session label regex '{}' to session label '{}'. Error: {}",
                      sessionLabelRegex, sessionLabel, e.getMessage());
            sessionLabel = UNKNOWN_SESSION_LABEL;
        }

        log.debug("Final session label: {}", sessionLabel);
        return sessionLabel;
    }

    // Extract a value for Scan Label for this instance of InveonImageRepresentation
    // TODO:  Need a better scheme than assume the PCIF naming convention
    private String extractScanLabel(InveonImageRepresentation inveonImageRepresentation) {
        log.debug("extractScanLabel returns {}", inveonImageRepresentation.getName());
        return inveonImageRepresentation.getName();
    }

    private PrearchiveCode shouldAutoArchive(final String projectId) {
        if (params.containsKey("dest")) {
            if (params.get("dest").equals("/prearchive")) {
                return PrearchiveCode.Manual;
            } else if (params.get("dest").equals("/archive")) {
                return PrearchiveCode.AutoArchive;
            }
        }

        if (null == projectId) {
            return null;
        }

        XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);

        if (project == null) {
            return null;
        }

        ArcProject arcProject = project.getArcSpecification();
        if (arcProject == null) {
            log.warn("Tried to get the arc project from project {}, but got null in return. Returning null for the " +
                    "prearchive code, but it's probably not good that the arc project wasn't found.", project.getId());
            return null;
        }

        return PrearchiveCode.code(arcProject.getPrearchiveCode());
    }

    private void transferFiles(Path temporarySessionFolder, Path scanFolder) throws IOException {
        log.debug("transfer files from {} to {}", temporarySessionFolder.toString(), scanFolder.toString());
        // mkdir if it doesn't exist
        if (Files.notExists(scanFolder)) {
            Files.createDirectories(scanFolder);
        }

        // Move each file individually
        try (DirectoryStream<Path> tempFileStream = Files.newDirectoryStream(temporarySessionFolder)) {
            for (Path tempFile: tempFileStream) {
                log.debug("Source file in move: {}", tempFile.toString());
                log.debug("Prearchive destination: {}", scanFolder.resolve(tempFile.getFileName()));
                Files.move(tempFile, scanFolder.resolve(tempFile.getFileName()));
            }
        }

        log.debug("Delete temporary session folder: {}", temporarySessionFolder.toString());
        Files.delete(temporarySessionFolder);
        File parentFolder = temporarySessionFolder.getParent().toFile();
        if (parentFolder.exists()) {
            recursiveFolderDelete(parentFolder);
        }
    }

    private void cleanupOnImportFailure() {
        try {
            for (Path timestampDirectory : timestampDirectories) {
                FileUtils.deleteDirectory(timestampDirectory.toFile());
            }

            for (SessionData session : sessions) {
                PrearcDatabase.deleteSession(session.getName(), session.getTimestamp(), session.getProject());
            }
        } catch (Exception e) {
            log.error("Error deleting sessions from the prearchive", e);
        }
    }

    private void recursiveFolderDelete(File folder) {
        log.debug("To Delete: {}", folder.getAbsolutePath());
        File[] listOfFiles = folder.listFiles();

        // Loop through any entries in this folder.
        // If any entry is a folder, recursively call this function with that folder.
        for (int j = 0; j < listOfFiles.length; j++) {
            if (listOfFiles[j].isDirectory()) {
                recursiveFolderDelete(listOfFiles[j]);
            }
        }

        // If there are no entries, this is a file or an empty folder.
        // In either case, delete it.
        // This marks the end of the recursive nature of this method.
        if (listOfFiles.length == 0) {
            try {
                Files.deleteIfExists(folder.toPath());
            } catch (java.io.IOException e) {
                log.error("Encountered IO exception when trying to delete folder: {}", folder.getAbsolutePath());
            }
        }
    }

    private void sendSessionBuildRequest(SessionData sessionData) {
        try {
            final File sessionDir = PrearcUtils.getPrearcSessionDir(user, sessionData.getProject(), sessionData.getTimestamp(), sessionData.getFolderName(), false);
            XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, Rebuild, sessionData, sessionDir));
            log.debug("Sent PrearchiveOperationRequest {} {} {} {}", sessionData.getProject(), sessionData.getFolderName(), sessionData.getName(), sessionDir.getPath());
        } catch (Exception e) {
            // TODO This does not seem like the right action
            log.error("Unable to request session build. Sitewide prearchive settings will be used instead.");
        }
    }

}
