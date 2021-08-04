package org.nrg.xnatx.plugins.pixi.services.impl;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.entities.AnimalModelEntity;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.repositories.AnimalModelDAO;
import org.nrg.xnatx.plugins.pixi.services.AnimalModelEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class HibernateAnimalModelEntityService extends AbstractHibernateEntityService<AnimalModelEntity, AnimalModelDAO> implements AnimalModelEntityService {

    @Override
    @Transactional
    public boolean animalModelExists(final String animalModelID) {
        return getDao().animalModelExists(animalModelID);
    }

    @Override
    @Transactional
    public Optional<AnimalModelEntity> getAnimalModelEntity(final String animalModelID) {
        return getDao().findByAnimalModelID(animalModelID);
    }

    @Override
    @Transactional
    public AnimalModelEntity createAnimalModelEntity(AnimalModelEntity animalModelEntity) throws ResourceAlreadyExistsException {
        if (this.animalModelExists(animalModelEntity.getAnimalModelID())) {
            throw new ResourceAlreadyExistsException(AnimalModelEntity.class.getSimpleName(), animalModelEntity.getAnimalModelID());
        }
        return super.create(animalModelEntity);
    }
}
