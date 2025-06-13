package org.nrg.xnatx.plugins.pixi.cmo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreClinicalReport implements Serializable {

    List<PreClinicalReportEntry> preClinicalReportEntryList;
    String projectTitle;
    String projectUrl;
    String projectId;
    String description;

}
