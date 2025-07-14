package org.nrg.xnatx.plugins.pixi.xenografts.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnatx.plugins.pixi.xenografts.models.PDX;
import org.nrg.xnatx.plugins.pixi.xenografts.models.Xenograft;

@Importsfrom(url = "https://www.cancermodels.org")
@Slf4j
public class CancerModelsModelJsonHandler implements ModelJsonHandler {

    public Xenograft process(Xenograft X) {
        if (StringUtils.isEmpty(X.getSourceURL())) {
            return X;
        }
        log.debug("Fetching model details from: " + X.getSourceURL());
        PDX pdx = new PDX(X.getSourceId(), X.getSource(), X.getSourceURL(), X.getCreatedBy(), X instanceof PDX ? ((PDX) X).getStorage(): null);
        try {
            String jsonModel = RemoteReader.ReadFromUrl(X.getSourceURL());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonModel);
            JsonNode processingNode = rootNode;
            if (rootNode.isArray()) {
                processingNode = rootNode.get(0);
            }

            final String modelId = processingNode.get("model_id").textValue();
            if (modelId != null && modelId.equals(pdx.getSourceId())) {
                if (processingNode.has("data_source")) {
                    pdx.setSource(processingNode.get("data_source").textValue());
                }
                if (processingNode.has("tumor_type")) {
                    pdx.setTumorType(processingNode.get("tumor_type").textValue());
                }
                if (processingNode.has("primary_site")) {
                    pdx.setPrimarySite(processingNode.get("primary_site").textValue());
                }
                if (processingNode.has("collection_site")) {
                    pdx.setCollectionSite(processingNode.get("collection_site").textValue());
                }
                if (processingNode.has("histology")) {
                    pdx.setTissueHistology(processingNode.get("histology").textValue());
                }
                if (processingNode.has("cancer_grade")) {
                    pdx.setTumorGradeClassification(processingNode.get("cancer_grade").textValue());
                }
                if (processingNode.has("cancer_stage")) {
                    pdx.setDiseaseStageClassification(processingNode.get("cancer_stage").textValue());
                }
                if (processingNode.has("patient_age")) {
                    pdx.setAge(processingNode.get("patient_age").textValue());
                }
                if (processingNode.has("patient_sex")) {
                    pdx.setGender(processingNode.get("patient_sex").textValue());
                }
            }
        } catch(Exception e) {
            log.error("Could not read from remote host {}", X.getSourceURL());
        }
        return pdx;
    }

}
