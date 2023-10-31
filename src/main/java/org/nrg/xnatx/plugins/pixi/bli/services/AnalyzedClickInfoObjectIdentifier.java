package org.nrg.xnatx.plugins.pixi.bli.services;

import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;

import java.util.Optional;

public interface AnalyzedClickInfoObjectIdentifier {

    Optional<String> getProjectLabel(AnalyzedClickInfo analyzedClickInfo);
    Optional<String> getSubjectLabel(AnalyzedClickInfo analyzedClickInfo);
    Optional<String> getSessionLabel(AnalyzedClickInfo analyzedClickInfo);
    Optional<String> getScanLabel(AnalyzedClickInfo analyzedClickInfo);

}
