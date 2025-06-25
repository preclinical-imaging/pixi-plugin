package org.nrg.xnatx.plugins.pixi.cmo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.xnatx.plugins.pixi.cmo.CMOUtils;

import java.io.Serializable;
import java.util.Hashtable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreClinicalImagingReportEntry implements Serializable {

    String treatments = CMOUtils.NOT_PROVIDED;
    String modality;
    Hashtable<String, Long> sequenceOrTracerCounts = new Hashtable();

    public void addContrastSequenceUsed(final String seq) {
        incrementNumberOfImages(seq);
    }

    private void incrementNumberOfImages(final String sequence) {
        String sequenceWithoutInvalidChars = sequence.replaceAll(CMOUtils.REGuLAR_EXP, "_");
        if (sequenceOrTracerCounts.containsKey(sequenceWithoutInvalidChars)) {
            sequenceOrTracerCounts.put(sequenceWithoutInvalidChars, sequenceOrTracerCounts.get(sequenceWithoutInvalidChars) + 1);
        } else {
            sequenceOrTracerCounts.put(sequenceWithoutInvalidChars, 1L);
        }
    }

}
