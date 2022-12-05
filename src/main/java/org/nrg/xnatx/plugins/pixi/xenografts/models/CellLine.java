package org.nrg.xnatx.plugins.pixi.xenografts.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class CellLine extends Xenograft {

    @Builder
    public CellLine(@NotBlank final String sourceId,
                    @Nullable final String source,
                    @Nullable final String sourceURL,
                    @Nullable final String createdBy) {
        super(sourceId, source, sourceURL, createdBy);
    }
}
