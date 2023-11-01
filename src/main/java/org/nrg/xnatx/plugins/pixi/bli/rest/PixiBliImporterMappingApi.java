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
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Api("PIXI BLI Importer Mappings API")
@XapiRestController
@RequestMapping(value = "/pixi/bli/import/mappings")
@Slf4j
public class PixiBliImporterMappingApi extends AbstractXapiRestController {

    private final AnalyzedClickInfoObjectIdentifierMappingService mappingService;

    protected PixiBliImporterMappingApi(final UserManagementServiceI userManagementService,
                                        final RoleHolder roleHolder,
                                        final AnalyzedClickInfoObjectIdentifierMappingService mappingService) {
        super(userManagementService, roleHolder);
        this.mappingService = mappingService;
    }

    @ApiOperation(value = "Get all BLI Importer Mappings.", response = AnalyzedClickInfoObjectIdentifierMapping.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The BLI Importer Mappings were successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Iterable<AnalyzedClickInfoObjectIdentifierMapping> getAll() {
        return mappingService.getAllMappings();
    }

    @ApiOperation(value = "Get a BLI Importer Mapping by name.", response = AnalyzedClickInfoObjectIdentifierMapping.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The BLI Importer Mapping was successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "The BLI Importer Mapping was not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "/{name}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public AnalyzedClickInfoObjectIdentifierMapping get(@PathVariable String name) throws NotFoundException {
        return mappingService.getMapping(name)
                                       .orElseThrow(() -> new NotFoundException(AnalyzedClickInfoObjectIdentifierMapping.class.getSimpleName(), name));
    }

    @ApiOperation(value = "Create a BLI Importer Mapping.", response = AnalyzedClickInfoObjectIdentifierMapping.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The BLI Importer Mapping was successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST
    )
    public AnalyzedClickInfoObjectIdentifierMapping create(@RequestBody AnalyzedClickInfoObjectIdentifierMapping mapping) {
        return mappingService.createOrUpdate(mapping.getName(), mapping);
    }

    @ApiOperation(value = "Update a BLI Importer Mapping.", response = AnalyzedClickInfoObjectIdentifierMapping.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "The BLI Importer Mapping was successfully updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "The BLI Importer Mapping was not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(
            value = "/{name}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.PUT,
            restrictTo = Admin
    )
    public AnalyzedClickInfoObjectIdentifierMapping update(@PathVariable String name,
                                                           @RequestBody AnalyzedClickInfoObjectIdentifierMapping mapping) throws NotFoundException {
        return mappingService.createOrUpdate(name, mapping);
    }

    @ApiOperation(value = "Delete a BLI Importer Mapping.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "The BLI Importer Mapping was successfully deleted."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "The BLI Importer Mapping was not found."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")
    })
    @XapiRequestMapping(value = "/{name}", method = RequestMethod.DELETE, restrictTo = Admin)
    public void delete(@PathVariable String name) throws NotFoundException {
        mappingService.delete(name);
    }

}
