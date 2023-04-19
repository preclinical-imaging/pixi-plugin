package org.nrg.xdat.om.base;

import org.nrg.xdat.model.PixiFastingdataI;
import org.nrg.xdat.om.PixiFastingdata;
import org.nrg.xdat.om.base.auto.AutoPixiFastingdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BasePixiFastingdata extends AutoPixiFastingdata {

	public BasePixiFastingdata(ItemI item)
	{
		super(item);
	}

	public BasePixiFastingdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BasePixiFastingdata(UserI user)
	 **/
	public BasePixiFastingdata()
	{}

	public BasePixiFastingdata(Hashtable properties, UserI user)
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
	public static PixiFastingdataI copy(PixiFastingdataI base, UserI user) {
		PixiFastingdata copy = new PixiFastingdata(user);
		copy.setFastingstatus(base.getFastingstatus());
		copy.setFastingduration(base.getFastingduration());
		return copy;
	}

}
