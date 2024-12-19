package org.nrg.xnatx.plugins.pixi.xenografts.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
@Slf4j
public abstract class XenograftEntity extends AbstractHibernateEntity {

    @Getter(AccessLevel.NONE) private String sourceId;
    @Nullable private String source;
    @Nullable @URL private String sourceURL;
    @Nullable private String createdBy;

    @Column(unique = true)
    @NotNull @NotBlank
    public String getSourceId() {
        return sourceId;
    }
}
