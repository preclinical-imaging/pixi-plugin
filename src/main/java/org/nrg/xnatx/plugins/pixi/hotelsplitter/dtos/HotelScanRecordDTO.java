package org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class HotelScanRecordDTO {

    @ApiModelProperty(required = true, example = "PreclinicalProject" ,position = 0)
    @NotBlank
    private String projectID;

    @ApiModelProperty(required = true, example = "session123", position = 1)
    @NotBlank
    private String hotelSessionID;

    @ApiModelProperty(required = true, dataType = "java.util.Set<org.nrg.xnatx.plugins.pixi.hotelsplitter.dtos>", position = 2)
    @NotBlank
    private Set<HotelSubjectDTO> hotelSubjects;
}
