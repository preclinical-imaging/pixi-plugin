/*
 * GENERATED FILE
 * Created on Wed Aug 04 11:09:37 CDT 2021
 *
 */
package org.nrg.xnat.turbine.modules.screens;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xft.ItemI;
import org.nrg.xnatx.plugins.pixi.entities.AnimalModelEntity;
import org.nrg.xnatx.plugins.pixi.services.AnimalModelEntityService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author XDAT
 *
 */
public class XDATScreen_edit_pixi_animalModelCreation extends EditSubjectAssessorScreen {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_animalModelCreation.class);

	@Override
	public void doBuildTemplate(RunData runData, Context context) {
		super.doBuildTemplate(runData, context);

		AnimalModelEntityService animalModelEntityService = XDAT.getContextService().getBean(AnimalModelEntityService.class);
		List<String> animalModelIDs = animalModelEntityService.getAll().stream()
				.map(AnimalModelEntity::getAnimalModelID)
				.collect(Collectors.toList());

		context.put("animalModelIDs", animalModelIDs);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
	 */
	public String getElementName() {
	    return "pixi:animalModelCreation";
	}
	
	public ItemI getEmptyItem(RunData data) throws Exception
	{
		return super.getEmptyItem(data);
	}
	/* (non-Javadoc)
	 * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void finalProcessing(RunData data, Context context) {
		super.finalProcessing(data,context);
	}}
