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
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.PixiAnimaldemographicdata;
import org.nrg.xdat.om.XnatDemographicdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;

@NrgPreferenceBean(toolId = PIXIPreferences.TOOL_ID,
                   toolName = "PIXI Preferences",
                   description = "Manages preferences for the PIXI plugin.")
@Slf4j
public class PIXIPreferences extends AbstractPreferenceBean {

    public static final String TOOL_ID = "pixi-preferences";
    public static final String DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID = "demographicDataImpl";
    public static final String SPECIES_PREFERENCE_ID =  "species";
    public static final String VENDOR_PREFERENCE_ID =  "vendors";
    public static final String ENDPOINT_PREFERENCE_ID =  "endpoints";
    public static final String UI_SHOW_HUMAN_SEARCH_FIELDS_PREFERENCE_ID =  "uiShowHumanSearchFields";
    public static final String UI_SHOW_USER_READABLE_COUNTS_PREFERENCE_ID =  "uiShowUserReadableCounts";
    public static final String UI_SHOW_INVEON_IMPORTER_PREFERENCE_ID =  "uiShowInveonImporter";
    public static final String UI_SHOW_INVEON_PCIF_OPTION_PREFERENCE_ID =  "uiShowInveonPcifOption";
    public static final String UI_HIDE_SITE_WIDE_COUNTS_PREFERENCE_ID =  "uiHideSiteWideCounts";
    public static final String DEFAULT_BLI_IMPORTER_MAPPING = "defaultBliImporterMapping";

    private static final List<String> demographicDataImpls = new ArrayList<>();
    static {
        demographicDataImpls.add(XnatDemographicdata.SCHEMA_ELEMENT_NAME);
        demographicDataImpls.add(PixiAnimaldemographicdata.SCHEMA_ELEMENT_NAME);
    }

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public PIXIPreferences(final NrgPreferenceService preferenceService,
                           final ConfigPaths configFolderPaths,
                           final OrderedProperties initPrefs,
                           final NamedParameterJdbcTemplate template) {
        super(preferenceService, configFolderPaths, initPrefs);
        this.template = template;
    }

    @NrgPreference(defaultValue = PixiAnimaldemographicdata.SCHEMA_ELEMENT_NAME)
    public String getDemographicDataImpl() {
        return getValue(PIXIPreferences.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID);
    }

    public void setDemographicDataImpl(String demographicDataImpl) throws DataFormatException {
        if (isNotValidDemographicDataImpl(demographicDataImpl)) {
            throw new DataFormatException(demographicDataImpl + " is not a valid demographic data implementation.");
        }

        try {
            set(demographicDataImpl, PIXIPreferences.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'demographicDataImpl': something is very wrong here.", e);
        }
    }

    public String getDemographicDataImpl(final String projectId) throws NotFoundException {
        if (!Permissions.verifyProjectExists(template, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }

        String preference = getValue(Scope.Project, projectId, PIXIPreferences.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID);
        return preference != null ? preference : getDemographicDataImpl();
    }

    public void setDemographicDataImpl(final String projectId, final String demographicDataImpl) throws NotFoundException, DataFormatException {
        if (!Permissions.verifyProjectExists(template, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }

        if (isNotValidDemographicDataImpl(demographicDataImpl)) {
            throw new DataFormatException(demographicDataImpl + " is not a valid demographic data implementation.");
        }

        try {
            set(Scope.Project, projectId, demographicDataImpl, PIXIPreferences.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'demographicDataImpl': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "[\n   " +
            "{\"id\": \"1\", \"scientificName\": \"Mus musculus\", \"commonName\": \"Mouse\"},\n   " +
            "{\"id\": \"2\", \"scientificName\": \"Rattus norvegicus\", \"commonName\": \"Rat\"}\n" +
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

    @NrgPreference(defaultValue = "[\n   " +
            "{\"id\": \"1\", \"vendor\": \"CRL\"},\n   " +
            "{\"id\": \"2\", \"vendor\": \"JAX\"}\n" +
            "]",
            key = "id")
    public List<Vendor> getVendors() {
        return getListValue(VENDOR_PREFERENCE_ID);
    }

    public void setVendors(final List<Vendor> vendors) {
        try {
            setListValue(VENDOR_PREFERENCE_ID, vendors);
        } catch (InvalidPreferenceName exception) {
            log.error("Error setting vendor preference.", exception);
        }
    }

    @NrgPreference(defaultValue = "[\n   " +
                "\"Euthanized\", " +
                "\"Repurposed\", " +
                "\"Expired - Age\", " +
                "\"Expired - Disease Burden\", " +
                "\"Expired - Other\", " +
                "\"Natural Causes\"" +
            "]")
    public List<String> getEndpoints() {
        return getListValue(ENDPOINT_PREFERENCE_ID);
    }

    public void setEndpoints(final List<String> endpoints) {
        try {
            setListValue(ENDPOINT_PREFERENCE_ID, endpoints);
        } catch (InvalidPreferenceName exception) {
            log.error("Error setting animal endpoints preference.", exception);
        }
    }

    @NrgPreference(defaultValue = "false")
    public Boolean getUiShowHumanSearchFields() {
        return getBooleanValue(UI_SHOW_HUMAN_SEARCH_FIELDS_PREFERENCE_ID);
    }

    public void setUiShowHumanSearchFields(final Boolean uiShowHumanSearchFields) {
        try {
            setBooleanValue(uiShowHumanSearchFields, UI_SHOW_HUMAN_SEARCH_FIELDS_PREFERENCE_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'uiShowHumanSearchFields': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public Boolean getUiShowUserReadableCounts() {
        return getBooleanValue(UI_SHOW_USER_READABLE_COUNTS_PREFERENCE_ID);
    }

    public void setUiShowUserReadableCounts(final Boolean uiShowUserReadableCounts) {
        try {
            setBooleanValue(uiShowUserReadableCounts, UI_SHOW_USER_READABLE_COUNTS_PREFERENCE_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'uiShowUserReadableCounts': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public Boolean getUiShowInveonImporter() {
        return getBooleanValue(UI_SHOW_INVEON_IMPORTER_PREFERENCE_ID);
    }

    public void setUiShowInveonImporter(final Boolean uiShowInveonImporter) {
        try {
            setBooleanValue(uiShowInveonImporter, UI_SHOW_INVEON_IMPORTER_PREFERENCE_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'uiShowInveonImporter': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public Boolean getUiShowInveonPcifOption() {
        return getBooleanValue(UI_SHOW_INVEON_PCIF_OPTION_PREFERENCE_ID);
    }

    public void setUiShowInveonPcifOption(final Boolean uiShowInveonPcifOption) {
        try {
            setBooleanValue(uiShowInveonPcifOption, UI_SHOW_INVEON_PCIF_OPTION_PREFERENCE_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'uiShowInveonPcifOption': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "true")
    public Boolean getUiHideSiteWideCounts() {
        return getBooleanValue(UI_HIDE_SITE_WIDE_COUNTS_PREFERENCE_ID);
    }

    public void setUiHideSiteWideCounts(final Boolean uiHideSiteWideCounts) {
        try {
            setBooleanValue(uiHideSiteWideCounts, UI_HIDE_SITE_WIDE_COUNTS_PREFERENCE_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'uiHideSiteWideCounts': something is very wrong here.", e);
        }
    }

    private boolean isNotValidDemographicDataImpl(final String demographicDataImpl) {
        return !demographicDataImpls.contains(demographicDataImpl);
    }

    @NrgPreference(defaultValue = "")
    public String getDefaultBliImporterMapping() {
        return getValue(DEFAULT_BLI_IMPORTER_MAPPING);
    }

    public void setDefaultBliImporterMapping(final String defaultBliImporterMapping) {
        try {
            set(defaultBliImporterMapping, DEFAULT_BLI_IMPORTER_MAPPING);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name " + DEFAULT_BLI_IMPORTER_MAPPING + ": something is very wrong here.", e);
        }
    }

}