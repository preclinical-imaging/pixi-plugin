package org.nrg.xnatx.plugins.pixi.xenografts.repositories;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.pixi.xenografts.entities.XenograftEntity;

import java.util.Optional;


public abstract class XenograftEntityDAO<E extends XenograftEntity> extends AbstractHibernateDAO<E> {

    public Optional<E> findBySourceId(final String sourceId) {
        return Optional.ofNullable(this.findByUniqueProperty("sourceId", sourceId));
    }

    public boolean xenograftExists(final String sourceId) {
        return exists("sourceId", sourceId);
    }

}
