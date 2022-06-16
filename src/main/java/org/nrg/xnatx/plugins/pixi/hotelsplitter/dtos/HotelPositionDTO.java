package org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value = "Hotel Position", description = "Position of a mouse in a multi mouse 'hotel'. X/Y/Z maps to DICOM Subject Relative Position.")
public class HotelPositionDTO {

    @ApiModelProperty(required = true, example = "Bottom Right") @NotBlank private String name;
    @ApiModelProperty(required = true, example = "2") @Positive private int x;
    @ApiModelProperty(required = true, example = "2") @Positive private int y;
    @ApiModelProperty(required = true, example = "1") @Positive private int z;

}
