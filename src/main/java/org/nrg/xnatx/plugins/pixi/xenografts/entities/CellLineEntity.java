package org.nrg.xnatx.plugins.pixi.xenografts.entities;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class CellLineEntity extends XenograftEntity {

    @Builder
    public CellLineEntity(@NotBlank final String sourceId,
                          @Nullable final String source,
                          @Nullable final String sourceURL,
                          @Nullable final String createdBy) {
        super(sourceId, source, sourceURL, createdBy);
    }

}
