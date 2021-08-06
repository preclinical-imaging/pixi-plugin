package org.nrg.xnatx.plugins.pixi.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Slf4j
public class PDX {

    @ApiModelProperty(example = "WUPDX1", required = true, position = 0)
    @NotBlank private String id;

    @ApiModelProperty(example = "Wash U PDX #1", position = 1)
    @Nullable private String pdxLabel;

    @ApiModelProperty(accessMode = ApiModelProperty.AccessMode.READ_ONLY, example = "user1", position = 2)
    @Nullable private String createdBy;

    @ApiModelProperty(example = "Notes regarding WUPDX1", position = 3)
    @Nullable private String notes;

    @ApiModelProperty(position = 4)
    @Nullable private Patient patient;

    @ApiModelProperty(position = 5)
    @Nullable private Tumor tumor;

}
