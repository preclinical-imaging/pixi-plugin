package org.nrg.xdat.om.base;

import org.nrg.xdat.model.PixiAnesthesiadataI;
import org.nrg.xdat.om.PixiAnesthesiadata;
import org.nrg.xdat.om.base.auto.AutoPixiAnesthesiadata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BasePixiAnesthesiadata extends AutoPixiAnesthesiadata {

	public BasePixiAnesthesiadata(ItemI item)
	{
		super(item);
	}

	public BasePixiAnesthesiadata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BasePixiAnesthesiadata(UserI user)
	 **/
	public BasePixiAnesthesiadata()
	{}

	public BasePixiAnesthesiadata(Hashtable properties, UserI user)
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
	public static PixiAnesthesiadataI copy(PixiAnesthesiadataI base, UserI user) {
		PixiAnesthesiadata copy = new PixiAnesthesiadata(user);
		copy.setAnesthesia(base.getAnesthesia());
		copy.setRouteofadministration(base.getRouteofadministration());
		return copy;
	}

}
