package org.nrg.xnatx.plugins.pixi.bli.helpers;

import org.nrg.xft.security.UserI;

import java.io.File;

public interface PrearcUtilsHelper {

    String makeTimestamp();
    File getPrearcSessionDir(UserI user, String project, String timestamp, String session, boolean allowUnassigned) throws Exception;

}
