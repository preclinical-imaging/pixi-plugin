package org.nrg.xnatx.plugins.pixi.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.entities.AnimalModelEntity;

import java.util.Optional;

public interface AnimalModelEntityService extends BaseHibernateService<AnimalModelEntity> {
    boolean animalModelExists(final String animalModelID);
    Optional<AnimalModelEntity> getAnimalModelEntity(final String animalModelID);
    AnimalModelEntity createAnimalModelEntity(final AnimalModelEntity animalModelEntity) throws ResourceAlreadyExistsException;
}
