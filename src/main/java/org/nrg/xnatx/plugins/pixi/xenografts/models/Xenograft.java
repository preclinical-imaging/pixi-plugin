package org.nrg.xnatx.plugins.pixi.xenografts.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public abstract class Xenograft {

    public Xenograft(@NotBlank final String sourceId,
                          @Nullable final String source,
                          @Nullable final String sourceURL,
                          @Nullable final String createdBy) {
        this.sourceId = sourceId;
        this.source = source;
        this.sourceURL = sourceURL;
        this.createdBy = createdBy;
    }

    @ApiModelProperty(required = true, position = 0)
    @NotBlank
    private String sourceId;

    @ApiModelProperty(position = 1)
    @Nullable
    private String source;

    @ApiModelProperty(position = 2)
    @Nullable private String sourceURL;

    @Nullable
    private String sampleId;

    @Nullable
    private String tumorType;

    @Nullable
    private String diagnosis;

    @Nullable
    private String primarySite;

    @Nullable
    private String collectionSite;

    @Nullable
    private String specimenTumorTissue;

    @Nullable
    private String tissueHistology;

    @Nullable
    private String tumorGradeClassification;

    @Nullable
    private String diseaseStageClassification;

    @Nullable
    private String specificMarkersPlatform;

    @Nullable
    private String tumorFromUntreatedPatient;


    @ApiModelProperty(hidden = true)
    @Nullable private String createdBy;


}
