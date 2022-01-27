package org.nrg.xnatx.plugins.pixi.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
@Slf4j
public abstract class XenograftEntity extends AbstractHibernateEntity {

    @Getter(AccessLevel.NONE) private String externalID;
    @Nullable private String dataSource;
    @Nullable @URL private String dataSourceURL;
    @Nullable private String createdBy;

    @Column(unique = true)
    @NotNull @NotBlank
    public String getExternalID() {
        return externalID;
    }
}
