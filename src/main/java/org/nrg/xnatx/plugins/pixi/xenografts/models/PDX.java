package org.nrg.xnatx.plugins.pixi.xenografts.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class PDX extends Xenograft {

    @ApiModelProperty(position = 3)
    @Nullable
    private String storage;

    @Builder
    public PDX(@NotBlank final String sourceId,
               @Nullable final String source,
               @Nullable final String sourceURL,
               @Nullable final String createdBy,
               @Nullable final String storage) {
        super(sourceId, source, sourceURL, createdBy);

        this.storage = storage;
    }
}
