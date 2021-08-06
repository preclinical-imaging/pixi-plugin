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
import org.nrg.xnatx.plugins.pixi.services.AnimalModelEntityService;
import org.nrg.xnatx.plugins.pixi.services.PDXEntityService;
import org.nrg.xnatx.plugins.pixi.services.PDXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.stream.Collectors;

@Api("PIXI API")
@XapiRestController
@RequestMapping(value = "/pixi")
@Slf4j
//TODO Versioning?
public class PIXIApi extends AbstractXapiRestController {

    private final PDXService pdxService;
    private final PDXEntityService pdxEntityService;
    private final AnimalModelEntityService animalModelEntityService;

    @Autowired
    public PIXIApi(final UserManagementServiceI userManagementService,
                   final RoleHolder roleHolder,
                   final PDXService pdxService,
                   final PDXEntityService pdxEntityService,
                   final AnimalModelEntityService animalModelEntityService) {
        super(userManagementService, roleHolder);
        this.pdxService = pdxService;
        this.pdxEntityService = pdxEntityService;
        this.animalModelEntityService = animalModelEntityService;
    }

    // TODO: API Responses don't always match what I expect in Swagger UI
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

    @ApiOperation(value = "Create/Update the supplied PDX.",
                  notes = "Based on the pdxID, not the primary key")
    @ApiResponses({@ApiResponse(code = 200, message = "PDX successfully created/updated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/pdx/{pdxID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public void createOrUpdatePDX(@PathVariable final String pdxID, @RequestBody final PDX pdx) throws DataFormatException, NotFoundException {
        if (!pdxID.equals(pdx.getId())) {
            throw new DataFormatException("DataFormatException: Updates to ID are not allowed");
        }

        pdx.setCreatedBy(getSessionUser().getUsername());

        try {
            pdxService.createPDX(pdx);
        } catch (ResourceAlreadyExistsException e) {
            pdxService.updatePDX(pdx);
        }
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
        return animalModelEntityService.getAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @ApiOperation(value = "Create new Animal Model.")
    @ApiResponses({@ApiResponse(code = 200, message = "Animal Model successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/animalModels", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = AccessLevel.Role)
    public void createAnimalModel(@RequestBody final AnimalModel animalModel) throws ResourceAlreadyExistsException {
        AnimalModelEntity animalModelEntity = toEntity(animalModel);
        animalModelEntityService.createAnimalModelEntity(animalModelEntity);
    }

    @ApiOperation(value = "Get the indicated Animal Model", response = AnimalModel.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Animal Model successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/animalModels/{animalModelID}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public AnimalModel getAnimalModel(@PathVariable final String animalModelID) throws NotFoundException {
        AnimalModelEntity animalModelEntity = animalModelEntityService.getAnimalModelEntity(animalModelID)
                                                                      .orElseThrow(() -> new NotFoundException(AnimalModelEntity.class.getSimpleName(), animalModelID));
        return toDTO(animalModelEntity);
    }

    @ApiOperation(value = "Create/Update an Animal Model.")
    @ApiResponses({@ApiResponse(code = 200, message = "Animal Model successfully created/updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/animalModels/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public AnimalModel updateAnimalModel(@PathVariable final String id, @RequestBody final AnimalModel animalModel) throws DataFormatException, NotFoundException {
        if (!id.equals(animalModel.getId())) {
            throw new DataFormatException("DataFormatException: Updates to ID are not allowed");
        }

        AnimalModelEntity animalModelEntity = animalModelEntityService.getAnimalModelEntity(id)
                                                                      .orElseThrow(() -> new NotFoundException(AnimalModelEntity.class.getSimpleName(), id));

        updateEntity(animalModelEntity, animalModel);
        animalModelEntityService.update(animalModelEntity);

        return animalModel;
    }


    private AnimalModel toDTO(final AnimalModelEntity animalModelEntity) {
        return AnimalModel.builder().id(animalModelEntity.getAnimalModelID())
                                    .animalModelName(animalModelEntity.getAnimalModelName())
                                    .passage(animalModelEntity.getPassage())
                                    .isImmuneSystemHumanized(animalModelEntity.getIsImmuneSystemHumanized())
                                    .humanizationType(animalModelEntity.getHumanizationType())
                                    .pdxIDs(animalModelEntity.getPdxs().stream().map(PDXEntity::getPdxID).collect(Collectors.toList()))
                                    .build();
    }

    private AnimalModelEntity toEntity(final AnimalModel animalModel) {
        AnimalModelEntity animalModelEntity = new AnimalModelEntity();
        animalModelEntity.setCreatedBy(getSessionUser().getUsername());
        updateEntity(animalModelEntity, animalModel);
        return animalModelEntity;
    }

    private void updateEntity(final AnimalModelEntity animalModelEntity, final AnimalModel animalModel) {
        animalModelEntity.setAnimalModelID(animalModel.getId());
        animalModelEntity.setAnimalModelName(animalModel.getAnimalModelName());
        animalModelEntity.setPassage(animalModel.getPassage());
        animalModelEntity.setIsImmuneSystemHumanized(animalModel.getIsImmuneSystemHumanized());
        animalModelEntity.setHumanizationType(animalModel.getHumanizationType());

        if (animalModel.getPdxIDs() != null) {
            List<PDXEntity> pdxs = animalModel.getPdxIDs().stream()
                    .map(pdxEntityService::getPDXEntity)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            animalModelEntity.setPdxs(pdxs);
        } else {
            animalModelEntity.setPdxs(Collections.emptyList());
        }
    }
}
