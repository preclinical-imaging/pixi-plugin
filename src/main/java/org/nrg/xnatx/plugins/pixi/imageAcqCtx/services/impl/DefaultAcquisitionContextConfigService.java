package org.nrg.xnatx.plugins.pixi.imageAcqCtx.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.AnesthesiaConfig;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.FastingConfig;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.models.HeatingConditionsConfig;
import org.nrg.xnatx.plugins.pixi.imageAcqCtx.services.ImageAcquisitionContextConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

/**
 * Manage the image acquisition context configs. The FastingConfig, HeatingConditionsConfig, and AnesthesiaConfig are
 * stored in the config service as JSON at the site and project level.
 */
@Service
@Slf4j
public class DefaultAcquisitionContextConfigService implements ImageAcquisitionContextConfigService {

    private final ConfigService configService;
    private final ObjectMapper mapper;

    public final String TOOL_NAME = "imageAcquisitionContext";
    public final String FASTING_CONFIG_PATH = "fasting-config";
    public final String HEATING_CONDITIONS_CONFIG_PATH = "heating-conditions-config";
    public final String ANESTHESIA_CONFIG_PATH = "anesthesia-config";

    @Autowired
    public DefaultAcquisitionContextConfigService(ConfigService configService) {
        this.configService = configService;
        this.mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
    }

    /**
     * Get the fasting config for the given scope and entity
     * @param user the user
     * @param scope the scope of the config (Scope.Site or Scope.Project)
     * @param entityId null for site, project ID for project
     * @return the fasting config
     */
    @Override
    public FastingConfig getFastingConfig(UserI user, Scope scope, String entityId) {
        log.debug("Getting fasting config for scope {} and entity {}", scope, entityId);

        final FastingConfig config = getConfig(scope, entityId, FASTING_CONFIG_PATH, FastingConfig.class);

        if (config == null) {
            final FastingConfig newFastingConfig = FastingConfig.builder()
                    .fastingTemplates(Collections.emptyList())
                    .build();

            createOrUpdateFastingConfig(user, scope, entityId, newFastingConfig);

            return newFastingConfig;
        } else {
            return config;
        }
    }

    /**
     * Create or update the fasting config for the given scope and entity
     * @param user the user
     * @param scope the scope of the config (Scope.Site or Scope.Project)
     * @param entityId null for site, project ID for project
     * @param fastingConfig the fasting config to save
     * @return the fasting config
     */
    @Override
    public FastingConfig createOrUpdateFastingConfig(UserI user, Scope scope, String entityId, FastingConfig fastingConfig) {
        log.debug("Creating/updating fasting config for scope {} and entity {}", scope, entityId);

        return createOrUpdateConfig(user, scope, entityId, FASTING_CONFIG_PATH, fastingConfig);
    }

    /**
     * Get the heating conditions config for the given scope and entity
     * @param user the user
     * @param scope the scope of the config (Scope.Site or Scope.Project)
     * @param entityId null for site, project ID for project
     * @return the heating conditions config
     */
    @Override
    public HeatingConditionsConfig getHeatingConditionsConfig(UserI user, Scope scope, String entityId) {
        log.debug("Getting heating conditions config for scope {} and entity {}", scope, entityId);

        final HeatingConditionsConfig config = getConfig(scope, entityId, HEATING_CONDITIONS_CONFIG_PATH, HeatingConditionsConfig.class);

        if (config == null) {
            final HeatingConditionsConfig newHeatingConditionsConfig = HeatingConditionsConfig.builder()
                    .templates(Collections.emptyList())
                    .build();

            createOrUpdateHeatingConditionsConfig(user, scope, entityId, newHeatingConditionsConfig);

            return newHeatingConditionsConfig;
        } else {
            return config;
        }
    }

    /**
     * Create or update the heating conditions config for the given scope and entity
     * @param user the user
     * @param scope the scope of the config (Scope.Site or Scope.Project)
     * @param entityId null for site, project ID for project
     * @param heatingConditionsConfig the heating conditions config to save
     * @return the heating conditions config
     */
    @Override
    public HeatingConditionsConfig createOrUpdateHeatingConditionsConfig(UserI user, Scope scope, String entityId, HeatingConditionsConfig heatingConditionsConfig) {
        log.debug("Creating/updating heating conditions config for scope {} and entity {}", scope, entityId);

        return createOrUpdateConfig(user, scope, entityId, HEATING_CONDITIONS_CONFIG_PATH, heatingConditionsConfig);
    }

    /**
     * Get the anesthesia config for the given scope and entity
     * @param user the user
     * @param scope the scope of the config (Scope.Site or Scope.Project)
     * @param entityId null for site, project ID for project
     * @return the anesthesia config
     */
    @Override
    public AnesthesiaConfig getAnesthesiaConfig(UserI user, Scope scope, String entityId) {
        log.debug("Getting anesthesia config for scope {} and entity {}", scope, entityId);

        final AnesthesiaConfig config = getConfig(scope, entityId, ANESTHESIA_CONFIG_PATH, AnesthesiaConfig.class);

        if (config == null) {
            final AnesthesiaConfig newAnesthesiaConfig = AnesthesiaConfig.builder()
                    .anesthesiaTemplates(Collections.emptyList())
                    .build();

            createOrUpdateAnesthesiaConfig(user, scope, entityId, newAnesthesiaConfig);

            return newAnesthesiaConfig;
        } else {
            return config;
        }
    }

    /**
     * Create or update the anesthesia config for the given scope and entity
     * @param user the user
     * @param scope the scope of the config (Scope.Site or Scope.Project)
     * @param entityId null for site, project ID for project
     * @param anesthesiaConfig the anesthesia config
     * @return the anesthesia config
     */
    @Override
    public AnesthesiaConfig createOrUpdateAnesthesiaConfig(UserI user, Scope scope, String entityId, AnesthesiaConfig anesthesiaConfig) {
        log.debug("Creating/updating anesthesia config for scope {} and entity {}", scope, entityId);

        return createOrUpdateConfig(user, scope, entityId, ANESTHESIA_CONFIG_PATH, anesthesiaConfig);
    }

    /**
     * Get the config for the given scope, entity, and config name
     *
     * @param scope      the scope of the config (Scope.Site or Scope.Project)
     * @param entityId   null for site, project ID for project
     * @param configPath the path of the config (e.g. "fasting-config", "heating-conditions-config", "anesthesia-config")
     * @param clazz      the class of the config object (e.g. FastingConfig.class, HeatingConditionsConfig.class, AnesthesiaConfig.class)
     * @return the config
     */
    protected <Config> Config getConfig(Scope scope, String entityId, String configPath, Class<Config> clazz) {
        Configuration config = configService.getConfig(TOOL_NAME, configPath, scope, entityId);

        if (config == null) {
            return null;
        }

        try {
            return mapper.readValue(config.getConfigData().getContents(), clazz);
        } catch (IOException e) {
            log.error("Error converting " + configPath + " json string to object", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create or update the config for the given scope, entity, and config name
     * @param user the user
     * @param scope the scope of the config (Scope.Site or Scope.Project)
     * @param entityId null for site, project ID for project
     * @param configPath the path of the config (e.g. "fasting-config", "heating-conditions-config", "anesthesia-config")
     * @param config the config object to save, will be converted to json
     * @return the config
     */
    protected <Config> Config createOrUpdateConfig(UserI user, Scope scope, String entityId, String configPath, Config config) {
        String configJson = null;
        try {
            configJson = mapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            log.error("Error converting " + configPath + " to json string", e);
            throw new RuntimeException(e);
        }

        try {
            final String username = user.getUsername();
            final String reason = "Updating " + configPath;
            final String path = configPath;
            final String content = configJson;
            configService.replaceConfig(username, reason, TOOL_NAME, path, content, scope, entityId);
        } catch (ConfigServiceException e) {
            log.error("Error updating " + configPath, e);
            throw new RuntimeException(e);
        }

        return getConfig(scope, entityId, configPath, (Class<Config>) config.getClass());
    }

}
