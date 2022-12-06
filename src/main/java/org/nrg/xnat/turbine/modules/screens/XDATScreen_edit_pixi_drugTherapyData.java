package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.PixiDrugtherapydata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;

import java.time.LocalDate;
import java.util.Optional;

@SuppressWarnings("unused")
public class XDATScreen_edit_pixi_drugTherapyData extends EditSubjectAssessorScreen {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_drugTherapyData.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "pixi:drugTherapyData";
	}
	
	public ItemI getEmptyItem(RunData data) throws Exception {
		final PixiDrugtherapydata pixiDrugtherapydata =
				new PixiDrugtherapydata(XFTItem.NewItem(getElementName(), getUser()));

		pixiDrugtherapydata.setDate(LocalDate.now());

		final UserI user = getUser();
		final Optional<Object> subject_id = Optional.ofNullable(TurbineUtils.GetPassedParameter("part_id", data));

		if (subject_id.isPresent()) {
			final int count = PixiDrugtherapydata.getCountForSubject((String) subject_id.get()) + 1;
			final XnatSubjectdata subjectData = XnatSubjectdata.getXnatSubjectdatasById(subject_id.get(), getUser(), false);
			final String label = subjectData.getLabel() + "_DT_" + count;
			pixiDrugtherapydata.setLabel(label);
		}

		return pixiDrugtherapydata.getItem();
	}
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		super.finalProcessing(data,context);
	}}
