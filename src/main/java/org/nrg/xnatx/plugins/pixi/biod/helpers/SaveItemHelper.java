package org.nrg.xnatx.plugins.pixi.biod.helpers;

import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;

public interface SaveItemHelper {

    void authorizedSave(ItemI i, UserI user, boolean overrideSecurity, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval, EventMetaI c) throws Exception;

}
