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
    @ApiModelProperty(example = "PAT-123") @Nullable private java.lang.String submitterPatientID;
    @ApiModelProperty(example = "Male") @Nullable private String gender;
    @ApiModelProperty(example = "35") @Nullable private Integer age;
    @ApiModelProperty(example = "Invasive breast cancer") @Nullable private java.lang.String diagnosis;
    @ApiModelProperty(example = "Yes/no/available to academic centers only") @Nullable private java.lang.String consentToShare;
    @ApiModelProperty(example = "Caucasian") @Nullable private java.lang.String ethnicityRace;
    @ApiModelProperty(example = "Everolimus") @Nullable private java.lang.String currentTreatmentDrug;
    @ApiModelProperty(example = "10 mg/day") @Nullable private java.lang.String currentTreatmentProtocol;
    @ApiModelProperty(example = "Surgery and nolvadex; 40 mg/day") @Nullable private java.lang.String priorTreatmentProtocol;
    @ApiModelProperty(example = "Progressive disease (RECIST1.1)") @Nullable private java.lang.String priorTreatmentResponse;
    @ApiModelProperty(example = "HIV-/HBV-/HCV+/HTLV-/EBV+") @Nullable private java.lang.String virologyStatus;
}
