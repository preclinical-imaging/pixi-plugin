package org.nrg.xnatx.plugins.pixi.biod.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.model.PixiBiodistributiondataI;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.biod.services.BiodistributionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public Map<String, String> createBiodistributionExperiments(@RequestParam @Project String project,
                                                                           @RequestParam String cachePath,
                                                                           @RequestParam String dataOverlapHandling) throws Exception {
        log.info("Creating biodistribution experiments for project: {} from cache path: {}", project, cachePath);

        List<PixiBiodistributiondataI> biodExps = biodistributionDataService.fromCsv(getSessionUser(), project, cachePath, dataOverlapHandling);
        log.info("Created {} biodistribution experiments for project: {}", biodExps.size(), project);

        List<String> ids = biodExps.stream().map(PixiBiodistributiondataI::getId).collect(Collectors.toList());
        List<String> labels = biodExps.stream().map(PixiBiodistributiondataI::getLabel).collect(Collectors.toList());

        return IntStream.range(0, ids.size()).boxed().collect(Collectors.toMap(ids::get, labels::get));
    }

    // EXCEPTION HANDLING

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataFormatException.class)
    public String handleDataFormatException(final DataFormatException e) {
        return e.getMessage();
    }

}
