package org.nrg.xnatx.plugins.pixi.bli.factories.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.factories.AnalyzedClickInfoObjectIdentifierFactory;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifier;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingService;
import org.nrg.xnatx.plugins.pixi.bli.services.impl.ConfigurableAnalyzedClickInfoObjectIdentifier;
import org.nrg.xnatx.plugins.pixi.bli.services.impl.FixedAnalyzedClickInfoObjectIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class DefaultAnalyzedClickInfoObjectIdentifierFactory implements AnalyzedClickInfoObjectIdentifierFactory {

    private final AnalyzedClickInfoObjectIdentifierMappingService analyzedClickInfoObjectIdentifierMappingService;

    @Autowired
    public DefaultAnalyzedClickInfoObjectIdentifierFactory(final AnalyzedClickInfoObjectIdentifierMappingService analyzedClickInfoObjectIdentifierMappingService) {
        this.analyzedClickInfoObjectIdentifierMappingService = analyzedClickInfoObjectIdentifierMappingService;
    }

    /**
     * Uses the mapping service to get the mapping with the given name. If the mapping is found, it is used to configure and
     * create a new {@link ConfigurableAnalyzedClickInfoObjectIdentifier}. If the mapping is not found, a default
     * {@link FixedAnalyzedClickInfoObjectIdentifier} is created.
     * @param name The name of the mapping to use
     * @return The object identifier
     */
    @Override
    public AnalyzedClickInfoObjectIdentifier create(String name) {
        Optional<AnalyzedClickInfoObjectIdentifierMapping> mapping = analyzedClickInfoObjectIdentifierMappingService.getMapping(name);

        if (mapping.isPresent()) {
            return new ConfigurableAnalyzedClickInfoObjectIdentifier(mapping.get());
        } else {
            log.error("Failed to find mapping with name: {}", name);
            return new FixedAnalyzedClickInfoObjectIdentifier();
        }
    }

}
