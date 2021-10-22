package org.nrg.xnatx.plugins.pixi.services.impl;

import org.nrg.xnatx.plugins.pixi.entities.CellLineEntity;
import org.nrg.xnatx.plugins.pixi.models.CellLine;
import org.nrg.xnatx.plugins.pixi.repositories.CellLineEntityDAO;
import org.springframework.stereotype.Service;

@Service("CellLineService")
public class HibernateCellLineEntityService extends HibernateXenograftEnityService<CellLineEntity, CellLineEntityDAO, CellLine>{
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
