package org.nrg.xnatx.plugins.pixi.biod.services;

import org.nrg.xdat.model.PixiBiodistributiondataI;
import org.nrg.xft.security.UserI;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BiodistributionDataService {

    Optional<PixiBiodistributiondataI> findByLabel(final UserI user, String project, String label);
    List<PixiBiodistributiondataI> fromCsv(final UserI user, final String project, final String userCachePath, String dataOverlapHandling) throws Exception;
    List<PixiBiodistributiondataI> fromCsv(final UserI user, final String project, final File file, String dataOverlapHandling) throws Exception;

}
