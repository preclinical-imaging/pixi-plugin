package org.nrg.xnatx.plugins.pixi.cmo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Hashtable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreClinicalImagingReportEntry implements Serializable {

    String treatments;
    String modality;
    Hashtable<String, Long> sequenceOrTracerCounts = new Hashtable();

    public void addContrastSequenceUsed(final String seq) {
        incrementNumberOfImages(seq);
    }

    private void incrementNumberOfImages(final String sequence) {
        if (sequenceOrTracerCounts.containsKey(sequence)) {
            sequenceOrTracerCounts.put(sequence, sequenceOrTracerCounts.get(sequence) + 1);
        } else {
            sequenceOrTracerCounts.put(sequence, 1L);
        }
    }

}
