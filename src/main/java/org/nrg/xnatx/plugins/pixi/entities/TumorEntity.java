package org.nrg.xnatx.plugins.pixi.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.models.Tumor;

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

    public static TumorEntity fromPojo(final Tumor tumor) {
        final TumorEntity tumorEntity = new TumorEntity();
        tumorEntity.update(tumor);
        return tumorEntity;
    }

    public TumorEntity update(final Tumor tumor) {
        this.setSubmitterTumorID(tumor.getSubmitterTumorID());
        this.setPrimaryTumorTissueOrigin(tumor.getPrimaryTumorTissueOrigin());
        this.setTumorType(tumor.getTumorType());
        this.setSpecimenTumorTissue(tumor.getSpecimenTumorTissue());
        this.setTissueHistology(tumor.getTissueHistology());
        this.setTumorGrade(tumor.getTumorGrade());
        this.setTumorClassification(tumor.getTumorClassification());
        this.setDiseaseStage(tumor.getDiseaseStage());
        this.setDiseaseClassification(tumor.getDiseaseClassification());
        this.setSpecificMarkers(tumor.getSpecificMarkers());
        this.setSpecificMarkersPlatform(tumor.getSpecificMarkersPlatform());
        this.setFromUntreatedPatient(tumor.getFromUntreatedPatient());
        this.setOriginalTumorSampleType(tumor.getOriginalTumorSampleType());
        this.setDerivedPDXModelID(tumor.getDerivedPDXModelID());
        this.setDerivedPDXReason(tumor.getDerivedPDXReason());
        return this;
    }

    public Tumor toPojo() {
        return Tumor.builder()
                .submitterTumorID(this.getSubmitterTumorID())
                .primaryTumorTissueOrigin(this.getPrimaryTumorTissueOrigin())
                .tumorType(this.getTumorType())
                .specimenTumorTissue(this.getSpecimenTumorTissue())
                .tissueHistology(this.getTissueHistology())
                .tumorGrade(this.getTumorGrade())
                .tumorClassification(this.getTumorClassification())
                .diseaseStage(this.getDiseaseStage())
                .diseaseClassification(this.getDiseaseClassification())
                .specificMarkers(this.getSpecificMarkers())
                .specificMarkersPlatform(this.getSpecificMarkersPlatform())
                .fromUntreatedPatient(this.getFromUntreatedPatient())
                .originalTumorSampleType(this.getOriginalTumorSampleType())
                .derivedPDXModelID(this.getDerivedPDXModelID())
                .derivedPDXReason(this.getDerivedPDXReason())
                .build();
    }
}
