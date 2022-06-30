package org.nrg.xnatx.plugins.pixi.bli.sessionBuilder;

import lombok.extern.slf4j.Slf4j;
import org.nrg.session.SessionBuilder;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.PixiBlisessiondataBean;
import org.nrg.xdat.bean.PixiBlisessiondataBlihotelsubjectBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnatx.plugins.pixi.bli.helpers.AnalyzedClickInfoHelper;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class BliSessionBuilder extends SessionBuilder {

    private final File sessionDir;
    private AnalyzedClickInfoHelper analyzedClickInfoHelper;

    public BliSessionBuilder(final File sessionDir, final Writer fileWriter) {
        super(sessionDir, sessionDir.getPath(), fileWriter);
        this.sessionDir = sessionDir;
        this.analyzedClickInfoHelper = XDAT.getContextService().getBean(AnalyzedClickInfoHelper.class);
    }

    @Override
    public String getSessionInfo() {
        return "(undetermined)";
    }

    @Override
    public XnatImagesessiondataBean call() throws Exception {
        // Get proj/subj/sess/... parameters
        Map<String, String> parameters = getParameters();
        String project = parameters.getOrDefault(PrearcUtils.PARAM_PROJECT, null);
        String subject = parameters.getOrDefault(PrearcUtils.PARAM_SUBJECT_ID, "");
        String label = parameters.getOrDefault(PrearcUtils.PARAM_LABEL, null);

        log.debug("Building BLI session for Project: {} Subject: {} Sesssion: {}", project, subject, label);

        // Initialize the session and populate
        PixiBlisessiondataBean bliSession = new PixiBlisessiondataBean();

        bliSession.setProject(project);
        bliSession.setSubjectId(subject);
        bliSession.setLabel(label);
        bliSession.setHotelsession(subject.equalsIgnoreCase("Hotel"));

        // Build scans
        Path scanDir = sessionDir.toPath().resolve("SCANS");
        List<Path> scans = Files.list(scanDir).filter(Files::isDirectory).collect(Collectors.toList());

        for (Path scan : scans) {
            final BliScanBuilder bliScanBuilder = new BliScanBuilder(scan);
            bliSession.addScans_scan(bliScanBuilder.call());

            if (bliSession.getHotelsession() && bliSession.getBlihotelsubject().isEmpty()) {
                // Only add hotel subjects once. This assumes all the subjects are in the same position in each scan
                AnalyzedClickInfo analyzedClickInfo = analyzedClickInfoHelper.readJson(scan.resolve("AnalyzedClickInfo.json"));
                String[] animalNumbers = analyzedClickInfo.getUserLabelNameSet().getAnimalNumber().split(",");

                for (int i = 0; i < animalNumbers.length; i++) {
                    PixiBlisessiondataBlihotelsubjectBean hotelSubject = new PixiBlisessiondataBlihotelsubjectBean();
                    hotelSubject.setSubjectlabel(replaceWhitespace(animalNumbers[i].trim()));
                    hotelSubject.setPosition(Integer.toString(i));
                    bliSession.addBlihotelsubject(hotelSubject);
                }
            }
        }

        // Set session date
        Optional<Date> sessionDate = bliSession.getScans_scan().stream()
                                                               .map(XnatImagescandataI::getStartDate)
                                                               .map(d -> (Date) d)
                                                               .distinct()
                                                               .sorted()
                                                               .findFirst();
        sessionDate.ifPresent(bliSession::setDate);

        // Set operator
        Optional<String> operator = bliSession.getScans_scan().stream()
                                                              .map(XnatImagescandataI::getOperator)
                                                              .distinct()
                                                              .findFirst();
        operator.ifPresent(bliSession::setOperator);

        return bliSession;
    }

    public void setAnalyzedClickInfoHelper(AnalyzedClickInfoHelper analyzedClickInfoHelper) {
        this.analyzedClickInfoHelper = analyzedClickInfoHelper;
    }

    private String replaceWhitespace(String string) {
        if (string == null) {
            return null;
        }

        return string.replaceAll("\\s","_");
    }
}
