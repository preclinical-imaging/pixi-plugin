package org.nrg.xnatx.plugins.pixi.imageAcqCtx.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This config stores the anesthesia templates for both sites and projects
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnesthesiaConfig {

    private List<AnesthesiaTemplate> anesthesiaTemplates;

}
