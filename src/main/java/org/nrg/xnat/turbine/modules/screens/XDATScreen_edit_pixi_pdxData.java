package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.PixiPdxdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.xenografts.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.xenografts.models.PDX;
import org.nrg.xnatx.plugins.pixi.xenografts.services.XenograftService;

import java.util.List;
import java.util.Optional;

public class XDATScreen_edit_pixi_pdxData extends EditSubjectAssessorScreen {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_pdxData.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "pixi:pdxData";
	}
	
	public ItemI getEmptyItem(RunData data) throws Exception {
		final UserI user = getUser();
		final PixiPdxdata pixiPdxdata = new PixiPdxdata(XFTItem.NewItem(getElementName(), user));
		final Optional<Object> subject_id = Optional.ofNullable(TurbineUtils.GetPassedParameter("part_id", data));

		// Auto generate experiment label for known subjects
		if (subject_id.isPresent()) {
			final int count = PixiPdxdata.getCountForSubject((String) subject_id.get()) + 1;
			final XnatSubjectdata subjectData = XnatSubjectdata.getXnatSubjectdatasById(subject_id.get(), getUser(), false);
			final String label = subjectData.getLabel() + "_PDX_" + count;
			pixiPdxdata.setLabel(label);
		}

		return pixiPdxdata.getItem();
	}

	@Override
	public void doBuildTemplate(RunData data, Context context) {
		super.doBuildTemplate(data, context);

		XenograftService<PDXEntity, PDX> pdxService = XDAT.getContextService().getBean("PDXService", XenograftService.class);
		List<PDX> pdxs = pdxService.getAllXenografts();
		context.put("pdxs", pdxs);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		super.finalProcessing(data,context);
	}}
