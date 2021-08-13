package org.nrg.xnatx.plugins.pixi.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.models.PDX;

import java.util.List;
import java.util.Optional;

public interface PDXService extends BaseHibernateService<PDXEntity> {
    boolean pdxExists(final String pdxID);
    Optional<PDX> getPDX(final String pdxID);
    Optional<PDXEntity> getPDXEntity(final String pdxID);
    List<PDX> getAllPDX();
    void createPDX(PDX pdx) throws ResourceAlreadyExistsException;
    void updatePDX(PDX pdx) throws NotFoundException;
    void deletePDX(String pdxID);
}
