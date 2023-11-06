package org.nrg.xnatx.plugins.pixi.bli.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.entities.AnalyzedClickInfoObjectIdentifierMappingEntity;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingEntityService;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefualtAnalyzedClickInfoObjectIdentifierMappingService implements AnalyzedClickInfoObjectIdentifierMappingService {

    private final AnalyzedClickInfoObjectIdentifierMappingEntityService mappingEntityService;

    @Autowired
    public DefualtAnalyzedClickInfoObjectIdentifierMappingService(final AnalyzedClickInfoObjectIdentifierMappingEntityService mappingEntityService) {
        super();
        this.mappingEntityService = mappingEntityService;
    }

    @Override
    public AnalyzedClickInfoObjectIdentifierMapping createOrUpdate(String name, AnalyzedClickInfoObjectIdentifierMapping mapping) {
        final Optional<AnalyzedClickInfoObjectIdentifierMappingEntity> existing = mappingEntityService.findMapping(name);

        if (existing.isPresent()) {
            existing.get().update(mapping);
            mappingEntityService.update(existing.get());
        } else {
            mapping.setName(name);
            final AnalyzedClickInfoObjectIdentifierMappingEntity entity = AnalyzedClickInfoObjectIdentifierMappingEntity.fromPojo(mapping);
            mappingEntityService.create(entity);
        }

        return mappingEntityService.findMapping(mapping.getName()) // use the name from the mapping object in case it was updated
                                   .map(AnalyzedClickInfoObjectIdentifierMappingEntity::getMapping)
                                   .orElseThrow(() -> new RuntimeException("Failed to get importer mapping even though it was just created/updated"));
    }

    @Override
    public Optional<AnalyzedClickInfoObjectIdentifierMapping> getMapping(String name) {
        return mappingEntityService.findMapping(name)
                                   .map(AnalyzedClickInfoObjectIdentifierMappingEntity::getMapping);
    }

    @Override
    public List<AnalyzedClickInfoObjectIdentifierMapping> getAllMappings() {
        return mappingEntityService.getAll().stream()
                                   .map(AnalyzedClickInfoObjectIdentifierMappingEntity::getMapping)
                                   .collect(Collectors.toList());
    }

    @Override
    public void delete(AnalyzedClickInfoObjectIdentifierMapping mapping) {
        delete(mapping.getName());
    }

    @Override
    public void delete(String name) {
        final Optional<AnalyzedClickInfoObjectIdentifierMappingEntity> existing = mappingEntityService.findMapping(name);
        existing.ifPresent(mappingEntityService::delete);
    }

}
