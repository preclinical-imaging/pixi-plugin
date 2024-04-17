package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnatx.plugins.pixi.bli.helpers.PrearcUtilsHelper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class DefaultPrearcUtilsHelper implements PrearcUtilsHelper {

    @Override
    public String makeTimestamp() {
        return PrearcUtils.makeTimestamp();
    }

    @Override
    public File getPrearcSessionDir(UserI user, String project, String timestamp, String session, boolean allowUnassigned) throws Exception {
        return PrearcUtils.getPrearcSessionDir(user, project, timestamp, session, allowUnassigned);
    }

}
