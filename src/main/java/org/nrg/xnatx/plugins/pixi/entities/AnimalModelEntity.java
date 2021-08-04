package org.nrg.xnatx.plugins.pixi.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "animalModelID"))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class AnimalModelEntity extends AbstractHibernateEntity {
    @NotBlank private String animalModelID;
    @NotBlank private String animalModelName;
    @Getter(AccessLevel.NONE) @Nullable private List<PDXEntity> pdxs;
    @Nullable private String passage;
    @Nullable private Boolean isImmuneSystemHumanized;
    @Nullable private String humanizationType;
    @Nonnull private String createdBy;

    @ManyToMany(fetch = FetchType.EAGER)
    public List<PDXEntity> getPdxs() {
        return pdxs;
    }

    public void associatePDX(PDXEntity pdxEntity) {
        if (pdxs == null) {
            pdxs = new ArrayList<>();
        }
        pdxs.add(pdxEntity);
    }

    public void disassociatePDX(PDXEntity pdxEntity) {
        if (pdxs != null) {
            pdxs.remove(pdxEntity);
        }
    }
}
