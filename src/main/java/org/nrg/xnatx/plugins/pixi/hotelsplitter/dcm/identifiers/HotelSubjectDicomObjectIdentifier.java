package org.nrg.xnatx.plugins.pixi.hotelsplitter.dcm.identifiers;

import com.google.common.collect.ImmutableList;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.ContainedAssignmentExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.dcm.TextExtractor;
import org.nrg.dcm.id.CompositeDicomObjectIdentifier;
import org.nrg.dcm.id.Xnat15DicomProjectIdentifier;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.dcm.extractors.HotelSubjectTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class HotelSubjectDicomObjectIdentifier extends CompositeDicomObjectIdentifier {

    private static final ImmutableList<Extractor> attributeExtractors = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "AA", Pattern.CASE_INSENSITIVE))
            .add(new ContainedAssignmentExtractor(Tag.StudyComments, "AA", Pattern.CASE_INSENSITIVE))
            .add(new ContainedAssignmentExtractor(Tag.AdditionalPatientHistory, "AA", Pattern.CASE_INSENSITIVE))
            .build();

    private static final ImmutableList<Extractor> sessionExtractors   = new ImmutableList.Builder<Extractor>().add(new ContainedAssignmentExtractor(Tag.PatientComments, "Session", Pattern.CASE_INSENSITIVE))
            .add(new ContainedAssignmentExtractor(Tag.StudyComments, "Session", Pattern.CASE_INSENSITIVE))
            .add(new ContainedAssignmentExtractor(Tag.AdditionalPatientHistory, "Session", Pattern.CASE_INSENSITIVE))
            .add(new TextExtractor(Tag.PatientID))
            .build();

    private static final ImmutableList<Extractor> subjectExtractors   = new ImmutableList.Builder<Extractor>().add(new HotelSubjectTextExtractor(Tag.PatientName)).build();

    @Autowired
    public HotelSubjectDicomObjectIdentifier(final UserProjectCache userProjectCache) {
        super("Hotel Subject Dicom Object Identifier", new Xnat15DicomProjectIdentifier(userProjectCache), subjectExtractors, sessionExtractors, attributeExtractors);
    }

}
