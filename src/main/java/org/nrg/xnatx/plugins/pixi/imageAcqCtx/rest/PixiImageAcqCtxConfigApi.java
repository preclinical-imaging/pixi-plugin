package org.nrg.xnatx.plugins.pixi.imageAcqCtx.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.constants.Scope;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.AnesthesiaConfig;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.FastingConfig;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.HeatingConditionsConfig;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.services.ImageAcquisitionContextConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Api("PIXI Image Acquisition Context Configuration API")
@XapiRestController
@RequestMapping(value = "/pixi/image-acq-ctx-config")
@Slf4j
public class PixiImageAcqCtxConfigApi extends AbstractXapiRestController {

    private final ImageAcquisitionContextConfigService imageAcquisitionContextConfigService;

    @Autowired
    public PixiImageAcqCtxConfigApi(final UserManagementServiceI userManagementService,
                                    final RoleHolder roleHolder,
                                    final ImageAcquisitionContextConfigService imageAcquisitionContextConfigService) {
        super(userManagementService, roleHolder);
        this.imageAcquisitionContextConfigService = imageAcquisitionContextConfigService;
    }

    @ApiOperation(value = "Get fasting config", response = FastingConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Fasting config successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @RequestMapping(value = "/fasting", produces = APPLICATION_JSON_VALUE, method = GET)
    public FastingConfig getFastingConfig(@RequestParam(value = "scope", required = true) final Scope scope,
                                          @RequestParam(value = "entityId", required = false) final String entityId) {
        return imageAcquisitionContextConfigService.getFastingConfig(getSessionUser(), scope, entityId);
    }

    @ApiOperation(value = "Create or update fasting config", response = FastingConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Fasting config successfully created or updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @RequestMapping(value = "/fasting", produces = APPLICATION_JSON_VALUE, method = PUT)
    public FastingConfig createOrUpdateFastingConfig(@RequestParam(value = "scope", required = true) final Scope scope,
                                                     @RequestParam(value = "entityId", required = false) final String entityId,
                                                     @RequestBody final FastingConfig fastingConfig) {
        return imageAcquisitionContextConfigService.createOrUpdateFastingConfig(getSessionUser(), scope, entityId, fastingConfig);
    }

    @ApiOperation(value = "Get heating conditions config", response = HeatingConditionsConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Heating conditions config successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @RequestMapping(value = "/heating-conditions", produces = APPLICATION_JSON_VALUE, method = GET)
    public HeatingConditionsConfig getHeatingConditionsConfig(@RequestParam(value = "scope", required = true) final Scope scope,
                                                              @RequestParam(value = "entityId", required = false) final String entityId) {
        return imageAcquisitionContextConfigService.getHeatingConditionsConfig(getSessionUser(), scope, entityId);
    }

    @ApiOperation(value = "Create or update heating conditions config", response = HeatingConditionsConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Heating conditions config successfully created or updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @RequestMapping(value = "/heating-conditions", produces = APPLICATION_JSON_VALUE, method = PUT)
    public HeatingConditionsConfig createOrUpdateHeatingConditionsConfig(@RequestParam(value = "scope", required = true) final Scope scope,
                                                                         @RequestParam(value = "entityId", required = false) final String entityId,
                                                                         @RequestBody final HeatingConditionsConfig heatingConditionsConfig) {
        return imageAcquisitionContextConfigService.createOrUpdateHeatingConditionsConfig(getSessionUser(), scope, entityId, heatingConditionsConfig);
    }

    @ApiOperation(value = "Get anesthesia config", response = AnesthesiaConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Anesthesia config successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @RequestMapping(value = "/anesthesia", produces = APPLICATION_JSON_VALUE, method = GET)
    public AnesthesiaConfig getAnesthesiaConfig(@RequestParam(value = "scope", required = true) final Scope scope,
                                                @RequestParam(value = "entityId", required = false) final String entityId) {
        return imageAcquisitionContextConfigService.getAnesthesiaConfig(getSessionUser(), scope, entityId);
    }

    @ApiOperation(value = "Create or update anesthesia config", response = AnesthesiaConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Anesthesia config successfully created or updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @RequestMapping(value = "/anesthesia", produces = APPLICATION_JSON_VALUE, method = PUT)
    public AnesthesiaConfig createOrUpdateAnesthesiaConfig(@RequestParam(value = "scope", required = true) final Scope scope,
                                                           @RequestParam(value = "entityId", required = false) final String entityId,
                                                           @RequestBody final AnesthesiaConfig anesthesiaConfig) {
        return imageAcquisitionContextConfigService.createOrUpdateAnesthesiaConfig(getSessionUser(), scope, entityId, anesthesiaConfig);
    }
}
