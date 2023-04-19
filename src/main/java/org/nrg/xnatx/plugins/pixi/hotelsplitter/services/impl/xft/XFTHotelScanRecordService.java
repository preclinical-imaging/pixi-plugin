package org.nrg.xnatx.plugins.pixi.hotelsplitter.services.impl.xft;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.PixiHotelscanrecordI;
import org.nrg.xdat.model.PixiHotelsubjectI;
import org.nrg.xdat.model.PixiImageacquisitioncontextdataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.services.HotelScanRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Service
@Slf4j
public class XFTHotelScanRecordService implements HotelScanRecordService {

    final SiteConfigPreferences siteConfigPreferences;

    @Autowired
    public XFTHotelScanRecordService(SiteConfigPreferences siteConfigPreferences) {
        this.siteConfigPreferences = siteConfigPreferences;
    }
    
    @Override
    public Optional<PixiHotelscanrecordI> findByHotelScanRecordLabel(final UserI user, final String projectId, final String hotelScanRecordLabel) {
        Optional<XnatExperimentdata> experiment = Optional.ofNullable(XnatExperimentdata.GetExptByProjectIdentifier(projectId, hotelScanRecordLabel, user, false));

        if (experiment.isPresent()) {
            String hotelScanRecordId = experiment.get().getId();
            return Optional.ofNullable(PixiHotelscanrecord.getPixiHotelscanrecordsById(hotelScanRecordId, user, false));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void updateHotelSubject(final UserI user, final String projectId, final String hotelScanRecordLabel, final String subjectId, final String splitSessionLabel) throws NotFoundException {
        log.debug("User {} is attempting to update hotel scan record {} -> hotel subject {} -> split session label {} ",
                  user.getUsername(), hotelScanRecordLabel, subjectId, splitSessionLabel);

        PixiHotelscanrecordI hotelScanRecord = findByHotelScanRecordLabel(user, projectId, hotelScanRecordLabel).orElseThrow(() -> new NotFoundException(PixiHotelscanrecord.SCHEMA_ELEMENT_NAME, hotelScanRecordLabel));

        List<PixiHotelsubjectI> hotelSubjects = hotelScanRecord.getHotelSubjects_subject();

        PixiHotelsubjectI hotelSubject = hotelSubjects.stream()
                .filter(subject -> subject.getSubjectId() != null)
                .filter(subject -> subject.getSubjectId().equals(subjectId))
                .findFirst().orElseThrow(() -> new NotFoundException(PixiHotelsubject.SCHEMA_ELEMENT_NAME, subjectId));

        hotelSubject.setSplitSessionLabel(splitSessionLabel);

        XFTItem item = ((ItemI) hotelSubject).getItem();

        try {
            log.debug("Saving subject");
            SaveItemHelper.authorizedSave(item, user, false, false, false, true, EventUtils.DEFAULT_EVENT(user, "Updated " + item.getXSIType()));
            log.debug("subject saved");
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public String getStatus(UserI user, String projectId, String hotelScanRecordLabel) throws NotFoundException {
        log.debug("User {} is attempting to get the status of hotel scan record {}", user.getUsername(), hotelScanRecordLabel);

        PixiHotelscanrecordI hotelScanRecord = findByHotelScanRecordLabel(user, projectId, hotelScanRecordLabel).orElseThrow(() -> new NotFoundException(PixiHotelscanrecord.SCHEMA_ELEMENT_NAME, hotelScanRecordLabel));
        return hotelScanRecord.getStatus();
    }

    @Override
    public void updateStatus(UserI user, String projectId, String hotelScanRecordLabel, String status) throws NotFoundException {
        log.debug("User {} is attempting to update the hotel scan record {} with status {}", user.getUsername(), hotelScanRecordLabel, status);

        PixiHotelscanrecordI hotelScanRecord = findByHotelScanRecordLabel(user, projectId, hotelScanRecordLabel).orElseThrow(() -> new NotFoundException(PixiHotelscanrecord.SCHEMA_ELEMENT_NAME, hotelScanRecordLabel));

        hotelScanRecord.setStatus(status);
        XFTItem item = ((ItemI) hotelScanRecord).getItem();

        try {
            log.debug("Saving hotel scan record");
            SaveItemHelper.authorizedSave(item, user, false, false, false, true, EventUtils.DEFAULT_EVENT(user, "Updated " + item.getXSIType()));
            log.debug("Hotel scan record saved");
        } catch (Exception e) {
            log.error("", e);
        }

        if (status.equals("Split Complete")) {
            // Update the image acquisition contexts for the split image sessions
            // This is done as a scheduled task because the split image sessions are not archived immediately after the
            // split and the image acquisition contexts cannot be saved until the sessions are archived. Delay for
            // the default archive delay plus two minutes.
            final long archiveDelay = (long) (siteConfigPreferences.getSessionXmlRebuilderInterval() + 2) * 60 * 1000;
            splitImageAcquisitionContext(archiveDelay, user, projectId, hotelScanRecordLabel);
        }
    }

    /**
     * This method immediately adds the image acquisition context data to the split image sessions.
     * @param user the user
     * @param projectId the project ID
     * @param hotelScanRecordLabel the hotel scan record label
     */
    @Override
    public void splitImageAcquisitionContext(UserI user, String projectId, String hotelScanRecordLabel) {
        splitImageAcquisitionContext(0L, user, projectId, hotelScanRecordLabel);
    }

    /**
     * This method schedules the image acquisition context data to be added to the split image sessions.
     * @param delay the delay in milliseconds
     * @param user the user
     * @param projectId the project ID
     * @param hotelScanRecordLabel the hotel scan record label
     */
    private void splitImageAcquisitionContext(Long delay, UserI user, String projectId, String hotelScanRecordLabel) {
        final Timer timer = new Timer();
        final TimerTask task = new SplitImageAcqCtxTask(user, projectId, hotelScanRecordLabel);
        timer.schedule(task, delay);
    }

    /**
     * This task updates the image acquisition contexts for the split image sessions.
     * <p>
     * This is done as a scheduled task because the split image sessions are not archived immediately after the
     * split and the image acquisition contexts cannot be saved until the sessions are archived.
     */
    class SplitImageAcqCtxTask extends TimerTask {

        private final UserI user;
        private final String projectId;
        private final String hotelScanRecordLabel;

        /**
         * Constructor
         *
         * @param user                 the user
         * @param projectId            the project ID containing the hotel scan record
         * @param hotelScanRecordLabel the hotel scan record label
         */
        public SplitImageAcqCtxTask(UserI user, String projectId, String hotelScanRecordLabel) {
            this.user = user;
            this.projectId = projectId;
            this.hotelScanRecordLabel = hotelScanRecordLabel;
        }

        /**
         * Split the image acquisition contexts from the hotel scan record into the split image sessions.
         */
        @Override
        public void run() {
            Optional<PixiHotelscanrecordI> hotelScanRecord = findByHotelScanRecordLabel(user, projectId, hotelScanRecordLabel);

            if (!hotelScanRecord.isPresent()) {
                log.error("Hotel scan record {} not found. Unable to update image acquisition contexts for the split image sessions", hotelScanRecordLabel);
                return;
            }

            if (hotelScanRecord.get().getStatus().equals("Split Complete")) {
                hotelScanRecord.get().getHotelSubjects_subject().stream()
                        .filter(hotelSubject -> hotelSubject.getSubjectId() != null) // Skip empty subjects
                        .filter(hotelSubject -> hotelSubject.getSplitSessionLabel() != null) // Skip subjects that were not split
                        .forEach(hotelSubject -> {
                            // Get the split session label
                            final String experimentLabel = hotelSubject.getSplitSessionLabel();

                            // Get the split session experiment
                            final XnatExperimentdata experiment = XnatExperimentdata.GetExptByProjectIdentifier(projectId, experimentLabel, user, false);

                            if (experiment == null) {
                                log.error("Split session {} not found. Unable to update image acquisition context for the split image session", experimentLabel);
                                return;
                            }

                            final XnatImagesessiondata imageSession = XnatImagesessiondata.getXnatImagesessiondatasById(experiment.getId(), user, false);

                            if (imageSession == null) {
                                log.error("Split session {} not found. Unable to update image acquisition context for the split image session", experimentLabel);
                                return;
                            }

                            // Get the image acquisition context from the hotel scan record
                            final PixiImageacquisitioncontextdataI imageAcquisitionContext = hotelSubject.getImageacquisitioncontext();

                            // If we don't have an image acquisition context, we can't do anything
                            if (imageAcquisitionContext == null) {
                                return;
                            }

                            // Create the image acquisition context assessor for the split session
                            final PixiImageacquisitioncontextassessordata imageAcquisitionContextAssessor = new PixiImageacquisitioncontextassessordata(user);
                            imageAcquisitionContextAssessor.setProject(projectId);
                            imageAcquisitionContextAssessor.setLabel( // Generate a label for the image acquisition context assessor
                                                                      imageSession.getAssessorCount(PixiImageacquisitioncontextassessordata.SCHEMA_ELEMENT_NAME) == 0 ?
                                                                              imageSession.getLabel() + "_imgAcqCtx" :
                                                                              imageSession.getLabel() + "_imgAcqCtx_" + imageSession.getAssessorCount(PixiImageacquisitioncontextassessordata.SCHEMA_ELEMENT_NAME)
                            );
                            imageAcquisitionContextAssessor.setId(imageAcquisitionContextAssessor.getLabel());
                            imageAcquisitionContextAssessor.setImageSessionData(imageSession);
                            imageAcquisitionContextAssessor.setImagesessionId(imageSession.getId());

                            // Try to set the image acquisition context assessor's image acquisition context
                            try {
                                imageAcquisitionContextAssessor.setImageacquisitioncontext(PixiImageacquisitioncontextdata.copy(imageAcquisitionContext, user));
                            } catch (Exception e) {
                                log.error("Unable to set image acquisition context assessor's image acquisition context", e);
                                throw new RuntimeException(e);
                            }

                            // Try to add the image acquisition context assessor to the split session
                            try {
                                imageSession.addAssessors_assessor(imageAcquisitionContextAssessor);
                            } catch (Exception e) {
                                log.error("Unable to add image acquisition context assessor to the split session", e);
                                throw new RuntimeException(e);
                            }

                            // Save the item
                            try {
                                SaveItemHelper.authorizedSave(imageSession.getItem(), user, false, false, false, true, EventUtils.DEFAULT_EVENT(user, "Updated " + imageSession.getItem().getXSIType()));
                                SaveItemHelper.authorizedSave(imageAcquisitionContextAssessor.getItem(), user, false, false, false, true, EventUtils.DEFAULT_EVENT(user, "Created " + imageAcquisitionContextAssessor.getItem().getXSIType()));
                            } catch (Exception e) {
                                log.error("Unable to save image acquisition context assessor", e);
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }
}
