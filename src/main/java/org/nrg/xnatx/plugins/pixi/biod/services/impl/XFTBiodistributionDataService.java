package org.nrg.xnatx.plugins.pixi.biod.services.impl;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.core.Local;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService;
import org.nrg.xnatx.plugins.pixi.biod.helpers.SaveItemHelper;
import org.nrg.xnatx.plugins.pixi.biod.helpers.XnatExperimentDataHelper;
import org.nrg.xnatx.plugins.pixi.biod.helpers.XnatSubjectDataHelper;
import org.nrg.xnatx.plugins.pixi.biod.services.BiodistributionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class XFTBiodistributionDataService implements BiodistributionDataService {

    private final UserDataCache userDataCache;
    private final XnatSubjectDataHelper xnatSubjectDataHelper;
    private final XnatExperimentDataHelper xnatExperimentDataHelper;
    private final SaveItemHelper saveItemHelper;
    private final DefaultCatalogService defaultCatalogService;
    private final SiteConfigPreferences siteConfigPreferences;

    private static final String SUBJECT_LABEL_COLUMN = "subject_id";


    @Autowired
    public XFTBiodistributionDataService(UserDataCache userDataCache,
                                         XnatSubjectDataHelper xnatSubjectDataHelper,
                                         XnatExperimentDataHelper xnatExperimentDataHelper,
                                         SaveItemHelper saveItemHelper, DefaultCatalogService defaultCatalogService, SiteConfigPreferences siteConfigPreferences) {
        this.userDataCache = userDataCache;
        this.xnatSubjectDataHelper = xnatSubjectDataHelper;
        this.xnatExperimentDataHelper = xnatExperimentDataHelper;
        this.saveItemHelper = saveItemHelper;
        this.defaultCatalogService = defaultCatalogService;
        this.siteConfigPreferences = siteConfigPreferences;
    }

    @Override
    public Optional<PixiBiodistributiondataI> findByLabel(UserI user, String project, String label) {
        Optional<XnatExperimentdataI> experiment = Optional.ofNullable(xnatExperimentDataHelper.getExptByProjectIdentifier(project, label, user, false));

        if (experiment.isPresent()) {
            String id = experiment.get().getId();
            return Optional.ofNullable(PixiBiodistributiondata.getPixiBiodistributiondatasById(id, user, false));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public PixiBiodistributiondataI createOrUpdate(UserI user, PixiBiodistributiondataI biodistributionData,
                                                   String dataOverlapHandling, Map<String, String> subjectToSubjectGroupMap) throws Exception {
        log.debug("User {} is attempting to create/update biodistribution data experiment in project {} with label {}",
                  user.getUsername(), biodistributionData.getProject(), biodistributionData.getLabel());

        String subjectId = biodistributionData.getSubjectId();

        // We can't save the biod subject assessor if we don't know the subject
        if (subjectId == null || subjectId.isEmpty()) {
            throw new DataFormatException("Subject ID is required");
        }

        Optional<XnatSubjectdataI> subject = Optional.ofNullable(xnatSubjectDataHelper.getSubjectByIdOrProjectlabelCaseInsensitive(biodistributionData.getProject(), subjectId, user, false));

        if (!subject.isPresent()) { // Create new subject if it does not exist
            XnatSubjectdata newSubject = new XnatSubjectdata(user);
            String newSubjectId = xnatSubjectDataHelper.createNewId();
            newSubject.setId(newSubjectId);
            newSubject.setProject(biodistributionData.getProject());
            newSubject.setLabel(biodistributionData.getSubjectId()); // If the subject ID could not be found, then the subject ID in the experiment is the subject label
            biodistributionData.setSubjectId(newSubjectId); // Set the subject ID in the experiment to the new subject ID
            if (subjectToSubjectGroupMap.containsKey(subjectId)) {
                newSubject.setGroup(subjectToSubjectGroupMap.get(subjectId));
            }
            saveSubject(user, newSubject);
        } else {
            biodistributionData.setSubjectId(subject.get().getId()); // Set the subject ID in the experiment to the existing subject ID, it may have been the subject label
        }

        Optional<PixiBiodistributiondataI> experiment = findByLabel(user, biodistributionData.getProject(), biodistributionData.getLabel());

        if(experiment.isPresent()) {
            switch (dataOverlapHandling) {
                case "throw_error":
                    throw new DataFormatException("Bio Distribution data has already been created for subject ID: " + subjectId + ". You may have uploaded this file in error.");
                case "ignore_matching":
                    return null;
                case "upload_overwrite":
                    String biodId = experiment.get().getId();
                    biodistributionData.setId(biodId);
                    break;
            }
        } else {
            String biodId = xnatExperimentDataHelper.createNewId();
            biodistributionData.setId(biodId);
        }

        saveExperiment(user, biodistributionData);

        return findByLabel(
                user, biodistributionData.getProject(), biodistributionData.getLabel()
        ).orElseThrow(
                () -> new NotFoundException("Failed to create or update biodistribution data experiment")
        );
    }

    @Override
    public List<PixiBiodistributiondataI> createOrUpdate(UserI user, List<PixiBiodistributiondataI> biodistributionDatas,
                                                         String dataOverlapHandling, Map<String, String> subjectToSubjectGroupMap) throws Exception {
        log.debug("User {} is attempting to create/update biodistribution data experiments in project {}",
                  user.getUsername(), biodistributionDatas.get(0).getProject());

        List<PixiBiodistributiondataI> createdExperiments = new ArrayList<>();

        for (PixiBiodistributiondataI biodistributionData : biodistributionDatas) {
            createdExperiments.add(createOrUpdate(user, biodistributionData, dataOverlapHandling, subjectToSubjectGroupMap));
        }

        return createdExperiments;
    }

    protected void saveSubject(UserI user, XnatSubjectdataI subject) throws Exception {
        log.debug("Saving subject {} in project {}", subject.getLabel(), subject.getProject());

        XFTItem item = ((ItemI) subject).getItem();

        log.debug("Saving subject");
        saveItemHelper.authorizedSave(item, user, false, false, false, true, EventUtils.DEFAULT_EVENT(user, "Saved " + item.getXSIType()));
        log.debug("subject saved");
    }

    protected void saveExperiment(UserI user, XnatExperimentdataI experiment) throws Exception {
        log.debug("Saving experiment {} in project {}", experiment.getLabel(), experiment.getProject());

        XFTItem item = ((ItemI) experiment).getItem();

        log.debug("Saving experiment");
        saveItemHelper.authorizedSave(item, user, false, false, false, true, EventUtils.DEFAULT_EVENT(user, "Saved " + item.getXSIType()));
        log.debug("Experiment saved");
    }

    @Override
    public List<PixiBiodistributiondataI> fromCsv(UserI user, String project, String userCachePath, String dataOverlapHandling) throws Exception {
        log.debug("User {} is attempting to create biodistribution data experiment in project {} from cache path {}",
                  user.getUsername(), project, userCachePath);

        File excelFile = userDataCache.getUserDataCacheFile(user, Paths.get(userCachePath));
        if (!excelFile.exists()) {
            throw new DataFormatException("Invalid file path: " + userCachePath);
        }

        return fromCsv(user, project, excelFile, dataOverlapHandling);
    }

    @Override
    public List<PixiBiodistributiondataI> fromCsv(UserI user, String project, File file, String dataOverlapHandling) throws Exception {
        log.debug("User {} is attempting to create biodistribution data experiment in project {} from file {}",
                  user.getUsername(), project, file.getAbsolutePath());

        List<PixiBiodistributiondataI> biodExperiments;

        //We need to pass this data on until we have the subject object
        Map<String, String> subjectToSubjectGroupMap = new HashMap<>();

        try (Stream<String> lines = Files.lines(Paths.get(file.toURI()))) {
            List<List<String>> biodImportRows = lines.map(line -> Arrays.asList(line.split(",")))
                    .collect(Collectors.toList());

            Map<String, PixiBiodistributiondataI> currentlyExistingBiod = new HashMap<>();

            Map<String, Integer> ingestionHeaderMap = getHeaderMap(biodImportRows.get(0));
            biodImportRows.remove(0);

            validateCsv(biodImportRows, ingestionHeaderMap);

            int currentRow = 1;

            for (List<String> row: biodImportRows) {
                //checking for rows (usually at the end of a sheet) which are accidentally left blank
                // we shouldn't throw exceptions for these
                if (isRowEmpty(row)) {
                    continue;
                }

                PixiBiodistributiondataI biodistributionData;

                int finalCurrentRow = currentRow;
                String subjectLabel = getCellValue(row, ingestionHeaderMap, SUBJECT_LABEL_COLUMN).orElseThrow(
                        () -> new DataFormatException("Missing required field: " + SUBJECT_LABEL_COLUMN + " in row " + finalCurrentRow)
                );

                if (currentlyExistingBiod.containsKey(subjectLabel)) {
                    biodistributionData = currentlyExistingBiod.get(subjectLabel);
                } else {
                    biodistributionData = handleCommonPortion(row, project, subjectLabel,ingestionHeaderMap);
                    Optional<String> subjectGroup = getCellValue(row, ingestionHeaderMap, "subject_group");
                    subjectGroup.ifPresent(s -> subjectToSubjectGroupMap.put(subjectLabel, s));
                }

                PixiBiodsampleuptakedataI sampleUptakeData = new PixiBiodsampleuptakedata();
                getCellValue(row, ingestionHeaderMap, "sample_type").ifPresent(sampleUptakeData::setSampleType);

                Optional<Double> sampleWeight = getCellValueAsDouble(row, ingestionHeaderMap, "sample_weight");
                Optional<String> sampleWeightUnit = getCellValue(row, ingestionHeaderMap, "sample_weight_unit");

                sampleWeight.ifPresent(sampleUptakeData::setSampleWeight);
                sampleWeightUnit.ifPresent(sampleUptakeData::setSampleWeightUnit);

                if (sampleWeight.isPresent() && !sampleWeightUnit.isPresent()) {
                    sampleUptakeData.setSampleWeight(sampleWeight.get());
                    sampleUptakeData.setSampleWeightUnit("g");
                }
                Optional<DateOptionalTime> measurementDate = getCellValueAsDate(row, ingestionHeaderMap, "measurement_datetime");
                if(measurementDate.isPresent()) {
                    sampleUptakeData.setMeasurementDate(measurementDate.get().date);
                    if (measurementDate.get().time != null) {
                        sampleUptakeData.setMeasurementTime(measurementDate.get().time);
                    }

                }

                Optional<Double> timepointValue = getCellValueAsDouble(row, ingestionHeaderMap, "timepoint_value");
                Optional<String> timepointUnit = getCellValue(row, ingestionHeaderMap, "timepoint_unit");

                timepointValue.ifPresent(sampleUptakeData::setTimepointValue);
                if (timepointUnit.isPresent()) {
                    String timepointStandardized = standardizeTimepointValues(timepointUnit.get());
                    if (timepointStandardized != null) {
                        sampleUptakeData.setTimepointUnit(timepointStandardized);
                    }
                }

                getCellValue(row, ingestionHeaderMap, "%_id_g").ifPresent(sampleUptakeData::setPercentInjectedDosePerGram);
                getCellValue(row, ingestionHeaderMap, "%_id_organ").ifPresent(sampleUptakeData::setPercentInjectedDosePerOrgan);
                getCellValue(row, ingestionHeaderMap, "decay_corrected_cpm").ifPresent(sampleUptakeData::setDecayCorrectedCpm);

                biodistributionData.addSampleUptakeData(sampleUptakeData);

                currentlyExistingBiod.put(subjectLabel, biodistributionData);
                currentRow++;
            }
            biodExperiments = new ArrayList<>(currentlyExistingBiod.values());
        } catch (IOException e) {
            log.error("Error reading csv file: {}", e.getMessage());
            throw new DataFormatException("Invalid csv file", e);
        }

        XnatProjectdata projectData = XnatProjectdata.getProjectByIDorAlias(project, user, false);
        Path projectResourcePath = Paths.get(siteConfigPreferences.getArchivePath()).getFileName().resolve(Paths.get("projects")).resolve(projectData.getArchiveDirectoryName());
        String resourcesPathWithLeadingElement = Paths.get(siteConfigPreferences.getArchivePath()).getRoot().toString() + projectResourcePath.toString();
        defaultCatalogService.insertResources(user, resourcesPathWithLeadingElement, file, "BioDUploadFiles", "", "", "");
        return createOrUpdate(user, biodExperiments, dataOverlapHandling, subjectToSubjectGroupMap);
    }

    private PixiBiodistributiondataI handleCommonPortion(List<String> row, String project, String subjectLabel,
                                                         Map<String, Integer> ingestionHeaderMap) throws Exception {
        PixiBiodistributiondataI biodistributionData = new PixiBiodistributiondata();
        biodistributionData.setProject(project);

        PixiBiodinjectiondataI injectionData = new PixiBiodinjectiondata();
        biodistributionData.setInjectionData(injectionData);

        biodistributionData.setSubjectId(subjectLabel);
        biodistributionData.setLabel(subjectLabel + "_Biod");

        Optional<DateOptionalTime> experimentDate = getCellValueAsDate(row, ingestionHeaderMap, "experiment_datetime");
        if (experimentDate.isPresent()) {
            biodistributionData.setDate(experimentDate.get().date);
            if (experimentDate.get().time!= null) {
                biodistributionData.setTime(experimentDate.get().time);
            }
        }
        getCellValue(row, ingestionHeaderMap, "acquisition_site").ifPresent(biodistributionData::setAcquisitionSite);
        getCellValue(row, ingestionHeaderMap, "note").ifPresent(biodistributionData::setNote);
        getCellValue(row, ingestionHeaderMap, "technician").ifPresent(biodistributionData::setTechnician);

        Optional<DateOptionalTime> animalSacrificeDate = getCellValueAsDate(row, ingestionHeaderMap, "animal_sacrifice_datetime");
        if (animalSacrificeDate.isPresent()) {
            biodistributionData.setAnimalSacrificeDate(animalSacrificeDate.get().date);
            if (animalSacrificeDate.get().time!= null) {
                biodistributionData.setAnimalSacrificeTime(animalSacrificeDate.get().time);
            }
        }

        Optional<Double> animalWeight = getCellValueAsDouble(row, ingestionHeaderMap, "animal_weight");
        Optional<String> animalWeightUnit = getCellValue(row, ingestionHeaderMap, "animal_weight_unit");

        animalWeight.ifPresent(biodistributionData::setAnimalWeight);
        animalWeightUnit.ifPresent(biodistributionData::setAnimalWeightUnit);

        if (animalWeight.isPresent() && !animalWeightUnit.isPresent()) {
            // Assume grams if unit is not specified
            biodistributionData.setAnimalWeightUnit("g");
        }

        getCellValue(row, ingestionHeaderMap, "tracer").ifPresent(injectionData::setTracer);
        getCellValue(row, ingestionHeaderMap, "isotope").ifPresent(injectionData::setIsotope);
        getCellValue(row, ingestionHeaderMap, "diluent").ifPresent(injectionData::setDiluent);

        Optional<Double> injectedDose = getCellValueAsDouble(row, ingestionHeaderMap, "injected_dose");
        Optional<String> injectedDoseUnit = getCellValue(row, ingestionHeaderMap, "injected_dose_unit");

        injectedDose.ifPresent(injectionData::setInjectedDose);
        injectedDoseUnit.ifPresent(injectionData::setInjectedDoseUnit);

        if (injectedDose.isPresent() && !injectedDoseUnit.isPresent()) {
            injectionData.setInjectedDose(injectedDose.get());
            injectionData.setInjectedDoseUnit("µL");
        }

        Optional<Double> injectionVolume = getCellValueAsDouble(row, ingestionHeaderMap, "injection_volume");
        Optional<String> injectionVolumeUnit = getCellValue(row, ingestionHeaderMap, "injection_volume_unit");

        injectionVolume.ifPresent(injectionData::setInjectionVolume);
        injectionVolumeUnit.ifPresent(injectionData::setInjectionVolumeUnit);

        if (injectionVolume.isPresent() && !injectionVolumeUnit.isPresent()) {
            injectionData.setInjectionVolume(injectionVolume.get());
            injectionData.setInjectionVolumeUnit("µL");
        }

        getCellValue(row, ingestionHeaderMap, "injection_total_counts").ifPresent(injectionData::setInjectionTotalCounts);
        getCellValue(row, ingestionHeaderMap, "injection_route").ifPresent(injectionData::setInjectionRoute);
        getCellValue(row, ingestionHeaderMap, "injection_site").ifPresent(injectionData::setInjectionSite);

        Optional<DateOptionalTime> injectionDate = getCellValueAsDate(row, ingestionHeaderMap, "injection_datetime");
        if(injectionDate.isPresent()) {
            injectionData.setInjectionDate(injectionDate.get().date);
            if (injectionDate.get().time != null) {
                injectionData.setInjectionTime(injectionDate.get().time);
            }
        }

        // Anesthesia is handled in a separate object, reused with the hotel splitter
        Optional<String> anesthesia = getCellValue(row, ingestionHeaderMap, "anesthesia");
        Optional<String> anesthesiaRoute = getCellValue(row, ingestionHeaderMap, "anesthesia_route");

        if (anesthesia.isPresent() || anesthesiaRoute.isPresent()) {
            PixiAnesthesiadataI anesthesiaData = new PixiAnesthesiadata();
            anesthesia.ifPresent(anesthesiaData::setAnesthesia);
            anesthesiaRoute.ifPresent(anesthesiaData::setRouteofadministration);
            biodistributionData.setAnesthesiaAdministration(anesthesiaData);
        }

        Optional<String> subjectGroup = getCellValue(row, ingestionHeaderMap, "subject_group");
        if (subjectGroup.isPresent()) {

        }

        return biodistributionData;
    }

    private Map<String, Integer> getHeaderMap(List<String> headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            headerMap.put(headerRow.get(i), i);
        }
        return headerMap;
    }

    private Optional<String> getCellValue(List<String> row, Map<String, Integer> headerMap, String headerName) {
        Integer cellIndex = headerMap.get(headerName);
        if (cellIndex == null) {
            return Optional.empty();
        }
        String cell = row.get(cellIndex);
        return !(cell.isEmpty()) ? Optional.of(cell) : Optional.empty();
    }

    private Optional<Double> getCellValueAsDouble(List<String> row, Map<String, Integer> headerMap, String headerName) {
        Integer cellIndex = headerMap.get(headerName);
        if (cellIndex == null) {
            return Optional.empty();
        }
        String cell = row.get(cellIndex);
        return !(cell.isEmpty()) ? Optional.of(Double.valueOf(cell)) : Optional.empty();
    }

    private Optional<DateOptionalTime> getCellValueAsDate(List<String> row, Map<String, Integer> headerMap, String headerName) {
        Integer cellIndex = headerMap.get(headerName);
        if (cellIndex == null) {
            return Optional.empty();
        }
        String cell = row.get(cellIndex);
        if (cell.isEmpty()) {
            return Optional.empty();
        }
        String cell2 = cell.replace("-", "").replace("/","")
                .replace(":","").replace(" ", "-");
        TemporalAccessor dt = new DateTimeFormatterBuilder()
                .appendPattern("MMddyyyy[-HHmmss]")
                .appendOptional(DateTimeFormatter.ISO_TIME).parseCaseInsensitive().toFormatter().parse(cell2);
        //Using inner class to work around needing to always have a time. Should give more options to users.
        DateOptionalTime dateOptionalTime = new DateOptionalTime();
        if (dt.query(TemporalQueries.localTime()) == null) {
            dateOptionalTime.date = dt.query(TemporalQueries.localDate());
        } else {
            dateOptionalTime.date = dt.query(TemporalQueries.localDate());
            dateOptionalTime.time = dt.query(TemporalQueries.localTime());
        }

        return Optional.of(dateOptionalTime);
    }

    private boolean isRowEmpty(List<String> row){
        List<String> nonEmptyStrings = row.stream().filter(i -> !i.isEmpty()).collect(Collectors.toList());
        return nonEmptyStrings.isEmpty();
    }

    private String standardizeTimepointValues(String timepointInput) {
        if (timepointInput.equals("s") || timepointInput.equals("sec") || timepointInput.equals("secs") || timepointInput.equals("second") | timepointInput.equals("seconds")) {
            return "seconds";
        } else if (timepointInput.equals("m") || timepointInput.equals("min") || timepointInput.equals("mins") || timepointInput.equals("minute") | timepointInput.equals("minutes")) {
            return "minutes";
        } else if (timepointInput.equals("h") || timepointInput.equals("hr") || timepointInput.equals("hrs") || timepointInput.equals("hour") | timepointInput.equals("hours")) {
            return "hours";
        } else if (timepointInput.equals("d") || timepointInput.equals("day") || timepointInput.equals("days")) {
            return "days";
        }
        return null;
    }

    protected void validateCsv(List<List<String>> biodImportRows, Map<String, Integer> ingestionHeaderMap) throws DataFormatException {
        log.debug("Validating injection and biodistribution sheets");

        DataFormatException e = new DataFormatException("There is a problem with the input injection sheet: ");
        boolean isValid = true;

        if (!ingestionHeaderMap.containsKey(SUBJECT_LABEL_COLUMN)) {
            e.addMissingField(SUBJECT_LABEL_COLUMN);
            isValid = false;
        }

        if (!ingestionHeaderMap.containsKey("sample_type")) {
            e.addMissingField("sample_type");
            isValid = false;
        }

        Map<String, List<String>> animalSampleTypes = new HashMap<>();
        final String SAMPLE_TYPE_COLUMN = "sample_type";
        int currentRowNumber = 1;
        for (List<String> row : biodImportRows) {

            Optional<String> animalId = getCellValue(row, ingestionHeaderMap, SUBJECT_LABEL_COLUMN);
            Optional<String> sampleType = getCellValue(row, ingestionHeaderMap, SAMPLE_TYPE_COLUMN);

            if (!animalId.isPresent()) {
                e.addInvalidField(SUBJECT_LABEL_COLUMN, "Missing " + SUBJECT_LABEL_COLUMN + " in row " + currentRowNumber);
                isValid = false;
            } else if (!sampleType.isPresent()) {
                e.addInvalidField(SAMPLE_TYPE_COLUMN, "Missing " + SAMPLE_TYPE_COLUMN + " in row " + currentRowNumber);
                isValid = false;
            }else {
                if (animalSampleTypes.containsKey(animalId.get())) {
                    if (animalSampleTypes.get(animalId.get()).contains(sampleType.get())) {
                        e.addInvalidField("Duplicate pairing", "The pairing of " + SUBJECT_LABEL_COLUMN + " and " +
                                SAMPLE_TYPE_COLUMN + " in row " + currentRowNumber + " is found together in another row.");
                        isValid = false;
                    } else {
                        animalSampleTypes.get(animalId.get()).add(sampleType.get());
                    }
                } else {
                    animalSampleTypes.put(animalId.get(), new ArrayList<>(Collections.singletonList(sampleType.get())));
                }
            }
            currentRowNumber++;
        }

        if (!isValid) {
            log.error("", e);
            throw e;
        }

        log.debug("Input file is valid");
    }

    protected void validateBiodistributionData(final PixiBiodistributiondataI biodistributionData) throws DataFormatException {
        log.debug("Validating biodistribution data");

        DataFormatException e = new DataFormatException();
        boolean isValid = true;

        if (biodistributionData.getSubjectId() == null) {
            e.addMissingField("subject_id");
            isValid = false;
        }

        if (!isValid) {
            log.error("", e);
            throw e;
        }

        log.debug("Biodistribution data is valid");
    }

    private class DateOptionalTime {
        LocalDate date;
        LocalTime time;
    }
}
