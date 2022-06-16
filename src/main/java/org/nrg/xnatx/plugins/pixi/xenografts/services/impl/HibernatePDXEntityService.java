package org.nrg.xnatx.plugins.pixi.xenografts.services.impl;

import org.nrg.xnatx.plugins.pixi.xenografts.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.xenografts.models.PDX;
import org.nrg.xnatx.plugins.pixi.xenografts.repositories.PDXEntityDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service("PDXService")
public class HibernatePDXEntityService extends HibernateXenograftEnityService<PDXEntity, PDXEntityDAO, PDX> {

    private final NamedParameterJdbcTemplate template;
    private static final String EXTERNAL_ID_PARAMETER = "external_id";
    private static final String QUERY_HAS_SUBJECT_REFERENCES = "SELECT EXISTS(SELECT externalid FROM pixi_pdx WHERE externalid = :external_id)";

    @Autowired
    public HibernatePDXEntityService(NamedParameterJdbcTemplate template) {
        super(PDX.class);
        this.template = template;
    }

    @Override
    public boolean hasSubjectReferences(final String externalID) {
        return template.queryForObject(QUERY_HAS_SUBJECT_REFERENCES, new MapSqlParameterSource(EXTERNAL_ID_PARAMETER, externalID), Boolean.class);
    }

    @Override
    protected PDX toDTO(PDXEntity pdxEntity) {
        return PDX.builder()
                .externalID(pdxEntity.getExternalID())
                .dataSource(pdxEntity.getDataSource())
                .dataSourceURL(pdxEntity.getDataSourceURL())
                .createdBy(pdxEntity.getCreatedBy())
                .build();
    }

    @Override
    protected PDXEntity toEntity(PDX pdx) {
        PDXEntity pdxEntity = new PDXEntity();
        updateEntity(pdxEntity, pdx);
        return pdxEntity;
    }

    @Override
    protected void updateEntity(PDXEntity pdxEntity, PDX pdx) {
        pdxEntity.setExternalID(pdx.getExternalID());
        pdxEntity.setDataSource(pdx.getDataSource());
        pdxEntity.setDataSourceURL(pdx.getDataSourceURL());
        pdxEntity.setCreatedBy(pdx.getCreatedBy());
    }
}
