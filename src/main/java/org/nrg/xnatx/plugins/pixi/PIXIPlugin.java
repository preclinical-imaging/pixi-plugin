package org.nrg.xnatx.plugins.pixi;

import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.om.PixiAnimalmodel;
import org.nrg.xdat.om.PixiTreatment;
import org.springframework.context.annotation.ComponentScan;

@XnatPlugin(value = "PIXIPlugin", name = "PIXI Plugin",
            logConfigurationFile = "pixi-logback.xml",
            entityPackages = "org.nrg.xnatx.plugins.pixi.entities",
            dataModels = {@XnatDataModel(value = PixiAnimalmodel.SCHEMA_ELEMENT_NAME,
                                         singular = "Animal Model",
                                         plural = "Animal Models",
                                         code = "AM"),
                          @XnatDataModel(value = PixiTreatment.SCHEMA_ELEMENT_NAME,
                                         singular = "Treatment",
                                         plural = "Treatments",
                                         code = "TX")
            })
@ComponentScan({"org.nrg.xnatx.plugins.pixi.repositories",
                "org.nrg.xnatx.plugins.pixi.services.impl",
                "org.nrg.xnatx.plugins.pixi.rest"})
public class PIXIPlugin {
}
