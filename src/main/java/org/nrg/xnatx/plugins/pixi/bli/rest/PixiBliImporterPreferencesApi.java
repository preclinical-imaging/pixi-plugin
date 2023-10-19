package org.nrg.xnatx.plugins.pixi.bli.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.bli.preferences.BliImporterPreferences;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Api("BLI Importer Preferences API")
@XapiRestController
@RequestMapping(value = "/pixi/bli-importer-preferences")
@Slf4j
public class PixiBliImporterPreferencesApi extends AbstractXapiRestController {

    private final BliImporterPreferences bliImporterPreferences;

    protected PixiBliImporterPreferencesApi(final UserManagementServiceI userManagementService,
                                            final RoleHolder roleHolder,
                                            final BliImporterPreferences bliImporterPreferences) {
        super(userManagementService, roleHolder);
        this.bliImporterPreferences = bliImporterPreferences;
    }

    @ApiOperation(value = "Get all BLI Importer preferences.",
                  notes = "Complex objects may be returned as encapsulated JSON strings.",
                  response = String.class,
                  responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "The preferences were successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public Map<String, Object> getAllPreferences() {
        final UserI user = getSessionUser();
        final String username = user.getUsername();

        log.debug("User {} requested the bli importer preferences.", username);

        Map<String, Object> preferences = new java.util.HashMap<>();

        preferences.put(BliImporterPreferences.SUBJECT_LABEL_FIELD, bliImporterPreferences.getSubjectLabelField());
        preferences.put(BliImporterPreferences.EXPERIMENT_LABEL_FIELD, bliImporterPreferences.getExperimentLabelField());
        preferences.put(BliImporterPreferences.SCAN_LABEL_FIELD, bliImporterPreferences.getScanLabelField());

        return preferences;
    }

    @ApiOperation(value = "Set a map of BLI Importer preferences.",
                  notes = "Complex objects may be passed as encapsulated JSON strings.")
    @ApiResponses({@ApiResponse(code = 200, message = "The preferences were successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set preferences."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(consumes = {APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = Admin)
    public void setPreferences(@ApiParam(value = "The map of preferences to be set.", required = true) @RequestBody final Map<String, Object> preferences) {
        if (!preferences.isEmpty()) {
            for (final String name : preferences.keySet()) {
                try {
                    final Object value = preferences.get(name);
                    if (value instanceof List) {
                        //noinspection unchecked,rawtypes
                        bliImporterPreferences.setListValue(name, (List) value);
                    } else if (value instanceof Map) {
                        //noinspection unchecked,rawtypes
                        bliImporterPreferences.setMapValue(name, (Map) value);
                    } else if (value.getClass().isArray()) {
                        bliImporterPreferences.setArrayValue(name, (Object[]) value);
                    } else {
                        bliImporterPreferences.set(value.toString(), name);
                    }
                    log.info("Set preference {} to value: {}", name, value);
                } catch (InvalidPreferenceName invalidPreferenceName) {
                    log.error("Got an invalid preference name error for the preference: " + name);
                }
            }
        }
    }
}
