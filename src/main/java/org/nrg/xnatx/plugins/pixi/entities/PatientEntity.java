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
public class PatientEntity {

    @Nullable private java.lang.String submitterPatientID;
    @Nullable private String gender;
    @Nullable private Integer age;
    @Nullable private java.lang.String diagnosis;
    @Nullable private java.lang.String consentToShare;
    @Nullable private java.lang.String ethnicityRace;
    @Nullable private java.lang.String currentTreatmentDrug;
    @Nullable private java.lang.String currentTreatmentProtocol;
    @Nullable private java.lang.String priorTreatmentProtocol;
    @Nullable private java.lang.String priorTreatmentResponse;
    @Nullable private java.lang.String virologyStatus;

}
