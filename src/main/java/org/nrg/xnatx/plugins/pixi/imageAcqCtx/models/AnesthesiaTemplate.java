package org.nrg.xnatx.plugins.pixi.imageAcqCtx.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used to store pixi:anesthesiaData templates/protocols
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnesthesiaTemplate {

    private String name;
    private boolean defaultTemplate;
    private Anesthesia anesthesia;

}
