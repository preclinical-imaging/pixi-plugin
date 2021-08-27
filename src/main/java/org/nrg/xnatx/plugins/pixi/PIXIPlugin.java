package org.nrg.xnatx.plugins.pixi;

import lombok.extern.slf4j.Slf4j;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.annotations.XnatPlugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@XnatPlugin(value = "PIXIPlugin", name = "PIXI Plugin",
            logConfigurationFile = "pixi-logback.xml")
@Slf4j
public class PIXIPlugin {

    private final ConfigService configService;
    private static final Map<String, String> jsonFormFiles;
    static {
        jsonFormFiles = new HashMap<>();
        jsonFormFiles.put("xnat:subjectData", "/forms/pixi/preclinical-subject.json");
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
