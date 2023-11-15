package org.nrg.xnatx.plugins.pixi.config;

import org.mockito.Mockito;
import org.nrg.framework.services.SerializerService;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnat.services.XnatAppInfo;
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
    public AnalyzedClickInfoObjectIdentifierMappingService mockAnalyzedClickInfoObjectIdentifierMappingService() {
        return Mockito.mock(AnalyzedClickInfoObjectIdentifierMappingService.class);
    }

    @Bean
    public PIXIPreferences mockPIXIPreferences() {
        return Mockito.mock(PIXIPreferences.class);
    }

}
