package org.nrg.xnatx.plugins.pixi.biod.helpers.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.biod.helpers.SaveItemHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultSaveItemHelper implements SaveItemHelper {

    @Override
    public void authorizedSave(ItemI i, UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval, EventMetaI c) throws Exception {
        org.nrg.xft.utils.SaveItemHelper.authorizedSave(i, user, overrideSecurity, quarantine, overrideQuarantine, allowItemRemoval, c);
    }

}
