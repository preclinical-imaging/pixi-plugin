package org.nrg.xnatx.plugins.pixi.hotelsplitter.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.model.PixiHotelI;
import org.nrg.xdat.model.PixiHotelpositionI;
import org.nrg.xdat.om.PixiHotel;
import org.nrg.xdat.om.PixiHotelposition;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos.HotelDTO;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos.HotelPositionDTO;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.exceptions.HotelReferenceException;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.services.HotelService;
import org.nrg.xnatx.plugins.pixi.security.PixiDataManagerUserAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;
import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;


@Api("Hotel API")
@XapiRestController
@RequestMapping("/pixi/hotels")
@Slf4j
public class PixiHotelAPI extends AbstractXapiRestController {

    private final HotelService hotelService;

    @Autowired
    protected PixiHotelAPI(UserManagementServiceI userManagementService,
                           RoleHolder roleHolder,
                           HotelService hotelService) {
        super(userManagementService, roleHolder);
        this.hotelService = hotelService;
    }

    @ApiOperation(value = "Returns a list of all Hotels.", response = HotelDTO.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Hotels successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<HotelDTO> getAll() {
        return hotelService.findAll(getSessionUser())
                           .stream()
                           .map(this::toDTO)
                           .collect(Collectors.toList());
    }

    @ApiOperation(value = "Get a Hotel.", response = HotelDTO.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Hotel successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{hotelName}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public HotelDTO get(@PathVariable String hotelName) throws NotFoundException {
        return toDTO(hotelService.findByName(getSessionUser(), hotelName).orElseThrow(() -> new NotFoundException("Hotel named " + hotelName + " does not exist.")));
    }

    @ApiOperation(value = "Create new Hotel.", response = HotelDTO.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Hotel successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthDelegate(PixiDataManagerUserAuthorization.class)
    @XapiRequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = Authorizer)
    public HotelDTO create(@RequestBody final HotelDTO hotelDTO) throws DataFormatException, ResourceAlreadyExistsException {
        return toDTO(hotelService.create(getSessionUser(), fromDTO(hotelDTO)));
    }

    @ApiOperation(value = "Update a Hotel.", response = HotelDTO.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Hotel successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthDelegate(PixiDataManagerUserAuthorization.class)
    @XapiRequestMapping(value = "/{hotelName}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Authorizer)
    public HotelDTO update(@PathVariable String hotelName, @RequestBody final HotelDTO hotelDTO) throws DataFormatException, NotFoundException, ResourceAlreadyExistsException {
        return toDTO(hotelService.update(getSessionUser(), hotelName, fromDTO(hotelDTO)));
    }

    @ApiOperation(value = "Delete a Hotel.")
    @ApiResponses({@ApiResponse(code = 200, message = "Hotel successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthDelegate(PixiDataManagerUserAuthorization.class)
    @XapiRequestMapping(value = "/{hotelName}", method = RequestMethod.DELETE, restrictTo = Authorizer)
    public void delete(@PathVariable String hotelName) throws NotFoundException, HotelReferenceException {
        hotelService.delete(getSessionUser(), hotelName);
    }

    protected HotelPositionDTO toDTO(final PixiHotelpositionI hotelPosition) {
        return HotelPositionDTO.builder()
                               .name(hotelPosition.getName())
                               .x((Integer) hotelPosition.getX())
                               .y((Integer) hotelPosition.getY())
                               .z((Integer) hotelPosition.getZ())
                               .build();
    }

    protected PixiHotelpositionI fromDTO(final HotelPositionDTO hotelPositionDTO) {
        PixiHotelpositionI hotelPosition = new PixiHotelposition();
        hotelPosition.setName(hotelPositionDTO.getName());
        hotelPosition.setX(hotelPositionDTO.getX());
        hotelPosition.setY(hotelPositionDTO.getY());
        hotelPosition.setZ(hotelPositionDTO.getZ());
        return hotelPosition;
    }

    protected HotelDTO toDTO(final PixiHotelI hotel) {
        List<HotelPositionDTO> hotelPositionDTOs = hotel.getPositions_position()
                                                       .stream()
                                                       .map(this::toDTO)
                                                       .collect(Collectors.toList());

        return HotelDTO.builder()
                       .name(hotel.getName())
                       .positions(hotelPositionDTOs)
                       .build();
    }

    protected PixiHotelI fromDTO(final HotelDTO hotelDTO) {
        PixiHotelI hotel = new PixiHotel();
        hotel.setName(hotelDTO.getName());

        hotelDTO.getPositions().forEach(positionDTO -> {
            try {
                hotel.addPositions_position(fromDTO(positionDTO));
            } catch (Exception e) {
                log.error("Unable to convert hotel position dto to XFT", e);
            }
        });

        return hotel;
    }
}
