package org.nrg.xnatx.plugins.pixi.xenografts.rest;

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
import org.nrg.xnatx.plugins.pixi.xenografts.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.xenografts.models.PDX;
import org.nrg.xnatx.plugins.pixi.xenografts.services.XenograftService;
import org.nrg.xnatx.plugins.pixi.xenografts.exceptions.XenograftDeletionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api("PDX API")
@XapiRestController
@RequestMapping(value = "/pixi")
@Slf4j
public class PDXApi extends XenograftAPI<PDXEntity, PDX> {

    @Autowired
    public PDXApi(final UserManagementServiceI userManagementService,
                  final RoleHolder roleHolder,
                  final XenograftService<PDXEntity, PDX> pdxService) {
        super(userManagementService, roleHolder, pdxService, PDX.class);
    }

    @Override
    @ApiOperation(value = "Returns a list of all PDXs.", response = PDX.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "PDXs successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/pdx", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<PDX> getAll() {
        return super.getAll();
    }

    @Override
    @ApiOperation(value = "Get the indicated PDX", response = PDX.class)
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/pdx/{externalID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public PDX get(@PathVariable final String externalID) throws NotFoundException {
        return super.get(externalID);
    }

    @Override
    @ApiOperation(value = "Create new PDX.")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/pdx", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public void create(@RequestBody final PDX pdx) throws ResourceAlreadyExistsException {
        super.create(pdx);
    }

    @Override
    @ApiOperation(value = "Update the supplied PDX.")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/pdx/{externalID}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public void update(@PathVariable final String externalID, @RequestBody final PDX pdx) throws ResourceAlreadyExistsException, NotFoundException {
        super.update(externalID, pdx);
    }

    @Override
    @ApiOperation(value = "Delete the specified PDX")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/pdx/{externalID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE, restrictTo = AccessLevel.Role)
    public void delete(@PathVariable final String externalID) throws XenograftDeletionException {
        super.delete(externalID);
    }

}
