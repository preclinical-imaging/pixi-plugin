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
		if (base == null) {
			return null;
		}

		PixiHeatingconditionsdata copy = new PixiHeatingconditionsdata(user);

		if (base.getProcedurephase() != null) {
			copy.setProcedurephase(base.getProcedurephase());
		}

		if (base.getHeatingmethod() != null) {
			copy.setHeatingmethod(base.getHeatingmethod());
		}

		if (base.getFeedbacktemperatureregulation() != null) {
			copy.setFeedbacktemperatureregulation(base.getFeedbacktemperatureregulation());
		}

		if (base.getTemperaturesensordevicecomponent() != null) {
			copy.setTemperaturesensordevicecomponent(base.getTemperaturesensordevicecomponent());
		}

		if (base.getSetpointtemperature() != null) {
			copy.setSetpointtemperature(base.getSetpointtemperature());
		}

		return copy;
	}

}
