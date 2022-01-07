package org.nrg.xnatx.plugins.pixi.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

@NrgPreferenceBean(toolId = SpeciesPreferences.TOOL_ID,
                   toolName = "PIXI Species Preferences",
                   description = "Manages preferences and settings for PIXI species.")
@Slf4j
public class SpeciesPreferences extends AbstractPreferenceBean {

    public static final String TOOL_ID = "pixi-species";
    public static final String SPECIES_PREFERENCE_ID =  "species";

    private final NamedParameterJdbcTemplate template;

    @Autowired
    protected SpeciesPreferences(final NrgPreferenceService preferenceService,
                                 final ConfigPaths configFolderPaths,
                                 final OrderedProperties initPrefs,
                                 final NamedParameterJdbcTemplate template) {
        super(preferenceService, configFolderPaths, initPrefs);
        this.template = template;
    }

    @NrgPreference(defaultValue = "[\n   " +
                                     "{\"id\": \"1\", \"scientificName\": \"Mus musculus\", \"commonName\": \"House Mouse\"},\n   " +
                                     "{\"id\": \"2\", \"scientificName\": \"Rattus norvegicus\", \"commonName\": \"Common Rat\"}\n" +
                                  "]",
                   key = "id")
    public List<Species> getSpecies() {
        return getListValue(SPECIES_PREFERENCE_ID);
    }

    public void setSpecies(final List<Species> species) {
        try {
            setListValue(SPECIES_PREFERENCE_ID, species);
        } catch (InvalidPreferenceName exception) {
            log.error("Error setting species preference.", exception);
        }
    }

    public List<Species> getSpecies(final String projectId) throws NotFoundException {
        if (!Permissions.verifyProjectExists(template, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }
        return getListValue(Scope.Project, projectId, SPECIES_PREFERENCE_ID);
    }

    public void setSpecies(final String projectId, final List<Species> species) throws NotFoundException {
        if (!Permissions.verifyProjectExists(template, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }
        try {
            setListValue(Scope.Project, projectId, SPECIES_PREFERENCE_ID, species);
        } catch (InvalidPreferenceName exception) {
            log.error("Error setting species preference for project: " + projectId, exception);
        }
    }

}
