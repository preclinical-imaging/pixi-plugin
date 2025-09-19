package org.nrg.xnatx.plugins.pixi.xenografts.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnatx.plugins.pixi.xenografts.models.CellLine;
import org.nrg.xnatx.plugins.pixi.xenografts.models.PDX;
import org.nrg.xnatx.plugins.pixi.xenografts.models.Xenograft;

@Importsfrom(url = "https://www.cancermodels.org")
@Slf4j
public class CancerModelsModelJsonHandler implements ModelJsonHandler {

    //All this is expected to change as per CancerModels migration to BioStudies as of 9/1/2025
    public Xenograft process(Xenograft X) {
        if (StringUtils.isEmpty(X.getSourceURL())) {
            return X;
        }
        log.debug("Fetching model details from: " + X.getSourceURL());
        JsonNode processingNode = null;
        Xenograft updatedXenograft = X;
        try {
            String jsonModel = RemoteReader.ReadFromUrl(X.getSourceURL());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonModel);
            processingNode = rootNode;
            if (rootNode.isArray()) {
                processingNode = rootNode.get(0);
            }
            if (processingNode != null) {
                final String modelId = processingNode.get("model_id").textValue();
                if (modelId != null && modelId.equals(updatedXenograft.getSourceId())) {
                    if (processingNode.has("data_source")) {
                        updatedXenograft.setSource(processingNode.get("data_source").textValue());
                    }
                    if (processingNode.has("tumor_type")) {
                        updatedXenograft.setTumorType(processingNode.get("tumor_type").textValue());
                    }
                    if (processingNode.has("primary_site")) {
                        updatedXenograft.setPrimarySite(processingNode.get("primary_site").textValue());
                    }
                    if (processingNode.has("collection_site")) {
                        updatedXenograft.setCollectionSite(processingNode.get("collection_site").textValue());
                    }
                    if (processingNode.has("histology")) {
                        updatedXenograft.setTissueHistology(processingNode.get("histology").textValue());
                    }
                    if (processingNode.has("cancer_grade")) {
                        updatedXenograft.setTumorGradeClassification(processingNode.get("cancer_grade").textValue());
                    }
                    if (processingNode.has("cancer_stage")) {
                        updatedXenograft.setDiseaseStageClassification(processingNode.get("cancer_stage").textValue());
                    }
                    if (updatedXenograft instanceof PDX) {
                        PDX pdx = new PDX(updatedXenograft.getSourceId(), updatedXenograft.getSource(), updatedXenograft.getSourceURL(), updatedXenograft.getCreatedBy(), ((PDX) updatedXenograft).getStorage());
                        if (processingNode.has("patient_age")) {
                            pdx.setAge(processingNode.get("patient_age").textValue());
                        }
                        if (processingNode.has("patient_sex")) {
                            pdx.setGender(processingNode.get("patient_sex").textValue());
                        }
                        updatedXenograft = pdx;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not read from remote host {}", X.getSourceURL());
        }
        return updatedXenograft;
    }
}
