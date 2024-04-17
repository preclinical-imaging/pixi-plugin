package org.nrg.xnatx.plugins.pixi.bli.helpers;

import org.nrg.xnat.helpers.prearchive.SessionData;

public interface PrearcDatabaseHelper {

    void addSession(final SessionData sessionData) throws Exception;
    boolean deleteSession(final String session, final String timestamp, final String project) throws Exception;

}
