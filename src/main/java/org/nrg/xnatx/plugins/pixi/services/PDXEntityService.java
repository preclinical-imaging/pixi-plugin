package org.nrg.xnatx.plugins.pixi.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;

import java.util.Optional;

public interface PDXEntityService extends BaseHibernateService<PDXEntity> {
    public Optional<PDXEntity> getPDXEntity(final String pdxID);
    public boolean pdxEntityExists(final String pdxID);
}
