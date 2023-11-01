package org.nrg.xnatx.plugins.pixi.bli.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.pixi.bli.entities.AnalyzedClickInfoObjectIdentifierMappingEntity;
import org.nrg.xnatx.plugins.pixi.bli.repositories.AnalyzedClickInfoObjectIdentifierConfigDao;
import org.nrg.xnatx.plugins.pixi.bli.services.AnalyzedClickInfoObjectIdentifierMappingEntityService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Slf4j
public class HibernateAnalyzedClickInfoObjectIdentifierMappingEntityService extends AbstractHibernateEntityService<AnalyzedClickInfoObjectIdentifierMappingEntity, AnalyzedClickInfoObjectIdentifierConfigDao> implements AnalyzedClickInfoObjectIdentifierMappingEntityService {

    @Override
    @Transactional
    public Optional<AnalyzedClickInfoObjectIdentifierMappingEntity> findMapping(String name) {
        return Optional.ofNullable(
                getDao().findByUniqueProperty("name", name)
        );
    }

}
