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

@NrgPreferenceBean(toolId = DemographicDataPreference.TOOL_ID,
                   toolName = "PIXI Demographic Data Implementation Preference",
                   description = "Manages the preferred abstractDemographicData implementation to display to the user.")
@Slf4j
public class DemographicDataPreference extends AbstractPreferenceBean {

    public static final String TOOL_ID = "pixi-demographics";
    public static final String DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID = "demographicDataImpl";

    private static final List<String> demographicDataImpls = new ArrayList<>();
    static {
        demographicDataImpls.add(XnatDemographicdata.SCHEMA_ELEMENT_NAME);
        demographicDataImpls.add(PixiAnimaldemographicdata.SCHEMA_ELEMENT_NAME);
    }

    private final NamedParameterJdbcTemplate template;

    @Autowired
    public DemographicDataPreference(final NrgPreferenceService preferenceService,
                                     final ConfigPaths configFolderPaths,
                                     final OrderedProperties initPrefs,
                                     final NamedParameterJdbcTemplate template) {
        super(preferenceService, configFolderPaths, initPrefs);
        this.template = template;
    }

    @NrgPreference(defaultValue = PixiAnimaldemographicdata.SCHEMA_ELEMENT_NAME)
    public String getDemographicDataImpl() {
        return getValue(DemographicDataPreference.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID);
    }

    public void setDemographicDataImpl(String demographicDataImpl) throws DataFormatException {
        if (isNotValidDemographicDataImpl(demographicDataImpl)) {
            throw new DataFormatException(demographicDataImpl + " is not a valid demographic data implementation.");
        }

        try {
            set(demographicDataImpl, DemographicDataPreference.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID);
        } catch (InvalidPreferenceName invalidPreferenceName) {
            invalidPreferenceName.printStackTrace();
        }
    }

    public String getDemographicDataImpl(final String projectId) throws NotFoundException {
        if (!Permissions.verifyProjectExists(template, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }

        String preference = getValue(Scope.Project, projectId, DemographicDataPreference.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID);
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
            set(Scope.Project, projectId, demographicDataImpl, DemographicDataPreference.DEMOGRAPHIC_DATA_IMPL_PREFERENCE_ID);
        } catch (InvalidPreferenceName invalidPreferenceName) {
            invalidPreferenceName.printStackTrace();
        }
    }

    private boolean isNotValidDemographicDataImpl(final String demographicDataImpl) {
        return !demographicDataImpls.contains(demographicDataImpl);
    }
}