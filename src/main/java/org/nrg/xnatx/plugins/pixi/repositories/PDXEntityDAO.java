package org.nrg.xnatx.plugins.pixi.repositories;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PDXEntityDAO extends AbstractHibernateDAO<PDXEntity> {

    public Optional<PDXEntity> findByPdxId(final String pdxID) {
        return Optional.ofNullable(this.findByUniqueProperty("pdxID", pdxID));
    }

    public boolean pdxEntityExists(final String pdxID) {
        return exists("pdxID", pdxID);
    }
}
