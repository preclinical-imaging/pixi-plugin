package org.nrg.xnat.turbine.modules.screens;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.PixiCalipermeasurementdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;

import java.time.LocalDate;

@SuppressWarnings("unused")
public class XDATScreen_edit_pixi_caliperMeasurementData extends EditSubjectAssessorScreen {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_caliperMeasurementData.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "pixi:caliperMeasurementData";
	}
	
	public ItemI getEmptyItem(RunData data) throws Exception {
		final PixiCalipermeasurementdata pixiCalipermeasurementdata =
				new PixiCalipermeasurementdata(XFTItem.NewItem(getElementName(), getUser()));

		pixiCalipermeasurementdata.setDate(LocalDate.now());

		return pixiCalipermeasurementdata.getItem();
	}
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		super.finalProcessing(data,context);
	}}
