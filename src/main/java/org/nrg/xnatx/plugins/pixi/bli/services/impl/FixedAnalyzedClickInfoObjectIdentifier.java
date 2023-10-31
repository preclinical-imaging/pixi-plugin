package org.nrg.xnatx.plugins.pixi.bli.services.impl;

import groovy.util.logging.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifier;

import java.util.Optional;

@Slf4j
public class FixedAnalyzedClickInfoObjectIdentifier implements AnalyzedClickInfoObjectIdentifier {

    @Override
    public Optional<String> getProjectLabel(AnalyzedClickInfo analyzedClickInfo) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSubjectLabel(AnalyzedClickInfo analyzedClickInfo) {
        return Optional.ofNullable(analyzedClickInfo.getUserLabelNameSet().getAnimalNumber());
    }

    @Override
    public Optional<String> getSessionLabel(AnalyzedClickInfo analyzedClickInfo) {
        return Optional.ofNullable(analyzedClickInfo.getUserLabelNameSet().getExperiment());
    }

    @Override
    public Optional<String> getScanLabel(AnalyzedClickInfo analyzedClickInfo) {
        return Optional.ofNullable(analyzedClickInfo.getUserLabelNameSet().getView());
    }

}
