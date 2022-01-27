package org.nrg.xnatx.plugins.pixi.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.entities.CellLineEntity;
import org.nrg.xnatx.plugins.pixi.exceptions.XenograftDeletionException;
import org.nrg.xnatx.plugins.pixi.models.CellLine;
import org.nrg.xnatx.plugins.pixi.services.XenograftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api("Cell Line API")
@XapiRestController
@RequestMapping(value = "/pixi")
@Slf4j
public class CellLineAPI extends XenograftAPI<CellLineEntity, CellLine> {

    @Autowired
    public CellLineAPI(final UserManagementServiceI userManagementService,
                  final RoleHolder roleHolder,
                  final XenograftService<CellLineEntity, CellLine> cellLineService) {
        super(userManagementService, roleHolder, cellLineService, CellLine.class);
    }

    @Override
    @ApiOperation(value = "Returns a list of all Cell Lines.", response = CellLine.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Cell Lines successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/cellline", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<CellLine> getAll() {
        return super.getAll();
    }

    @Override
    @ApiOperation(value = "Get the indicated Cell Line", response = CellLine.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Cell Line successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/cellline/{externalID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public CellLine get(@PathVariable final String externalID) throws NotFoundException {
        return super.get(externalID);
    }

    @Override
    @ApiOperation(value = "Create new Cell Line.")
    @ApiResponses({@ApiResponse(code = 200, message = "Cell Line successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/cellline", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public void create(@RequestBody final CellLine cellLine) throws ResourceAlreadyExistsException {
        super.create(cellLine);
    }

    @Override
    @ApiOperation(value = "Update the supplied Cell Line.")
    @ApiResponses({@ApiResponse(code = 200, message = "Cell Line successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/cellline/{externalID}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public void update(@PathVariable final String externalID, @RequestBody final CellLine cellLine) throws ResourceAlreadyExistsException, NotFoundException {
        super.update(externalID, cellLine);
    }

    @Override
    @ApiOperation(value = "Delete the specified Cell Line")
    @ApiResponses({@ApiResponse(code = 200, message = "Cell Line successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/cellline/{externalID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE, restrictTo = AccessLevel.Role)
    public void delete(@PathVariable final String externalID) throws XenograftDeletionException {
        super.delete(externalID);
    }

}
