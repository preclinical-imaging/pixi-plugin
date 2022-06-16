package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoPixiHotel;
import org.nrg.xft.ItemI;
import org.nrg.xft.identifier.IDGeneratorFactory;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BasePixiHotel extends AutoPixiHotel {

	public BasePixiHotel(ItemI item)
	{
		super(item);
	}

	public BasePixiHotel(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BasePixiHotel(UserI user)
	 **/
	public BasePixiHotel()
	{}

	public BasePixiHotel(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	public static String CreateNewID() throws Exception{
		return IDGeneratorFactory.getInstance().getIDGenerator("pixi_hotel").generateIdentifier();
	}

}
