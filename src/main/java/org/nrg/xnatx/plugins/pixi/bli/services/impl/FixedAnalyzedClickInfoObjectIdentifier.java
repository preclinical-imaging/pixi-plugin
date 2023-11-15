package org.nrg.xnatx.plugins.pixi.bli.services.impl;

import groovy.util.logging.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifier;

import java.util.Optional;

/**
 * This class is used to identify the XNAT project, subject, session, and scan labels from the AnalyzedClickInfo metadata.
 */
@Slf4j
public class FixedAnalyzedClickInfoObjectIdentifier implements AnalyzedClickInfoObjectIdentifier {

    /**
     * Extract the XNAT project label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return Optional of the XNAT project label
     */
    @Override
    public Optional<String> getProjectLabel(AnalyzedClickInfo analyzedClickInfo) {
        return Optional.empty();
    }

    /**
     * Extract the XNAT subject label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return Optional of the XNAT subject label
     */
    @Override
    public Optional<String> getSubjectLabel(AnalyzedClickInfo analyzedClickInfo) {
        return Optional.ofNullable(analyzedClickInfo.getUserLabelNameSet().getAnimalNumber());
    }

    /**
     * Extract the XNAT session label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return Optional of the XNAT session label
     */
    @Override
    public Optional<String> getSessionLabel(AnalyzedClickInfo analyzedClickInfo) {
        return Optional.ofNullable(analyzedClickInfo.getUserLabelNameSet().getExperiment());
    }

    /**
     * Extract the XNAT scan label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return Optional of the XNAT scan label
     */
    @Override
    public Optional<String> getScanLabel(AnalyzedClickInfo analyzedClickInfo) {
        return Optional.ofNullable(analyzedClickInfo.getUserLabelNameSet().getView());
    }

}
