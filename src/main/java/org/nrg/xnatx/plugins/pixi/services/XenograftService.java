package org.nrg.xnatx.plugins.pixi.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.pixi.entities.XenograftEntity;
import org.nrg.xnatx.plugins.pixi.models.Xenograft;

import java.util.List;
import java.util.Optional;

public interface XenograftService<E extends XenograftEntity, X extends Xenograft> extends BaseHibernateService<E> {
    boolean xenograftExists(final String externalID);
    Optional<X> getXenograft(final String externalID);
    List<X> getAllXenografts();
    void createXenograft(X x);
    void updateXenograft(X x) throws NotFoundException;
    void deleteXenograft(String externalID);
}