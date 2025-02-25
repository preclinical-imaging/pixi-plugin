package org.nrg.xnatx.plugins.pixi.biod.helpers;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultXnatExperimentDataHelper implements XnatExperimentDataHelper {

    @Override
    public String createNewId() throws Exception {
        return XnatExperimentdata.CreateNewID();
    }

    @Override
    public XnatExperimentdataI getExptByProjectIdentifier(String project, String identifier, UserI user, boolean preLoad) {
        return XnatExperimentdata.GetExptByProjectIdentifier(project, identifier, user, preLoad);
    }

}
