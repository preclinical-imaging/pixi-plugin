package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatExperimentdataShareI;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureReport;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author XDAT
 *
 */
public class XDATScreen_report_pixi_caliperMeasurementData extends SecureReport {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_report_pixi_caliperMeasurementData.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		try{
			org.nrg.xdat.om.PixiCalipermeasurementdata om = new org.nrg.xdat.om.PixiCalipermeasurementdata(item);
			context.put("om",om);
			System.out.println("Loaded om object (org.nrg.xdat.om.PixiCalipermeasurementdata) as context parameter 'om'.");
			context.put("subject",om.getSubjectData());
			System.out.println("Loaded subject object (org.nrg.xdat.om.XnatSubjectdata) as context parameter 'subject'.");
			if(context.get("project")==null) {
				String proj = om.getProject();
				if (!Permissions.canReadProject(XDAT.getUserDetails(), proj)) {
					// If user cannot read that project, look through the projects that session is shared into. If user
					// can view the data in one of those projects they should view this session from that project's context.
					List<XnatExperimentdataShareI> list = om.getSharing_share();
					for (XnatExperimentdataShareI exptShare : list) {
						if (Permissions.canReadProject(XDAT.getUserDetails(), exptShare.getProject())) {
							proj = exptShare.getProject();
							break;
						}
					}
				}
				context.put("project", proj);
			}

			String lengthS = this.om.getStringProperty("pixi:caliperMeasurementData/length");
			String widthS = this.om.getStringProperty("pixi:caliperMeasurementData/width");

			double length;
			double width;
			double volume;

			if (lengthS != null && widthS != null) {
				length = Double.parseDouble(lengthS);
				width = Double.parseDouble(widthS);
				volume = 0.5 * length * width * width;

				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(2);
				context.put("volume", df.format(volume));
			} else {
				context.put("volume", "");
			}

		} catch(Exception ignored){}
	}}
