package org.nrg.xnatx.plugins.pixi.bli.services;

import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierConfig;

import java.util.List;
import java.util.Optional;

public interface AnalyzedClickInfoObjectIdentifierConfigService {

    AnalyzedClickInfoObjectIdentifierConfig createOrUpdate(String name, AnalyzedClickInfoObjectIdentifierConfig config);
    Optional<AnalyzedClickInfoObjectIdentifierConfig> getConfig(String name);
    List<AnalyzedClickInfoObjectIdentifierConfig> getAllConfigs();
    void delete(AnalyzedClickInfoObjectIdentifierConfig config);
    void delete(String name);

}
