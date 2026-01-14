package org.nrg.xnatx.plugins.pixi.biod.helpers;

import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.security.UserI;

public interface XnatSubjectDataHelper {

    String createNewId() throws Exception;
    XnatSubjectdata getSubjectByIdOrProjectlabelCaseInsensitive(String project, String identifier, UserI user, boolean preLoad);

}
