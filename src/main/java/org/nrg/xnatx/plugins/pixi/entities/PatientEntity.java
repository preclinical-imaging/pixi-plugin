package org.nrg.xnatx.plugins.pixi.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.models.Gender;
import org.nrg.xnatx.plugins.pixi.models.Patient;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Slf4j
public class PatientEntity {

    @Nullable private String submitterPatientID;
    @Getter(AccessLevel.NONE) @Nullable private Gender gender;
    @Nullable private Integer age;
    @Nullable private String diagnosis;
    @Nullable private String consentToShare;
    @Nullable private String ethnicityRace;
    @Nullable private String currentTreatmentDrug;
    @Nullable private String currentTreatmentProtocol;
    @Nullable private String priorTreatmentProtocol;
    @Nullable private String priorTreatmentResponse;
    @Nullable private String virologyStatus;

    @Enumerated(EnumType.STRING)
    public Gender getGender() {
        return this.gender;
    }

    public static PatientEntity fromPojo(final Patient patient) {
        final PatientEntity patientEntity = new PatientEntity();
        patientEntity.update(patient);
        return patientEntity;
    }

    public PatientEntity update(final Patient patient) {
        this.setSubmitterPatientID(patient.getSubmitterPatientID());
        this.setGender(patient.getGender());
        this.setAge(patient.getAge());
        this.setDiagnosis(patient.getDiagnosis());
        this.setConsentToShare(patient.getConsentToShare());
        this.setEthnicityRace(patient.getEthnicityRace());
        this.setCurrentTreatmentDrug(patient.getCurrentTreatmentDrug());
        this.setCurrentTreatmentProtocol(patient.getCurrentTreatmentProtocol());
        this.setPriorTreatmentProtocol(patient.getPriorTreatmentProtocol());
        this.setPriorTreatmentResponse(patient.getPriorTreatmentResponse());
        this.setVirologyStatus(patient.getVirologyStatus());
        return this;
    }

    public Patient toPojo() {
        return Patient.builder()
                .submitterPatientID(this.getSubmitterPatientID())
                .gender(this.getGender())
                .age(this.getAge())
                .diagnosis(this.getDiagnosis())
                .consentToShare(this.getConsentToShare())
                .ethnicityRace(this.getEthnicityRace())
                .currentTreatmentDrug(this.getCurrentTreatmentDrug())
                .currentTreatmentProtocol(this.getCurrentTreatmentProtocol())
                .priorTreatmentProtocol(this.getPriorTreatmentProtocol())
                .priorTreatmentResponse(this.getPriorTreatmentResponse())
                .virologyStatus(this.getVirologyStatus())
                .build();
    }
}
