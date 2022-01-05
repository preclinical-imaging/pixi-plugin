package org.nrg.xnatx.plugins.pixi;

import lombok.extern.slf4j.Slf4j;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.om.PixiAnimaldemographicdata;
import org.nrg.xdat.om.PixiCalipermeasurementdata;
import org.nrg.xdat.om.PixiDrugtherapydata;
import org.nrg.xdat.om.PixiWeightdata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@XnatPlugin(value = "PIXIPlugin", name = "PIXI Plugin",
            logConfigurationFile = "pixi-logback.xml",
            entityPackages = "org.nrg.xnatx.plugins.pixi.entities",
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
                                         code = "CM")
                          })
@ComponentScan({"org.nrg.xnatx.plugins.pixi.entities",
                "org.nrg.xnatx.plugins.pixi.repositories",
                "org.nrg.xnatx.plugins.pixi.services.impl",
                "org.nrg.xnatx.plugins.pixi.rest"})
@Slf4j
public class PIXIPlugin {

    public static final String PIXI_PDX_DATATYPE = "xhbm:pixi:pdx";
    public static final String PIXI_CELLLINE_DATATYPE = "xhbm:pixi:cellLine";

    private final ConfigService configService;
    private static final Map<String, String> jsonFormFiles;
    static {
        jsonFormFiles = new HashMap<>();
        jsonFormFiles.put(PIXI_PDX_DATATYPE, "/forms/pixi/pdx.json");
        jsonFormFiles.put(PIXI_CELLLINE_DATATYPE, "/forms/pixi/cellLine.json");
    }

    @Autowired
    public PIXIPlugin(final ConfigService configService) {
        this.configService = configService;
        initializePIXIForms();
    }

    private void initializePIXIForms() {
        jsonFormFiles.forEach((xsiType,fileName) -> {
            String jsonForm = getJsonFromFile(fileName);
            storeJsonFormConfig(xsiType, jsonForm);
        });
    }

    private String getJsonFromFile(final String fileName) {
        InputStream in = getClass().getResourceAsStream(fileName);
        return (new BufferedReader(new InputStreamReader(in))).lines().collect(Collectors.joining());
    }

    private void storeJsonFormConfig(final String xsiType, final String jsonForm) {
        try {
            final String tool = "forms";
            final String path = "datatype/" + xsiType;
            Configuration c = configService.getConfig(tool, path);
            if (c == null) {
                configService.replaceConfig("admin", "", "forms", "datatype/" + xsiType, true, jsonForm);
            }
        } catch (ConfigServiceException e) {
            e.printStackTrace();
        }
    }
}
