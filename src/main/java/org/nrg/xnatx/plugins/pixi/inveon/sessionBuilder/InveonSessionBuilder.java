package org.nrg.xnatx.plugins.pixi.inveon.sessionBuilder;

import lombok.extern.slf4j.Slf4j;
import org.nrg.session.SessionBuilder;
import org.nrg.xdat.bean.XnatImagesessiondataBean;

import java.io.File;
import java.io.Writer;

@Slf4j
public class InveonSessionBuilder extends SessionBuilder {

    private final File sessionDir;

    public InveonSessionBuilder(final File sessionDir, final Writer fileWriter) {
        super(sessionDir, sessionDir.getPath(), fileWriter);
        this.sessionDir = sessionDir;
        log.debug("Inveon session builder created for session: {}", sessionDir.getPath());
    }

    @Override
    public String getSessionInfo() {
        // TODO - Steve - See PETSesssionBuilder in xnat-web for an example. Low priority.
        return "(undetermined)";
    }

    @Override
    public XnatImagesessiondataBean call() throws Exception {
        log.info("Building Inveon session for session: {}", sessionDir.getPath());

        // TODO - Steve - Implement the InveonSessionBuilder. See BliSessionBuilder for an example.
        log.error("InveonSessionBuilder not implemented yet");

        return null;
    }

}
