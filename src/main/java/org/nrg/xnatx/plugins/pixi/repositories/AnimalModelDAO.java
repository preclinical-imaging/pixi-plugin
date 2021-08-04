package org.nrg.xnatx.plugins.pixi.repositories;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.pixi.entities.AnimalModelEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AnimalModelDAO extends AbstractHibernateDAO<AnimalModelEntity> {
    public Optional<AnimalModelEntity> findByAnimalModelID(final String animalModelID) {
        return Optional.ofNullable(this.findByUniqueProperty("animalModelID", animalModelID));
    }

    public boolean animalModelExists(final String animalModelID) {
        return exists("animalModelID", animalModelID);
    }
}
