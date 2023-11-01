package org.nrg.xnatx.plugins.pixi.bli.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value = "AnalyzeClickInfo Mapping", description = "Used to map AnalyzedClickInfo to XNAT objects")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzedClickInfoObjectIdentifierMapping {

    private String name;

    private String projectLabelField;
    private String projectLabelRegex;

    private String subjectLabelField;
    private String subjectLabelRegex;

    private Boolean hotelSession;
    private String hotelSubjectSeparator;

    private String sessionLabelField;
    private String sessionLabelRegex;

    private String scanLabelField;
    private String scanLabelRegex;

}
