package org.nrg.xnatx.plugins.pixi.bli.sessionBuilder;

import lombok.extern.slf4j.Slf4j;
import org.nrg.session.SessionBuilder;
import org.nrg.xdat.bean.PixiBlisessiondataBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class BliSessionBuilder extends SessionBuilder {

    private final File sessionDir;

    public BliSessionBuilder(final File sessionDir, final Writer fileWriter) {
        super(sessionDir, sessionDir.getPath(), fileWriter);
        this.sessionDir = sessionDir;
        log.debug("BLI session builder created for session: {}", sessionDir.getPath());
    }

    @Override
    public String getSessionInfo() {
        return "(undetermined)";
    }

    @Override
    public XnatImagesessiondataBean call() throws Exception {
        log.debug("Building BLI session for session: {}", sessionDir.getPath());

        // Get proj/subj/sess/... parameters
        Map<String, String> parameters = getParameters();
        String project = parameters.getOrDefault(PrearcUtils.PARAM_PROJECT, null);
        String subject = parameters.getOrDefault(PrearcUtils.PARAM_SUBJECT_ID, "");
        String label = parameters.getOrDefault(PrearcUtils.PARAM_LABEL, null);

        log.debug("Building BLI session for Project: {} Subject: {} Session: {}", project, subject, label);

        // Initialize the session and populate
        PixiBlisessiondataBean bliSession = new PixiBlisessiondataBean();

        bliSession.setPrearchivepath(sessionDir.getPath());
        bliSession.setProject(project);
        bliSession.setSubjectId(subject);
        bliSession.setLabel(label);
        bliSession.setHotelsession(subject.equalsIgnoreCase("Hotel"));

        // Build scans
        Path scanDir = sessionDir.toPath().resolve("SCANS");

        try (final Stream<Path> scans = Files.list(scanDir)) {
            List<Path> scanList = scans.filter(Files::isDirectory).collect(Collectors.toList());

            for (Path scan : scanList) {
                final BliScanBuilder bliScanBuilder = new BliScanBuilder(scan);
                bliSession.addScans_scan(bliScanBuilder.call());
            }
        } catch (FileNotFoundException e) { // Exceptions are expected to be passed up the stack. Log and rethrow
            log.error("AnalyzedClickInfo.json not found in " + scanDir + ". This might not be a BLI session.", e);
            throw e;
        } catch (IOException e) {
            log.error("IO error building BLI session " + sessionDir.getPath(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error building BLI session " + sessionDir.getPath(), e);
            throw e;
        }

        // If no scans were found, throw an exception. This might not be a BLI session if no scans were found
        if (bliSession.getScans_scan().isEmpty()) {
            throw new FileNotFoundException("No BLI scans found in " + scanDir + ". This might not be a BLI session.");
        }

        // Set session date to earliest scan date
        Optional<Date> sessionDate = bliSession.getScans_scan().stream()
                                                               .map(XnatImagescandataI::getStartDate)
                                                               .map(d -> (Date) d)
                                                               .distinct()
                                                               .sorted()
                                                               .findFirst();
        sessionDate.ifPresent(bliSession::setDate);

        // Try to set operator. If there is only one operator, set it. Otherwise, leave it blank and user can see
        // operators in scan metadata
        List<String> operators = bliSession.getScans_scan().stream()
                                                              .map(XnatImagescandataI::getOperator)
                                                              .distinct()
                                                              .collect(Collectors.toList());

        Optional<String> operator = operators.size() == 1 ? Optional.of(operators.get(0)) : Optional.empty();
        operator.ifPresent(bliSession::setOperator);

        return bliSession;
    }

}
