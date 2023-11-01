package org.nrg.xnatx.plugins.pixi.bli.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.pixi.bli.entities.AnalyzedClickInfoObjectIdentifierMappingEntity;

import java.util.Optional;

public interface AnalyzedClickInfoObjectIdentifierMappingEntityService extends BaseHibernateService<AnalyzedClickInfoObjectIdentifierMappingEntity> {

    Optional<AnalyzedClickInfoObjectIdentifierMappingEntity> findMapping(String name);

}
