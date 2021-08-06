package org.nrg.xnatx.plugins.pixi.services.impl;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.entities.AnimalModelEntity;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.models.AnimalModel;
import org.nrg.xnatx.plugins.pixi.repositories.AnimalModelDAO;
import org.nrg.xnatx.plugins.pixi.services.AnimalModelService;
import org.nrg.xnatx.plugins.pixi.services.PDXEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HibernateAnimalModelService extends AbstractHibernateEntityService<AnimalModelEntity, AnimalModelDAO> implements AnimalModelService {

    private final PDXEntityService pdxEntityService;

    @Autowired
    public HibernateAnimalModelService(final PDXEntityService pdxEntityService) {
        super();
        this.pdxEntityService = pdxEntityService;
    }

    @Override
    @Transactional
    public boolean animalModelExists(final String animalModelID) {
        return getDao().animalModelExists(animalModelID);
    }

    @Override
    @Transactional
    public Optional<AnimalModel> getAnimalModel(final String animalModelID) {
        return getDao().findByAnimalModelID(animalModelID).map(this::toDTO);
    }

    @Override
    @Transactional
    public List<AnimalModel> getAllAnimalModels() {
        return getAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createAnimalModel(AnimalModel animalModel) throws ResourceAlreadyExistsException {
        if (this.animalModelExists(animalModel.getId())) {
            throw new ResourceAlreadyExistsException(AnimalModelEntity.class.getSimpleName(), animalModel.getId());
        }
        super.create(toEntity(animalModel));
    }

    @Override
    @Transactional
    public void updateAnimalModel(AnimalModel animalModel) throws NotFoundException {
        AnimalModelEntity ame = getDao().findByAnimalModelID(animalModel.getId())
                                        .orElseThrow(() -> new NotFoundException(AnimalModel.class.getSimpleName(), animalModel.getId()));
        updateEntity(ame, animalModel);
        update(ame);
    }

    @Override
    @Transactional
    public void deleteAnimalModel(String animalModelID) {
        getDao().findByAnimalModelID(animalModelID).ifPresent(this::delete);
    }

    private AnimalModel toDTO(final AnimalModelEntity animalModelEntity) {
        return AnimalModel.builder().id(animalModelEntity.getAnimalModelID())
                .name(animalModelEntity.getAnimalModelName())
                .passage(animalModelEntity.getPassage())
                .isImmuneSystemHumanized(animalModelEntity.getIsImmuneSystemHumanized())
                .humanizationType(animalModelEntity.getHumanizationType())
                .pdxIDs(animalModelEntity.getPdxs().stream().map(PDXEntity::getPdxID).collect(Collectors.toList()))
                .build();
    }

    private AnimalModelEntity toEntity(final AnimalModel animalModel) {
        AnimalModelEntity animalModelEntity = new AnimalModelEntity();
        animalModelEntity.setCreatedBy(getSessionUser());
        updateEntity(animalModelEntity, animalModel);
        return animalModelEntity;
    }

    private void updateEntity(final AnimalModelEntity animalModelEntity, final AnimalModel animalModel) {
        animalModelEntity.setAnimalModelID(animalModel.getId());
        animalModelEntity.setAnimalModelName(animalModel.getName());
        animalModelEntity.setPassage(animalModel.getPassage());
        animalModelEntity.setIsImmuneSystemHumanized(animalModel.getIsImmuneSystemHumanized());
        animalModelEntity.setHumanizationType(animalModel.getHumanizationType());

        if (animalModel.getPdxIDs() != null) {
            List<PDXEntity> pdxs = animalModel.getPdxIDs().stream()
                    .map(pdxEntityService::getPDXEntity)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            animalModelEntity.setPdxs(pdxs);
        } else {
            animalModelEntity.setPdxs(Collections.emptyList());
        }
    }

    private String getSessionUser() {
        Optional<UserI> user = Optional.ofNullable(Users.getUserPrincipal(SecurityContextHolder.getContext().getAuthentication().getPrincipal()));

        if (user.isPresent()) {
            return user.get().getUsername();
        } else {
            return "";
        }
    }
}
