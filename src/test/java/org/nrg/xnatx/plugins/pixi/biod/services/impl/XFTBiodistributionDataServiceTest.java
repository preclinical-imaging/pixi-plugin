package org.nrg.xnatx.plugins.pixi.biod.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnatx.plugins.pixi.config.XFTBiodistributionDataServiceTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = XFTBiodistributionDataServiceTestConfig.class)
public class XFTBiodistributionDataServiceTest {

    @Autowired private XFTBiodistributionDataService xftBiodistributionDataService;

    @Test
    public void test_autoWiredBeans() {
        assertNotNull(xftBiodistributionDataService);
    }

}