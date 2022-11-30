package org.nrg.xnatx.plugins.pixi.xenografts.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.pixi.xenografts.entities.XenograftEntity;
import org.nrg.xnatx.plugins.pixi.xenografts.models.Xenograft;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.xenografts.exceptions.XenograftDeletionException;

import java.util.List;
import java.util.Optional;

public interface XenograftService<E extends XenograftEntity, X extends Xenograft> extends BaseHibernateService<E> {
    boolean xenograftExists(final String sourceId);
    Optional<X> getXenograft(final String sourceId);
    List<X> getAllXenografts();
    void createXenograft(X x) throws ResourceAlreadyExistsException;
    void updateXenograft(final String sourceId, X x) throws ResourceAlreadyExistsException, NotFoundException;
    void deleteXenograft(String sourceId) throws XenograftDeletionException;
    boolean hasSubjectReferences(String sourceId);
}
