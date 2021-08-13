package org.nrg.xnatx.plugins.pixi.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nullable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "pdxID"))
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class PDXEntity extends AbstractHibernateEntity {

    @NotBlank private String pdxID;
    @Nullable private String pdxLabel;
    @Nullable private String notes;
    @Nullable private String createdBy;
    @Embedded @Nullable private PatientEntity patientEntity;
    @Embedded @Nullable private TumorEntity tumorEntity;

}
