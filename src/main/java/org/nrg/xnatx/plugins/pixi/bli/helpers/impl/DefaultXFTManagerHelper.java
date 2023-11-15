package org.nrg.xnatx.plugins.pixi.bli.helpers.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xnatx.plugins.pixi.bli.helpers.XFTManagerHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultXFTManagerHelper implements XFTManagerHelper {

    /**
     * Checks if the XFTManager is initialized.
     * @return True if the XFTManager is initialized, false otherwise.
     */
    @Override
    public boolean isInitialized() {
        return XFTManager.isInitialized();
    }

}
