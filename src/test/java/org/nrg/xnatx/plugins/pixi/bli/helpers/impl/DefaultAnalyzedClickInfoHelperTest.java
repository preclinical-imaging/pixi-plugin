package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnatx.plugins.pixi.config.DefaultAnalyzedClickInfoHelperTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultAnalyzedClickInfoHelperTestConfig.class)
public class DefaultAnalyzedClickInfoHelperTest {

    @Autowired
    private DefaultAnalyzedClickInfoHelper defaultAnalyzedClickInfoHelper;

    @Test
    public void testAutoWiredBeans() {
        assertThat(defaultAnalyzedClickInfoHelper, notNullValue());
    }

    @Test(expected = FileNotFoundException.class)
    public void testEmptyDirThrowsException() throws IOException {
        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("tempDir");
        Path jsonFileDNE = tempDir.resolve("AnalyzedClickInfo.json");

        // Do not create the json file

        // Execute and expect FileNotFoundException
        defaultAnalyzedClickInfoHelper.readJson(jsonFileDNE);
    }

}