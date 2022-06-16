package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.PixiDrugtherapydata;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;

import java.time.LocalDate;

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

		return pixiDrugtherapydata.getItem();
	}
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		super.finalProcessing(data,context);
	}}
