package org.nrg.xnatx.plugins.pixi.bli.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.pixi.bli.helpers.XFTManagerHelper;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingService;
import org.nrg.xnatx.plugins.pixi.preferences.PIXIPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Initializes the default BLI importer mapping.
 */
@Component
@Slf4j
public class AnalyzedClickInfoObjectIdentifierMappingInitializer extends AbstractInitializingTask {

    private final XFTManagerHelper xftManagerHelper;
    private final XnatAppInfo appInfo;
    private final AnalyzedClickInfoObjectIdentifierMappingService mappingService;
    private final PIXIPreferences pixiPreferences;

    @Autowired
    public AnalyzedClickInfoObjectIdentifierMappingInitializer(@Qualifier("PixiXFTManagerHelper") final XFTManagerHelper xftManagerHelper,
                                                               final XnatAppInfo appInfo,
                                                               final AnalyzedClickInfoObjectIdentifierMappingService mappingService,
                                                               final PIXIPreferences pixiPreferences) {
        super();
        this.xftManagerHelper = xftManagerHelper;
        this.appInfo = appInfo;
        this.mappingService = mappingService;
        this.pixiPreferences = pixiPreferences;
    }

    @Override
    public String getTaskName() {
        return "AnalyzedClickInfoObjectIdentifierMappingInitializer";
    }

    /**
     * Creates the default BLI importer mapping.
     * @throws InitializingTaskException When the XFTManagerHelper or XnatAppInfo is not initialized.
     */
    @Override
    protected void callImpl() throws InitializingTaskException {
        log.debug("Initializing default BLI importer mapping.");

        if (!xftManagerHelper.isInitialized() || !appInfo.isInitialized()) {
            log.debug("XFTManagerHelper or XnatAppInfo is not initialized, skipping creation.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (!mappingService.getAllMappings().isEmpty()) {
            log.info("BLI importer mappings already exists, skipping creation.");
        } else {
            AnalyzedClickInfoObjectIdentifierMapping mapping = AnalyzedClickInfoObjectIdentifierMapping.builder()
                                                                                                       .name("PIXI Default")
                                                                                                       .projectLabelField("")
                                                                                                       .projectLabelRegex("")
                                                                                                       .subjectLabelField("animalNumber")
                                                                                                       .subjectLabelRegex("(.*)")
                                                                                                       .hotelSession(false)
                                                                                                       .hotelSubjectSeparator("")
                                                                                                       .sessionLabelField("experiment")
                                                                                                       .sessionLabelRegex("(.*)")
                                                                                                       .scanLabelField("view")
                                                                                                       .scanLabelRegex("(.*)")
                                                                                                       .build();

            mappingService.createOrUpdate(mapping.getName(), mapping);
            pixiPreferences.setDefaultBliImporterMapping(mapping.getName());
            log.info("Created default BLI importer mapping.");
        }
    }
}
