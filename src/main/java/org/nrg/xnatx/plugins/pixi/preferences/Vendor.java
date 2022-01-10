package org.nrg.xnatx.plugins.pixi.preferences;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class Vendor {
    @ApiModelProperty(required = true, position = 0)
    @NotNull @Positive
    private Integer id;

    @ApiModelProperty(required = true, position = 1)
    @NotBlank
    private String vendor;
}
