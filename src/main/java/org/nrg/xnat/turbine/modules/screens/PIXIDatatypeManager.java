package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.models.AnimalModel;
import org.nrg.xnatx.plugins.pixi.services.AnimalModelService;
import org.nrg.xnatx.plugins.pixi.services.PDXEntityService;

import java.util.List;

public class PIXIDatatypeManager extends SecureScreen {
    @Override
    protected void doBuildTemplate(RunData runData, Context context) throws Exception {
        AnimalModelService animalModelService = XDAT.getContextService().getBean(AnimalModelService.class);
        List<AnimalModel> animalModels = animalModelService.getAllAnimalModels();
        context.put("animalModels", animalModels);

        PDXEntityService pdxEntityService = XDAT.getContextService().getBean(PDXEntityService.class);
        List<PDXEntity> pdxs = pdxEntityService.getAll();
        context.put("pdxs", pdxs);
    }
}
