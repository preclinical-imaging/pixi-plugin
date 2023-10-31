package org.nrg.xnatx.plugins.pixi.bli.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierConfig;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifier;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ConfigurableAnalyzedClickInfoObjectIdentifier implements AnalyzedClickInfoObjectIdentifier {

    private final AnalyzedClickInfoObjectIdentifierConfig config;

    public ConfigurableAnalyzedClickInfoObjectIdentifier(AnalyzedClickInfoObjectIdentifierConfig config) {
        this.config = config;
    }

    /**
     * Extract the XNAT project label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return The XNAT project label
     */
    @Override
    public Optional<String> getProjectLabel(AnalyzedClickInfo analyzedClickInfo) {
        return get(analyzedClickInfo, config.getProjectLabelField(), config.getProjectLabelRegex(), 1, null,null);
    }

    /**
     * Extract the XNAT subject label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return The XNAT subject label
     */
    @Override
    public Optional<String> getSubjectLabel(AnalyzedClickInfo analyzedClickInfo) {
        if (config.getHotelSession()) {
            return Optional.of("Hotel");
        }

        return get(analyzedClickInfo, config.getSubjectLabelField(), config.getSubjectLabelRegex(), 1, null,null);
    }

    /**
     * Extract the XNAT session label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return The XNAT session label
     */
    @Override
    public Optional<String> getSessionLabel(AnalyzedClickInfo analyzedClickInfo) {
        return get(analyzedClickInfo, config.getSessionLabelField(), config.getSessionLabelRegex(), 1, null,null);
    }

    /**
     * Extract the XNAT scan label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return The XNAT scan label
     */
    @Override
    public Optional<String> getScanLabel(AnalyzedClickInfo analyzedClickInfo) {
        return get(analyzedClickInfo, config.getScanLabelField(), config.getScanLabelRegex(), 1, null,null);
    }

    /**
     * Extracts a value from the AnalyzedClickInfo metadata using a regex pattern
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @param field The field in the UserLabelNameSet within the AnalyzedClickInfo object to extract the value from
     * @param regexPattern The regex pattern to use to extract the value
     * @param group The group in the regex pattern to use to extract the value
     * @param targetRegex The regex pattern to use to replace the extracted value
     * @param replacementString The string to replace the extracted value with
     * @return The extracted value
     */
    public Optional<String> get(AnalyzedClickInfo analyzedClickInfo,
                                String field,
                                String regexPattern,
                                @Nullable Integer group,
                                @Nullable String targetRegex,
                                @Nullable String replacementString) {

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(analyzedClickInfo.getUserLabelNameSet().get(field));

        if (matcher.find()) {
            String value = matcher.group(group != null ? group : 1);
            if (targetRegex != null && replacementString != null) {
                Pattern targetPattern = Pattern.compile(targetRegex);
                Matcher targetMatcher = targetPattern.matcher(value);
                return Optional.of(targetMatcher.replaceAll(replacementString));
            }
            return Optional.of(value);
        }

        return Optional.empty();
    }

}
