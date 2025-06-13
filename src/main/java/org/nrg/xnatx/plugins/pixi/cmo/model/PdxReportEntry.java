package org.nrg.xnatx.plugins.pixi.cmo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdxReportEntry implements Serializable {
    String passageNumber;
    String sourceId;
    String engraftmentSite;
    List<String> subjectIds = new ArrayList();

    public void addSubjectId(String id) {
        subjectIds.add(id);
    }
}
