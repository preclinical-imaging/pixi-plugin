package org.nrg.xnatx.plugins.pixi.biod.helpers.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.biod.helpers.XnatSubjectDataHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultXnatSubjectDataHelper implements XnatSubjectDataHelper {

    @Override
    public String createNewId() throws Exception {
        return XnatSubjectdata.CreateNewID();
    }

    @Override
    public XnatSubjectdata getSubjectByIdOrProjectlabelCaseInsensitive(String project, String identifier, UserI user, boolean preLoad) {
        return XnatSubjectdata.GetSubjectByIdOrProjectlabelCaseInsensitive(project, identifier, user, preLoad);
    }

}
