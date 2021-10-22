package org.nrg.xnatx.plugins.pixi.repositories;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.pixi.entities.XenograftEntity;

import java.util.Optional;


public abstract class XenograftEntityDAO<E extends XenograftEntity> extends AbstractHibernateDAO<E> {

    public Optional<E> findByExternalID(final String externalID) {
        return Optional.ofNullable(this.findByUniqueProperty("externalID", externalID));
    }

    public boolean xenograftExists(final String externalID) {
        return exists("externalID", externalID);
    }

}
