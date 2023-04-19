package org.nrg.xnatx.plugins.pixi.hotelsplitter.services;

import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.PixiHotelscanrecordI;
import org.nrg.xft.security.UserI;

import java.util.Optional;

public interface HotelScanRecordService {
    Optional<PixiHotelscanrecordI> findByHotelScanRecordLabel(UserI user, String projectId, String hotelScanRecordLabel);
    void updateHotelSubject(UserI user, String projectId, String hotelScanRecordLabel, String subjectId, String splitSessionLabel) throws NotFoundException;
    String getStatus(UserI user, String projectId, String hotelScanRecordLabel) throws NotFoundException;
    void updateStatus(UserI user, String projectId, String hotelScanRecordLabel, String status) throws NotFoundException;
    void splitImageAcquisitionContext(UserI user, String projectId, String hotelScanRecordLabel) throws NotFoundException;
}
