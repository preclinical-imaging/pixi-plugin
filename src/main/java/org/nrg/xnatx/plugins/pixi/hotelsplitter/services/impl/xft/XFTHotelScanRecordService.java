package org.nrg.xnatx.plugins.pixi.hotelsplitter.services.impl.xft;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.PixiHotelscanrecordI;
import org.nrg.xdat.model.PixiHotelsubjectI;
import org.nrg.xdat.om.PixiHotelscanrecord;
import org.nrg.xdat.om.PixiHotelsubject;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.services.HotelScanRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class XFTHotelScanRecordService implements HotelScanRecordService {

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

        PixiHotelsubjectI hotelSubject =  hotelSubjects.stream()
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
    }
}
