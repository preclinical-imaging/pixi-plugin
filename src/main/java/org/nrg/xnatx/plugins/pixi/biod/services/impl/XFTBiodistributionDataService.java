package org.nrg.xnatx.plugins.pixi.biod.services.impl;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
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
    public PixiBiodistributiondataI createOrUpdate(UserI user, PixiBiodistributiondataI biodistributionData, String dataOverlapHandling) throws Exception {
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
    public List<PixiBiodistributiondataI> createOrUpdate(UserI user, List<PixiBiodistributiondataI> biodistributionDatas, String dataOverlapHandling) throws Exception {
        log.debug("User {} is attempting to create/update biodistribution data experiments in project {}",
                  user.getUsername(), biodistributionDatas.get(0).getProject());

        List<PixiBiodistributiondataI> createdExperiments = new ArrayList<>();

        for (PixiBiodistributiondataI biodistributionData : biodistributionDatas) {
            createdExperiments.add(createOrUpdate(user, biodistributionData, dataOverlapHandling));
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
    public List<PixiBiodistributiondataI> fromExcel(UserI user, String project, String userCachePath) throws Exception {
        log.debug("User {} is attempting to create biodistribution data experiment in project {} from cache path {}",
                  user.getUsername(), project, userCachePath);

        File excelFile = userDataCache.getUserDataCacheFile(user, Paths.get(userCachePath));
        if (!excelFile.exists()) {
            throw new DataFormatException("Invalid file path: " + userCachePath);
        }

        return fromExcel(user, project, excelFile);
    }

    @Override
    public List<PixiBiodistributiondataI> fromExcel(UserI user, String project, File file) throws Exception {
        log.debug("User {} is attempting to create biodistribution data experiment in project {} from file {}",
                  user.getUsername(), project, file.getAbsolutePath());

        List<PixiBiodistributiondataI> biodExperiments = new ArrayList<>();

        // Read Excel file with Apache POI
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().endsWith(".xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {

            // Expecting sheets: pixi_injection, pixi_biod

            // Get the injection and biodistribution sheets
            Sheet injectionSheet = workbook.getSheet("pixi_injection");
            Sheet biodSheet = workbook.getSheet("pixi_biod");

            if (injectionSheet == null || biodSheet == null) {
                throw new DataFormatException("Missing required sheets: pixi_injection, pixi_biod");
            }

            validateSheets(injectionSheet, biodSheet);

            Map<String, Integer> injectionHeaderMap = getHeaderMap(injectionSheet.getRow(0));
            Map<String, Integer> biodHeaderMap = getHeaderMap(biodSheet.getRow(0));

            // Process injection sheet
            for (Row row : injectionSheet) {
                if (row.getRowNum() == 0) {
                    // Skip header row
                    continue;
                }

                // Create a new biodistribution data object for each row
                PixiBiodistributiondataI biodistributionData = new PixiBiodistributiondata();
                biodistributionData.setProject(project);

                // Process the injection data first. Make sure we add it to the biodistribution data object
                PixiBiodinjectiondataI injectionData = new PixiBiodinjectiondata();
                biodistributionData.setInjectionData(injectionData);

                String subjectLabel = getCellValue(row, injectionHeaderMap, SUBJECT_LABEL_COLUMN).orElseThrow(
                        () -> new DataFormatException("Missing required field: " + SUBJECT_LABEL_COLUMN + " in row " + row.getRowNum())
                );
                biodistributionData.setSubjectId(subjectLabel);
                biodistributionData.setLabel(subjectLabel + "_Biod");

                Optional<Date> experimentDate = getCellValueAsDate(row, injectionHeaderMap, "experiment_datetime");
                if (experimentDate.isPresent()) {
                    LocalDateTime experimentDateTime = getDateTimeFromDate(experimentDate.get());
                    biodistributionData.setDate(experimentDateTime.toLocalDate());
                    biodistributionData.setTime(experimentDateTime.toLocalTime());
                }

                getCellValue(row, injectionHeaderMap, "acquisition_site").ifPresent(biodistributionData::setAcquisitionSite);
                getCellValue(row, injectionHeaderMap, "note").ifPresent(biodistributionData::setNote);
                getCellValue(row, injectionHeaderMap, "technician").ifPresent(biodistributionData::setTechnician);

                Optional<Date> animalSacrificeDate = getCellValueAsDate(row, injectionHeaderMap, "animal_sacrifice_datetime");
                if (animalSacrificeDate.isPresent()) {
                    LocalDateTime animalSacrificeDateTime = getDateTimeFromDate(animalSacrificeDate.get());
                    biodistributionData.setAnimalSacrificeDate(animalSacrificeDateTime.toLocalDate());
                    biodistributionData.setAnimalSacrificeTime(animalSacrificeDateTime.toLocalTime());
                }


                // Handle animal weight and unit in separate columns or combined
                Optional<Double> animalWeight = getCellValueAsDouble(row, injectionHeaderMap, "animal_weight");
                Optional<String> animalWeightUnit = getCellValue(row, injectionHeaderMap, "animal_weight_unit");

                animalWeight.ifPresent(biodistributionData::setAnimalWeight);
                animalWeightUnit.ifPresent(biodistributionData::setAnimalWeightUnit);

                if (animalWeight.isPresent() && !animalWeightUnit.isPresent()) {
                    // Assume grams if unit is not specified
                    biodistributionData.setAnimalWeightUnit("g");
                }

                Optional<Double> animalWeightGrams = getCellValueAsDouble(row, injectionHeaderMap, "animal_weight_g");

                if (animalWeightGrams.isPresent()) {
                    biodistributionData.setAnimalWeight(animalWeightGrams.get());
                    biodistributionData.setAnimalWeightUnit("g");
                }

                getCellValue(row, injectionHeaderMap, "tracer").ifPresent(injectionData::setTracer);
                getCellValue(row, injectionHeaderMap, "isotope").ifPresent(injectionData::setIsotope);
                getCellValue(row, injectionHeaderMap, "diluent").ifPresent(injectionData::setDiluent);

                Optional<Double> injectedDose = getCellValueAsDouble(row, injectionHeaderMap, "injected_dose");
                Optional<String> injectedDoseUnit = getCellValue(row, injectionHeaderMap, "injected_dose_unit");

                injectedDose.ifPresent(injectionData::setInjectedDose);
                injectedDoseUnit.ifPresent(injectionData::setInjectedDoseUnit);

                if (injectedDose.isPresent() && !injectedDoseUnit.isPresent()) {
                    injectionData.setInjectedDose(injectedDose.get());
                    injectionData.setInjectedDoseUnit("µL");
                }

                Optional<Double> injectionVolume = getCellValueAsDouble(row, injectionHeaderMap, "injection_volume");
                Optional<String> injectionVolumeUnit = getCellValue(row, injectionHeaderMap, "injection_volume_unit");

                injectionVolume.ifPresent(injectionData::setInjectionVolume);
                injectionVolumeUnit.ifPresent(injectionData::setInjectionVolumeUnit);

                if (injectionVolume.isPresent() && !injectionVolumeUnit.isPresent()) {
                    injectionData.setInjectionVolume(injectionVolume.get());
                    injectionData.setInjectionVolumeUnit("µL");
                }

                getCellValue(row, injectionHeaderMap, "injection_total_counts").ifPresent(injectionData::setInjectionTotalCounts);
                getCellValue(row, injectionHeaderMap, "injection_route").ifPresent(injectionData::setInjectionRoute);
                getCellValue(row, injectionHeaderMap, "injection_site").ifPresent(injectionData::setInjectionSite);

                Optional<Date> injectionDate = getCellValueAsDate(row, injectionHeaderMap, "injection_datetime");
                if(injectionDate.isPresent()) {
                    LocalDateTime injectionDateTime = getDateTimeFromDate(injectionDate.get());
                    injectionData.setInjectionDate(injectionDateTime.toLocalDate());
                    injectionData.setInjectionTime(injectionDateTime.toLocalTime());
                }

                // Anesthesia is handled in a separate object, reused with the hotel splitter
                Optional<String> anesthesia = getCellValue(row, injectionHeaderMap, "anesthesia");
                Optional<String> anesthesiaRoute = getCellValue(row, injectionHeaderMap, "anesthesia_route");

                if (anesthesia.isPresent() || anesthesiaRoute.isPresent()) {
                    PixiAnesthesiadataI anesthesiaData = new PixiAnesthesiadata();
                    anesthesia.ifPresent(anesthesiaData::setAnesthesia);
                    anesthesiaRoute.ifPresent(anesthesiaData::setRouteofadministration);
                    biodistributionData.setAnesthesiaAdministration(anesthesiaData);
                }

                biodExperiments.add(biodistributionData);
            }

            // Process the biodistribution sheet
            for (Row row : biodSheet) {
                if (row.getRowNum() == 0) {
                    // Skip header row
                    continue;
                }

                if (isRowEmpty(row)) {
                    continue;
                }

                PixiBiodsampleuptakedataI biodistributionData = new PixiBiodsampleuptakedata();

                // Get the subject ID. We will need to add it to the appropriate biodistribution data object
                String subjectLabel = getCellValue(row, biodHeaderMap, SUBJECT_LABEL_COLUMN).orElseThrow(
                        () -> new DataFormatException("Missing required field: " + SUBJECT_LABEL_COLUMN + " in row " + row.getRowNum())
                );

                Optional<PixiBiodistributiondataI> exp = biodExperiments.stream().filter(biod -> biod.getSubjectId().equals(subjectLabel)).findFirst();

                if (exp.isPresent()) {
                    exp.get().addSampleUptakeData(biodistributionData);
                } else {
                    throw new DataFormatException("No matching injection data found for subject ID: " + subjectLabel);
                }

                getCellValue(row, biodHeaderMap, "sample_type").ifPresent(biodistributionData::setSampleType);

                Optional<Double> sampleWeight = getCellValueAsDouble(row, biodHeaderMap, "sample_weight");
                Optional<String> sampleWeightUnit = getCellValue(row, biodHeaderMap, "sample_weight_unit");

                sampleWeight.ifPresent(biodistributionData::setSampleWeight);
                sampleWeightUnit.ifPresent(biodistributionData::setSampleWeightUnit);

                if (sampleWeight.isPresent() && !sampleWeightUnit.isPresent()) {
                    biodistributionData.setSampleWeight(sampleWeight.get());
                    biodistributionData.setSampleWeightUnit("g");
                }

                Optional<Double> sampleWeightGrams = getCellValueAsDouble(row, biodHeaderMap, "sample_weight_g");

                if (sampleWeightGrams.isPresent()) {
                    biodistributionData.setSampleWeight(sampleWeightGrams.get());
                    biodistributionData.setSampleWeightUnit("g");
                }

                Optional<Date> measurementDate = getCellValueAsDate(row, biodHeaderMap, "measurement_datetime");
                if(measurementDate.isPresent()) {
                    LocalDateTime measurementDateTime = getDateTimeFromDate(measurementDate.get());
                    biodistributionData.setMeasurementDate(measurementDateTime.toLocalDate());
                    biodistributionData.setMeasurementTime(measurementDateTime.toLocalTime());
                }

                Optional<Double> timepointValue = getCellValueAsDouble(row, biodHeaderMap, "timepoint_value");
                Optional<String> timepointUnit = getCellValue(row, biodHeaderMap, "timepoint_unit");

                // TODO HOW DO WE WANT TO HAVE UNIFORM TIME POINTS UNITS (ie d vs day vs days, h vs hr vs hour vs hours, ...)
                timepointValue.ifPresent(biodistributionData::setTimepointValue);
                timepointUnit.ifPresent(biodistributionData::setTimepointUnit);

                getCellValue(row, biodHeaderMap, "%_id_g").ifPresent(biodistributionData::setPercentInjectedDosePerGram);
                getCellValue(row, biodHeaderMap, "%_id_organ").ifPresent(biodistributionData::setPercentInjectedDosePerOrgan);
                getCellValue(row, biodHeaderMap, "decay_corrected_cpm").ifPresent(biodistributionData::setDecayCorrectedCpm);
            }
        } catch (NotOLE2FileException e) {
            log.error("Error reading Excel file: {}", e.getMessage());
            throw new DataFormatException("Invalid Excel file format", e);
        }

        XnatProjectdata projectData = XnatProjectdata.getProjectByIDorAlias(project, user, false);
        Path projectResourcePath = Paths.get(siteConfigPreferences.getArchivePath()).getFileName().resolve(Paths.get("projects")).resolve(projectData.getArchiveDirectoryName());
        String resourcesPathWithLeadingElement = Paths.get(siteConfigPreferences.getArchivePath()).getRoot().toString() + projectResourcePath.toString();
        defaultCatalogService.insertResources(user, resourcesPathWithLeadingElement, file, "BioDExcelFiles", "", "", "");
        return biodExperiments;
    }

    private Map<String, Integer> getHeaderMap(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            headerMap.put(cell.getStringCellValue(), cell.getColumnIndex());
        }
        return headerMap;
    }

    private Optional<String> getCellValue(Row row, Map<String, Integer> headerMap, String headerName) {
        Integer cellIndex = headerMap.get(headerName);
        if (cellIndex == null) {
            return Optional.empty();
        }
        Cell cell = row.getCell(cellIndex);
        return cell != null ? Optional.of(cell.toString()) : Optional.empty();
    }

    private Optional<Double> getCellValueAsDouble(Row row, Map<String, Integer> headerMap, String headerName) {
        Integer cellIndex = headerMap.get(headerName);
        if (cellIndex == null) {
            return Optional.empty();
        }
        Cell cell = row.getCell(cellIndex);
        return cell != null ? Optional.of(cell.getNumericCellValue()) : Optional.empty();
    }

    private Optional<Date> getCellValueAsDate(Row row, Map<String, Integer> headerMap, String headerName) {
        Integer cellIndex = headerMap.get(headerName);
        if (cellIndex == null) {
            return Optional.empty();
        }
        Cell cell = row.getCell(cellIndex);
        return cell != null ? Optional.ofNullable(cell.getDateCellValue()) : Optional.empty();
    }

    private LocalDateTime getDateTimeFromDate(Date date) {
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private boolean isRowEmpty(Row row){
        Iterable<Cell> iterable = row::cellIterator;
        return StreamSupport.stream(iterable.spliterator(), false).allMatch(cell -> cell.getCellType().toString().equals("BLANK"));
    }

    protected void validateSheets(@NotNull Sheet injectionSheet, @NotNull Sheet biodSheet) throws DataFormatException {
        log.debug("Validating injection and biodistribution sheets");

        validateInjectionSheet(injectionSheet);
        validateBiodSheet(biodSheet);
    }

    protected void validateInjectionSheet(@NotNull Sheet injectionSheet) throws DataFormatException {
        log.debug("Validating injection sheet");

        DataFormatException e = new DataFormatException("There is a problem with the input injection sheet: ");
        boolean isValid = true;

        Row headerRow = injectionSheet.getRow(0);
        Map<String, Integer> headerMap = getHeaderMap(headerRow);

        if (!headerMap.containsKey(SUBJECT_LABEL_COLUMN)) {
            e.addMissingField(SUBJECT_LABEL_COLUMN);
            isValid = false;
        }

        // Validate that each row has a unique subject_id. Only one injection per animal is allowed.
        Set<String> animalIds = new HashSet<>();
        for (Row row : injectionSheet) {
            if (row.getRowNum() == 0) {
                // Skip header row
                continue;
            }

            Optional<String> animalId = getCellValue(row, headerMap, SUBJECT_LABEL_COLUMN);

            if (!animalId.isPresent()) {
                e.addInvalidField(SUBJECT_LABEL_COLUMN, "Missing " + SUBJECT_LABEL_COLUMN + " in row " + row.getRowNum());
                isValid = false;
            } else if (animalIds.contains(animalId.get())) {
                e.addInvalidField(SUBJECT_LABEL_COLUMN, "Duplicate " + SUBJECT_LABEL_COLUMN + " " + animalId.get() + " in row " + row.getRowNum());
                isValid = false;
            } else {
                animalIds.add(animalId.get());
            }
        }

        if (!isValid) {
            log.error("", e);
            throw e;
        }

        log.debug("Injection sheet is valid");
    }

    protected void validateBiodSheet(@NotNull Sheet biodSheet) throws DataFormatException {
        log.debug("Validating biodistribution sheet");

        DataFormatException e = new DataFormatException("There is a problem with the input biodistribution sheet: ");
        boolean isValid = true;

        // Required columns for biodistribution sheet
        Row headerRow = biodSheet.getRow(0);
        Map<String, Integer> headerMap = getHeaderMap(headerRow);

        if (!headerMap.containsKey(SUBJECT_LABEL_COLUMN)) {
            e.addMissingField(SUBJECT_LABEL_COLUMN);
            isValid = false;
        }

        if (!headerMap.containsKey("sample_type")) {
            e.addMissingField("sample_type");
            isValid = false;
        }

        Map<String, List<String>> animalSampleTypes = new HashMap<>();
        final String SAMPLE_TYPE_COLUMN = "sample_type";
        for (Row row: biodSheet) {
            if (row.getRowNum() == 0) {
                // Skip header row
                continue;
            }
            Optional<String> animalId = getCellValue(row, headerMap, SUBJECT_LABEL_COLUMN);
            Optional<String> sampleType = getCellValue(row, headerMap, SAMPLE_TYPE_COLUMN);
            if (!animalId.isPresent()) {
                e.addInvalidField(SUBJECT_LABEL_COLUMN, "Missing " + SUBJECT_LABEL_COLUMN + " in row " + row.getRowNum());
                isValid = false;
            } else if (!sampleType.isPresent()) {
                e.addInvalidField(SAMPLE_TYPE_COLUMN, "Missing " + SAMPLE_TYPE_COLUMN + " in row " + row.getRowNum());
                isValid = false;
            }else {
                if (animalSampleTypes.containsKey(animalId.get())) {
                    if (animalSampleTypes.get(animalId.get()).contains(sampleType.get())) {
                        e.addInvalidField("Duplicate pairing", "The pairing of " + SUBJECT_LABEL_COLUMN + " and " +
                                SAMPLE_TYPE_COLUMN + " in row " + row.getRowNum() + " is found together in another row.");
                        isValid = false;
                    } else {
                        animalSampleTypes.get(animalId.get()).add(sampleType.get());
                    }
                } else {
                    animalSampleTypes.put(animalId.get(), new ArrayList<>(Collections.singletonList(sampleType.get())));
                }
            }

        }

        if (!isValid) {
            log.error("", e);
            throw e;
        }

        log.debug("Biodistribution sheet is valid");
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
}
