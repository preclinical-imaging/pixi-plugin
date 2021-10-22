package org.nrg.xnatx.plugins.pixi.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nullable;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = "externalID"))
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
@Slf4j
public abstract class XenograftEntity extends AbstractHibernateEntity {

    @NotBlank private String externalID;
    @Nullable private String dataSource;
    @Nullable private String dataSourceURL;
    @Nullable private String createdBy;

}
