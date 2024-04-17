package org.nrg.xnatx.plugins.pixi.inveon.importer;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.nrg.xnat.archive.Operation.Rebuild;

@Slf4j
@ImporterHandler(handler = InveonImporter.IMPORTER_HANDLER)
public class InveonImporter extends ImporterHandlerA {

    public static final String IMPORTER_HANDLER = "INVEON";

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
        String sessionLabel = (String) params.get(URIManager.EXPT_LABEL);

        // Only accepting ZIP format
        if (format == Format.ZIP) {
            try (final ZipInputStream zin = new ZipInputStream(in)) {
                log.debug("Zip file received by Inveon importer.");

                ZipEntry ze = zin.getNextEntry();
                while (null != ze) {
                    // Make no assumptions about folder structure.
                    // Stage and organize files based on file naming convention from
                    // WUSTL Pre-Clinical Imaging Facility (PCIF).
                    // TODO: Repair for files that do not follow that convention
                    if (!ze.isDirectory()) {
                        ze = stageInveonFile(projectId, sessionLabel, ze, zin);
                    }
                    ze = zin.getNextEntry();
                }
            } catch (IOException e) {
                log.error("Error uploading Inveon session.", e);
                cleanupOnImportFailure();
                throw new ServerException(e);
            } catch (ServerException | ClientException e) {
            } catch (Exception e) {
                log.error("Error uploading Inveon session.", e);
                cleanupOnImportFailure();
                throw e;
            }
        } else {
            ClientException e = new ClientException("Unsupported file format " + format);
            log.error("Unsupported file format: {}", format);
            throw e;
        }

        // Work through all files that were staged during the unzip process above.
        // The call the processSessionMap walks through everything and creates
        // prearchive entries that will be the input to the Archive process.

        try {
            processSessionMap(projectId);
        } catch (IOException e) {
            log.error("Error uploading Inveon session.", e);
            // TODO repair
//                cleanupOnImportFailure();
            throw new ServerException(e);
        }

        // Files have been staged and placed in folders in the prearchive.
        // Make queue entries to trigger session building.

        log.debug("Send the Session Build Requests");
        // Send a build request after all sessions have been imported
        sessions.forEach(this::sendSessionBuildRequest);
        log.debug("Done sending Session Build Requests");

        // TODO Review this. I don't think we clean up folders because
        // Session building needs them.
        //cleanupPrearchiveFolders();

        return Lists.newArrayList(uris);
    }

    private void processSessionMap(String projectId) throws ServerException, IOException {
        log.debug("processSessionMap");
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

        Iterator<InveonSessionFiles> it = inveonSessionFilesMap.values().iterator();
        while (it.hasNext()) {
            InveonSessionFiles inveonSessionFiles = it.next();
            log.debug("Session Label {} with {} files", inveonSessionFiles.getSessionLabel(), inveonSessionFiles.getInveonImageMap().size());
        }
        log.debug("Done with index");

        // The loop below is executed one time for each of the sessions that were identified
        // during the initial staging process.
        // Each iteration corresponds to one session.
        // The inveonSessionFiles instance contains a list of the PET and CT header and pixel files
        // that were discovered during the staging process.
        Iterator<InveonSessionFiles> inveonSessionFilesIterator = inveonSessionFilesMap.values().iterator();
        while (inveonSessionFilesIterator.hasNext()) {
            InveonSessionFiles inveonSessionFiles = inveonSessionFilesIterator.next();
            log.debug("Session label: {}", inveonSessionFiles.getSessionLabel());
            log.debug("Session Files Count {}", inveonSessionFiles.getInveonImageMap().size());
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
                factory.fillInveonHeaderMap(inveonImageRepresentation);
                log.debug("Key: {}, value {} {} {}", key, map.get(key).getModality(), map.get(key).getHeaderFileName(), map.get(key).getPixelFileName());
                modalitySet.add(map.get(key).getModality());
            }

            // The step above set the value for modality in the inveonImageRepresentation instances
            // and created a set that lists the modalities that are included.
            // Process the session depending on the type.
            if (modalitySet.contains("PET")) {
                processPETSession(inveonSessionFiles, projectId);
            } else {
                processCTSession(inveonSessionFiles, projectId);
            }
        }
    }

    protected ZipEntry stageInveonFile(final String projectId, String sessionLabel, ZipEntry ze, ZipInputStream zin) throws IOException, ServerException, ClientException {
        final String pathName = ze.getName();
        log.debug("Importing file: {}", pathName);
        String extension = FilenameUtils.getExtension(pathName);
        String fileName  = FilenameUtils.getName(pathName);

        String name = "";
        if (extension.equals("hdr")) {
            name = FilenameUtils.getBaseName(fileName);
        } else if (extension.equals("img")) {
            name = fileName;
        } else {
            // TODO Determine if we need anything else
            log.error("Unrecognized file extension: {}", extension);
        }

        // TODO It would be better to extract a scan label from something in the header itself
        String imageName = FilenameUtils.getBaseName(name);
        String scanLabel = imageName;
        log.debug("File Name {} Name {} Image Name {}", fileName, name, imageName);
        log.debug("Session Label: {}, Scan Label: {}", sessionLabel, scanLabel);

        // Find existing instance of InveonSessionFiles based on sessionLabel or create a new one
        // At one time, we computed Session Label from the files and supported multiple sessions in one zip
        // Now, we limit scope to one session in a zip file, and the session label is passed in.
        // It does no harm to have a map that will have a single entry based on the Session Label that
        // is passed in. If we return to computing a Session Label from the files, this map will
        // be handy to have.
        InveonSessionFiles inveonSessionFiles = inveonSessionFilesMap.get(sessionLabel);
        if (inveonSessionFiles == null) {
            inveonSessionFiles = new InveonSessionFiles();
            inveonSessionFiles.setSessionLabel(sessionLabel);

            // The timestamp will be used as part of a directory path
            inveonSessionFiles.setTimeStamp(PrearcUtils.makeTimestamp());
        }

        boolean keepFile = (extension.equals("hdr") || extension.equals("img"));

        if (keepFile) {
            InveonImageRepresentation inveonImageRepresentation = inveonSessionFiles.getInveonImageRepresentation(imageName);
            if (inveonImageRepresentation == null) {
                inveonImageRepresentation = new InveonImageRepresentation();
                inveonImageRepresentation.setName(imageName);
                inveonImageRepresentation.setTimestamp(inveonSessionFiles.getTimeStamp());

                Path prearchiveTimestampPath = Paths.get(ArcSpecManager.GetInstance().getGlobalPrearchivePath(), projectId, inveonSessionFiles.getTimeStamp());
                Path prearchiveTempDirectoryPath = prearchiveTimestampPath.resolve(UNKNOWN_SESSION_LABEL).resolve(scanLabel);
                timestampDirectories.add(prearchiveTimestampPath); // Keep track of timestamp paths, will delete these folders in case of error
                Files.createDirectories(prearchiveTempDirectoryPath);
                inveonImageRepresentation.setPrearchiveTempFolder(prearchiveTempDirectoryPath.toString());
                inveonImageRepresentation.setPrearchiveTimestampPath(prearchiveTimestampPath.toString());
            }

            if (extension.equals("hdr")) {
                inveonImageRepresentation.setHeaderFileName(fileName);
            } else if (extension.equals("img")) {
                inveonImageRepresentation.setPixelFileName(fileName);
            } else {
                // TODO Code for this error case
                // This should not happen
            }
            inveonSessionFiles.putInveonImageRepresentation(imageName, inveonImageRepresentation);
            log.debug("InveonImageRepresentation Name {}", inveonImageRepresentation.getName());
            log.debug("About to place inveonSessionFiles in the Session Files Map, session label is {} {}", sessionLabel, inveonSessionFiles.getSessionLabel());
            inveonSessionFilesMap.put(sessionLabel, inveonSessionFiles);

            Path prearchiveFile = Paths.get(inveonImageRepresentation.getPrearchiveTempFolder(), fileName);
            ZipEntryFileWriterWrapper zipEntryFileWriterWrapper = new ZipEntryFileWriterWrapper(ze, zin);
            zipEntryFileWriterWrapper.write(prearchiveFile.toFile());
            log.debug("Wrote file: {}", prearchiveFile.toString());
        } else {
            // TODO Determine if any explicit cleanup is needed
            /*
            Path prearchiveFile = Paths.get(inveonImageRepresentation.getPrearchiveTempFolder(), fileName);
            ZipEntryFileWriterWrapper zipEntryFileWriterWrapper = new ZipEntryFileWriterWrapper(ze, zin);
            zipEntryFileWriterWrapper.write(prearchiveFile.toFile());
            log.info("Wrote file: {}", prearchiveFile.toString());
            log.info("Discard this file: {}", prearchiveFile.toString());
            Files.deleteIfExists(prearchiveFile);
             */
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
        String[] tokens = inveonImageRepresentation.getName().split("_");
        return tokens[0];
    }

    // Extract a value for Session Label for this file name
    // TODO:  Need a better scheme than assume the PCIF naming convention
    private String extractSessionLabel(String fileName) {
        String[] tokens = fileName.split("_");
        return tokens[0];
    }

    // Extract a value for Session Label for this instance of InveonImageRepresentation
    // TODO:  Need a better scheme than assume the PCIF naming convention
    private String extractSessionLabel(InveonImageRepresentation inveonImageRepresentation) {
        return extractSessionLabel(inveonImageRepresentation.getName());
    }

    // Extract a value for Scan Label for this file name
    // TODO:  Need a better scheme than assume the PCIF naming convention
    private String extractScanLabel(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (extension.equals("img")) {
            int index = fileName.lastIndexOf("img");
            return fileName.substring(0,index);
            //return FilenameUtils.getName(fileName);
        }
        String[] tokens = fileName.split("\\.");
        return tokens[0];
    }


    // Extract a value for Scan Label for this instance of InveonImageRepresentation
    // TODO:  Need a better scheme than assume the PCIF naming convention
    private String extractScanLabel(InveonImageRepresentation inveonImageRepresentation) {
        //return extractScanLabel(inveonImageRepresentation.getName());
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

    private void cleanupPrearchiveFolders() {
        log.debug("Start Inveon prearchive cleanup");
        // TODO Repair the code below
        // Need to review how the temporary folders are created and how to unwind everything
        try {
            for (Path timestampDirectory : timestampDirectories) {
                log.debug("Remove folder: {}", timestampDirectory.toString());
//                recursiveFolderDelete(timestampDirectory.toFile());
            }
        } catch (Exception e) {
            log.error("Error deleting folders from the prearchive", e);
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
