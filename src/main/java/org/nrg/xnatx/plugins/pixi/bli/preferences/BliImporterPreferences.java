package org.nrg.xnatx.plugins.pixi.bli.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Manages preferences for the PIXI BLI Importer.
 *
 */
@NrgPreferenceBean(toolId = BliImporterPreferences.TOOL_ID,
        toolName = "PIXI BLI Importer Preferences",
        description = "Manages preferences for the PIXI BLI Importer.")
@Slf4j
public class BliImporterPreferences extends AbstractPreferenceBean {

    public static final String TOOL_ID = "pixi--bli-preferences";

    public static final String SUBJECT_LABEL_FIELD = "subjectLabelField";
    public static final String EXPERIMENT_LABEL_FIELD = "experimentLabelField";
    public static final String SCAN_LABEL_FIELD = "scanLabelField";

    @Autowired
    protected BliImporterPreferences(final NrgPreferenceService preferenceService,
                                     final ConfigPaths configFolderPaths,
                                     final OrderedProperties initPrefs) {
        super(preferenceService, configFolderPaths, initPrefs);
    }

    @NrgPreference(defaultValue = "animalNumber")
    public String getSubjectLabelField() {
        return getValue(SUBJECT_LABEL_FIELD);
    }

    public void setSubjectLabelField(String subjectLabelField) {
        try {
            set(SUBJECT_LABEL_FIELD, subjectLabelField);
        } catch (InvalidPreferenceName e) {
            log.error("Error setting subjectLabelField preference", e);
        }
    }

    @NrgPreference(defaultValue = "experiment")
    public String getExperimentLabelField() {
        return getValue(EXPERIMENT_LABEL_FIELD);
    }

    public void setExperimentLabelField(String experimentLabelField) {
        try {
            set(EXPERIMENT_LABEL_FIELD, experimentLabelField);
        } catch (InvalidPreferenceName e) {
            log.error("Error setting experimentLabelField preference", e);
        }
    }

    @NrgPreference(defaultValue = "view")
    public String getScanLabelField() {
        return getValue(SCAN_LABEL_FIELD);
    }

    public void setScanLabelField(String scanLabelField) {
        try {
            set(SCAN_LABEL_FIELD, scanLabelField);
        } catch (InvalidPreferenceName e) {
            log.error("Error setting scanLabelField preference", e);
        }
    }

}
