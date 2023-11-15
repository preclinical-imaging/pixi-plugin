package org.nrg.xnatx.plugins.pixi.config;

import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.pixi.bli.helpers.XFTManagerHelper;
import org.nrg.xnatx.plugins.pixi.bli.initialize.AnalyzedClickInfoObjectIdentifierMappingInitializer;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingService;
import org.nrg.xnatx.plugins.pixi.preferences.PIXIPreferences;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class AnalyzedClickInfoObjectIdentifierMappingInitializerTestConfig {

    @Bean
    public AnalyzedClickInfoObjectIdentifierMappingInitializer AnalyzedClickInfoObjectIdentifierMappingInitializer(final XFTManagerHelper mockXFTManagerHelper,
                                                                                                                   final XnatAppInfo mockXnatAppInfo,
                                                                                                                   final AnalyzedClickInfoObjectIdentifierMappingService mockAnalyzedClickInfoObjectIdentifierMappingService,
                                                                                                                   final PIXIPreferences mockPIXIPreferences) {
        return new AnalyzedClickInfoObjectIdentifierMappingInitializer(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockAnalyzedClickInfoObjectIdentifierMappingService,
                mockPIXIPreferences

        );
    }
}
