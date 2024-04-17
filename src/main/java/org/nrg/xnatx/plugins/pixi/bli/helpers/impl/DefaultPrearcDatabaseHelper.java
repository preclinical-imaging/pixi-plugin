package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnatx.plugins.pixi.bli.helpers.PrearcDatabaseHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultPrearcDatabaseHelper implements PrearcDatabaseHelper {

    /**
     * Adds a session to the prearchive database.
     * @param sessionData The session data to add.
     * @throws Exception If an error occurs while adding the session to the prearchive database.
     */
    @Override
    public void addSession(SessionData sessionData) throws Exception {
        log.trace("Adding session to prearchive database: {}", sessionData);
        PrearcDatabase.addSession(sessionData);
    }

    /**
     * Deletes a session from the prearchive database.
     * @param session The session to delete.
     * @param timestamp The timestamp of the session to delete.
     * @param project The project of the session to delete.
     * @return True if the session was deleted, false otherwise.
     * @throws Exception If an error occurs while deleting the session from the prearchive database.
     */
    @Override
    public boolean deleteSession(String session, String timestamp, String project) throws Exception {
        log.trace("Deleting session from prearchive database: session={}, timestamp={}, project={}", session, timestamp, project);
        return PrearcDatabase.deleteSession(session, timestamp, project);
    }

}
