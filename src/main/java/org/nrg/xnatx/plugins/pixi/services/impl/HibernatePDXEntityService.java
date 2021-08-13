package org.nrg.xnatx.plugins.pixi.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.pixi.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.entities.PatientEntity;
import org.nrg.xnatx.plugins.pixi.entities.TumorEntity;
import org.nrg.xnatx.plugins.pixi.models.AnimalModel;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.models.Patient;
import org.nrg.xnatx.plugins.pixi.models.Tumor;
import org.nrg.xnatx.plugins.pixi.repositories.PDXEntityDAO;
import org.nrg.xnatx.plugins.pixi.services.PDXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HibernatePDXEntityService extends AbstractHibernateEntityService<PDXEntity, PDXEntityDAO> implements PDXService {

    @Autowired
    public HibernatePDXEntityService() {
        super();
    }

    @Override
    @Transactional
    public boolean pdxExists(String pdxID) {
        return super.getDao().pdxExists(pdxID);
    }

    @Override
    @Transactional
    public Optional<PDX> getPDX(final String pdxID) {
        return super.getDao().findByPdxId(pdxID).map(this::toDTO);
    }

    @Override
    @Transactional
    public Optional<PDXEntity> getPDXEntity(final String pdxID) {
        return super.getDao().findByPdxId(pdxID);
    }

    @Override
    @Transactional
    public List<PDX> getAllPDX() {
        return super.getAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createPDX(PDX pdx) throws ResourceAlreadyExistsException{
        if (this.pdxExists(pdx.getId())) {
            throw new ResourceAlreadyExistsException(PDXEntity.class.getSimpleName(), pdx.getId());
        }

        super.create(toEntity(pdx));
    }

    @Override
    @Transactional
    public void updatePDX(PDX pdx) throws NotFoundException {
        PDXEntity pdxEntity = getDao().findByPdxId(pdx.getId())
                                      .orElseThrow(() -> new NotFoundException(AnimalModel.class.getSimpleName(), pdx.getId()));
        updateEntity(pdxEntity, pdx);
        super.update(pdxEntity);
    }

    @Override
    @Transactional
    public void deletePDX(String pdxID) {
        super.getDao().findByPdxId(pdxID).ifPresent(super::delete);
    }

    private PDX toDTO(final PDXEntity pdxEntity) {
        Optional<PatientEntity> patientEntity = Optional.ofNullable(pdxEntity.getPatientEntity());
        Patient patient = patientEntity.map(this::toDTO).orElse(null);

        Optional<TumorEntity> tumorEntity = Optional.ofNullable(pdxEntity.getTumorEntity());
        Tumor tumor = tumorEntity.map(this::toDTO).orElse(null);

        return PDX.builder()
                .id(pdxEntity.getPdxID())
                .pdxLabel(pdxEntity.getPdxLabel())
                .createdBy(pdxEntity.getCreatedBy())
                .notes(pdxEntity.getNotes())
                .patient(patient)
                .tumor(tumor)
                .build();
    }

    private PDXEntity toEntity(final PDX pdx) {
        final PDXEntity pdxEntity = new PDXEntity();
        updateEntity(pdxEntity, pdx);
        return pdxEntity;
    }

    private void updateEntity(final PDXEntity pdxEntity, final PDX pdx) {
        pdxEntity.setPdxID(pdx.getId());
        pdxEntity.setPdxLabel(pdx.getPdxLabel());
        pdxEntity.setNotes(pdx.getNotes());
        pdxEntity.setCreatedBy(pdx.getCreatedBy());

        if (pdx.getPatient() == null) {
            pdxEntity.setPatientEntity(null);
        } else if (pdx.getPatient() != null && pdxEntity.getPatientEntity() == null) {
            PatientEntity patientEntity = toEntity(pdx.getPatient());
            pdxEntity.setPatientEntity(patientEntity);
        } else if (pdx.getPatient() != null && pdxEntity.getPatientEntity() != null) {
           updateEntity(pdxEntity.getPatientEntity(), pdx.getPatient());
        }

        if (pdx.getTumor() == null) {
            pdxEntity.setTumorEntity(null);
        } else if (pdx.getTumor() != null && pdxEntity.getTumorEntity() == null) {
            TumorEntity tumorEntity = toEntity(pdx.getTumor());
            pdxEntity.setTumorEntity(tumorEntity);
        } else if (pdx.getTumor() != null && pdxEntity.getTumorEntity() != null) {
            updateEntity(pdxEntity.getTumorEntity(), pdx.getTumor());
        }

    }

    private Patient toDTO(final PatientEntity patientEntity) {
        return Patient.builder()
                .submitterPatientID(patientEntity.getSubmitterPatientID())
                .gender(patientEntity.getGender())
                .age(patientEntity.getAge())
                .diagnosis(patientEntity.getDiagnosis())
                .consentToShare(patientEntity.getConsentToShare())
                .ethnicityRace(patientEntity.getEthnicityRace())
                .currentTreatmentDrug(patientEntity.getCurrentTreatmentDrug())
                .currentTreatmentProtocol(patientEntity.getCurrentTreatmentProtocol())
                .priorTreatmentProtocol(patientEntity.getPriorTreatmentProtocol())
                .priorTreatmentResponse(patientEntity.getPriorTreatmentResponse())
                .virologyStatus(patientEntity.getVirologyStatus())
                .build();
    }

    private PatientEntity toEntity(final Patient patient) {
        final PatientEntity patientEntity = new PatientEntity();
        updateEntity(patientEntity, patient);
        return patientEntity;
    }

    private void updateEntity(final PatientEntity patientEntity, final Patient patient) {
        patientEntity.setSubmitterPatientID(patient.getSubmitterPatientID());
        patientEntity.setGender(patient.getGender());
        patientEntity.setAge(patient.getAge());
        patientEntity.setDiagnosis(patient.getDiagnosis());
        patientEntity.setConsentToShare(patient.getConsentToShare());
        patientEntity.setEthnicityRace(patient.getEthnicityRace());
        patientEntity.setCurrentTreatmentDrug(patient.getCurrentTreatmentDrug());
        patientEntity.setCurrentTreatmentProtocol(patient.getCurrentTreatmentProtocol());
        patientEntity.setPriorTreatmentProtocol(patient.getPriorTreatmentProtocol());
        patientEntity.setPriorTreatmentResponse(patient.getPriorTreatmentResponse());
        patientEntity.setVirologyStatus(patient.getVirologyStatus());
    }

    private Tumor toDTO(final TumorEntity tumorEntity) {
        return Tumor.builder()
                .submitterTumorID(tumorEntity.getSubmitterTumorID())
                .primaryTumorTissueOrigin(tumorEntity.getPrimaryTumorTissueOrigin())
                .tumorType(tumorEntity.getTumorType())
                .specimenTumorTissue(tumorEntity.getSpecimenTumorTissue())
                .tissueHistology(tumorEntity.getTissueHistology())
                .tumorGrade(tumorEntity.getTumorGrade())
                .tumorClassification(tumorEntity.getTumorClassification())
                .diseaseStage(tumorEntity.getDiseaseStage())
                .diseaseClassification(tumorEntity.getDiseaseClassification())
                .specificMarkers(tumorEntity.getSpecificMarkers())
                .specificMarkersPlatform(tumorEntity.getSpecificMarkersPlatform())
                .fromUntreatedPatient(tumorEntity.getFromUntreatedPatient())
                .originalTumorSampleType(tumorEntity.getOriginalTumorSampleType())
                .derivedPDXModelID(tumorEntity.getDerivedPDXModelID())
                .derivedPDXReason(tumorEntity.getDerivedPDXReason())
                .build();
    }

    private TumorEntity toEntity(final Tumor tumor) {
        final TumorEntity tumorEntity = new TumorEntity();
        updateEntity(tumorEntity, tumor);
        return tumorEntity;
    }

    private void updateEntity(final TumorEntity tumorEntity, final Tumor tumor) {
        tumorEntity.setSubmitterTumorID(tumor.getSubmitterTumorID());
        tumorEntity.setPrimaryTumorTissueOrigin(tumor.getPrimaryTumorTissueOrigin());
        tumorEntity.setTumorType(tumor.getTumorType());
        tumorEntity.setSpecimenTumorTissue(tumor.getSpecimenTumorTissue());
        tumorEntity.setTissueHistology(tumor.getTissueHistology());
        tumorEntity.setTumorGrade(tumor.getTumorGrade());
        tumorEntity.setTumorClassification(tumor.getTumorClassification());
        tumorEntity.setDiseaseStage(tumor.getDiseaseStage());
        tumorEntity.setDiseaseClassification(tumor.getDiseaseClassification());
        tumorEntity.setSpecificMarkers(tumor.getSpecificMarkers());
        tumorEntity.setSpecificMarkersPlatform(tumor.getSpecificMarkersPlatform());
        tumorEntity.setFromUntreatedPatient(tumor.getFromUntreatedPatient());
        tumorEntity.setOriginalTumorSampleType(tumor.getOriginalTumorSampleType());
        tumorEntity.setDerivedPDXModelID(tumor.getDerivedPDXModelID());
        tumorEntity.setDerivedPDXReason(tumor.getDerivedPDXReason());
    }

}
