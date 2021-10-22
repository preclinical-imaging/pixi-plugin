package org.nrg.xnatx.plugins.pixi.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@Slf4j
public abstract class Xenograft {

    @ApiModelProperty(required = true, position = 0)
    @NotBlank
    private String externalID;

    @ApiModelProperty(position = 1)
    @Nullable
    private String dataSource;

    @ApiModelProperty(position = 2)
    @Nullable private String dataSourceURL;

    @ApiModelProperty(hidden = true)
    @Nullable private String createdBy;

}
