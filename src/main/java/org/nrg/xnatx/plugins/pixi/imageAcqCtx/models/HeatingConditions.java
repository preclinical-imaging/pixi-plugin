package org.nrg.xnatx.plugins.pixi.imageAcqCtx.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the pixi:heatingConditionsData element
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HeatingConditions {

    private String procedurePhase;
    private String heatingMethod;
    private Boolean feedbackTemperatureRegulation;
    private String temperatureSensorDeviceComponent;
    private Double setpointTemperature;
}
