package org.nrg.xnatx.plugins.pixi.services.impl;

import org.nrg.xnatx.plugins.pixi.entities.CellLineEntity;
import org.nrg.xnatx.plugins.pixi.models.CellLine;
import org.nrg.xnatx.plugins.pixi.repositories.CellLineEntityDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service("CellLineService")
public class HibernateCellLineEntityService extends HibernateXenograftEnityService<CellLineEntity, CellLineEntityDAO, CellLine>{

    private final NamedParameterJdbcTemplate template;
    private static final String EXTERNAL_ID_PARAMETER = "external_id";
    private static final String QUERY_HAS_SUBJECT_REFERENCES = "SELECT EXISTS(SELECT externalid FROM pixi_cellline WHERE externalid = :external_id)";

    @Autowired
    public HibernateCellLineEntityService(NamedParameterJdbcTemplate template) {
        super(CellLine.class);
        this.template = template;
    }

    @Override
    public boolean hasSubjectReferences(final String externalID) {
        return template.queryForObject(QUERY_HAS_SUBJECT_REFERENCES, new MapSqlParameterSource(EXTERNAL_ID_PARAMETER, externalID), Boolean.class);
    }

    @Override
    protected CellLine toDTO(CellLineEntity cellLineEntity) {
        return CellLine.builder()
                .externalID(cellLineEntity.getExternalID())
                .dataSource(cellLineEntity.getDataSource())
                .dataSourceURL(cellLineEntity.getDataSourceURL())
                .createdBy(cellLineEntity.getCreatedBy())
                .build();
    }

    @Override
    protected CellLineEntity toEntity(CellLine cellLine) {
        CellLineEntity cellLineEntity = new CellLineEntity();
        updateEntity(cellLineEntity, cellLine);
        return cellLineEntity;
    }

    @Override
    protected void updateEntity(CellLineEntity cellLineEntity, CellLine cellLine) {
        cellLineEntity.setExternalID(cellLine.getExternalID());
        cellLineEntity.setDataSource(cellLine.getDataSource());
        cellLineEntity.setDataSourceURL(cellLine.getDataSourceURL());
        cellLineEntity.setCreatedBy(cellLine.getCreatedBy());
    }
}
