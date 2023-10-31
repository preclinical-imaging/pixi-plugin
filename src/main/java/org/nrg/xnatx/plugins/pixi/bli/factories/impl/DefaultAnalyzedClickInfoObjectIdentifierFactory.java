package org.nrg.xnatx.plugins.pixi.bli.factories.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.factories.AnalyzedClickInfoObjectIdentifierFactory;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierConfig;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifier;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierConfigService;
import org.nrg.xnatx.plugins.pixi.bli.services.impl.ConfigurableAnalyzedClickInfoObjectIdentifier;
import org.nrg.xnatx.plugins.pixi.bli.services.impl.FixedAnalyzedClickInfoObjectIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class DefaultAnalyzedClickInfoObjectIdentifierFactory implements AnalyzedClickInfoObjectIdentifierFactory {

    private final AnalyzedClickInfoObjectIdentifierConfigService analyzedClickInfoObjectIdentifierConfigService;

    @Autowired
    public DefaultAnalyzedClickInfoObjectIdentifierFactory(final AnalyzedClickInfoObjectIdentifierConfigService analyzedClickInfoObjectIdentifierConfigService) {
        this.analyzedClickInfoObjectIdentifierConfigService = analyzedClickInfoObjectIdentifierConfigService;
    }

    /**
     * Uses the config name to find the config needed to create the object identifier with
     * @param configName The name of the config to use
     * @return The object identifier
     */
    @Override
    public AnalyzedClickInfoObjectIdentifier create(String configName) {
        Optional<AnalyzedClickInfoObjectIdentifierConfig> config = analyzedClickInfoObjectIdentifierConfigService.getConfig(configName);

        if (config.isPresent()) {
            return new ConfigurableAnalyzedClickInfoObjectIdentifier(config.get());
        } else {
            log.error("Failed to find config with name: {}", configName);
            return new FixedAnalyzedClickInfoObjectIdentifier();
        }
    }

}
