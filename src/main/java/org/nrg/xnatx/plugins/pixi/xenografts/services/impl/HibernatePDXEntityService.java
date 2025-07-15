package org.nrg.xnatx.plugins.pixi.xenografts.services.impl;

import org.nrg.xnatx.plugins.pixi.xenografts.entities.PDXEntity;
import org.nrg.xnatx.plugins.pixi.xenografts.models.PDX;
import org.nrg.xnatx.plugins.pixi.xenografts.repositories.PDXEntityDAO;
import org.nrg.xnatx.plugins.pixi.xenografts.services.XenograftModelImporterHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service("PDXService")
public class HibernatePDXEntityService extends HibernateXenograftEnityService<PDXEntity, PDXEntityDAO, PDX> {

    private final NamedParameterJdbcTemplate template;
    private static final String SOURCE_ID_PARAMETER = "source_id";
    private static final String QUERY_HAS_SUBJECT_REFERENCES = "SELECT EXISTS(SELECT sourceid FROM pixi_pdxdata WHERE sourceid = :source_id)";

    @Autowired
    public HibernatePDXEntityService(NamedParameterJdbcTemplate template, final XenograftModelImporterHandlerService importerHandlerService) {
        super(PDX.class, importerHandlerService);
        this.template = template;
    }

    @Override
    public boolean hasSubjectReferences(final String sourceId) {
        return template.queryForObject(QUERY_HAS_SUBJECT_REFERENCES, new MapSqlParameterSource(SOURCE_ID_PARAMETER, sourceId), Boolean.class);
    }

    @Override
    protected PDX toDTO(PDXEntity pdxEntity) {
        PDX pdx = PDX.builder()
                .sourceId(pdxEntity.getSourceId())
                .source(pdxEntity.getSource())
                .sourceURL(pdxEntity.getSourceURL())
                .storage(pdxEntity.getStorage())
                .createdBy(pdxEntity.getCreatedBy())
                .build();
        pdx.setGender(pdxEntity.getGender());
        pdx.setAge(pdxEntity.getAge());
        pdx.setPatientId(pdxEntity.getPatientId());
        pdx.setDiagnosis(pdxEntity.getDiagnosis());
        pdx.setDiseaseStageClassification(pdxEntity.getDiseaseStageClassification());
        pdx.setTissueHistology(pdxEntity.getTissueHistology());
        pdx.setPrimarySite(pdxEntity.getPrimarySite());
        pdx.setCollectionSite(pdxEntity.getCollectionSite());
        pdx.setTumorType(pdxEntity.getTumorType());
        pdx.setTumorFromUntreatedPatient(pdxEntity.getTumorFromUntreatedPatient());
        pdx.setTumorGradeClassification(pdxEntity.getTumorGradeClassification());
        pdx.setSpecificMarkersPlatform(pdxEntity.getSpecificMarkersPlatform());
        pdx.setSpecimenTumorTissue(pdxEntity.getSpecimenTumorTissue());
        return pdx;
    }

    @Override
    protected PDXEntity toEntity(PDX pdx) {
        PDXEntity pdxEntity = new PDXEntity();
        updateEntity(pdxEntity, pdx);
        return pdxEntity;
    }

    @Override
    protected void updateEntity(PDXEntity pdxEntity, PDX pdx) {
        pdxEntity.setSourceId(pdx.getSourceId());
        pdxEntity.setSource(pdx.getSource());
        pdxEntity.setSourceURL(pdx.getSourceURL());
        pdxEntity.setStorage(pdx.getStorage());
        pdxEntity.setCreatedBy(pdx.getCreatedBy());
        pdxEntity.setAge(pdx.getAge());
        pdxEntity.setGender(pdx.getGender());
        pdxEntity.setPatientId(pdx.getPatientId());
        pdxEntity.setCollectionSite(pdx.getCollectionSite());
        pdxEntity.setDiagnosis(pdx.getDiagnosis());
        pdxEntity.setDiseaseStageClassification(pdx.getDiseaseStageClassification());
        pdxEntity.setPrimarySite(pdx.getPrimarySite());
        pdxEntity.setSpecificMarkersPlatform(pdx.getSpecificMarkersPlatform());
        pdxEntity.setSampleId(pdx.getSampleId());
        pdxEntity.setSpecimenTumorTissue(pdx.getSpecimenTumorTissue());
        pdxEntity.setTissueHistology(pdx.getTissueHistology());
        pdxEntity.setTumorFromUntreatedPatient(pdx.getTumorFromUntreatedPatient());
        pdxEntity.setTumorGradeClassification(pdx.getTumorGradeClassification());
        pdxEntity.setTumorType(pdx.getTumorType());
    }
}
