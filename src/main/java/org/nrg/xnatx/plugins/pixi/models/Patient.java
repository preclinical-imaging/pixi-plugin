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
public class Patient {
    @ApiModelProperty(example = "PAT-123") @Nullable private String submitterPatientID;
    @ApiModelProperty(example = "MALE") @Nullable private Gender gender;
    @ApiModelProperty(example = "35") @Nullable private Integer age;
    @ApiModelProperty(example = "Invasive breast cancer") @Nullable private String diagnosis;
    @ApiModelProperty(example = "Yes/no/available to academic centers only") @Nullable private String consentToShare;
    @ApiModelProperty(example = "Caucasian") @Nullable private String ethnicityRace;
    @ApiModelProperty(example = "Everolimus; CHEMBL83") @Nullable private String currentTreatmentDrug;
    @ApiModelProperty(example = "Afinitor; 10 mg/day") @Nullable private String currentTreatmentProtocol;
    @ApiModelProperty(example = "Surgery and nolvadex; 40 mg/day") @Nullable private String priorTreatmentProtocol;
    @ApiModelProperty(example = "Progressive disease (RECIST1.1)") @Nullable private String priorTreatmentResponse;
    @ApiModelProperty(example = "HIV-/HBV-/HCV+/HTLV-/EBV+") @Nullable private String virologyStatus;
}
