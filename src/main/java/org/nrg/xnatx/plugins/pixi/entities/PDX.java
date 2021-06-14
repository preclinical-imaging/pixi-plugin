package org.nrg.xnatx.plugins.pixi.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "pdxID"))
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class PDX extends AbstractHibernateEntity {

    @NotNull
    private String pdxID;

    private String submitterPatientID;
    @Getter(AccessLevel.NONE)
    private Gender gender;
    private Integer age;
    private String diagnosis;
    private String consentToShare;
    private String ethnicityRace;
    private String currentTreatmentDrug;
    private String currentTreatmentProtocol;
    private String priorTreatmentProtocol;
    private String priorTreatmentResponse;
    private String virologyStatus;

    private String submitterTumorID;
    private String primaryTumorTissueOrigin;
    private String tumorType;
    private String specimenTumorTissue;
    private String tissueHistology;
    private String tumorGradeAndClassification;
    private String diseaseStageAndClassification;
    private String specificMarkersAndPlatform;
    private Boolean fromUntreatedPatient;
    private String originalTumorSampleType;
    private String existingPdxIDAndSublineReason;


    @Enumerated(EnumType.STRING)
    public Gender getGender() {
        return this.gender;
    }

    enum Gender {MALE, FEMALE}
}

