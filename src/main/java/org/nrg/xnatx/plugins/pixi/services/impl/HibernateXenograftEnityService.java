package org.nrg.xnatx.plugins.pixi.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.entities.XenograftEntity;
import org.nrg.xnatx.plugins.pixi.exceptions.XenograftDeletionException;
import org.nrg.xnatx.plugins.pixi.models.Xenograft;
import org.nrg.xnatx.plugins.pixi.repositories.XenograftEntityDAO;
import org.nrg.xnatx.plugins.pixi.services.XenograftService;
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
    public boolean xenograftExists(final String externalID) {
        return super.getDao().xenograftExists(externalID);
    }

    @Override
    @Transactional
    public Optional<X> getXenograft(final String externalID) {
        return super.getDao().findByExternalID(externalID).map(this::toDTO);
    }

    @Override
    @Transactional
    public List<X> getAllXenografts() {
        return super.getAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createXenograft(X x) throws ResourceAlreadyExistsException {
        // If a xenograft with the same external id already exists, cannot create
        if (getDao().findByExternalID(x.getExternalID()).isPresent()) {
            throw new ResourceAlreadyExistsException(type.getSimpleName(), x.getExternalID());
        }

        super.create(toEntity(x));
    }

    @Override
    @Transactional
    public void updateXenograft(final String externalID, X x) throws ResourceAlreadyExistsException, NotFoundException {
        // If changing the xenograft id (not the hibernate entity id)
        if (!externalID.equals(x.getExternalID())) {
            // Check to see if the new id is already in use
            if (xenograftExists(x.getExternalID())) {
                // If it is, not allowed to change ids as they should be unique
                throw new ResourceAlreadyExistsException(type.getSimpleName(), x.getExternalID());
            }
        }

        E e = getDao().findByExternalID(externalID)
                      .orElseThrow(() -> new NotFoundException("Entity with ID " + externalID + " not found. Cannot update."));
        updateEntity(e, x);
        super.update(e);
    }

    @Override
    @Transactional
    public void deleteXenograft(String externalID) throws XenograftDeletionException {
        // Don't delete a xenograft if its referenced by a subject
        if (hasSubjectReferences(externalID)) {
            throw new XenograftDeletionException();
        }

        super.getDao().findByExternalID(externalID).ifPresent(super::delete);
    }

    protected abstract X toDTO(final E e);
    protected abstract E toEntity(final X x);
    protected abstract void updateEntity(final E e, final X x);
}
