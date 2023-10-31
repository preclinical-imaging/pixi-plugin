package org.nrg.xnatx.plugins.pixi.bli.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Slf4j
public class AnalyzedClickInfoObjectIdentifierConfigEntity extends AbstractHibernateEntity {

    private String name;
    private AnalyzedClickInfoObjectIdentifierConfig config;

    @Column(columnDefinition = "TEXT")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Type(type = "jsonb")
    @Column(
            name = "config",
            columnDefinition = "jsonb"
    )
    public AnalyzedClickInfoObjectIdentifierConfig getConfig() {
        return config;
    }

    public void setConfig(AnalyzedClickInfoObjectIdentifierConfig aciObjIdConfig) {
        this.config = aciObjIdConfig;
    }

    public void update(final AnalyzedClickInfoObjectIdentifierConfig pojo) {
        setName(pojo.getName());
        setConfig(pojo);
    }

    public static AnalyzedClickInfoObjectIdentifierConfigEntity fromPojo(AnalyzedClickInfoObjectIdentifierConfig pojo) {
        final AnalyzedClickInfoObjectIdentifierConfigEntity entity = new AnalyzedClickInfoObjectIdentifierConfigEntity();
        entity.update(pojo);
        return entity;
    }

}
