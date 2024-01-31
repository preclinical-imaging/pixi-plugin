package org.nrg.xnatx.plugins.pixi.inveon.sessionBuilder;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.bean.XnatImagescandataBean;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Slf4j
public class InveonScanBuilder implements Callable<XnatImagescandataBean> {

    private final Path scanDir;

    public InveonScanBuilder(final Path scanDir) {
        this.scanDir = scanDir;
    }

    @Override
    public XnatImagescandataBean call() throws Exception {
        log.debug("Building Inveon scans for {}", scanDir);

        // TODO - Steve - Implement the InveonScanBuilder. See BliScanBuilder for an example.
        log.error("InveonScanBuilder not implemented yet");

        return null;
    }
}
