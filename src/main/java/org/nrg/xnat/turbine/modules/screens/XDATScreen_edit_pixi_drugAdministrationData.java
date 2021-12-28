package org.nrg.xnat.turbine.modules.screens;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.PixiDrugadministrationdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;

import java.time.LocalDate;

/**
 * @author XDAT
 *
 */
public class XDATScreen_edit_pixi_drugAdministrationData extends EditSubjectAssessorScreen {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_drugAdministrationData.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "pixi:drugAdministrationData";
	}
	
	public ItemI getEmptyItem(RunData data) throws Exception {
		final PixiDrugadministrationdata pixiDrugadministrationdata =
				new PixiDrugadministrationdata(XFTItem.NewItem(getElementName(), getUser()));

		pixiDrugadministrationdata.setDate(LocalDate.now());

		return pixiDrugadministrationdata;
	}
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		super.finalProcessing(data,context);
	}}
