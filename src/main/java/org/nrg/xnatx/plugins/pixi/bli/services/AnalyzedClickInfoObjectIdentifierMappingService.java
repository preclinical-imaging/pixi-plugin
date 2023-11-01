package org.nrg.xnatx.plugins.pixi.bli.services;

import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;

import java.util.List;
import java.util.Optional;

public interface AnalyzedClickInfoObjectIdentifierMappingService {

    AnalyzedClickInfoObjectIdentifierMapping createOrUpdate(String name, AnalyzedClickInfoObjectIdentifierMapping mapping);
    Optional<AnalyzedClickInfoObjectIdentifierMapping> getMapping(String name);
    List<AnalyzedClickInfoObjectIdentifierMapping> getAllMappings();
    void delete(AnalyzedClickInfoObjectIdentifierMapping mapping);
    void delete(String name);

}
