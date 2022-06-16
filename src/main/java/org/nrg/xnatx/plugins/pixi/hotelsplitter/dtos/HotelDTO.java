package org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value = "Hotel", description = "Multiple mouse 'hotel' model")
public class HotelDTO {

    @ApiModelProperty(required = true, example = "4_Mouse")
    @NotBlank
    private String name;

    @ApiModelProperty(required = true, dataType = "java.util.Set<org.nrg.xnatx.plugins.pixi.hotelsplitter.models.hotel.HotelPosition>")
    @NotEmpty
    private List<HotelPositionDTO> positions;

}
