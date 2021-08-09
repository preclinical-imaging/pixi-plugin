package org.nrg.xnatx.plugins.pixi;

import lombok.extern.slf4j.Slf4j;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.om.PixiModelcreation;
import org.nrg.xdat.om.PixiTreatment;
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
            dataModels = {@XnatDataModel(value = PixiModelcreation.SCHEMA_ELEMENT_NAME,
                                         singular = "Model Creation",
                                         plural = "Model Creations",
                                         code = "MC"),
                          @XnatDataModel(value = PixiTreatment.SCHEMA_ELEMENT_NAME,
                                         singular = "Treatment",
                                         plural = "Treatments",
                                         code = "T")})
@ComponentScan({"org.nrg.xnatx.plugins.pixi.entities",
                "org.nrg.xnatx.plugins.pixi.repositories",
                "org.nrg.xnatx.plugins.pixi.services.impl",
                "org.nrg.xnatx.plugins.pixi.rest"})
@Slf4j
public class PIXIPlugin {

    private final ConfigService configService;
    private static final Map<String, String> jsonFormFiles;
    static {
        jsonFormFiles = new HashMap<>();
        jsonFormFiles.put("xnat:subjectData", "/forms/pixi/preclinical-subject.json");
        jsonFormFiles.put("xhbm:pixi:animalModel", "/forms/pixi/animal-model.json");
        jsonFormFiles.put("xhbm:pixi:pdx", "/forms/pixi/pdx.json");
    }

    @Autowired
    public PIXIPlugin(final ConfigService configService) {
        this.configService = configService;
        initializePIXIForms();
    }

    private void initializePIXIForms() {
        // TODO: This will overwrite on every restart. Need to check for an existing form.
        jsonFormFiles.forEach((datatype,fileName) -> {
            String jsonForm = getJsonFormFromFile(fileName);
            storeJsonFormConfig(datatype, jsonForm);
        });
    }

    private String getJsonFormFromFile(final String fileName) {
        InputStream in = getClass().getResourceAsStream(fileName);
        return (new BufferedReader(new InputStreamReader(in))).lines().collect(Collectors.joining());
    }

    private void storeJsonFormConfig(final String datatype, final String jsonForm) {
        try {
            configService.replaceConfig("admin", "", "forms", "datatype/" + datatype, true, jsonForm);
        } catch (ConfigServiceException e) {
            e.printStackTrace();
        }
    }
}
