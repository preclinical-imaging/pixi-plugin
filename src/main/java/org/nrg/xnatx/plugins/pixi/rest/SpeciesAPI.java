package org.nrg.xnatx.plugins.pixi.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.preferences.Species;
import org.nrg.xnatx.plugins.pixi.preferences.SpeciesPreferences;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api("Species Preferences API")
@XapiRestController
@RequestMapping(value = "/pixi/preferences/species")
@Slf4j
public class SpeciesAPI extends AbstractXapiRestController {

    private final SpeciesPreferences speciesPreferences;

    public SpeciesAPI(final UserManagementServiceI userManagementService,
                      final RoleHolder roleHolder,
                      final SpeciesPreferences speciesPreferences) {
        super(userManagementService, roleHolder);
        this.speciesPreferences = speciesPreferences;
    }

    @ApiOperation(value = "Returns site-wide list of species", response = Species.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Species successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<Species> get() {
        return speciesPreferences.getSpecies();
    }

//    @ApiOperation(value = "Returns list of species for the indicated project", response = Species.class, responseContainer = "List")
//    @ApiResponses({@ApiResponse(code = 200, message = "Species successfully retrieved."),
//                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
//                   @ApiResponse(code = 500, message = "Unexpected error")})
//    @XapiRequestMapping(value = "/projects/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Read)
//    public List<Species> get(@PathVariable @Project final String projectId) throws NotFoundException {
//        List<Species> species = speciesPreferences.getSpecies(projectId);
//
//        // Default to site-wide setting if no species are defined for a project
//        if (species.size() == 0) {
//            species = speciesPreferences.getSpecies();
//        }
//
//        return species;
//    }

    @ApiOperation(value="Sets side-wide species preference.")
    @ApiResponses({@ApiResponse(code = 200, message = "Species successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public void set(@RequestBody final List<Species> species) {
        speciesPreferences.setSpecies(species);
    }

//    @ApiOperation(value="Sets species preference for the indicated project.")
//    @ApiResponses({@ApiResponse(code = 200, message = "Species successfully set."),
//                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
//                   @ApiResponse(code = 500, message = "Unexpected error")})
//    @AuthorizedRoles({"PIXI", "Administrator"})
//    @XapiRequestMapping(value = "/projects/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Edit)
//    public void set(@PathVariable @Project final String projectId, @RequestBody final List<Species> species) throws NotFoundException {
//        speciesPreferences.setSpecies(projectId, species);
//    }
}
