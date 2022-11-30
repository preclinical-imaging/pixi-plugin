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
    private static final String SOURCE_ID_PARAMETER = "source_id";
    private static final String QUERY_HAS_SUBJECT_REFERENCES = "SELECT EXISTS(SELECT sourceid FROM pixi_pdxdata WHERE sourceid = :source_id)";

    @Autowired
    public HibernatePDXEntityService(NamedParameterJdbcTemplate template) {
        super(PDX.class);
        this.template = template;
    }

    @Override
    public boolean hasSubjectReferences(final String sourceId) {
        return template.queryForObject(QUERY_HAS_SUBJECT_REFERENCES, new MapSqlParameterSource(SOURCE_ID_PARAMETER, sourceId), Boolean.class);
    }

    @Override
    protected PDX toDTO(PDXEntity pdxEntity) {
        return PDX.builder()
                .sourceId(pdxEntity.getSourceId())
                .source(pdxEntity.getSource())
                .sourceURL(pdxEntity.getSourceURL())
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
        pdxEntity.setSourceId(pdx.getSourceId());
        pdxEntity.setSource(pdx.getSource());
        pdxEntity.setSourceURL(pdx.getSourceURL());
        pdxEntity.setCreatedBy(pdx.getCreatedBy());
    }
}
