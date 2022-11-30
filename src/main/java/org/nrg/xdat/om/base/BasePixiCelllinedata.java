package org.nrg.xdat.om.base;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.base.auto.AutoPixiCelllinedata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Hashtable;

/**
 * Override of generated implementation of this class to count Cell Line experiments by subject.
 */
public abstract class BasePixiCelllinedata extends AutoPixiCelllinedata {

	public BasePixiCelllinedata(ItemI item)
	{
		super(item);
	}

	public BasePixiCelllinedata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BasePixiCelllinedata(UserI user)
	 **/
	public BasePixiCelllinedata()
	{}

	public BasePixiCelllinedata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	public static Integer getCountForSubject(final String subject_id) {
		final NamedParameterJdbcTemplate template = XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class);
		return template.queryForObject(QUERY_COUNT_EXPERIMENT_BY_SUBJECT_ID, new MapSqlParameterSource(SUBJECT_ID_PARAMETER, subject_id), Integer.class);
	}

	private static final String SUBJECT_ID_PARAMETER = "subject_id";
	private static final String QUERY_COUNT_EXPERIMENT_BY_SUBJECT_ID = "SELECT COUNT(*) " +
			"FROM pixi_celllinedata as e INNER JOIN (" +
			"SELECT id, subject_id " +
			"FROM xnat_subjectassessordata " +
			"WHERE subject_id = :subject_id" +
			") as s " +
			"ON e.id = s.id";
}
