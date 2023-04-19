package org.nrg.xnatx.plugins.pixi.imageAcqCtx.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the pixi:anesthesiaData element
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Anesthesia {

    private String anesthesia;
    private String routeOfAdministration;

}
