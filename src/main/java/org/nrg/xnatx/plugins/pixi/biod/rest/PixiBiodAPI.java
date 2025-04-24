package org.nrg.xnatx.plugins.pixi.biod.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.model.PixiBiodistributiondataI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.biod.services.BiodistributionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Api("PIXI Biodistribution API")
@XapiRestController
@RequestMapping("/pixi/biodistribution")
@Slf4j
public class PixiBiodAPI extends AbstractXapiRestController {

    private final BiodistributionDataService biodistributionDataService;

    @Autowired
    public PixiBiodAPI(UserManagementServiceI userManagementService,
                       RoleHolder roleHolder,
                       BiodistributionDataService biodistributionDataService) {
        super(userManagementService, roleHolder);
        this.biodistributionDataService = biodistributionDataService;
    }

    @ApiOperation(value = "Create biodistribution experiments from Excel file")
    @ApiResponses({@ApiResponse(code = 200, message = "Biodistribution experiments successfully created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public List<PixiBiodistributiondataI> createBiodistributionExperiments(@RequestParam @Project String project,
                                                                           @RequestParam String cachePath,
                                                                           @RequestParam String dataOverlapHandling) throws Exception {
        log.info("Creating biodistribution experiments for project: {} from cache path: {}", project, cachePath);

        // Call the service to create biodistribution experiments
        List<PixiBiodistributiondataI> biodExps = biodistributionDataService.fromExcel(getSessionUser(), project, cachePath);
        List<PixiBiodistributiondataI> createdBiodExps = biodistributionDataService.createOrUpdate(getSessionUser(), biodExps, dataOverlapHandling);
        log.info("Created {} biodistribution experiments for project: {}", createdBiodExps.size(), project);

        return Collections.emptyList(); // TODO: Return the created biodistribution experiments, update front end to handle this
    }

}
