package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.PixiWeightdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;

import java.util.Optional;

@SuppressWarnings("unused")
public class XDATScreen_edit_pixi_weightData extends EditSubjectAssessorScreen {
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_weightData.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
     */
    public String getElementName() {
        return "pixi:weightData";
    }

    public ItemI getEmptyItem(RunData data) throws Exception {
        final UserI user = getUser();
        final PixiWeightdata pixiWeightdata = new PixiWeightdata(XFTItem.NewItem(getElementName(), user));
        final Optional<Object> subject_id = Optional.ofNullable(TurbineUtils.GetPassedParameter("part_id", data));

        // Auto generate experiment label for known subjects
        if (subject_id.isPresent()) {
            final int count = PixiWeightdata.getCountForSubject((String) subject_id.get()) + 1;
            final XnatSubjectdata subjectData = XnatSubjectdata.getXnatSubjectdatasById(subject_id.get(), getUser(), false);
            final String label = subjectData.getLabel() + "_WT_" + count;
            pixiWeightdata.setLabel(label);
        }

        return pixiWeightdata.getItem();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        super.finalProcessing(data, context);
    }
}
