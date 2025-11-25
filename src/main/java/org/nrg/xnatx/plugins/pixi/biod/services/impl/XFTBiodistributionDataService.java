package org.nrg.xnatx.plugins.pixi.biod.services.impl;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.model.PixiAnesthesiadataI;
import org.nrg.xdat.model.PixiBiodinjectiondataI;
import org.nrg.xdat.model.PixiBiodistributiondataI;
import org.nrg.xdat.model.PixiBiodsampleuptakedataI;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.PixiAnesthesiadata;
import org.nrg.xdat.om.PixiBiodinjectiondata;
import org.nrg.xdat.om.PixiBiodistributiondata;
import org.nrg.xdat.om.PixiBiodsampleuptakedata;
import org.nrg.xdat.om.XnatExperimentdataField;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
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
import org.nrg.xnatx.plugins.pixi.preferences.PIXIPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class XFTBiodistributionDataService implements BiodistributionDataService {

    private final UserDataCache userDataCache;
    private final XnatSubjectDataHelper xnatSubjectDataHelper;
    private final XnatExperimentDataHelper xnatExperimentDataHelper;
    private final SaveItemHelper saveItemHelper;
    private final DefaultCatalogService defaultCatalogService;
    private final SiteConfigPreferences siteConfigPreferences;
    private final PIXIPreferences pixiPreferences;

    private static final String SUBJECT_LABEL_COLUMN = "subject_id";
    private static final String SAMPLE_TYPE_COLUMN = "sample_type";

    //all required columns mapped to their type (string, date, etc.) to facilitate validation checking
    Map<String, String> requiredColumnsWithTypes = ImmutableMap.of(SUBJECT_LABEL_COLUMN, "String",
                                                                   SAMPLE_TYPE_COLUMN, "String",
                                                                   "%_id_g", "String",
                                                                   "tracer", "String",
                                                                   "experiment_datetime", "date",
                                                                   "injection_datetime", "date",
                                                                   "subject_group", "String");

    @Autowired
    public XFTBiodistributionDataService(UserDataCache userDataCache,
                                         XnatSubjectDataHelper xnatSubjectDataHelper,
                                         XnatExperimentDataHelper xnatExperimentDataHelper,
                                         SaveItemHelper saveItemHelper, DefaultCatalogService defaultCatalogService, SiteConfigPreferences siteConfigPreferences, PIXIPreferences pixiPreferences) {
        this.userDataCache = userDataCache;
        this.xnatSubjectDataHelper = xnatSubjectDataHelper;
        this.xnatExperimentDataHelper = xnatExperimentDataHelper;
        this.saveItemHelper = saveItemHelper;
        this.defaultCatalogService = defaultCatalogService;
        this.siteConfigPreferences = siteConfigPreferences;
        this.pixiPreferences = pixiPreferences;
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

    private Optional<BiodistributionSubjectToSave> createOrUpdate(UserI user,
                                                                PixiBiodistributiondataI biodistributionData,
                                                        String dataOverlapHandling, Map<String,
                                                        String> subjectToSubjectGroupMap) throws Exception {
        log.debug("User {} is attempting to create/update biodistribution data experiment in project {} with label {}",
                  user.getUsername(), biodistributionData.getProject(), biodistributionData.getLabel());
        BiodistributionSubjectToSave biodistributionSubjectToSave = new BiodistributionSubjectToSave();

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
            //don't save until we know there is no overlap with old data and this sheet of data
            biodistributionSubjectToSave.optionalSubjectDataToSave = newSubject;
        } else {
            biodistributionData.setSubjectId(subject.get().getId()); // Set the subject ID in the experiment to the existing subject ID, it may have been the subject label
        }

        Optional<PixiBiodistributiondataI> experiment = findByLabel(user, biodistributionData.getProject(), biodistributionData.getLabel());

        if(experiment.isPresent()) {
            switch (dataOverlapHandling) {
                case "throw_error":
                    throw new DataFormatException("Bio Distribution data has already been created for subject ID: " + subjectId + ". You may have uploaded this file in error.");
                case "ignore_matching":
                    return Optional.empty();
                case "upload_overwrite":
                    String biodId = experiment.get().getId();
                    biodistributionData.setId(biodId);
                    break;
            }
        } else {
            String biodId = xnatExperimentDataHelper.createNewId();
            biodistributionData.setId(biodId);
        }

        //don't save until we know there is no overlap with old data and this sheet of data
        biodistributionSubjectToSave.biodistributionDataToSave = biodistributionData;

        return Optional.of(biodistributionSubjectToSave);
    }

    protected List<PixiBiodistributiondataI> createOrUpdate(UserI user, List<PixiBiodistributiondataI> biodistributionDatas,
                                                            String dataOverlapHandling, Map<String,
                                                            String> subjectToSubjectGroupMap) throws Exception {
        log.debug("User {} is attempting to create/update biodistribution data experiments in project {}",
                  user.getUsername(), biodistributionDatas.get(0).getProject());

        List<BiodistributionSubjectToSave> elementsToSave = new ArrayList<>();

        for (PixiBiodistributiondataI biodistributionData : biodistributionDatas) {
            Optional<BiodistributionSubjectToSave> optionalOfBiodistribution = createOrUpdate(user, biodistributionData,
                                                                                          dataOverlapHandling,
                                                                                          subjectToSubjectGroupMap);
            optionalOfBiodistribution.ifPresent(elementsToSave::add);
        }

        //we're going through all of this in case we find already existing data in the sheet.
        //in that case we don't want to save any of the data for risk of leaving the user in the lurch with half saved
        List<PixiBiodistributiondataI> createdExperiments = new ArrayList<>();

        for (BiodistributionSubjectToSave elementToSave: elementsToSave) {
            PixiBiodistributiondataI biodistributionDataToSave = elementToSave.biodistributionDataToSave;
            if (elementToSave.optionalSubjectDataToSave!=null) {
                saveSubject(user, elementToSave.optionalSubjectDataToSave);
            }
            saveExperiment(user, biodistributionDataToSave);
            Optional<PixiBiodistributiondataI> createdBiodistributionData = findByLabel(user, biodistributionDataToSave.getProject(), biodistributionDataToSave.getLabel());
            createdBiodistributionData.ifPresent(createdExperiments::add);
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
    public List<String> findAllSubjectsToBeCreated(UserI user, String project, String userCachePath)
            throws DataFormatException {
        log.debug("User {} is opening {} for preprocessing of biodistribution upload. ",
                  user.getUsername(), userCachePath);

        List<String> subjectsToCreate = new ArrayList<>();
        File file = userDataCache.getUserDataCacheFile(user, Paths.get(userCachePath));
        if (!file.exists()) {
            throw new DataFormatException("Invalid file path: " + userCachePath);
        }
        try (Stream<String> lines = Files.lines(Paths.get(file.toURI()))) {
            List<List<String>> biodImportRows = lines.map(line -> Arrays.asList(line.split(",")))
                    .collect(Collectors.toList());

            Map<String, Integer> ingestionHeaderMap = getHeaderMap(biodImportRows.get(0));
            biodImportRows.remove(0);

            for (List<String> row: biodImportRows) {
                Optional<String> subjectLabel = getCellValue(row, ingestionHeaderMap, SUBJECT_LABEL_COLUMN);
                if (subjectLabel.isPresent()) {
                    Optional<XnatSubjectdataI> subject =
                            Optional.ofNullable(xnatSubjectDataHelper.getSubjectByIdOrProjectlabelCaseInsensitive(
                                    project, subjectLabel.get(), user, false));
                    if (!subject.isPresent()) {
                        subjectsToCreate.add(subjectLabel.get());
                    }
                }
            }

        } catch (IOException e) {
            log.error("Error opening csv file for preprocessing: {}", e.getMessage());
            throw new DataFormatException("Invalid csv file", e);
        }
        return subjectsToCreate.stream().distinct().collect(Collectors.toList());
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

            //This map is so we don't create several biodistribution elements in the case where several rows are for
            //the same biod element but for different sample uptake instances. This will hold a connection between
            //the subject id and the already created biod so we can simply add the sample uptake element to the
            //existing biod.
            Map<String, PixiBiodistributiondataI> currentlyExistingBiod = new HashMap<>();

            Map<String, Integer> ingestionHeaderMap = getHeaderMap(biodImportRows.get(0));
            biodImportRows.remove(0);

            validateCsv(biodImportRows, ingestionHeaderMap, project);

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
                getCellValue(row, ingestionHeaderMap, SAMPLE_TYPE_COLUMN).ifPresent(sampleUptakeData::setSampleType);

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

        String projectResourceName = "BioDUploadedFiles";

        XnatProjectdata projectData = XnatProjectdata.getProjectByIDorAlias(project, user, false);
        Path projectResourcePath = Paths.get(siteConfigPreferences.getArchivePath()).getFileName().resolve(Paths.get("projects")).resolve(projectData.getArchiveDirectoryName());
        String resourcesPathWithLeadingElement = Paths.get(siteConfigPreferences.getArchivePath()).getRoot().toString() + projectResourcePath;
        defaultCatalogService.insertResources(user, resourcesPathWithLeadingElement, file, projectResourceName, "", "", "");

        String uploadedResourcePath = Paths.get(resourcesPathWithLeadingElement, projectResourceName, file.getName()).toString();

        for (PixiBiodistributiondataI biodistribution: biodExperiments) {
            XnatExperimentdataField ingestionFileProvenanceField = new XnatExperimentdataField();
            ingestionFileProvenanceField.setName("sourcefile");
            ingestionFileProvenanceField.setField(uploadedResourcePath);
            biodistribution.addFields_field(ingestionFileProvenanceField);
        }

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
        if (cellIndex == null || row.size() <= cellIndex) {
            return Optional.empty();
        }
        String cell = row.get(cellIndex).trim();
        return !(cell.isEmpty()) ? Optional.of(cell) : Optional.empty();
    }

    private Optional<Double> getCellValueAsDouble(List<String> row, Map<String, Integer> headerMap, String headerName) {
        Integer cellIndex = headerMap.get(headerName);
        if (cellIndex == null || row.size() <= cellIndex) {
            return Optional.empty();
        }
        String cell = row.get(cellIndex).trim();
        return !(cell.isEmpty()) ? Optional.of(Double.valueOf(cell)) : Optional.empty();
    }

    private Optional<DateOptionalTime> getCellValueAsDate(List<String> row, Map<String, Integer> headerMap, String headerName) throws DataFormatException {
        Integer cellIndex = headerMap.get(headerName);
        if (cellIndex == null || row.size() <= cellIndex) {
            return Optional.empty();
        }
        String cell = row.get(cellIndex).trim();
        if (cell.isEmpty()) {
            return Optional.empty();
        }

        //we don't know whether user will use time or not or whether that time will have milliseconds.
        //this will allow us to accept any of the above.
        List<String> allPossibleDateTimePatterns = Arrays.asList(siteConfigPreferences.getUiDateTimeSecondsFormat(),
                                                                 siteConfigPreferences.getUiDateTimeFormat(),
                                                                 siteConfigPreferences.getUiDateFormat());
        TemporalAccessor dt = null;

        for (String dateTimePattern: allPossibleDateTimePatterns) {
            try {
                dt = new DateTimeFormatterBuilder().appendPattern(dateTimePattern)
                        .appendOptional(DateTimeFormatter.ISO_TIME).parseCaseInsensitive().toFormatter().parse(cell);
            } catch (DateTimeParseException e) {
                //check to see if the problem is the date is simply not a functional date even though the format is
                // right
                if (e.getMessage().contains("MonthOfYear") || e.getMessage().contains(("DayOfMonth"))) {
                    throw new DataFormatException("The input datetime with value: " + cell + " in column: " + headerName +
                                                          " is not a functional date. Please check this cell again.");
                }
                //this format didn't work. try until we run out of them
            }
        }

        if (dt == null) {
            throw new DataFormatException("The input datetime with value: " + cell + " in column: " + headerName + " " +
                                                 "is not compatible with site config preferences for date/time. The " +
                                                  "available options for datetimes are: \n"  +
                                                  siteConfigPreferences.getUiDateFormat() + "\n" +
                                                  siteConfigPreferences.getUiDateTimeFormat() + "\n" +
                                                  siteConfigPreferences.getUiDateTimeSecondsFormat());
        }

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

    protected void validateCsv(List<List<String>> biodImportRows,
                               Map<String,
                               Integer> ingestionHeaderMap,
                               String projectId) throws DataFormatException, NotFoundException {
        log.debug("Validating injection and biodistribution sheets");

        if (biodImportRows.isEmpty()){
            throw new DataFormatException("The input sheet does not have any data. Please check your input and try " +
                                                  "again.");
        }

        DataFormatException e = new DataFormatException("There is a problem with the input injection sheet: ");

        validatePresentHeaders(ingestionHeaderMap, e);

        List<String> allowedSampleTypes = pixiPreferences.getBiodistributionAcceptedSampleTypes(projectId);

        Map<String, List<String>> animalSampleTypes = new HashMap<>();
        for (int i = 0; i < biodImportRows.size(); i++) {
            List<String> row = biodImportRows.get(i);

            //row is two past number found here as list is zero indexed, and we removed the header row above
            validatePresentValuesInRow(ingestionHeaderMap, e, row, i+2);

            //validating uniqueness constraint for the pair of subject_id and sample_type
            Optional<String> animalIdOptional = getCellValue(row, ingestionHeaderMap, SUBJECT_LABEL_COLUMN);
            Optional<String> sampleTypeOptional = getCellValue(row, ingestionHeaderMap, SAMPLE_TYPE_COLUMN);
            if (animalIdOptional.isPresent() && sampleTypeOptional.isPresent()) {
                String animalId = animalIdOptional.get();
                String sampleType = sampleTypeOptional.get();
                if (animalSampleTypes.containsKey(animalId)) {
                    if (animalSampleTypes.get(animalId).contains(sampleType)) {
                        //row is two past number found here as list is zero indexed, and we removed the header row above
                        e.addInvalidField("Duplicate pairing", "The pairing of subject id and sample type "
                                + "in row " + (i+2) + " is found together in another row");
                    } else {
                        animalSampleTypes.get(animalId).add(sampleType);
                    }
                } else {
                    animalSampleTypes.put(animalId, new ArrayList<>(Collections.singletonList(sampleType)));
                }

                if (!allowedSampleTypes.contains(sampleType)) {
                    //row is two past number found here as list is zero indexed, and we removed the header row above
                    e.addInvalidField("Sample Type Not Allowed Row " + (i+2), "The sample type in row " +  (i+2) +
                            " does not contain an accepted sample type for this project.");
                }
            }
        }

        if (!e.getMissingFields().isEmpty() || !e.getInvalidFields().isEmpty()) {
            log.error("", e);
            throw e;
        }

        log.debug("Input file is valid");
    }

    private void validatePresentHeaders(Map<String, Integer> ingestionHeaderMap, DataFormatException e) {
        for (String header : requiredColumnsWithTypes.keySet()) {
            if (!ingestionHeaderMap.containsKey(header)) {
                e.addMissingField("Missing column " + header);
            }
        }
    }

    private void validatePresentValuesInRow(Map<String, Integer> ingestionHeaderMap,
                                            DataFormatException e,
                                            List<String> row,
                                            int currentRowNumber) throws DataFormatException {
        for(String requiredElement : requiredColumnsWithTypes.keySet()) {
            if (requiredColumnsWithTypes.get(requiredElement).equals("String")) {
                if (!getCellValue(row, ingestionHeaderMap, requiredElement).isPresent()) {
                    //line break will make list more clear when shown to the user
                    e.addMissingField("\nMissing " + requiredElement + " in row " + currentRowNumber);
                }
            } else {
                if (!getCellValueAsDate(row, ingestionHeaderMap, requiredElement).isPresent()) {
                    //line break will make list more clear when shown to the user
                    e.addMissingField("\nMissing " + requiredElement + " in row " + currentRowNumber);
                }
            }
        }
    }

    private static class DateOptionalTime {
        LocalDate date;
        LocalTime time;
    }

    private class BiodistributionSubjectToSave {
        PixiBiodistributiondataI biodistributionDataToSave;
        XnatSubjectdataI optionalSubjectDataToSave;
    }
}
