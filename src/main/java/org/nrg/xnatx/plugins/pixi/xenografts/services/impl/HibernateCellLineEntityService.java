package org.nrg.xnatx.plugins.pixi.xenografts.services.impl;

import org.nrg.xnatx.plugins.pixi.xenografts.entities.CellLineEntity;
import org.nrg.xnatx.plugins.pixi.xenografts.models.CellLine;
import org.nrg.xnatx.plugins.pixi.xenografts.repositories.CellLineEntityDAO;
import org.nrg.xnatx.plugins.pixi.xenografts.services.XenograftModelImporterHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service("CellLineService")
public class HibernateCellLineEntityService extends HibernateXenograftEnityService<CellLineEntity, CellLineEntityDAO, CellLine>{

    private final NamedParameterJdbcTemplate template;
    private static final String SOURCE_ID_PARAMETER = "source_id";
    private static final String QUERY_HAS_SUBJECT_REFERENCES = "SELECT EXISTS(SELECT sourceid FROM pixi_celllinedata WHERE sourceid = :source_id)";

    @Autowired
    public HibernateCellLineEntityService(NamedParameterJdbcTemplate template, final XenograftModelImporterHandlerService importerHandlerService) {
        super(CellLine.class, importerHandlerService);
        this.template = template;
    }

    @Override
    public boolean hasSubjectReferences(final String sourceId) {
        return template.queryForObject(QUERY_HAS_SUBJECT_REFERENCES, new MapSqlParameterSource(SOURCE_ID_PARAMETER, sourceId), Boolean.class);
    }

    @Override
    protected CellLine toDTO(CellLineEntity cellLineEntity) {
        return CellLine.builder()
                .sourceId(cellLineEntity.getSourceId())
                .source(cellLineEntity.getSource())
                .sourceURL(cellLineEntity.getSourceURL())
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
        cellLineEntity.setSourceId(cellLine.getSourceId());
        cellLineEntity.setSource(cellLine.getSource());
        cellLineEntity.setSourceURL(cellLine.getSourceURL());
        cellLineEntity.setCreatedBy(cellLine.getCreatedBy());
    }
}
