package org.nrg.xnatx.plugins.pixi.config;

import org.mockito.Mockito;
import org.nrg.framework.services.SerializerService;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService;
import org.nrg.xnatx.plugins.pixi.biod.helpers.SaveItemHelper;
import org.nrg.xnatx.plugins.pixi.biod.helpers.XnatExperimentDataHelper;
import org.nrg.xnatx.plugins.pixi.biod.helpers.XnatSubjectDataHelper;
import org.nrg.xnatx.plugins.pixi.bli.helpers.XFTManagerHelper;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingService;
import org.nrg.xnatx.plugins.pixi.preferences.PIXIPreferences;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockConfig {

    @Bean
    public XFTManagerHelper mockXFTManagerHelper() {
        return Mockito.mock(XFTManagerHelper.class);
    }

    @Bean
    public XnatAppInfo mockXnatAppInfo() {
        return Mockito.mock(XnatAppInfo.class);
    }

    @Bean
    public SerializerService mockSerializerService() {
        return Mockito.mock(SerializerService.class);
    }

    @Bean
    public NrgPreferenceService mockNrgPreferenceService() {
        return Mockito.mock(NrgPreferenceService.class);
    }

    @Bean
    public UserDataCache mockUserDataCache() {
        return Mockito.mock(UserDataCache.class);
    }

    @Bean
    public XnatSubjectDataHelper mockXnatSubjectDataHelper() {
        return Mockito.mock(XnatSubjectDataHelper.class);
    }

    @Bean
    public XnatExperimentDataHelper mockXnatExperimentDataHelper() {
        return Mockito.mock(XnatExperimentDataHelper.class);
    }

    @Bean
    SaveItemHelper mockSaveItemHelper() {
        return Mockito.mock(SaveItemHelper.class);
    }

    @Bean
    public AnalyzedClickInfoObjectIdentifierMappingService mockAnalyzedClickInfoObjectIdentifierMappingService() {
        return Mockito.mock(AnalyzedClickInfoObjectIdentifierMappingService.class);
    }

    @Bean
    public PIXIPreferences mockPIXIPreferences() {
        return Mockito.mock(PIXIPreferences.class);
    }

    @Bean
    public DefaultCatalogService mockDefaultCatalogService() {
        return Mockito.mock(DefaultCatalogService.class);
    }

    @Bean
    public SiteConfigPreferences mockSiteConfigPreferences() {
        return Mockito.mock(SiteConfigPreferences.class);
    }

}
