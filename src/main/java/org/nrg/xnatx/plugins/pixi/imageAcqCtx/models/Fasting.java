package org.nrg.xnatx.plugins.pixi.imageAcqCtx.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the pixi:fastingData element
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Fasting {

    private Boolean fastingStatus;
    private Double fastingDuration;

}
