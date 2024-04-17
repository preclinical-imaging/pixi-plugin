package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnatx.plugins.pixi.bli.helpers.ArcSpecManagerHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultArcSpecManagerHelper implements ArcSpecManagerHelper {

    @Override
    public String getGlobalPrearchivePath() {
        return ArcSpecManager.GetInstance().getGlobalPrearchivePath();
    }

}
