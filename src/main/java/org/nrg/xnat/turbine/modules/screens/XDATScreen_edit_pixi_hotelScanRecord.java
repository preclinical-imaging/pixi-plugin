package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.PixiHotelI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.services.HotelService;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class XDATScreen_edit_pixi_hotelScanRecord extends EditScreenA {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_hotelScanRecord.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "pixi:hotelScanRecord";
	}
	
	public ItemI getEmptyItem(RunData data) throws Exception
	{
		return super.getEmptyItem(data);
	}

	@Override
	public void doBuildTemplate(RunData data, Context context) {
		super.doBuildTemplate(data, context);

		// Add subjects to the velocity context
		if (data.getParameters().containsKey("project")) {
			String projectId = (String) TurbineUtils.GetPassedParameter("project", data);

			// Call AutoXnatProjectdata to bypass cache
			XnatProjectdata project = AutoXnatProjectdata.getXnatProjectdatasById(projectId, getUser(), false);

			if (project != null) {
				context.put("project", project.getId());

				List<XnatSubjectdata> subjects = project.getParticipants_participant();
				context.put("subjects", subjects);
			}
		}

		try {
			String hotelName = item.getStringProperty("hotel");
			HotelService hotelService = XDAT.getContextService().getBean(HotelService.class);
			Optional<PixiHotelI> hotel = hotelService.findByName(getUser(), hotelName);

			if (hotel.isPresent()) {
				context.put("hotel", hotel.get());
			} else {
				List<PixiHotelI> hotels = hotelService.findAll(getUser());
				context.put("hotel", hotels.get(0));
			}
		} catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
			logger.error(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
	}

}
