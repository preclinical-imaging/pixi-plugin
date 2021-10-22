package org.nrg.xnatx.plugins.pixi.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.pixi.entities.XenograftEntity;
import org.nrg.xnatx.plugins.pixi.models.Xenograft;
import org.nrg.xnatx.plugins.pixi.repositories.XenograftEntityDAO;
import org.nrg.xnatx.plugins.pixi.services.XenograftService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class HibernateXenograftEnityService<E extends XenograftEntity, T extends XenograftEntityDAO<E>, X extends Xenograft> extends AbstractHibernateEntityService<E, T> implements XenograftService<E,X> {

    public HibernateXenograftEnityService() {
        super();
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
    public void createXenograft(X x) {
        super.create(toEntity(x));
    }

    @Override
    @Transactional
    public void updateXenograft(X x) throws NotFoundException {
        E e = getDao().findByExternalID(x.getExternalID())
                      .orElseThrow(() -> new NotFoundException("Entity with ID " + x.getExternalID() + " not found. Cannot update."));
        updateEntity(e, x);
        super.update(e);
    }

    @Override
    @Transactional
    public void deleteXenograft(String externalID) {
        super.getDao().findByExternalID(externalID).ifPresent(super::delete);
    }

    protected abstract X toDTO(final E e);
    protected abstract E toEntity(final X x);
    protected abstract void updateEntity(final E e, final X x);
}
