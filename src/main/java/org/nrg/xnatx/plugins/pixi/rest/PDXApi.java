package org.nrg.xnatx.plugins.pixi.rest;

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
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.entities.PDX;
import org.nrg.xnatx.plugins.pixi.services.PDXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

@Api("PDX API")
@XapiRestController
@RequestMapping(value = "/pdx")
@Slf4j
public class PDXApi extends AbstractXapiRestController {

    private final PDXService pdxService;

    @Autowired
    public PDXApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final PDXService pdxService) {
        super(userManagementService, roleHolder);
        this.pdxService = pdxService;
    }

    // TODO: API Responses don't always match what I expect in Swagger UI
    @ApiOperation(value = "Returns a list of all PDXs.", response = PDX.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "PDXs successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<PDX> getPDXs() {
        // TODO: Should this also get non enabled PDXs
        return pdxService.getAll();
    }

    @ApiOperation(value = "Create new PDX.", response = PDX.class)
    @ApiResponses({@ApiResponse(code = 201, message = "PDX successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"Pdx", "Administrator"})
    @XapiRequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = AccessLevel.Role)
    public PDX createPDX(@RequestBody final PDX pdx) throws ResourceAlreadyExistsException {
        if (pdxService.exists("pdxID", pdx.getPdxID())) {
            throw new ResourceAlreadyExistsException(PDX.class.getSimpleName(), pdx.getPdxID());
        }

        return pdxService.create(pdx);
    }

    @ApiOperation(value = "Get the indicated PDX",
                  notes = "Based on pdxID, not the primary key ID",
                  response = PDX.class)
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{pdxID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public PDX getPDX(@PathVariable final String pdxID) throws NotFoundException {
        return pdxService.findByPdxID(pdxID)
                         .orElseThrow(() -> new NotFoundException(PDX.class.getSimpleName(), pdxID));
    }

    @ApiOperation(value = "Create/Update the supplied PDX.",
                  notes = "Based on the pdxID, not the primary key")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully created/updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"Pdx", "Administrator"})
    @XapiRequestMapping(value = "{pdxID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public PDX updatePDX(@PathVariable final String pdxID, @RequestBody final PDX pdx) throws DataFormatException {
        if (!pdxID.equals(pdx.getPdxID())) {
            throw new DataFormatException("pdxID Mismatch. URI pdxID: " + pdxID + " does not match the request body PDX ID: " + pdx.getPdxID());
        }

        if (!pdxService.exists("pdxID", pdxID)) {
            return pdxService.create(pdx);
        }

        pdxService.update(pdx);
        return pdxService.retrieve(pdx.getId());
    }

    @ApiOperation(value = "Delete the specified PDX",
                  notes = "Based on pdxID, not the primary key")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"Pdx", "Administrator"})
    @XapiRequestMapping(value = "{pdxID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE, restrictTo = AccessLevel.Role)
    public void deletePDX(@PathVariable final String pdxID) throws NotFoundException {
        Optional<PDX> pdx = pdxService.findByPdxID(pdxID);

        if (!pdx.isPresent()) {
            throw new NotFoundException(PDX.class.getSimpleName(), pdxID);
        } else {
            pdxService.delete(pdx.get());
        }
    }
}
