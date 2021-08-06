package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.config.entities.Configuration;
import org.nrg.config.services.ConfigService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnatx.plugins.pixi.entities.AnimalModelEntity;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.services.AnimalModelEntityService;
import org.nrg.xnatx.plugins.pixi.services.PDXEntityService;

import java.util.List;

public class PIXIDatatypeManager extends SecureScreen {
    @Override
    protected void doBuildTemplate(RunData runData, Context context) throws Exception {
        AnimalModelEntityService animalModelEntityService = XDAT.getContextService().getBean(AnimalModelEntityService.class);
        List<AnimalModelEntity> animalModels = animalModelEntityService.getAll();
        context.put("animalModels", animalModels);

        PDXEntityService pdxEntityService = XDAT.getContextService().getBean(PDXEntityService.class);
        List<PDXEntity> pdxs = pdxEntityService.getAll();
        context.put("pdxs", pdxs);
    }
}
