package org.nrg.xnatx.plugins.pixi.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
public class TumorEntity {

    @Nullable private String submitterTumorID;
    @Nullable private String primaryTumorTissueOrigin;
    @Nullable private String tumorType;
    @Nullable private String specimenTumorTissue;
    @Nullable private String tissueHistology;
    @Nullable private String tumorGrade;
    @Nullable private String tumorClassification;
    @Nullable private String diseaseStage;
    @Nullable private String diseaseClassification;
    @Nullable private String specificMarkers;
    @Nullable private String specificMarkersPlatform;
    @Nullable private Boolean fromUntreatedPatient;
    @Nullable private String originalTumorSampleType;
    @Nullable private String derivedPDXModelID; // TODO : FK -> Derived pdxID
    @Nullable private String derivedPDXReason;

}
