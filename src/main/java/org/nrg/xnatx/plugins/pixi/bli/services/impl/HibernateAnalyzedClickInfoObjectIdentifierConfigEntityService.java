package org.nrg.xnatx.plugins.pixi.bli.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.pixi.bli.entities.AnalyzedClickInfoObjectIdentifierConfigEntity;
import org.nrg.xnatx.plugins.pixi.bli.repositories.AnalyzedClickInfoObjectIdentifierConfigDao;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierConfigEntityService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Slf4j
public class HibernateAnalyzedClickInfoObjectIdentifierConfigEntityService extends AbstractHibernateEntityService<AnalyzedClickInfoObjectIdentifierConfigEntity, AnalyzedClickInfoObjectIdentifierConfigDao> implements AnalyzedClickInfoObjectIdentifierConfigEntityService {

    @Override
    @Transactional
    public Optional<AnalyzedClickInfoObjectIdentifierConfigEntity> findConfig(String name) {
        return Optional.ofNullable(
                getDao().findByUniqueProperty("name", name)
        );
    }

}
