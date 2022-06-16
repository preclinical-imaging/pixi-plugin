package org.apache.turbine.app.xnat.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.turbine.modules.actions.ModifyItem;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.InvalidValueException;

@Slf4j
public class ModifyPixiItem extends ModifyItem {
    @Override
    public void preProcess(final XFTItem item, final RunData data, final Context context) {
        try {
            if (StringUtils.isBlank(item.getStringProperty("ID"))) {
                item.setProperty("ID", XnatExperimentdata.CreateNewID());
            }
        } catch (InvalidValueException e) {
            log.error("An invalid value was specified", e);
        } catch (Exception e) {
            log.error("Uh oh", e);
        }
    }

    public boolean allowDataDeletion(){
        return true;
    }
}
