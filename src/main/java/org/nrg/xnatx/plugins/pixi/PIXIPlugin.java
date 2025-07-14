package org.nrg.xnatx.plugins.pixi;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.om.*;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.identifier.IDGeneratorI;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.system.HostInfoService;
import org.nrg.xnat.turbine.utils.IDGenerator;
import org.nrg.xnatx.plugins.pixi.biod.config.BiodConfig;
import org.nrg.xnatx.plugins.pixi.bli.config.BliConfig;
import org.nrg.xnatx.plugins.pixi.inveon.config.InveonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.nrg.xft.identifier.IDGeneratorFactory.DEFAULT_COLUMN;
import static org.nrg.xft.identifier.IDGeneratorFactory.DEFAULT_DIGITS;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@XnatPlugin(value = "PIXIPlugin", name = "PIXI Plugin",
            description = "The Preclinical Imaging XNAT-Enabled Informatics (PIXI) Plugin provides support for preclinical imaging data.",
            logConfigurationFile = "pixi-logback.xml",
            entityPackages = {
                                "org.nrg.xnatx.plugins.pixi.xenografts.entities",
                                "org.nrg.xnatx.plugins.pixi.hotelsplitter.entities",
                                "org.nrg.xnatx.plugins.pixi.bli.entities",
            },
            dataModels = {@XnatDataModel(value = PixiAnimaldemographicdata.SCHEMA_ELEMENT_NAME,
                                         singular = "Animal Demographic Data",
                                         plural = "Animal Demographics",
                                         code = "AD"),
                          @XnatDataModel(value = PixiPdxdata.SCHEMA_ELEMENT_NAME,
                                        singular = "PDX",
                                        plural = "PDXs",
                                        code = "PDX"),
                          @XnatDataModel(value = PixiCelllinedata.SCHEMA_ELEMENT_NAME,
                                         singular = "Cell Line",
                                         plural = "Cell Lines",
                                         code = "CL"),
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
                                         code = "HTL"),
                          @XnatDataModel(value = PixiAnimalhusbandrydata.SCHEMA_ELEMENT_NAME,
                                         singular = "Animal Husbandry",
                                         plural = "Animal Husbandry",
                                         code = "AH"),
                          @XnatDataModel(value = PixiImageacquisitioncontextassessordata.SCHEMA_ELEMENT_NAME,
                                         singular = "Image Acquisition Context",
                                         plural = "Image Acquisition Contexts",
                                         code = "IAC"),
                          @XnatDataModel(value = PixiBlisessiondata.SCHEMA_ELEMENT_NAME,
                                         singular = "BLI Session",
                                         plural = "BLI Sessions",
                                         code = "BLI"),
                          @XnatDataModel(value = PixiBliscandata.SCHEMA_ELEMENT_NAME,
                                         singular = "BLI Scan",
                                         plural = "BLI Scans",
                                         code = "BLIScan"),
                          @XnatDataModel(value = PixiBiodistributiondata.SCHEMA_ELEMENT_NAME,
                                         singular = "Biodistribution",
                                         plural = "Biodistributions",
                                         code = "BIOD"),
                          })
@ComponentScan({"org.nrg.xnatx.plugins.pixi.xenografts.entities",
                "org.nrg.xnatx.plugins.pixi.xenografts.repositories",
                "org.nrg.xnatx.plugins.pixi.xenografts.services.impl",
                "org.nrg.xnatx.plugins.pixi.xenografts.rest",
                "org.nrg.xnatx.plugins.pixi.hotelsplitter.services.impl",
                "org.nrg.xnatx.plugins.pixi.hotelsplitter.rest",
                "org.nrg.xnatx.plugins.pixi.hotelsplitter.initialize",
                "org.nrg.xnatx.plugins.pixi.hotelsplitter.eventservice.actions",
                "org.nrg.xnatx.plugins.pixi.preferences",
                "org.nrg.xnatx.plugins.pixi.imageAcqCtx.rest",
                "org.nrg.xnatx.plugins.pixi.imageAcqCtx.services.impl",
                "org.nrg.xnatx.plugins.pixi.rest",
                "org.nrg.xnatx.plugins.pixi.security",
                "org.nrg.xnatx.plugins.pixi.cmo.rest"
})
@Import({BliConfig.class, InveonConfig.class, BiodConfig.class})
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

    @Bean
    public ImporterHandlerPackages pixiImporterHandlerPackages() {
        return new ImporterHandlerPackages("org.nrg.xnatx.plugins.pixi.bli.importer",
                                           "org.nrg.xnatx.plugins.pixi.inveon.importer");
    }

}
