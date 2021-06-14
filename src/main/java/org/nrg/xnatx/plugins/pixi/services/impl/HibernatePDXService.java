package org.nrg.xnatx.plugins.pixi.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.pixi.entities.PDX;
import org.nrg.xnatx.plugins.pixi.repositories.PDXRepository;
import org.nrg.xnatx.plugins.pixi.services.PDXService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class HibernatePDXService extends AbstractHibernateEntityService<PDX, PDXRepository> implements PDXService {

    @Transactional
    @Override
    public Optional<PDX> findByPdxID(final String pdxID) {
        return Optional.ofNullable(getDao().findByUniqueProperty("pdxID", pdxID));
    }

}
