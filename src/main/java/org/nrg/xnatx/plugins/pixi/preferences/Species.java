package org.nrg.xnatx.plugins.pixi.preferences;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class Species {

    @ApiModelProperty(required = true, position = 0)
    private int id;

    @ApiModelProperty(required = true, position = 1)
    @NotBlank
    private String scientificName;

    @ApiModelProperty(required = true, position = 2)
    @NotBlank
    private String commonName;

}
