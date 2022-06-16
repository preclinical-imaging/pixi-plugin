package org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class HotelSubjectDTO {

    @ApiModelProperty(required = true, example = "XNAT_S00001", position = 0) @NotBlank private String subjectId;
    @ApiModelProperty(required = true, example = "Mouse123", position = 1) @NotBlank private String subjectLabel;
    @ApiModelProperty(required = true, example = "Top Left", position = 2) @NotBlank private HotelPositionDTO position;
    @ApiModelProperty private String orientation;
    @ApiModelProperty @Positive private Double weight;
    @ApiModelProperty @JsonFormat(pattern= "HHmmss") private LocalTime injectionTime; // Time is in DICOM Format
    @ApiModelProperty @Positive private Double activity;
    @ApiModelProperty private String notes;
    @ApiModelProperty private String splitSessionLabel;

}
