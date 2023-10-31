package org.nrg.xnatx.plugins.pixi.bli.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.entities.AnalyzedClickInfoObjectIdentifierConfigEntity;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierConfig;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierConfigEntityService;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefualtAnalyzedClickInfoObjectIdentifierConfigService implements AnalyzedClickInfoObjectIdentifierConfigService {

    private final AnalyzedClickInfoObjectIdentifierConfigEntityService configEntityService;

    @Autowired
    public DefualtAnalyzedClickInfoObjectIdentifierConfigService(final AnalyzedClickInfoObjectIdentifierConfigEntityService configEntityService) {
        super();
        this.configEntityService = configEntityService;
    }

    @Override
    public AnalyzedClickInfoObjectIdentifierConfig createOrUpdate(String name, AnalyzedClickInfoObjectIdentifierConfig config) {
        final Optional<AnalyzedClickInfoObjectIdentifierConfigEntity> existing = configEntityService.findConfig(name);

        if (existing.isPresent()) {
            existing.get().update(config);
            configEntityService.update(existing.get());
        } else {
            config.setName(name);
            final AnalyzedClickInfoObjectIdentifierConfigEntity analyzedClickInfoObjectIdentifierConfigEntity = AnalyzedClickInfoObjectIdentifierConfigEntity.fromPojo(config);
            configEntityService.create(analyzedClickInfoObjectIdentifierConfigEntity);
        }

        return configEntityService.findConfig(name)
                                             .map(AnalyzedClickInfoObjectIdentifierConfigEntity::getConfig)
                                             .orElseThrow(() -> new RuntimeException("Failed to get importer config even though it was just created/updated"));
    }

    @Override
    public Optional<AnalyzedClickInfoObjectIdentifierConfig> getConfig(String name) {
        return configEntityService.findConfig(name)
                                  .map(AnalyzedClickInfoObjectIdentifierConfigEntity::getConfig);
    }

    @Override
    public List<AnalyzedClickInfoObjectIdentifierConfig> getAllConfigs() {
        return configEntityService.getAll().stream()
                                           .map(AnalyzedClickInfoObjectIdentifierConfigEntity::getConfig)
                                           .collect(Collectors.toList());
    }

    @Override
    public void delete(AnalyzedClickInfoObjectIdentifierConfig config) {
        delete(config.getName());
    }

    @Override
    public void delete(String name) {
        final Optional<AnalyzedClickInfoObjectIdentifierConfigEntity> existing = configEntityService.findConfig(name);
        existing.ifPresent(configEntityService::delete);
    }

}
