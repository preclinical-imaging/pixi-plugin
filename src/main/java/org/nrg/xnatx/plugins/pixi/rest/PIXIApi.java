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
import org.nrg.xnatx.plugins.pixi.entities.AnimalModelEntity;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.models.AnimalModel;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.services.AnimalModelService;
import org.nrg.xnatx.plugins.pixi.services.PDXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api("PIXI API")
@XapiRestController
@RequestMapping(value = "/pixi")
@Slf4j
public class PIXIApi extends AbstractXapiRestController {

    private final PDXService pdxService;
    private final AnimalModelService animalModelService;

    @Autowired
    public PIXIApi(final UserManagementServiceI userManagementService,
                   final RoleHolder roleHolder,
                   final PDXService pdxService,
                   final AnimalModelService animalModelService) {
        super(userManagementService, roleHolder);
        this.pdxService = pdxService;
        this.animalModelService = animalModelService;
    }

    @ApiOperation(value = "Returns a list of all PDXs.", response = PDX.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "PDXs successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/pdx", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<PDX> getAllPDX() {
        return pdxService.getAllPDX();
    }

    @ApiOperation(value = "Create new PDX.")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/pdx", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = AccessLevel.Role)
    public void createPDX(@RequestBody final PDX pdx) throws ResourceAlreadyExistsException {
        pdx.setCreatedBy(getSessionUser().getUsername());
        pdxService.createPDX(pdx);
    }

    @ApiOperation(value = "Get the indicated PDX",
                  notes = "Based on pdxID, not the primary key ID",
                  response = PDX.class)
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/pdx/{pdxID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public PDX getPDX(@PathVariable final String pdxID) throws NotFoundException {
        return pdxService.getPDX(pdxID)
                         .orElseThrow(() -> new NotFoundException(PDXEntity.class.getSimpleName(), pdxID));
    }

    @ApiOperation(value = "Update the supplied PDX.",
                  notes = "Based on the pdxID, not the primary key")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully created/updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/pdx/{pdxID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public void updatePDX(@PathVariable final String pdxID, @RequestBody final PDX pdx) throws DataFormatException, NotFoundException {
        if (!pdxID.equals(pdx.getId())) {
            throw new DataFormatException("DataFormatException: Updates to ID are not allowed");
        }

        pdxService.updatePDX(pdx);
    }

    @ApiOperation(value = "Delete the specified PDX",
                  notes = "Based on pdxID, not the primary key")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/pdx/{pdxID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE, restrictTo = AccessLevel.Role)
    public void deletePDX(@PathVariable final String pdxID) throws NotFoundException {
        pdxService.deletePDX(pdxID);
    }

    @ApiOperation(value = "Returns a list of all Animal Models.", response = AnimalModel.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Animal Models successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/animalModels", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<AnimalModel> getAllAnimalModels() {
        return animalModelService.getAllAnimalModels();
    }

    @ApiOperation(value = "Create new Animal Model.")
    @ApiResponses({@ApiResponse(code = 200, message = "Animal Model successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/animalModels", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = AccessLevel.Role)
    public void createAnimalModel(@RequestBody final AnimalModel animalModel) throws ResourceAlreadyExistsException {
        animalModelService.createAnimalModel(animalModel);
    }

    @ApiOperation(value = "Get the indicated Animal Model", response = AnimalModel.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Animal Model successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/animalModels/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public AnimalModel getAnimalModel(@PathVariable final String id) throws NotFoundException {
        return animalModelService.getAnimalModel(id)
                                 .orElseThrow(() -> new NotFoundException(AnimalModelEntity.class.getSimpleName(), id));
    }

    @ApiOperation(value = "Update an Animal Model.")
    @ApiResponses({@ApiResponse(code = 200, message = "Animal Model successfully updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/animalModels/{id}", method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public void updateAnimalModel(@PathVariable final String id, @RequestBody final AnimalModel animalModel) throws DataFormatException, NotFoundException {
        if (!id.equals(animalModel.getId())) {
            throw new DataFormatException("DataFormatException: Updates to ID are not allowed");
        }

        animalModelService.updateAnimalModel(animalModel);
    }

    @ApiOperation(value = "Delete an Animal Model.")
    @ApiResponses({@ApiResponse(code = 200, message = "Animal Model successfully deleted."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/animalModels/{id}", method = RequestMethod.DELETE, restrictTo = AccessLevel.Role)
    public void deleteAnimalModel(@PathVariable final String id) {
        animalModelService.deleteAnimalModel(id);
    }
}
