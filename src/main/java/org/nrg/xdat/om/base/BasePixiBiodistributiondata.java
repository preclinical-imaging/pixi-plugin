package org.nrg.xdat.om.base;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.base.auto.AutoPixiBiodistributiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Hashtable;


@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class BasePixiBiodistributiondata extends AutoPixiBiodistributiondata {

    public BasePixiBiodistributiondata(ItemI item) {
        super(item);
    }

    public BasePixiBiodistributiondata(UserI user) {
        super(user);
    }

    /*
     * @deprecated Use BasePixiBiodistributiondata(UserI user)
     **/
    public BasePixiBiodistributiondata() {
    }

    public BasePixiBiodistributiondata(Hashtable properties, UserI user) {
        super(properties, user);
    }

    public static Integer getCountForSubject(final String subject_id) {
        final NamedParameterJdbcTemplate template = XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class);
        return template.queryForObject(QUERY_COUNT_EXPERIMENTS_BY_SUBJECT_ID, new MapSqlParameterSource(SUBJECT_ID_PARAMETER, subject_id), Integer.class);
    }

    private static final String SUBJECT_ID_PARAMETER = "subject_id";
    private static final String QUERY_COUNT_EXPERIMENTS_BY_SUBJECT_ID = "SELECT COUNT(*) " +
            "FROM pixi_biodistributiondata as biod INNER JOIN (" +
            "SELECT id, subject_id " +
            "FROM xnat_subjectassessordata " +
            "WHERE subject_id = :subject_id" +
            ") as s " +
            "ON biod.id = s.id";

}
