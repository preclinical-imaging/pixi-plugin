package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.services.XenograftService;
import org.nrg.xnatx.plugins.pixi.services.impl.HibernatePDXEntityService;

import java.util.List;

public class PIXIDatatypeManager extends SecureScreen {
    @Override
    protected void doBuildTemplate(RunData runData, Context context) throws Exception {
        XenograftService<PDXEntity, PDX> pdxService = (HibernatePDXEntityService) XDAT.getContextService().getBean("PDXService");
        List<PDXEntity> pdxs = pdxService.getAll();
        context.put("pdxs", pdxs);
    }
}
