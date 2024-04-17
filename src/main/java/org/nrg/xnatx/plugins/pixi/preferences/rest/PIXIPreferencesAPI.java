package org.nrg.xnatx.plugins.pixi.preferences.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.preferences.PIXIPreferences;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Api("PIXI Preferences API")
@XapiRestController
@RequestMapping(value = "/pixi/preferences")
@Slf4j
public class PIXIPreferencesAPI extends AbstractXapiRestController {

    private final PIXIPreferences pixiPreferences;

    public PIXIPreferencesAPI(final UserManagementServiceI userManagementService,
                              final RoleHolder roleHolder,
                              final PIXIPreferences pixiPreferences) {
        super(userManagementService, roleHolder);
        this.pixiPreferences = pixiPreferences;
    }

    @ApiOperation(value = "Returns the full map of PIXI plugin preferences.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "PIXI plugin preferences successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set PIXI plugin preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public Map<String, Object> getPixiPreferences() {
        final UserI user     = getSessionUser();
        final String username = user.getUsername();

        log.debug("User {} requested the site configuration.", username);

        Map<String, Object> preferences=  pixiPreferences.entrySet()
                                                         .stream()
                                                         .collect(Collectors.toMap(Map.Entry::getKey, entry -> ObjectUtils.defaultIfNull(entry.getValue(), "")));

        // I think there is a bug in the preference library with caching. Manually add these.
        preferences.put(PIXIPreferences.SPECIES_PREFERENCE_ID , pixiPreferences.getSpecies());
        preferences.put(PIXIPreferences.VENDOR_PREFERENCE_ID  , pixiPreferences.getVendors());
        preferences.put(PIXIPreferences.ENDPOINT_PREFERENCE_ID  , pixiPreferences.getEndpoints());
        preferences.put(PIXIPreferences.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID  , pixiPreferences.getDemographicDataImpl());
        preferences.put(PIXIPreferences.UI_SHOW_HUMAN_SEARCH_FIELDS_PREFERENCE_ID  , pixiPreferences.getUiShowHumanSearchFields());
        preferences.put(PIXIPreferences.UI_SHOW_USER_READABLE_COUNTS_PREFERENCE_ID, pixiPreferences.getUiShowUserReadableCounts());
        preferences.put(PIXIPreferences.UI_SHOW_INVEON_IMPORTER_PREFERENCE_ID, pixiPreferences.getUiShowInveonImporter());
        preferences.put(PIXIPreferences.UI_HIDE_SITE_WIDE_COUNTS_PREFERENCE_ID, pixiPreferences.getUiHideSiteWideCounts());
        preferences.put(PIXIPreferences.DEFAULT_BLI_IMPORTER_MAPPING, pixiPreferences.getDefaultBliImporterMapping());

        return preferences;
    }

    @ApiOperation(value = "Sets a map of PIXI plugin preferences.", notes = "Sets the PIXI plugin preferences specified in the map.")
    @ApiResponses({@ApiResponse(code = 200, message = "PIXI plugin preferences successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(consumes = {APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = Admin)
    public void setPixiPreferences(@ApiParam(value = "The map of PIXI plugin preferences to be set.", required = true) @RequestBody final Map<String, Object> preferences) {
        if (!preferences.isEmpty()) {
            for (final String name : preferences.keySet()) {
                try {
                    final Object value = preferences.get(name);
                    if (value instanceof List) {
                        //noinspection unchecked,rawtypes
                        pixiPreferences.setListValue(name, (List) value);
                    } else if (value instanceof Map) {
                        //noinspection unchecked,rawtypes
                        pixiPreferences.setMapValue(name, (Map) value);
                    } else if (value.getClass().isArray()) {
                        pixiPreferences.setArrayValue(name, (Object[]) value);
                    } else {
                        pixiPreferences.set(value.toString(), name);
                    }
                    log.info("Set preference {} to value: {}", name, value);
                } catch (InvalidPreferenceName invalidPreferenceName) {
                    log.error("Got an invalid preference name error for the preference: " + name);
                }
            }
        }
    }

    @ApiOperation(value = "Returns the value of the selected PIXI plugin preference.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "PIXI plugin preference successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access site JupyterHub plugin preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{preference}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Map<String, Object> getSpecificPixiPreference(@ApiParam(value = "The preference to retrieve.", required = true) @PathVariable final String preference) throws NotFoundException {
        if (!pixiPreferences.containsKey(preference)) {
            throw new NotFoundException("No PIXI plugin preference named " + preference);
        }

        Object value;

        // I think there is a bug in the preference library with caching hence the switch.
        switch (preference) {
            case (PIXIPreferences.SPECIES_PREFERENCE_ID): {
                value = pixiPreferences.getSpecies();
                break;
            }
            case (PIXIPreferences.VENDOR_PREFERENCE_ID): {
                value = pixiPreferences.getVendors();
                break;
            }
            case (PIXIPreferences.ENDPOINT_PREFERENCE_ID): {
                value = pixiPreferences.getEndpoints();
                break;
            }
            case (PIXIPreferences.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID): {
                value = pixiPreferences.getDemographicDataImpl();
                break;
            }
            case (PIXIPreferences.UI_SHOW_HUMAN_SEARCH_FIELDS_PREFERENCE_ID): {
                value = pixiPreferences.getUiShowHumanSearchFields();
                break;
            }
            case (PIXIPreferences.UI_SHOW_USER_READABLE_COUNTS_PREFERENCE_ID): {
                value = pixiPreferences.getUiShowUserReadableCounts();
                break;
            }
            case (PIXIPreferences.UI_SHOW_INVEON_IMPORTER_PREFERENCE_ID): {
                value = pixiPreferences.getUiShowInveonImporter();
                break;
            }
            case (PIXIPreferences.UI_HIDE_SITE_WIDE_COUNTS_PREFERENCE_ID): {
                value = pixiPreferences.getUiHideSiteWideCounts();
                break;
            }
            case (PIXIPreferences.DEFAULT_BLI_IMPORTER_MAPPING): {
                value = pixiPreferences.getDefaultBliImporterMapping();
                break;
            }
            default:
                value = pixiPreferences.get(preference);
        }

        log.debug("User {} requested the value for the PIXI plugin preference {}, got value: {}", getSessionUser().getUsername(), preference, value);
        return Collections.singletonMap(preference, value);
    }

    @ApiOperation(value = "Sets a single PIXI plugin preference.", notes = "Sets the PIXI plugin preference specified in the URL to the value set in the body.")
    @ApiResponses({@ApiResponse(code = 200, message = "PIXI plugin preference successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{preference}", consumes = {TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = Admin)
    public void setSpecificPixiPreference(@ApiParam(value = "The preference to be set.", required = true) @PathVariable("preference") final String preference,
                                          @ApiParam("The value to be set for the property.") @RequestBody final String value) throws InvalidPreferenceName {
        log.info("User '{}' set the value of the site configuration property {} to: {}", getSessionUser().getUsername(), preference, value);
        pixiPreferences.set(value, preference);
    }

    @ApiOperation(value = "Returns project preferred abstractDemographicData implementation", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Demographic data implementation preference successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/demographicDataImpl/projects/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Authenticated)
    public Map<String, String> getDemographicDataImpl(@PathVariable @Project final String projectId) throws NotFoundException {
        return Collections.singletonMap("demographicDataImpl", pixiPreferences.getDemographicDataImpl(projectId));
    }

    @ApiOperation(value="Set project preferred abstractDemographicData implementation.")
    @ApiResponses({@ApiResponse(code = 200, message = "Demographic data implementation preference successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/demographicDataImpl/projects/{projectId}", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = AccessLevel.Edit)
    public void setDemographicDataImpl(@PathVariable @Project final String projectId, @RequestBody final Map<String, String> preference) throws NotFoundException, DataFormatException {
        if (!preference.containsKey("demographicDataImpl")) {
            throw new NotFoundException("No demographicDataImpl submitted");
        }

        pixiPreferences.setDemographicDataImpl(projectId, preference.get("demographicDataImpl"));
    }
}
