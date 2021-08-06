package org.nrg.xnatx.plugins.pixi.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.entities.AnimalModelEntity;
import org.nrg.xnatx.plugins.pixi.models.AnimalModel;

import java.util.List;
import java.util.Optional;

public interface AnimalModelService extends BaseHibernateService<AnimalModelEntity> {
    boolean animalModelExists(final String animalModelID);
    Optional<AnimalModel> getAnimalModel(final String animalModelID);
    List<AnimalModel> getAllAnimalModels();
    void createAnimalModel(final AnimalModel animalModel) throws ResourceAlreadyExistsException;
    void updateAnimalModel(final AnimalModel animalModel) throws NotFoundException;
    void deleteAnimalModel(final String animalModelID);
}
