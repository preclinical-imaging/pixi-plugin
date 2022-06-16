package org.nrg.xnatx.plugins.pixi.hotelsplitter.dcm.extractors;

import org.dcm4che2.data.DicomObject;
import org.nrg.dcm.TextExtractor;

public class HotelSubjectTextExtractor extends TextExtractor {

    public HotelSubjectTextExtractor(int tag) {
        super(tag);
    }

    @Override
    public String extract(final DicomObject o) {
        return "Hotel";
    }
}