package org.nrg.xnatx.plugins.pixi.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.pixi.preferences.DemographicDataPreference;
import org.nrg.xnatx.plugins.pixi.preferences.Species;
import org.nrg.xnatx.plugins.pixi.preferences.SubjectPreferences;
import org.nrg.xnatx.plugins.pixi.preferences.Vendor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api("PIXI Preferences API")
@XapiRestController
@RequestMapping(value = "/pixi/preferences")
@Slf4j
public class PIXIPreferencesAPI extends AbstractXapiRestController {

    private final SubjectPreferences subjectPreferences;
    private final DemographicDataPreference demographicDataPreference;

    public PIXIPreferencesAPI(final UserManagementServiceI userManagementService,
                              final RoleHolder roleHolder,
                              final SubjectPreferences subjectPreferences,
                              final DemographicDataPreference demographicDataPreference) {
        super(userManagementService, roleHolder);
        this.subjectPreferences = subjectPreferences;
        this.demographicDataPreference = demographicDataPreference;
    }

    @ApiOperation(value = "Returns site-wide list of species", response = Species.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Species successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/species", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<Species> getSpecies() {
        return subjectPreferences.getSpecies();
    }

//    @ApiOperation(value = "Returns list of species for the indicated project", response = Species.class, responseContainer = "List")
//    @ApiResponses({@ApiResponse(code = 200, message = "Species successfully retrieved."),
//                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
//                   @ApiResponse(code = 500, message = "Unexpected error")})
//    @XapiRequestMapping(value = "/species/projects/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Read)
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
    @XapiRequestMapping(value = "/species", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public void setSpecies(@RequestBody final List<Species> species) {
        subjectPreferences.setSpecies(species);
    }

//    @ApiOperation(value="Sets species preference for the indicated project.")
//    @ApiResponses({@ApiResponse(code = 200, message = "Species successfully set."),
//                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
//                   @ApiResponse(code = 500, message = "Unexpected error")})
//    @AuthorizedRoles({"PIXI", "Administrator"})
//    @XapiRequestMapping(value = "/species/projects/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Edit)
//    public void set(@PathVariable @Project final String projectId, @RequestBody final List<Species> species) throws NotFoundException {
//        speciesPreferences.setSpecies(projectId, species);
//    }

    @ApiOperation(value = "Returns site-wide list of vendors", response = Vendor.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Vendors successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/vendors", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<Vendor> getVendors() {
        return subjectPreferences.getVendors();
    }

    @ApiOperation(value="Sets side-wide vendors preference.")
    @ApiResponses({@ApiResponse(code = 200, message = "Vendors successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/vendors", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public void setVendors(@RequestBody final List<Vendor> vendors) {
        subjectPreferences.setVendors(vendors);
    }

    @ApiOperation(value = "Returns site-wide preferred abstractDemographicData implementation", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Demographic data implementation preference successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/demographic-data-impl", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET)
    public String getDemographicDataImpl() {
        return demographicDataPreference.getDemographicDataImpl();
    }

    @ApiOperation(value="Set site-wide preferred abstractDemographicData implementation.")
    @ApiResponses({@ApiResponse(code = 200, message = "Demographic data implementation preference successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"PIXI", "Administrator"})
    @XapiRequestMapping(value = "/demographic-data-impl", consumes = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Role)
    public void setDemographicDataImpl(@RequestBody final String demographicDataImpl) throws DataFormatException {
        demographicDataPreference.setDemographicDataImpl(demographicDataImpl);
    }

    @ApiOperation(value = "Returns project preferred abstractDemographicData implementation", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Demographic data implementation preference successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/demographic-data-impl/projects/{projectId}", produces = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Read)
    public String getDemographicDataImpl(@PathVariable @Project final String projectId) throws NotFoundException {
        return demographicDataPreference.getDemographicDataImpl(projectId);
    }

    @ApiOperation(value="Set project preferred abstractDemographicData implementation.")
    @ApiResponses({@ApiResponse(code = 200, message = "Demographic data implementation preference successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/demographic-data-impl/projects/{projectId}", consumes = MediaType.TEXT_PLAIN_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Edit)
    public void set(@PathVariable @Project final String projectId, @RequestBody final String demographicDataImpl) throws NotFoundException, DataFormatException {
        demographicDataPreference.setDemographicDataImpl(projectId, demographicDataImpl);
    }
}
