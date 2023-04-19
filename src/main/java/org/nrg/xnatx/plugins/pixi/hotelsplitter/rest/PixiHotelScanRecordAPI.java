package org.nrg.xnatx.plugins.pixi.hotelsplitter.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractExperimentXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.model.PixiHotelI;
import org.nrg.xdat.model.PixiHotelpositionI;
import org.nrg.xdat.model.PixiHotelscanrecordI;
import org.nrg.xdat.model.PixiHotelsubjectI;
import org.nrg.xdat.om.PixiHotelscanrecord;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos.HotelPositionDTO;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos.HotelScanRecordDTO;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos.HotelSubjectDTO;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.exceptions.PixiRuntimeException;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.services.HotelScanRecordService;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.services.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Api("Hotel Scan Record API")
@XapiRestController
@RequestMapping("/pixi/hotelscanrecords")
@Slf4j
public class PixiHotelScanRecordAPI extends AbstractExperimentXapiRestController<PixiHotelscanrecord> {

    private final HotelScanRecordService hotelScanRecordService;
    private final HotelService hotelService;

    @Autowired
    protected PixiHotelScanRecordAPI(NamedParameterJdbcTemplate template,
                                     UserManagementServiceI userManagementService,
                                     RoleHolder roleHolder,
                                     HotelScanRecordService hotelScanRecordService,
                                     HotelService hotelService) throws NoSuchFieldException, IllegalAccessException {
        super(template, userManagementService, roleHolder);
        this.hotelScanRecordService = hotelScanRecordService;
        this.hotelService = hotelService;
    }

    @ApiOperation(value = "Get a Hotel Scan Record for a particular hotel session.", response = HotelScanRecordDTO.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Hotel Scan Record successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{hotelScanRecordLabel}/project/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Read)
    public HotelScanRecordDTO get(final @Project @PathVariable String projectId, final @PathVariable String hotelScanRecordLabel) throws NotFoundException {
       return toDTO(hotelScanRecordService.findByHotelScanRecordLabel(getSessionUser(), projectId, hotelScanRecordLabel)
                                          .orElseThrow(() -> new NotFoundException(this.getXsiType(), projectId + ":" + hotelScanRecordLabel)));
    }

    @ApiOperation(value = "Updates the split image session labels for the supplied subjects", notes = "Consumes a map from subject ids to split session labels")
    @ApiResponses({@ApiResponse(code = 200, message = "Split image session label set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{hotelScanRecordLabel}/project/{projectId}/subjects", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Edit)
    public void updateHotelSubject(@PathVariable @Project final String projectId, @PathVariable final String hotelScanRecordLabel, @RequestBody final Map<String, String> splitSessionLabels) throws NotFoundException {
        for (Map.Entry<String, String> entry : splitSessionLabels.entrySet()) {
            final String subjectId = entry.getKey();
            final String splitSessionLabel = entry.getValue();
            hotelScanRecordService.updateHotelSubject(getSessionUser(), projectId, hotelScanRecordLabel, subjectId, splitSessionLabel);
        }
    }

    @ApiOperation(value = "Gets the status of a hotel scan record")
    @ApiResponses({@ApiResponse(code = 200, message = "Status retrieved"),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{hotelScanRecordLabel}/project/{projectId}/status", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Read)
    public String getHotelScanRecordStatus(@PathVariable @Project final String projectId, @PathVariable final String hotelScanRecordLabel) throws NotFoundException {
        return hotelScanRecordService.getStatus(getSessionUser(), projectId, hotelScanRecordLabel);
    }

    @ApiOperation(value = "Updates the status of a hotel scan record")
    @ApiResponses({@ApiResponse(code = 200, message = "Status updated"),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{hotelScanRecordLabel}/project/{projectId}/status", consumes = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Edit)
    public void updateHotelScanRecordStatus(@PathVariable @Project final String projectId, @PathVariable final String hotelScanRecordLabel, @RequestBody final String status) throws NotFoundException {
        hotelScanRecordService.updateStatus(getSessionUser(), projectId, hotelScanRecordLabel, status);
    }

    @ApiOperation(value = "Split Image Acquisition Contexts")
    @ApiResponses({@ApiResponse(code = 200, message = "Split Image Acquisition Contexts successfully."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{hotelScanRecordLabel}/project/{projectId}/split-image-acq-ctx", method = RequestMethod.POST, restrictTo = AccessLevel.Edit)
    public void splitImageAcqCtx(@PathVariable @Project final String projectId,
                                 @PathVariable final String hotelScanRecordLabel) throws NotFoundException {
        hotelScanRecordService.splitImageAcquisitionContext(getSessionUser(), projectId, hotelScanRecordLabel);
    }

    protected HotelSubjectDTO toDTO(final PixiHotelsubjectI hotelSubject, final PixiHotelpositionI hotelPosition) {
        Optional<String> subjectID = Optional.ofNullable(hotelSubject.getSubjectId());
        String subjectLabel = "";

        if (subjectID.isPresent()) {
            Optional<XnatSubjectdata> subjectData = Optional.ofNullable(XnatSubjectdata.getXnatSubjectdatasById(subjectID.get(), getSessionUser(), false));

            if (subjectData.isPresent()) {
                subjectLabel = subjectData.get().getLabel();
            } else {
                throw new PixiRuntimeException("Unexpected error retrieving subject with " + subjectID.get());
            }
        }

        String orientation = Optional.ofNullable(hotelSubject.getOrientation()).orElse("");
        Double weight = Optional.ofNullable(hotelSubject.getWeight())
                                .map(Objects::toString)
                                .map(Double::parseDouble)
                                .orElse(null);
        Double activity = Optional.ofNullable(hotelSubject.getActivity())
                                  .map(Objects::toString)
                                  .map(Double::parseDouble)
                                  .orElse(null);
        LocalTime injectionTime = Optional.ofNullable(hotelSubject.getInjectionTime())
                                          .map(Time.class::cast)
                                          .map(Time::toLocalTime)
                                          .orElse(null);
        String notes = Optional.ofNullable(hotelSubject.getNotes()).orElse("");
        String splitSessionLabel = Optional.ofNullable(hotelSubject.getSplitSessionLabel()).orElse("");

        HotelPositionDTO hotelPositionDTO = HotelPositionDTO.builder()
                                                            .name(hotelPosition.getName())
                                                            .x((Integer) hotelPosition.getX())
                                                            .y((Integer) hotelPosition.getY())
                                                            .z((Integer) hotelPosition.getZ())
                                                            .build();

        return HotelSubjectDTO.builder()
                              .subjectId(subjectID.orElse(""))
                              .subjectLabel(subjectLabel)
                              .position(hotelPositionDTO)
                              .orientation(orientation)
                              .weight(weight)
                              .activity(activity)
                              .injectionTime(injectionTime)
                              .notes(notes)
                              .splitSessionLabel(splitSessionLabel)
                              .build();
    }

    protected HotelScanRecordDTO toDTO(PixiHotelscanrecordI hotelScanRecord) {
        PixiHotelI hotel = hotelService.findByName(getSessionUser(), hotelScanRecord.getHotel())
                                       .orElseThrow(() -> new PixiRuntimeException("Unexpected error retrieving Hotel " + hotelScanRecord.getHotel()));

        Set<HotelSubjectDTO>  hotelSubjectDTOs = hotelScanRecord.getHotelSubjects_subject()
                                                             .stream()
                                                             .map(subject -> {
                                                                 PixiHotelpositionI hotelPosition = hotel.getPositions_position()
                                                                                                         .stream()
                                                                                                         .filter(position -> position.getName().equals(subject.getPosition()))
                                                                                                         .findFirst()
                                                                                                         .orElseThrow(() -> new PixiRuntimeException("Unexpected error retrieving Hotel " + hotelScanRecord.getHotel()));
                                                                 return this.toDTO(subject, hotelPosition);
                                                             })
                                                             .collect(Collectors.toSet());

       return HotelScanRecordDTO.builder()
                .projectID(hotelScanRecord.getProject())
                .hotelSessionID(hotelScanRecord.getSessionLabel())
                .hotelSubjects(hotelSubjectDTOs)
                .build();
    }

}
