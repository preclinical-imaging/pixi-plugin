package org.nrg.xdat.om.base;

import org.nrg.xdat.model.PixiHeatingconditionsdataI;
import org.nrg.xdat.om.PixiHeatingconditionsdata;
import org.nrg.xdat.om.base.auto.AutoPixiHeatingconditionsdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BasePixiHeatingconditionsdata extends AutoPixiHeatingconditionsdata {

	public BasePixiHeatingconditionsdata(ItemI item)
	{
		super(item);
	}

	public BasePixiHeatingconditionsdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BasePixiHeatingconditionsdata(UserI user)
	 **/
	public BasePixiHeatingconditionsdata()
	{}

	public BasePixiHeatingconditionsdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	/**
	 * Copy constructor
	 * @param base The object to copy
	 * @param user The user to copy the object for
	 * @return The copied object
	 * @throws Exception If there was an error copying the object
	 */
	public static PixiHeatingconditionsdataI copy(PixiHeatingconditionsdataI base, UserI user) {
		PixiHeatingconditionsdata copy = new PixiHeatingconditionsdata(user);
		copy.setProcedurephase(base.getProcedurephase());
		copy.setHeatingmethod(base.getHeatingmethod());
		copy.setFeedbacktemperatureregulation(base.getFeedbacktemperatureregulation());
		copy.setTemperaturesensordevicecomponent(base.getTemperaturesensordevicecomponent());
		copy.setSetpointtemperature(base.getSetpointtemperature());
		return copy;
	}

}
