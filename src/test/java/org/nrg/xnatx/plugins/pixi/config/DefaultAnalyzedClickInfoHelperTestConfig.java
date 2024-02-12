package org.nrg.xnatx.plugins.pixi.config;

import org.nrg.xnatx.plugins.pixi.bli.helpers.impl.DefaultAnalyzedClickInfoHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultAnalyzedClickInfoHelperTestConfig {

    @Bean
    public DefaultAnalyzedClickInfoHelper DefaultAnalyzedClickInfoHelper() {
        return new DefaultAnalyzedClickInfoHelper();
    }

}
