package org.nrg.xnatx.plugins.pixi;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.om.PixiAnimaldemographicdata;
import org.nrg.xdat.om.PixiHotel;
import org.nrg.xdat.om.PixiHotelscanrecord;
import org.nrg.xdat.om.PixiCalipermeasurementdata;
import org.nrg.xdat.om.PixiDrugtherapydata;
import org.nrg.xdat.om.PixiWeightdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.identifier.IDGeneratorI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.system.HostInfoService;
import org.nrg.xnat.turbine.utils.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.nrg.xft.identifier.IDGeneratorFactory.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@XnatPlugin(value = "PIXIPlugin", name = "PIXI Plugin",
            logConfigurationFile = "pixi-logback.xml",
            entityPackages = {"org.nrg.xnatx.plugins.pixi.xenografts.entities",
                              "org.nrg.xnatx.plugins.pixi.hotelsplitter.entities"},
            dataModels = {@XnatDataModel(value = PixiAnimaldemographicdata.SCHEMA_ELEMENT_NAME,
                                         singular = "Animal Demographic Data",
                                         plural = "Animal Demographics",
                                         code = "AD"),
                          @XnatDataModel(value = PixiWeightdata.SCHEMA_ELEMENT_NAME,
                                         singular = "Weight",
                                         plural = "Weights",
                                         code = "WT"),
                          @XnatDataModel(value = PixiDrugtherapydata.SCHEMA_ELEMENT_NAME,
                                         singular = "Drug Therapy",
                                         plural = "Drug Therapies",
                                         code = "DT"),
                          @XnatDataModel(value = PixiCalipermeasurementdata.SCHEMA_ELEMENT_NAME,
                                         singular = "Caliper Measurement",
                                         plural = "Caliper Measurements",
                                         code = "CM"),
                          @XnatDataModel(value = PixiHotelscanrecord.SCHEMA_ELEMENT_NAME,
                                         singular = "Hotel Scan Record",
                                         plural = "Hotel Scan Records",
                                         code = "HSR"),
                          @XnatDataModel(value = PixiHotel.SCHEMA_ELEMENT_NAME,
                                         singular = "Hotel",
                                         plural = "Hotels",
                                         code = "HTL")
                          })
@ComponentScan({"org.nrg.xnatx.plugins.pixi.xenografts.entities",
                "org.nrg.xnatx.plugins.pixi.xenografts.repositories",
                "org.nrg.xnatx.plugins.pixi.xenografts.services.impl",
                "org.nrg.xnatx.plugins.pixi.xenografts.rest",
                "org.nrg.xnatx.plugins.pixi.hotelsplitter.services.impl",
                "org.nrg.xnatx.plugins.pixi.hotelsplitter.rest",
                "org.nrg.xnatx.plugins.pixi.hotelsplitter.initialize",
                "org.nrg.xnatx.plugins.pixi.hotelsplitter.dcm.identifiers",
                "org.nrg.xnatx.plugins.pixi.preferences"
})
@Slf4j
public class PIXIPlugin {

    @Autowired
    public PIXIPlugin() { }

    @Bean
    public IDGeneratorI hotelIdGenerator(final JdbcTemplate template, final SiteConfigPreferences preferences, final HostInfoService hostInfoService, final XnatAppInfo appInfo) {
        final IDGeneratorI generator = new IDGenerator(template, preferences, hostInfoService, appInfo);
        generator.setTable("pixi_hotel");
        generator.setDigits(DEFAULT_DIGITS);
        generator.setColumn(DEFAULT_COLUMN);
        generator.setCode("HTL");
        return generator;
    }
}
