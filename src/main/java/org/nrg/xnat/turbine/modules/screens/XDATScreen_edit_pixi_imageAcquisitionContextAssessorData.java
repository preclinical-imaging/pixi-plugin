package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.PixiImageacquisitioncontextassessordata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;

import java.util.Optional;

@SuppressWarnings("unused")
public class XDATScreen_edit_pixi_imageAcquisitionContextAssessorData extends EditImageAssessorScreen {
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XDATScreen_edit_pixi_imageAcquisitionContextAssessorData.class);

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
     */
    public String getElementName() {
        return PixiImageacquisitioncontextassessordata.SCHEMA_ELEMENT_NAME;
    }

    public ItemI getEmptyItem(RunData data) throws Exception {
		final PixiImageacquisitioncontextassessordata pixiImageacquisitioncontextassessordata =
				new PixiImageacquisitioncontextassessordata(XFTItem.NewItem(getElementName(), getUser()));

		Optional<String> experiment = Optional.ofNullable((String) TurbineUtils.GetPassedParameter("experiment", data));

		pixiImageacquisitioncontextassessordata.setProject((String) TurbineUtils.GetPassedParameter("project", data));
		pixiImageacquisitioncontextassessordata.setImagesessionId(experiment.orElse(""));

		experiment.ifPresent(e -> {
			XnatImagesessiondata xnatImagesessiondata = XnatImagesessiondata.getXnatImagesessiondatasById(e, getUser(), false);
			int count = xnatImagesessiondata.getAssessorCount(getElementName());

			if (count == 0) {
				pixiImageacquisitioncontextassessordata.setLabel(xnatImagesessiondata.getLabel() + "_imgAcqCtx");
			} else {
				pixiImageacquisitioncontextassessordata.setLabel(xnatImagesessiondata.getLabel() + "_imgAcqCtx_" + (count + 1));
			}
		});

        return pixiImageacquisitioncontextassessordata.getItem();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
    }

	@Override
	public void doBuildTemplate(RunData data, Context context) {
		super.doBuildTemplate(data, context);

		if (data.getParameters().containsKey("project")) {
			String projectId = (String) TurbineUtils.GetPassedParameter("project", data);

			if (projectId != null) {
				context.put("project", projectId);
			} else {
				context.put("project", "");
			}
		} else {
			context.put("project", "");
		}

		if (data.getParameters().containsKey("subject")) {
			String subjectId = (String) TurbineUtils.GetPassedParameter("subject", data);

			if (subjectId != null) {
				context.put("subject", subjectId);
			} else {
				context.put("subject", "");
			}
		} else {
			context.put("subject", "");
		}

		if (data.getParameters().containsKey("experiment")) {
			String experimentId = (String) TurbineUtils.GetPassedParameter("experiment", data);

			if (experimentId != null) {
				context.put("experiment", experimentId);
			} else {
				context.put("experiment", "null");
			}
		} else {
			context.put("experiment", "no key");
		}
	}
}
