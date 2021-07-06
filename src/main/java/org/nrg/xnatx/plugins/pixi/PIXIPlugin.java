package org.nrg.xnatx.plugins.pixi;

import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.om.PixiAnesthesia;
import org.nrg.xdat.om.PixiAnimalfeeding;
import org.nrg.xdat.om.PixiAnimalhousing;
import org.nrg.xdat.om.PixiAnimalmodel;
import org.nrg.xdat.om.PixiCircadianeffects;
import org.nrg.xdat.om.PixiHeatingconditions;
import org.nrg.xdat.om.PixiTreatmentprotocol;
import org.springframework.context.annotation.ComponentScan;

@XnatPlugin(value = "PIXIPlugin", name = "PIXI Plugin",
            logConfigurationFile = "pixi-logback.xml",
            entityPackages = "org.nrg.xnatx.plugins.pixi.entities",
            dataModels = {@XnatDataModel(value = PixiAnimalmodel.SCHEMA_ELEMENT_NAME,
                                         singular = "Animal Model",
                                         plural = "Animal Models",
                                         code = "AM"),
                          @XnatDataModel(value = PixiTreatmentprotocol.SCHEMA_ELEMENT_NAME,
                                         singular = "Treatment Protocol",
                                         plural = "Treatment Protocols",
                                         code = "TXP"),
                          @XnatDataModel(value = PixiAnimalhousing.SCHEMA_ELEMENT_NAME,
                                         singular = "Animal Housing",
                                         plural = "Animal Housings",
                                         code = "AH"),
                          @XnatDataModel(value = PixiAnimalfeeding.SCHEMA_ELEMENT_NAME,
                                         singular = "Animal Feeding",
                                         plural = "Animal Feedings",
                                         code = "AF"),
                          @XnatDataModel(value = PixiHeatingconditions.SCHEMA_ELEMENT_NAME,
                                         singular = "Heating Condition",
                                         plural = "Heating Conditions",
                                         code = "HC"),
                          @XnatDataModel(value = PixiAnesthesia.SCHEMA_ELEMENT_NAME,
                                         singular = "Anesthesia",
                                         plural = "Anesthesias",
                                         code = "ANA"),
                          @XnatDataModel(value = PixiCircadianeffects.SCHEMA_ELEMENT_NAME,
                                         singular = "Circadian Effect",
                                         plural = "Circadian Effects",
                                         code = "CIRC"),

            })
@ComponentScan({"org.nrg.xnatx.plugins.pixi.repositories",
                "org.nrg.xnatx.plugins.pixi.services.impl",
                "org.nrg.xnatx.plugins.pixi.rest"})
public class PIXIPlugin {
}
