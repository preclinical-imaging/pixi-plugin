package org.nrg.xnatx.plugins.pixi.bli.importer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.fileExtraction.Format;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnatx.plugins.pixi.bli.factories.AnalyzedClickInfoObjectIdentifierFactory;
import org.nrg.xnatx.plugins.pixi.bli.helpers.*;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.nrg.xnat.archive.Operation.Rebuild;

/**
 * An import handler for BLI sessions.
 *
 * Supports ZIP uploads of a BLI session. Each directory in the folder is assumed to be a single session.
 *
 */
@ImporterHandler(handler = BliImporter.BLI_IMPORTER)
@Slf4j
public class BliImporter extends ImporterHandlerA {
    public static final String BLI_IMPORTER = "BLI";

    public static final String SUBJECT_LABELING_OPTION_PARAM = "subjectLabelOption";
    public static final String SESSION_LABELING_OPTION_PARAM = "sessionLabelOption";
    public static final String SUBJECT_LABEL_REGEX_PARAM = "subjectLabelRegex";
    public static final String SESSION_LABEL_REGEX_PARAM = "sessionLabelRegex";

    private final FileWriterWrapperI fw;
    private final InputStream in;
    private final UserI user;
    private final Map<String, Object> params;
    private final Format format;
    private final Date uploadDate;
    private final Set<String> uris;
    private final Set<Path> timestampDirectories;
    private final Set<SessionData> sessions;
    private AnalyzedClickInfoHelper analyzedClickInfoHelper;
    private static final String UNKNOWN_SESSION_LABEL = "bli_zip_upload";

    private final AnalyzedClickInfoObjectIdentifierFactory analyzedClickInfoObjectIdentifierFactory;

    private final ZipFileUtils zipFileUtils;
    private final PrearcUtilsHelper prearcUtilsHelper;
    private final PrearcDatabaseHelper prearcDatabaseHelper;
    private final ArcSpecManagerHelper arcSpecManagerHelper;
    private final UserDataCache userDataCache;

    public BliImporter(final Object listenerControl,
                       final UserI user,
                       final FileWriterWrapperI fw,
                       final Map<String, Object> params) throws IOException {
        super(listenerControl, user);
        this.user = user;
        this.params = params;
        this.fw = fw;
        this.in = fw.getInputStream();
        this.format = Format.getFormat(fw.getName());
        this.uploadDate = new Date();
        this.uris = Sets.newLinkedHashSet();
        this.timestampDirectories = Sets.newLinkedHashSet();
        this.sessions = Sets.newLinkedHashSet();
        this.analyzedClickInfoHelper = XDAT.getContextService().getBean(AnalyzedClickInfoHelper.class);
        this.analyzedClickInfoObjectIdentifierFactory = XDAT.getContextService().getBean(AnalyzedClickInfoObjectIdentifierFactory.class);
        this.zipFileUtils = XDAT.getContextService().getBean(ZipFileUtils.class);
        this.prearcUtilsHelper = XDAT.getContextService().getBean(PrearcUtilsHelper.class);
        this.prearcDatabaseHelper = XDAT.getContextService().getBean(PrearcDatabaseHelper.class);
        this.arcSpecManagerHelper = XDAT.getContextService().getBean(ArcSpecManagerHelper.class);
        this.userDataCache = XDAT.getContextService().getBean(UserDataCache.class);
    }

    @Override
    public List<String> call() throws ClientException, ServerException {
        log.info("Importing BLI data for user {}, upload date {}, file name {}",
                 user.getLogin(), uploadDate, fw.getName());
        log.debug("Importing BLI data with parameters: {}", params);

        // Validate parameters
        if (!params.containsKey(URIManager.PROJECT_ID) || !params.containsKey("cachePath")) {
            ClientException e = new ClientException("Both PROJECT_ID and cachePath are required parameters for BLI session uploads.");
            log.error("Both Project ID and cachePath are required for BLI session uploads", e);
            throw e;
        }

        String projectId = (String) params.get(URIManager.PROJECT_ID);
        String cachePathParam = (String) params.get("cachePath");

        // Unzip the file
        Path userCachePath = Paths.get(cachePathParam);
        File zipFile;
        Path unzipDirectory;

        try {
            zipFile = userDataCache.getUserDataCacheFile(user, userCachePath);
            unzipDirectory = zipFile.toPath().getParent();
            zipFileUtils.unzipFile(zipFile.toPath(), unzipDirectory);
        } catch (IOException e) {
            log.error("Error unzipping the file", e);
            throw new ServerException(e);
        }

        // Find all directories containing AnalyzedClickInfo.txt files.
        // Each directory is assumed to be a single scan of a session with a session consisting of one or more scans.
        Set<Path> analyzedClickInfoDirectories = new HashSet<>();
        try (Stream<Path> walk = Files.walk(unzipDirectory)) {
            walk.filter(Files::isRegularFile)
                .filter(f -> f.getFileName().toString().equalsIgnoreCase("AnalyzedClickInfo.txt"))
                .forEach(f -> analyzedClickInfoDirectories.add(f.getParent()));
        } catch (IOException e) {
            log.error("Error finding AnalyzedClickInfo.txt files", e);
            throw new ServerException(e);
        }

        try {
            for (Path directory : analyzedClickInfoDirectories) {
                importDirectory(projectId, directory);
            }
        } catch (IOException e) {
            log.error("Error uploading BLI session.", e);
            cleanupOnImportFailure();
            throw new ServerException(e);
        } catch (ServerException | ClientException e) {
            log.error("Error uploading BLI session.", e);
            cleanupOnImportFailure();
            throw e;
        }

        // Send a build request after all sessions have been imported
        sessions.forEach(this::sendSessionBuildRequest);

        return Lists.newArrayList(uris);
    }

    protected void importDirectory(String projectId, Path directory) throws IOException, ServerException, ClientException {
        log.debug("Importing BLI session for project {} from directory {}", projectId, directory);

        // Verify that the directory exists and contains AnalyzedClickInfo.txt
        verifyBliDirectory(directory);

        // Initialize the SessionData object
        final SessionData session = new SessionData();
        final String timestamp = prearcUtilsHelper.makeTimestamp();

        // Parse AnalyzedClickInfo.txt
        final AnalyzedClickInfoObjectIdentifier analyzedClickInfoObjectIdentifier = createAnalyzedClickInfoObjectIdentifier();
        final AnalyzedClickInfo analyzedClickInfo = analyzedClickInfoHelper.parseTxt(directory.resolve("AnalyzedClickInfo.txt"));

        Optional<String> sessionLabel = analyzedClickInfoObjectIdentifier.getSessionLabel(analyzedClickInfo).map(this::replaceWhitespace);
        Optional<String> subjectId  = analyzedClickInfoObjectIdentifier.getSubjectLabel(analyzedClickInfo).map(this::replaceWhitespace);
        Optional<Date> scanDate = Optional.ofNullable(analyzedClickInfo.getLuminescentImage().getAcquisitionDateTime());
        Optional<String> scanLabel = analyzedClickInfoObjectIdentifier.getScanLabel(analyzedClickInfo).map(this::replaceWhitespace);
        Optional<String> uid = Optional.ofNullable(replaceWhitespace(analyzedClickInfo.getClickNumber().getClickNumber()));

        if (!subjectId.isPresent() || StringUtils.isBlank(subjectId.get())) {
            subjectId = Optional.of("Unknown");
        }

        if (!uid.isPresent() || uid.get().isEmpty() || StringUtils.isBlank(uid.get())) {
            log.warn("No click number found in AnalyzedClickInfo.txt for project {} and directory {}. Generating a random UUID.", projectId, directory);
            uid = Optional.of(UUID.randomUUID().toString());
        }

        if (!scanLabel.isPresent() || StringUtils.isBlank(scanLabel.get())) {
            scanLabel = uid;
        }

        // Override subject, expt, and scan label if provided in the params
        if (params.containsKey(URIManager.SUBJECT_ID)) {
            subjectId = Optional.of((String) params.get(URIManager.SUBJECT_ID));
        }

        if (params.containsKey(URIManager.EXPT_LABEL)) {
            sessionLabel = Optional.of((String) params.get(URIManager.EXPT_LABEL));
        }

        if (params.containsKey(URIManager.SCAN_ID)) {
            scanLabel = Optional.of((String) params.get(URIManager.SCAN_ID));
        }

        // Populate session
        session.setFolderName(sessionLabel.orElse(UNKNOWN_SESSION_LABEL));
        session.setName(sessionLabel.orElse(UNKNOWN_SESSION_LABEL));
        session.setProject(projectId);
        session.setScan_date(scanDate.orElse(null));
        session.setUploadDate(uploadDate);
        session.setTimestamp(timestamp);
        session.setStatus(PrearcUtils.PrearcStatus.RECEIVING);
        session.setLastBuiltDate(Calendar.getInstance().getTime());
        session.setSubject(subjectId.orElse("Unknown"));
        session.setSource(params.get(URIManager.SOURCE));
        session.setPreventAnon(Boolean.valueOf((String) params.get(URIManager.PREVENT_ANON)));
        session.setPreventAutoCommit(Boolean.valueOf((String) params.get(URIManager.PREVENT_AUTO_COMMIT)));
        session.setAutoArchive(shouldAutoArchive(projectId));

        // Check if a matching session already exists in the prearchive (ie we're adding scans to an existing session)
        Optional<SessionData> matchingSession = sessions.stream().filter(s -> s.getProject().equals(session.getProject()) &&
                                                                              s.getFolderName().equals(session.getFolderName()) &&
                                                                              s.getName().equals(session.getName()) &&
                                                                              s.getSubject().equals(session.getSubject()) &&
                                                                              (!s.getName().equalsIgnoreCase(UNKNOWN_SESSION_LABEL) ||
                                                                               !session.getName().equalsIgnoreCase(UNKNOWN_SESSION_LABEL))).findAny();

        // If a matching session exists, transfer files to the existing session directory
        // otherwise we will create a new session directory in the prearchive
        Path timestampDirectory = Paths.get(arcSpecManagerHelper.getGlobalPrearchivePath(),
                                            projectId,
                                            matchingSession.isPresent() ? matchingSession.get().getTimestamp() : session.getTimestamp());

        Path sessionFolder = Paths.get(timestampDirectory.toString(),
                                       matchingSession.isPresent() ? matchingSession.get().getFolderName() : session.getFolderName());

        Path scanFolder = Paths.get(sessionFolder.toString(),
                                    "SCANS",
                                    scanLabel.get(),
                                    "IVIS");

        // Add the timestamp directory to the list of directories to delete if something goes wrong
        timestampDirectories.add(timestampDirectory);

        // Move files to the prearchive
        if (Files.notExists(scanFolder)) {
            Files.createDirectories(scanFolder);
        } else {
            // if it does exist, then we have duplicate scan labels.
            // use the UUID as the scan label to avoid conflicts
            scanFolder = Paths.get(sessionFolder.toString(), "SCANS", uid.get(), "IVIS");

            if (Files.notExists(scanFolder)) {
                Files.createDirectories(scanFolder);
            } else {
                log.warn("Scan directory already exists. This should not happen.");
            }
        }

        // Move each file individually
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path file: directoryStream) {
                Files.move(file, scanFolder.resolve(file.getFileName()));
            }

            // Delete the directory after moving the files to the prearchive
            FileUtils.deleteDirectory(directory.toFile());
        } catch (IOException e) {
            log.error("Error moving files to the prearchive", e);
            throw new ServerException(e);
        }

        // Add the session to the prearchive database if it doesn't already exist
        if (!matchingSession.isPresent()) {
            session.setUrl(sessionFolder.toString());
            try {
                prearcDatabaseHelper.addSession(session);
            } catch (Exception e) {
                log.error("Unable to add BLI session", e);
                throw new ServerException(e);
            }
            sessions.add(session);
            uris.add(sessionFolder.toString());
        }

    }

    protected void verifyBliDirectory(Path directory) throws ClientException {
        // Verify that the directory exists and contains AnalyzedClickInfo.txt
        if (Files.notExists(directory)) {
            String errorMsg = "The directory does not exist. Please upload the zip file to the user cache then resubmit the request with the cachePath parameter.";
            ClientException e = new ClientException(errorMsg);
            log.error(errorMsg, e);
            throw e;
        }

        Path analyzedClickInfoTxt = directory.resolve("AnalyzedClickInfo.txt");
        if (Files.notExists(analyzedClickInfoTxt)) {
            String errorMsg = "The directory does not contain AnalyzedClickInfo.txt. Please upload the zip file to the user cache then resubmit the request with the cachePath parameter.";
            ClientException e = new ClientException(errorMsg);
            log.error(errorMsg, e);
            throw e;
        }
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

    /**
     * If something goes wrong with the import process, delete all the timestamp directories and delete sessions from
     * the prearchive database
     */
    private void cleanupOnImportFailure() {
        try {
            for (Path timestampDirectory : timestampDirectories) {
                FileUtils.deleteDirectory(timestampDirectory.toFile());
            }

            for (SessionData session : sessions) {
                prearcDatabaseHelper.deleteSession(session.getName(), session.getTimestamp(), session.getProject());
            }
        } catch (Exception e) {
            log.error("Error deleting sessions from the prearchive", e);
        }
    }

    /**
     * Replaces whitespaces with underscores. Needed for various XNAT labels / ids.
     *
     * @param string String to replace whitespaces with underscores
     *
     * @return String with whitespaces replaced with underscores.
     */
    private String replaceWhitespace(String string) {
        if (string == null) {
            return null;
        }

        return string.replaceAll("\\s","_");
    }

    /**
     * Send a session build request for each session in the prearchive instead of waiting for the
     * 'Session Time Interval' to elapse. If the request can't be sent, then the time interval will kick in instead.
     *
     * @param sessionData The session to request a build for.
     */
    private void sendSessionBuildRequest(SessionData sessionData) {
        try {
            final File sessionDir = prearcUtilsHelper.getPrearcSessionDir(user, sessionData.getProject(), sessionData.getTimestamp(), sessionData.getFolderName(), false);
            XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, Rebuild, sessionData, sessionDir));
        } catch (Exception e) {
            log.info("Unable to request session build. Sitewide prearchive settings will be used instead.");
        }
    }

    protected void setAnalyzedClickInfoHelper(final AnalyzedClickInfoHelper analyzedClickInfoHelper) {
        this.analyzedClickInfoHelper = analyzedClickInfoHelper;
    }

    /**
     * Creates an AnalyzedClickInfoObjectIdentifier object based on the URL parameters.
     *
     * @return An AnalyzedClickInfoObjectIdentifier object
     */
    protected AnalyzedClickInfoObjectIdentifier createAnalyzedClickInfoObjectIdentifier() {
        String subjectLabelField = (String) params.getOrDefault(SUBJECT_LABELING_OPTION_PARAM, "animalNumber");
        String sessionLabelField = (String) params.getOrDefault(SESSION_LABELING_OPTION_PARAM, "clickNumber");
        String subjectLabelRegex = (String) params.getOrDefault(SUBJECT_LABEL_REGEX_PARAM, "(.*)");
        String sessionLabelRegex = (String) params.getOrDefault(SESSION_LABEL_REGEX_PARAM, "(.*)");

        Boolean hotelSession = subjectLabelField.toLowerCase(Locale.ROOT).contains("hotel");

        if (StringUtils.isBlank(subjectLabelRegex)) {
            subjectLabelRegex = "(.*)";
        }

        if (StringUtils.isBlank(sessionLabelRegex)) {
            sessionLabelRegex = "(.*)";
        }

        AnalyzedClickInfoObjectIdentifierMapping mapping = AnalyzedClickInfoObjectIdentifierMapping.builder()
                                                                                                   .name("ImporterMapping")
                                                                                                   .projectLabelField("")
                                                                                                   .projectLabelRegex("")
                                                                                                   .subjectLabelField(subjectLabelField)
                                                                                                   .subjectLabelRegex(subjectLabelRegex)
                                                                                                   .hotelSession(hotelSession)
                                                                                                   .hotelSubjectSeparator("")
                                                                                                   .sessionLabelField(sessionLabelField)
                                                                                                   .sessionLabelRegex(sessionLabelRegex)
                                                                                                   .scanLabelField("clickNumber")
                                                                                                   .scanLabelRegex("(.*)")
                                                                                                   .build();

        return analyzedClickInfoObjectIdentifierFactory.create(mapping);
    }

}
