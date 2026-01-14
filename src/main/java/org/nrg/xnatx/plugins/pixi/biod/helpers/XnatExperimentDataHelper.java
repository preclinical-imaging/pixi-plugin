package org.nrg.xnatx.plugins.pixi.biod.helpers;

import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xft.security.UserI;

public interface XnatExperimentDataHelper {

    String createNewId() throws Exception;
    XnatExperimentdataI getExptByProjectIdentifier(String project, String identifier, UserI user, boolean preLoad);

}
