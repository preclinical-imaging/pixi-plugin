package org.nrg.xnatx.plugins.pixi.biod.services;

import org.nrg.xdat.model.PixiBiodistributiondataI;
import org.nrg.xft.security.UserI;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface BiodistributionDataService {

    Optional<PixiBiodistributiondataI> findByLabel(final UserI user, String project, String label);
    PixiBiodistributiondataI createOrUpdate(final UserI user, final PixiBiodistributiondataI biodistributionData, String dataOverlapHandling) throws Exception;
    List<PixiBiodistributiondataI> createOrUpdate(final UserI user, final List<PixiBiodistributiondataI> biodistributionDatas, String dataOverlapHandling) throws Exception;
    List<PixiBiodistributiondataI> fromExcel(final UserI user, final String project, final String userCachePath) throws Exception;
    List<PixiBiodistributiondataI> fromExcel(final UserI user, final String project, final File file) throws Exception;

}
