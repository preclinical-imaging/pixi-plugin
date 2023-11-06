package org.nrg.xnatx.plugins.pixi.bli.initialize;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.pixi.bli.helpers.XFTManagerHelper;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingService;
import org.nrg.xnatx.plugins.pixi.config.AnalyzedClickInfoObjectIdentifierMappingInitializerTestConfig;
import org.nrg.xnatx.plugins.pixi.preferences.PIXIPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AnalyzedClickInfoObjectIdentifierMappingInitializerTestConfig.class)
public class AnalyzedClickInfoObjectIdentifierMappingInitializerTest {

    @Autowired private XFTManagerHelper mockXFTManagerHelper;
    @Autowired private XnatAppInfo mockXnatAppInfo;
    @Autowired private AnalyzedClickInfoObjectIdentifierMappingService mockAnalyzedClickInfoObjectIdentifierMappingService;
    @Autowired private PIXIPreferences mockPIXIPreferences;

    @Autowired private AnalyzedClickInfoObjectIdentifierMappingInitializer analyzedClickInfoObjectIdentifierMappingInitializer;

    @After
    public void after() {
        Mockito.reset(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockAnalyzedClickInfoObjectIdentifierMappingService,
                mockPIXIPreferences
        );
    }

    @Test
    public void test_autoWiredBeans() {
        // Verify
        assertThat(mockXFTManagerHelper, notNullValue());
        assertThat(mockXnatAppInfo, notNullValue());
        assertThat(mockAnalyzedClickInfoObjectIdentifierMappingService, notNullValue());
        assertThat(mockPIXIPreferences, notNullValue());
    }

    @Test
    public void test_getTaskName() {
        // Execute
        String taskName = analyzedClickInfoObjectIdentifierMappingInitializer.getTaskName();

        // Verify
        assertThat(taskName, notNullValue());
    }

    @Test(expected = InitializingTaskException.class)
    public void test_callImpl_xnatNotInitialized() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(false);
        when(mockXnatAppInfo.isInitialized()).thenReturn(false);

        // Execute
        analyzedClickInfoObjectIdentifierMappingInitializer.callImpl();

        // Verify
        // Exception thrown
    }

    @Test
    public void test_callImpl_mappingExists() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockAnalyzedClickInfoObjectIdentifierMappingService.getAllMappings())
                .thenReturn(Collections.singletonList(new AnalyzedClickInfoObjectIdentifierMapping()));

        // Execute
        analyzedClickInfoObjectIdentifierMappingInitializer.callImpl();

        // Verify
        verify(mockAnalyzedClickInfoObjectIdentifierMappingService, times(0)).createOrUpdate(anyString(), any());
        verify(mockPIXIPreferences, times(0)).setDefaultBliImporterMapping(anyString());
    }

    @Test
    public void test_callImpl_mappingDoesNotExist() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockAnalyzedClickInfoObjectIdentifierMappingService.getAllMappings()).thenReturn(Collections.emptyList());

        // Execute
        analyzedClickInfoObjectIdentifierMappingInitializer.callImpl();

        // Verify
        verify(mockAnalyzedClickInfoObjectIdentifierMappingService, times(1)).createOrUpdate(anyString(), any());
        verify(mockPIXIPreferences, times(1)).setDefaultBliImporterMapping(anyString());
    }

}