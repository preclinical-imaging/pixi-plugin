package org.nrg.xdat.om.base;

import org.nrg.xdat.model.PixiFastingdataI;
import org.nrg.xdat.model.PixiImageacquisitioncontextdataI;
import org.nrg.xdat.om.PixiAnesthesiadata;
import org.nrg.xdat.om.PixiFastingdata;
import org.nrg.xdat.om.PixiHeatingconditionsdata;
import org.nrg.xdat.om.PixiImageacquisitioncontextdata;
import org.nrg.xdat.om.base.auto.AutoPixiImageacquisitioncontextdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BasePixiImageacquisitioncontextdata extends AutoPixiImageacquisitioncontextdata {

	public BasePixiImageacquisitioncontextdata(ItemI item)
	{
		super(item);
	}

	public BasePixiImageacquisitioncontextdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BasePixiImageacquisitioncontextdata(UserI user)
	 **/
	public BasePixiImageacquisitioncontextdata()
	{}

	public BasePixiImageacquisitioncontextdata(Hashtable properties, UserI user)
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
	public static PixiImageacquisitioncontextdataI copy(PixiImageacquisitioncontextdataI base, UserI user) throws Exception {
		// Create the new copy
		final PixiImageacquisitioncontextdataI copy = new PixiImageacquisitioncontextdata(user);

		// Copy fasting data
		try {
			final PixiFastingdataI fasting = PixiFastingdata.copy(base.getFasting(), user);

			if (fasting != null) {
				copy.setFasting(fasting);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// Copy anesthesia data
		base.getAnesthesias_anesthesia().stream().map(anesthesia -> {
			try {
				return PixiAnesthesiadata.copy(anesthesia, user);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).forEach(anesthesia -> {
			try {
				if (anesthesia != null) {
					copy.addAnesthesias_anesthesia(anesthesia);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		// Copy heating conditions data
		base.getHeatingconditions_heatingconditions().stream().map(heatingCondition -> {
			try {
				return PixiHeatingconditionsdata.copy(heatingCondition, user);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).forEach(heatingCondition -> {
			try {
				if (heatingCondition != null) {
					copy.addHeatingconditions_heatingconditions(heatingCondition);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		return copy;
	}
}
