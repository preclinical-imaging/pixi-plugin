package org.nrg.xnatx.plugins.pixi.entities;

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
    public CellLineEntity(@NotBlank final String externalID,
                          @Nullable final String dataSource,
                          @Nullable final String dataSourceURL,
                          @Nullable final String createdBy) {
        super(externalID, dataSource, dataSourceURL, createdBy);
    }

}
