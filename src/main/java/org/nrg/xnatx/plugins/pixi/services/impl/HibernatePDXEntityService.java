package org.nrg.xnatx.plugins.pixi.services.impl;

import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.repositories.PDXEntityDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("PDXService")
public class HibernatePDXEntityService extends HibernateXenograftEnityService<PDXEntity, PDXEntityDAO, PDX> {

    @Autowired
    public HibernatePDXEntityService() {
        super();
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
