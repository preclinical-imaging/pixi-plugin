package org.nrg.xnatx.plugins.pixi.bli.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.pixi.bli.entities.AnalyzedClickInfoObjectIdentifierConfigEntity;

import java.util.Optional;

public interface AnalyzedClickInfoObjectIdentifierConfigEntityService extends BaseHibernateService<AnalyzedClickInfoObjectIdentifierConfigEntity> {

    Optional<AnalyzedClickInfoObjectIdentifierConfigEntity> findConfig(String name);

}
