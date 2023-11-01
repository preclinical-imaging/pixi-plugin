package org.nrg.xnatx.plugins.pixi.bli.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.pixi.bli.helpers.XFTManagerHelper;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnalyzedClickInfoObjectIdentifierMappingInitializer extends AbstractInitializingTask {

    private final XFTManagerHelper xftManagerHelper;
    private final XnatAppInfo appInfo;
    private final AnalyzedClickInfoObjectIdentifierMappingService mappingService;

    @Autowired
    public AnalyzedClickInfoObjectIdentifierMappingInitializer(final XFTManagerHelper xftManagerHelper,
                                                               final XnatAppInfo appInfo,
                                                               final AnalyzedClickInfoObjectIdentifierMappingService mappingService) {
        super();
        this.xftManagerHelper = xftManagerHelper;
        this.appInfo = appInfo;
        this.mappingService = mappingService;
    }

    @Override
    public String getTaskName() {
        return "AnalyzedClickInfoObjectIdentifierMappingInitializer";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        log.info("Initializing default BLI importer mapping.");

        if (!xftManagerHelper.isInitialized()) {
            log.debug("XFT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (!appInfo.isInitialized()) {
            log.debug("XNAT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (!mappingService.getAllMappings().isEmpty()) {
            log.info("BLI importer mappings already exists, skipping creation.");
        } else {
            AnalyzedClickInfoObjectIdentifierMapping mapping = AnalyzedClickInfoObjectIdentifierMapping.builder()
                                                                                                       .name("Default")
                                                                                                       .projectLabelField("")
                                                                                                       .projectLabelRegex("")
                                                                                                       .subjectLabelField("animalNumber")
                                                                                                       .subjectLabelRegex("(.*)")
                                                                                                       .hotelSession(false)
                                                                                                       .hotelSubjectSeparator("")
                                                                                                       .sessionLabelField("experiment")
                                                                                                       .sessionLabelRegex("(.*)")
                                                                                                       .scanLabelField("view")
                                                                                                       .subjectLabelRegex("(.*)")
                                                                                                       .build();

            mappingService.createOrUpdate("Default", mapping);
            log.info("Created default BLI importer mapping.");
        }
    }
}
