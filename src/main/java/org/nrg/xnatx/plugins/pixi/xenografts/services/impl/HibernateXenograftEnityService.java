package org.nrg.xnatx.plugins.pixi.xenografts.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.pixi.xenografts.entities.XenograftEntity;
import org.nrg.xnatx.plugins.pixi.xenografts.models.Xenograft;
import org.nrg.xnatx.plugins.pixi.xenografts.repositories.XenograftEntityDAO;
import org.nrg.xnatx.plugins.pixi.xenografts.services.XenograftService;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.xenografts.exceptions.XenograftDeletionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class HibernateXenograftEnityService<E extends XenograftEntity, T extends XenograftEntityDAO<E>, X extends Xenograft> extends AbstractHibernateEntityService<E, T> implements XenograftService<E,X> {

    private final Class<X> type;

    @Autowired
    public HibernateXenograftEnityService(final Class<X> type) {
        super();
        this.type = type;
    }

    @Override
    @Transactional
    public boolean xenograftExists(final String sourceId) {
        return super.getDao().xenograftExists(sourceId);
    }

    @Override
    @Transactional
    public Optional<X> getXenograft(final String sourceId) {
        return super.getDao().findBySourceId(sourceId).map(this::toDTO);
    }

    @Override
    @Transactional
    public List<X> getAllXenografts() {
        return super.getAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createXenograft(X x) throws ResourceAlreadyExistsException {
        // If a xenograft with the same source id already exists, cannot create
        if (getDao().findBySourceId(x.getSourceId()).isPresent()) {
            throw new ResourceAlreadyExistsException(type.getSimpleName(), x.getSourceId());
        }

        super.create(toEntity(x));
    }

    @Override
    @Transactional
    public void updateXenograft(final String sourceId, X x) throws ResourceAlreadyExistsException, NotFoundException {
        // If changing the xenograft id (not the hibernate entity id)
        if (!sourceId.equals(x.getSourceId())) {
            // Check to see if the new id is already in use
            if (xenograftExists(x.getSourceId())) {
                // If it is, not allowed to change ids as they should be unique
                throw new ResourceAlreadyExistsException(type.getSimpleName(), x.getSourceId());
            }
        }

        E e = getDao().findBySourceId(sourceId)
                      .orElseThrow(() -> new NotFoundException("Entity with ID " + sourceId + " not found. Cannot update."));
        updateEntity(e, x);
        super.update(e);
    }

    @Override
    @Transactional
    public void deleteXenograft(String sourceId) throws XenograftDeletionException {
        // Don't delete a xenograft if it's referenced by a subject
        if (hasSubjectReferences(sourceId)) {
            throw new XenograftDeletionException();
        }

        super.getDao().findBySourceId(sourceId).ifPresent(super::delete);
    }

    protected abstract X toDTO(final E e);
    protected abstract E toEntity(final X x);
    protected abstract void updateEntity(final E e, final X x);
}
