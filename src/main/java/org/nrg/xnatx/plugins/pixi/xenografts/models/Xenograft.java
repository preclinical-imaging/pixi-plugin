package org.nrg.xnatx.plugins.pixi.xenografts.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public abstract class Xenograft {

    @ApiModelProperty(required = true, position = 0)
    @NotBlank
    private String sourceId;

    @ApiModelProperty(position = 1)
    @Nullable
    private String source;

    @ApiModelProperty(position = 2)
    @Nullable private String sourceURL;

    @ApiModelProperty(hidden = true)
    @Nullable private String createdBy;

}
