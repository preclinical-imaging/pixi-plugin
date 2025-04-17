package org.nrg.xnatx.plugins.pixi.config;

import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService;
import org.nrg.xnatx.plugins.pixi.biod.helpers.SaveItemHelper;
import org.nrg.xnatx.plugins.pixi.biod.helpers.XnatExperimentDataHelper;
import org.nrg.xnatx.plugins.pixi.biod.helpers.XnatSubjectDataHelper;
import org.nrg.xnatx.plugins.pixi.biod.services.impl.XFTBiodistributionDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class XFTBiodistributionDataServiceTestConfig {

    @Bean
    public XFTBiodistributionDataService XFTBiodistributionDataService(UserDataCache mockUserDataCache,
                                                                       XnatSubjectDataHelper mockXnatSubjectDataHelper,
                                                                       XnatExperimentDataHelper mockXnatExperimentDataHelper,
                                                                       SaveItemHelper mockSaveItemHelper,
                                                                       DefaultCatalogService defaultCatalogService,
                                                                       SiteConfigPreferences siteConfigPreferences) {
        return new XFTBiodistributionDataService(
                mockUserDataCache,
                mockXnatSubjectDataHelper,
                mockXnatExperimentDataHelper,
                mockSaveItemHelper,
                defaultCatalogService,
                siteConfigPreferences
        );
    }

}
