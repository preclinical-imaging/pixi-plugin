package org.nrg.xnatx.plugins.pixi.bli.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.pixi.bli.models.AnalyzedClickInfoObjectIdentifierMapping;

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
public class AnalyzedClickInfoObjectIdentifierMappingEntity extends AbstractHibernateEntity {

    private String name;
    private AnalyzedClickInfoObjectIdentifierMapping mapping;

    @Column(columnDefinition = "TEXT")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Type(type = "jsonb")
    @Column(
            name = "mapping",
            columnDefinition = "jsonb"
    )
    public AnalyzedClickInfoObjectIdentifierMapping getMapping() {
        return mapping;
    }

    public void setMapping(AnalyzedClickInfoObjectIdentifierMapping mapping) {
        this.mapping = mapping;
    }

    public void update(final AnalyzedClickInfoObjectIdentifierMapping pojo) {
        setName(pojo.getName());
        setMapping(pojo);
    }

    public static AnalyzedClickInfoObjectIdentifierMappingEntity fromPojo(AnalyzedClickInfoObjectIdentifierMapping pojo) {
        final AnalyzedClickInfoObjectIdentifierMappingEntity entity = new AnalyzedClickInfoObjectIdentifierMappingEntity();
        entity.update(pojo);
        return entity;
    }

}
