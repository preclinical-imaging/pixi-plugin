package org.nrg.xnatx.plugins.pixi.imageAcqCtx.services;

import org.nrg.framework.constants.Scope;
import org.nrg.framework.services.NrgService;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.AnesthesiaConfig;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.FastingConfig;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.HeatingConditionsConfig;

public interface ImageAcquisitionContextConfigService extends NrgService {

    FastingConfig getFastingConfig(UserI user, Scope scope, String entityId);
    FastingConfig createOrUpdateFastingConfig(UserI user, Scope scope, String entityId, FastingConfig fastingConfig);

    HeatingConditionsConfig getHeatingConditionsConfig(UserI user, Scope scope, String entityId);
    HeatingConditionsConfig createOrUpdateHeatingConditionsConfig(UserI user, Scope scope, String entityId, HeatingConditionsConfig heatingConditionsConfig);

    AnesthesiaConfig getAnesthesiaConfig(UserI user, Scope scope, String entityId);
    AnesthesiaConfig createOrUpdateAnesthesiaConfig(UserI user, Scope scope, String entityId, AnesthesiaConfig anesthesiaConfig);

}
