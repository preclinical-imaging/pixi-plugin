package org.nrg.xnatx.plugins.pixi.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Slf4j
public class Tumor {
    @ApiModelProperty(example = "TUM-123") @Nullable private String submitterTumorID;
    @ApiModelProperty(example = "Breast") @Nullable private String primaryTumorTissueOrigin;
    @ApiModelProperty(example = "Primary, metastasis, or recurrence") @Nullable private String tumorType;
    @ApiModelProperty(example = "Liver") @Nullable private String specimenTumorTissue;
    @ApiModelProperty(example = "Invasive ductal carcinoma") @Nullable private String tissueHistology;
    @ApiModelProperty(example = "Grade 3") @Nullable private String tumorGrade;
    @ApiModelProperty(example = "Elston") @Nullable private String tumorClassification;
    @ApiModelProperty(example = "T3N2M1") @Nullable private String diseaseStage;
    @ApiModelProperty(example = "TNM") @Nullable private String diseaseClassification;
    @ApiModelProperty(example = "ER+, PR+, HER2+") @Nullable private String specificMarkers;
    @ApiModelProperty(example = "IHC") @Nullable private String specificMarkersPlatform;
    @ApiModelProperty(example = "true") @Nullable private Boolean fromUntreatedPatient;
    @ApiModelProperty(example = "Biopsy, surgical sample, ascites fluid, blood, etc.") @Nullable private String originalTumorSampleType;
    @ApiModelProperty(example = "PDX123") @Nullable private String derivedPDXModelID;
    @ApiModelProperty(example = "lost cisplatin resistance") @Nullable private String derivedPDXReason;
}
