package org.nrg.xnatx.plugins.pixi.inveon.sessionBuilder;

import lombok.extern.slf4j.Slf4j;
import org.nrg.session.SessionBuilder;
import org.nrg.xdat.bean.*;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class InveonSessionBuilder extends SessionBuilder {

    private final File sessionDir;

    public InveonSessionBuilder(final File sessionDir, final Writer fileWriter) {
        super(sessionDir, sessionDir.getPath(), fileWriter);
        this.sessionDir = sessionDir;
    }

    @Override
    public String getSessionInfo() {
        // TODO - Steve - See PETSesssionBuilder in xnat-web for an example. Low priority.
        return "(undetermined)";
    }

    @Override
    public XnatImagesessiondataBean call() throws Exception {
        log.info("Building Inveon session for session: {}", sessionDir.getPath());

        // Get proj/subj/sess/... parameters
        Map<String, String> parameters = getParameters();
        String project = parameters.getOrDefault(PrearcUtils.PARAM_PROJECT, null);
        String subject = parameters.getOrDefault(PrearcUtils.PARAM_SUBJECT_ID, "");
        String label   = parameters.getOrDefault(PrearcUtils.PARAM_LABEL, null);

        Set<String> keys = parameters.keySet();
        log.debug("Building Inveon Session for Project: {} Subject: {} Session: {}", project, subject, label);

        // Build scans
        Path scanDir = sessionDir.toPath().resolve("SCANS");

        log.debug("Scan Dir path: {}", scanDir);
        List<XnatImagescandataBean> scandataBeans = new ArrayList<>();
        boolean hasPET = false;
        boolean hasCT  = false;
        try (final Stream<Path> scans = Files.list(scanDir)) {
            List<Path> scanList = scans.filter(Files::isDirectory).collect(Collectors.toList());

            for (Path scan : scanList) {
                Path inveonFolder = scan.resolve("INVEON");
                log.debug("About to call Inveon scan builder: {}", inveonFolder);
                final InveonScanBuilder inveonScanBuilder = new InveonScanBuilder(inveonFolder);
                XnatImagescandataBean imagescandataBean = inveonScanBuilder.call();
                scandataBeans.add(imagescandataBean);
                if (imagescandataBean.getModality().equals("PET")) {
                    hasPET = true;
                } else if (imagescandataBean.getModality().equals("CT")) {
                    hasCT = true;
                } else {
                    // TODO review/repair
                    log.error("Unrecognized modality {} for this scan {}", imagescandataBean.getModality(), imagescandataBean.getId());
                }
            }
        } catch (FileNotFoundException e) { // Exceptions are expected to be passed up the stack. Log and rethrow
            log.error("Inveon Session Builder logging a file not found error in folder: {}", scanDir, e);
            throw e;
        } catch (IOException e) {
            log.error("IO error building Inveon session " + sessionDir.getPath(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error building Inveon session " + sessionDir.getPath(), e);
            throw e;
        }

        // If no scans were found, throw an exception. T
        if (scandataBeans.isEmpty()) {
            throw new FileNotFoundException("Zero Inveon scans found in " + scanDir + ". Assuming this is not an Inveon session.");
        }
        if (! (hasPET || hasCT)) {
            throw new FileNotFoundException("Did not find at least one CT or PET scan in " + scanDir + ". Assuming this is not an Inveon session.");
        }

        XnatImagesessiondataBean sessionBean;
        if (hasPET) {
            sessionBean = buildPetsessiondataBean(scandataBeans);
        } else {
            sessionBean = buildCTSessiondataBean(scandataBeans);
        }

        sessionBean.setPrearchivepath(sessionDir.getPath());
        sessionBean.setProject(project);
        sessionBean.setSubjectId(subject);
        sessionBean.setLabel(label);

        // Set session date to earliest scan date
        Optional<Date> sessionDate = sessionBean.getScans_scan().stream()
                .map(XnatImagescandataI::getStartDate)
                .map(d -> (Date) d)
                .distinct()
                .sorted()
                .findFirst();

        sessionDate.ifPresent(sessionBean::setDate);

        return sessionBean;
    }

    private XnatPetsessiondataBean buildPetsessiondataBean(List<XnatImagescandataBean> scandataBeans) {
        XnatPetsessiondataBean sessionBean = new XnatPetsessiondataBean();
        for (XnatImagescandataBean imagescandataBean : scandataBeans) {
            sessionBean.addScans_scan(imagescandataBean);
            if (imagescandataBean.getModality().equals("PET")) {
                // Fill in PET data;
                XnatPetscandataBean petscandataBean = (XnatPetscandataBean) imagescandataBean;

                // The method that built the PET Scan Data Bean stuffed tracer information
                // into the Scan Data Bean note. This is terrible and should be repaired.
                // TODO Repair using the Note field in the PET Scan Data Bean as a temp store for tracer info
                log.debug("When building session, PET note {}", petscandataBean.getNote());
                // TODO Review and add more metadata
                String[] tokens = petscandataBean.getNote().split("\t");
                sessionBean.setTracer_name(tokens[0]);
                sessionBean.setTracer_isotope(tokens[1]);
                sessionBean.setTracer_isotope_halfLife(tokens[2]);
                petscandataBean.setNote("");
            }
        }

        return sessionBean;
    }

    private XnatCtsessiondataBean buildCTSessiondataBean(List<XnatImagescandataBean> scandataBeans) {
        // TODO Fill in this implementation
        XnatCtsessiondataBean sessionBean = new XnatCtsessiondataBean();
        for (XnatImagescandataBean imagescandataBean : scandataBeans) {
            sessionBean.addScans_scan(imagescandataBean);
            if (imagescandataBean.getModality().equals("CT")) {
                // Fill in CT data;

            }
        }

        return sessionBean;
    }

}
