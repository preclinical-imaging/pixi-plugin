package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.PixiCelllinedata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.xenografts.entities.CellLineEntity;
import org.nrg.xnatx.plugins.pixi.xenografts.models.CellLine;
import org.nrg.xnatx.plugins.pixi.xenografts.services.XenograftService;

import java.util.List;
import java.util.Optional;

public class XDATScreen_edit_pixi_cellLineData extends EditSubjectAssessorScreen {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_cellLineData.class);
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "pixi:cellLineData";
	}
	
	public ItemI getEmptyItem(RunData data) throws Exception
	{
		final UserI user = getUser();
		final PixiCelllinedata pixiCelllinedata = new PixiCelllinedata(XFTItem.NewItem(getElementName(), user));
		final Optional<Object> subject_id = Optional.ofNullable(TurbineUtils.GetPassedParameter("part_id", data));

		// Auto generate experiment label for known subjects
		if (subject_id.isPresent()) {
			final int count = PixiCelllinedata.getCountForSubject((String) subject_id.get()) + 1;
			final XnatSubjectdata subjectData = XnatSubjectdata.getXnatSubjectdatasById(subject_id.get(), getUser(), false);
			final String label = subjectData.getLabel() + "_CL_" + count;
			pixiCelllinedata.setLabel(label);
		}

		return pixiCelllinedata.getItem();
	}

	@Override
	public void doBuildTemplate(RunData data, Context context) {
		super.doBuildTemplate(data, context);

		XenograftService<CellLineEntity, CellLine> cellLineService = XDAT.getContextService().getBean("CellLineService", XenograftService.class);
		List<CellLine> cellLines = cellLineService.getAllXenografts();
		context.put("cellLines", cellLines);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		super.finalProcessing(data,context);
	}}
