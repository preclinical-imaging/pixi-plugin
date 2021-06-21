package org.nrg.xnatx.plugins.pixi.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.models.Patient;
import org.nrg.xnatx.plugins.pixi.models.Tumor;

import javax.annotation.Nullable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import java.util.Optional;

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

    public static PDXEntity fromPojo(final PDX pdx) {
        final PDXEntity pdxEntity = new PDXEntity();
        pdxEntity.update(pdx);
        return pdxEntity;
    }

    public PDXEntity update(final PDX pdx) {
        this.setPdxID(pdx.getPdxID());
        this.setPdxLabel(pdx.getPdxLabel());
        this.setNotes(pdx.getNotes());
        this.setCreatedBy(pdx.getCreatedBy());

        if (pdx.getPatient() == null) {
            this.setPatientEntity(null);
        } else if (pdx.getPatient() != null && this.getPatientEntity() == null) {
            PatientEntity patientEntity = PatientEntity.fromPojo(pdx.getPatient());
            this.setPatientEntity(patientEntity);
        } else if (pdx.getPatient() != null && this.getPatientEntity() != null) {
            this.getPatientEntity().update(pdx.getPatient());
        }

        if (pdx.getTumor() == null) {
            this.setTumorEntity(null);
        } else if (pdx.getTumor() != null && this.getTumorEntity() == null) {
            TumorEntity tumorEntity = TumorEntity.fromPojo(pdx.getTumor());
            this.setTumorEntity(tumorEntity);
        } else if (pdx.getTumor() != null && this.getTumorEntity() != null) {
            this.getTumorEntity().update(pdx.getTumor());
        }

        return this;
    }

    public PDX toPojo() {
        Optional<PatientEntity> patientEntity = Optional.ofNullable(this.getPatientEntity());
        Patient patient = patientEntity.map(PatientEntity::toPojo).orElse(null);

        Optional<TumorEntity> tumorEntity = Optional.ofNullable(this.getTumorEntity());
        Tumor tumor = tumorEntity.map(TumorEntity::toPojo).orElse(null);

        return PDX.builder()
                .pdxID(this.getPdxID())
                .pdxLabel(this.getPdxLabel())
                .createdBy(this.getCreatedBy())
                .notes(this.getNotes())
                .patient(patient)
                .tumor(tumor)
                .build();
    }
}
