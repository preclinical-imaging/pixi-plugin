package org.nrg.xnatx.plugins.pixi.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.services.PDXEntityService;
import org.nrg.xnatx.plugins.pixi.services.PDXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PDXServiceImpl implements PDXService {

    private final PDXEntityService pdxEntityService;

    @Autowired
    public PDXServiceImpl(final PDXEntityService pdxEntityService) {
        this.pdxEntityService = pdxEntityService;
    }

    @Override
    public Optional<PDX> getPDX(final String pdxID) {
        return pdxEntityService.getPDXEntity(pdxID).map(PDXEntity::toPojo);
    }

    @Override
    public List<PDX> getAllPDX() {
        return pdxEntityService.getAll().stream().map(PDXEntity::toPojo).collect(Collectors.toList());
    }

    @Override
    public void createPDX(PDX pdx) throws ResourceAlreadyExistsException{
        if (!pdxEntityService.pdxEntityExists(pdx.getId())) {
            pdxEntityService.create(PDXEntity.fromPojo(pdx));
        } else {
            throw new ResourceAlreadyExistsException(PDXEntity.class.getSimpleName(), pdx.getId());
        }
    }

    @Override
    public void updatePDX(PDX pdx) throws NotFoundException {
        Optional<PDXEntity> pdxEntity = pdxEntityService.getPDXEntity(pdx.getId());

        if (!pdxEntity.isPresent()) {
            throw new NotFoundException(PDXEntity.class.getSimpleName(), pdx.getId());
        } else {
            pdxEntityService.update(pdxEntity.get().update(pdx));
        }
    }

    @Override
    public void deletePDX(String pdxID) throws NotFoundException {
        Optional<PDXEntity> pdxEntity = pdxEntityService.getPDXEntity(pdxID);

        if (!pdxEntity.isPresent()) {
            throw new NotFoundException(PDXEntity.class.getSimpleName(), pdxID);
        } else {
            pdxEntityService.delete(pdxEntity.get());
        }
    }
}
