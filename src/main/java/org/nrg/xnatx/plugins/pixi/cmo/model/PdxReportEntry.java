package org.nrg.xnatx.plugins.pixi.cmo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.xnatx.plugins.pixi.cmo.CMOUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdxReportEntry implements Serializable {
    PdxPojo pdx;
    List<String> subjectIds = new ArrayList();

    public void addSubjectId(String id) {
        subjectIds.add(id);
    }
}
