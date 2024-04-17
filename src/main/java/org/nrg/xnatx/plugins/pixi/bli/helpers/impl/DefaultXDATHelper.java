package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xnatx.plugins.pixi.bli.helpers.XDATHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultXDATHelper implements XDATHelper {

    @Override
    public void sendJmsRequest(Object request) {
        XDAT.sendJmsRequest(request);
    }

}
