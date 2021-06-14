package org.nrg.xnatx.plugins.pixi.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.pixi.entities.PDX;

import java.util.Optional;

public interface PDXService extends BaseHibernateService<PDX> {
    Optional<PDX> findByPdxID(final String pdxID);
}
