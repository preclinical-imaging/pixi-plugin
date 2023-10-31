package org.nrg.xnatx.plugins.pixi.bli.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierConfig;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierConfigService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Api("PIXI BLI Importer Profiles API")
@XapiRestController
@RequestMapping(value = "/pixi/bli/import/profiles")
@Slf4j
public class PixiBliImporterProfilesApi extends AbstractXapiRestController {

    private final AnalyzedClickInfoObjectIdentifierConfigService configService;

    protected PixiBliImporterProfilesApi(final UserManagementServiceI userManagementService,
                                         final RoleHolder roleHolder,
                                         final AnalyzedClickInfoObjectIdentifierConfigService configService) {
        super(userManagementService, roleHolder);
        this.configService = configService;
    }

    @ApiOperation(value = "Get all BLI Importer Profiles.", response = AnalyzedClickInfoObjectIdentifierConfig.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The BLI Importer Profiles were successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Iterable<AnalyzedClickInfoObjectIdentifierConfig> getAll() {
        return configService.getAllConfigs();
    }

    @ApiOperation(value = "Get a BLI Importer Profile by name.", response = AnalyzedClickInfoObjectIdentifierConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The BLI Importer Profile was successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "The BLI Importer Profile was not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public AnalyzedClickInfoObjectIdentifierConfig get(@PathVariable String name) throws NotFoundException {
        return configService.getConfig(name)
                                       .orElseThrow(() -> new NotFoundException("AnalyzedClickInfoObjectIdentifierConfig", name));
    }

    @ApiOperation(value = "Create a BLI Importer Profile.", response = AnalyzedClickInfoObjectIdentifierConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The BLI Importer Profile was successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST
    )
    public AnalyzedClickInfoObjectIdentifierConfig create(@RequestBody AnalyzedClickInfoObjectIdentifierConfig aciObjIdConfig) {
        return configService.createOrUpdate(aciObjIdConfig.getName(), aciObjIdConfig);
    }

    @ApiOperation(value = "Update a BLI Importer Profile.", response = AnalyzedClickInfoObjectIdentifierConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The BLI Importer Profile was successfully updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "The BLI Importer Profile was not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(
            value = "/{name}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.PUT,
            restrictTo = Admin
    )
    public AnalyzedClickInfoObjectIdentifierConfig update(@PathVariable String name,
                                                          @RequestBody AnalyzedClickInfoObjectIdentifierConfig aciObjIdConfig) throws NotFoundException {
        return configService.createOrUpdate(name, aciObjIdConfig);
    }

    @ApiOperation(value = "Delete a BLI Importer Profile.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "The BLI Importer Profile was successfully deleted."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "The BLI Importer Profile was not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "/{name}", method = RequestMethod.DELETE, restrictTo = Admin)
    public void delete(@PathVariable String name) throws NotFoundException {
        configService.delete(name);
    }

}
