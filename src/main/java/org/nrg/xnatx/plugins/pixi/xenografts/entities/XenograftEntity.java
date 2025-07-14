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

    @Getter(AccessLevel.NONE) private String sourceId; //Model ID: The original ID at the source providing this Patient-Derived Tumor model
    @Nullable private String source; //The source providing this Patient-Derived Tumor model
    @Nullable @URL private String sourceURL;
    @Nullable private String sampleId; //Unique ID of the patient tumour sample used to generate PDX(s)
    @Nullable private String tumorType;
    @Nullable private String diagnosis;
    @Nullable private String primarySite;
    @Nullable private String collectionSite;
    @Nullable private String specimenTumorTissue;
    @Nullable private String tissueHistology;
    @Nullable private String tumorGradeClassification;
    @Nullable private String diseaseStageClassification;
    @Nullable private String specificMarkersPlatform;
    @Nullable private String tumorFromUntreatedPatient;
    @Nullable private String createdBy;


    public XenograftEntity(@NotBlank final String sourceId,
                     @Nullable final String source,
                     @Nullable final String sourceURL,
                     @Nullable final String createdBy) {
        this.sourceId = sourceId;
        this.source  = source;
        this.sourceURL = sourceURL;
        this.createdBy = createdBy;
    }

    @Column(unique = true)
    @NotNull @NotBlank
    public String getSourceId() {
        return sourceId;
    }
}
