package org.nrg.xnatx.plugins.pixi.services.impl;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.repositories.PDXEntityDAO;
import org.nrg.xnatx.plugins.pixi.services.PDXEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class HibernatePDXEntityService extends AbstractHibernateEntityService<PDXEntity, PDXEntityDAO> implements PDXEntityService {

    @Override
    @Transactional
    public Optional<PDXEntity> getPDXEntity(final String pdxID) {
        return getDao().findByPdxId(pdxID);
    }

    @Override
    @Transactional
    public boolean pdxEntityExists(final String pdxID) {
        return getDao().pdxEntityExists(pdxID);
    }
}
