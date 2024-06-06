package org.nrg.xnatx.plugins.pixi.bli.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfo;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can be configured with regex patterns to identify the XNAT project, subject, session, and scan labels
 * from the AnalyzedClickInfo metadata.
 */
@Slf4j
public class ConfigurableAnalyzedClickInfoObjectIdentifier implements AnalyzedClickInfoObjectIdentifier {

    private final AnalyzedClickInfoObjectIdentifierMapping mapping;

    public ConfigurableAnalyzedClickInfoObjectIdentifier(AnalyzedClickInfoObjectIdentifierMapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Extract the XNAT project label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return The XNAT project label
     */
    @Override
    public Optional<String> getProjectLabel(AnalyzedClickInfo analyzedClickInfo) {
        return get(analyzedClickInfo, mapping.getProjectLabelField(), mapping.getProjectLabelRegex(), 1, null,null);
    }

    /**
     * Extract the XNAT subject label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return The XNAT subject label
     */
    @Override
    public Optional<String> getSubjectLabel(AnalyzedClickInfo analyzedClickInfo) {
        if (mapping.getHotelSession()) {
            return Optional.of("Hotel");
        }

        return get(analyzedClickInfo, mapping.getSubjectLabelField(), mapping.getSubjectLabelRegex(), 1, null,null);
    }

    /**
     * Extract the XNAT session label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return The XNAT session label
     */
    @Override
    public Optional<String> getSessionLabel(AnalyzedClickInfo analyzedClickInfo) {
        return get(analyzedClickInfo, mapping.getSessionLabelField(), mapping.getSessionLabelRegex(), 1, null,null);
    }

    /**
     * Extract the XNAT scan label from the AnalyzedClickInfo metadata
     * @param analyzedClickInfo The AnalyzedClickInfo object containing the BLI scan metadata
     * @return The XNAT scan label
     */
    @Override
    public Optional<String> getScanLabel(AnalyzedClickInfo analyzedClickInfo) {
        return get(analyzedClickInfo, mapping.getScanLabelField(), mapping.getScanLabelRegex(), 1, null,null);
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
    public Optional<String> get(@Nonnull AnalyzedClickInfo analyzedClickInfo,
                                @Nullable String field,
                                @Nullable String regexPattern,
                                @Nullable Integer group,
                                @Nullable String targetRegex,
                                @Nullable String replacementString) {
        if (StringUtils.isBlank(field) || StringUtils.isBlank(regexPattern)) {
            return Optional.empty();
        }

        Pattern pattern = Pattern.compile(regexPattern);

        Matcher matcher;
        if (field.equalsIgnoreCase("ClickNumber")) {
            String clickNumber = analyzedClickInfo.getClickNumber().getClickNumber();

            if (clickNumber == null) {
                return Optional.empty();
            }

            matcher = pattern.matcher(clickNumber);
        } else if (field.equalsIgnoreCase("datetime")) {
            LocalDateTime datetime = null;
            datetime = analyzedClickInfo.getLuminescentImage().getLocalAcquisitionDateTime();

            if (datetime == null) {
                datetime = analyzedClickInfo.getPhotographicImage().getLocalAcquisitionDateTime();

                if (datetime == null) {
                    return Optional.empty();
                }
            }

            // reformat to YYYYMMDDHHMMSS
            String datetimeString = datetime.toString();
            datetimeString = datetimeString.replace("-", "")
                                           .replace("T", "")
                                           .replace(":", "")
                                           .replace(".", "")
                                           .replace("_", "")
                                           .replace(" ", "");

            matcher = pattern.matcher(datetimeString);
        } else { // General case for all other fields in UserLabelNameSet
            String fieldVal = analyzedClickInfo.getUserLabelNameSet().get(field);

            if (fieldVal == null) {
                return Optional.empty();
            }

            matcher = pattern.matcher(fieldVal);
        }

        if (matcher.find()) {
            String value = matcher.group(group != null ? group : 1);
            if (targetRegex != null && replacementString != null) {
                Pattern targetPattern = Pattern.compile(targetRegex);
                Matcher targetMatcher = targetPattern.matcher(value);
                return Optional.of(targetMatcher.replaceAll(replacementString));
            }
            // Trim whitespace from extracted value, and return null if empty string
            return Optional.ofNullable(StringUtils.trimToNull(value));
        }

        return Optional.empty();
    }

}
