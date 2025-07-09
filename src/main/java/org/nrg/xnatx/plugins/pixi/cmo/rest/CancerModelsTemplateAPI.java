package org.nrg.xnatx.plugins.pixi.cmo.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.PixiPdxdata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatPetmrsessiondata;
import org.nrg.xdat.om.XnatPetsessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.cmo.CMOUtils;
import org.nrg.xnatx.plugins.pixi.cmo.model.PdxPojo;
import org.nrg.xnatx.plugins.pixi.cmo.model.PdxReportEntry;
import org.nrg.xnatx.plugins.pixi.cmo.model.PreClinicalImagingReportEntry;
import org.nrg.xnatx.plugins.pixi.cmo.model.PreClinicalReport;
import org.nrg.xnatx.plugins.pixi.cmo.model.PreClinicalReportEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.nrg.xdat.security.helpers.AccessLevel.Edit;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@XapiRestController
@RequestMapping(value = "/cmo/template")
@Api("API for Generating Templates for submission to CancerModels.org")
public class CancerModelsTemplateAPI extends AbstractXapiRestController {

    @Autowired
    public CancerModelsTemplateAPI(final UserManagementServiceI userManagementService,
                                   final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    @ApiResponses({@ApiResponse(code = 200, message = "Template successfully generated"),
            @ApiResponse(code = 400, message = "Invalid value passed."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/preclinical/project/{project}", method = GET, restrictTo = Edit, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get Preclinical Template")
    public PreClinicalReport getPreclinicaltemplate(@Project @PathVariable final String project) throws NotFoundException {
        if (project == null) {
            throw new NotFoundException("No value passed");
        }
        UserI user = getSessionUser();
        final XnatProjectdata projectdata = XnatProjectdata.getXnatProjectdatasById(project,user,false);
        if (projectdata == null) {
            throw new NotFoundException("Invalid value passed");
        }
        List<XnatExperimentdata> allProjectExperiments = getAllExperiments(user, projectdata);
        final String hotelSubjectIdInProject = getHotelSubjectIdinProject(projectdata, user);

        List<PreClinicalReportEntry> entries =  extractPreClinicalImaging(projectdata, allProjectExperiments, hotelSubjectIdInProject, groupPdxByPassageNumber(getPdxExperiments(allProjectExperiments, hotelSubjectIdInProject)));
        PreClinicalReport template = new PreClinicalReport();
        template.setPreClinicalReportEntryList(entries);
        template.setProjectId(project);
        template.setProjectTitle(projectdata.getId().replaceAll(CMOUtils.REGULAR_EXP, "_"));
        template.setProjectUrl(XDAT.getSiteUrl() + "/data/projects/" + project  + "?format=html");
        template.setDescription(projectdata.getDescription());
        return template;
    }


    @Nullable
    private String getHotelSubjectIdinProject(final XnatProjectdata projectdata, final UserI user) {
        final CriteriaCollection cc   = new CriteriaCollection("AND");
        cc.addClause("xnat:subjectData/label", "Hotel");
        cc.addClause("xnat:subjectData/project", projectdata.getId());
        final List<XnatSubjectdata> subjectdatas =  XnatSubjectdata.getXnatSubjectdatasByField(cc, user, false);
        return (subjectdatas == null || subjectdatas.size() == 0) ? null : subjectdatas.get(0).getId();
    }

    private List<XnatExperimentdata> getAllExperiments(final UserI user, final XnatProjectdata projectdata) {
        //projectData.getExperimentsByXSIType does not refresh the cache fetched experiments
        final CriteriaCollection cc   = new CriteriaCollection("OR");
        cc.addClause("xnat:experimentData/project", projectdata.getId());
        cc.addClause("xnat:experimentData/sharing/share/project", projectdata.getId());
        final List<XnatExperimentdata> experiments =  XnatExperimentdata.getXnatExperimentdatasByField(cc, user, false);
        log.debug("Total experiments found including ones for Hotel subject: " + experiments.size());
        return experiments;
    }

    private List<XnatExperimentdata> getPdxExperiments(final List<XnatExperimentdata> experiments, final String hotelSubjectIdInProject) {
        return experiments.stream()
                .filter(experiment -> experiment != null && StringUtils.equalsIgnoreCase(PixiPdxdata.SCHEMA_ELEMENT_NAME, experiment.getXSIType()))
                .filter(experiment ->  !((PixiPdxdata)experiment).getSubjectId().equals(hotelSubjectIdInProject)).collect(Collectors.toCollection(ArrayList::new));
    }

    private List<XnatImagesessiondata> getImagingExperiments(final List<XnatExperimentdata> experiments, final String hotelSubjectIdInProject) {
        return experiments.stream()
                .filter(experiment -> experiment != null && (experiment instanceof XnatImagesessiondata))
                .filter(experiment ->  !((XnatImagesessiondata)experiment).getSubjectId().equals(hotelSubjectIdInProject))
                .map(exp -> (XnatImagesessiondata)exp)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Hashtable<PdxPojo, PdxReportEntry> groupPdxByPassageNumber(final List pdxExperiments) {
        Hashtable<PdxPojo, PdxReportEntry> pdxDataHash = new Hashtable<>();
        for (Object pdx : pdxExperiments) {
            PixiPdxdata pdxdata = (PixiPdxdata) pdx;
            final String passage = pdxdata.getPassage() == null ? CMOUtils.NOT_PROVIDED : pdxdata.getPassage();
            PdxPojo pdxPojo = new PdxPojo(passage, pdxdata.getSourceid(),
                    pdxdata.getInjectionsite() == null ? CMOUtils.NOT_PROVIDED : pdxdata.getInjectionsite());
            if (!pdxDataHash.containsKey(pdxPojo)) {
                PdxReportEntry pdxReportEntry = new PdxReportEntry();
                pdxReportEntry.addSubjectId(pdxdata.getSubjectId());
                pdxReportEntry.setPdx(pdxPojo);
                pdxDataHash.put(pdxPojo, pdxReportEntry);
            } else {
                PdxReportEntry pdxReportEntry = pdxDataHash.get(pdxPojo);
                pdxReportEntry.addSubjectId(pdxdata.getSubjectId());
            }
        }
        return pdxDataHash;
    }

    private List<PreClinicalReportEntry> extractPreClinicalImaging(final XnatProjectdata projectdata, final List<XnatExperimentdata> allProjectExperiments, final String hotelSubjectId, final Hashtable<PdxPojo, PdxReportEntry> pdxGroupByPassageNumber) {
        List<PreClinicalReportEntry> preclinicalReport = new ArrayList();
        Collection<PdxReportEntry> pdxReportEntries = pdxGroupByPassageNumber.values();
        List<XnatImagesessiondata> imagingExperiments = getImagingExperiments(allProjectExperiments, hotelSubjectId);
        for(PdxReportEntry pdxReportEntry: pdxReportEntries) {
            Hashtable<String, PreClinicalImagingReportEntry> imagingEntry = getImagingSessionsByModality(projectdata, imagingExperiments, pdxReportEntry.getSubjectIds());
            for (String modality : imagingEntry.keySet()) {
                PreClinicalImagingReportEntry preClinicalImagingReportEntryOfAllSequences = imagingEntry.get(modality);
                Set<String> sequenceOfTracerUsed = preClinicalImagingReportEntryOfAllSequences.getSequenceOrTracerCounts().keySet();
                if (sequenceOfTracerUsed.size() == 1) {
                    preclinicalReport.add(new PreClinicalReportEntry(pdxReportEntry, preClinicalImagingReportEntryOfAllSequences));
                } else {
                    for (String seq : sequenceOfTracerUsed) {
                        PreClinicalImagingReportEntry single = new PreClinicalImagingReportEntry();
                        single.setTreatments(preClinicalImagingReportEntryOfAllSequences.getTreatments());
                        single.setModality(preClinicalImagingReportEntryOfAllSequences.getModality());
                        Long countOfImages = preClinicalImagingReportEntryOfAllSequences.getSequenceOrTracerCounts().get(seq);
                        Hashtable<String, Long> singleCounts = new Hashtable();
                        singleCounts.put(seq, countOfImages);
                        single.setSequenceOrTracerCounts(singleCounts);
                        preclinicalReport.add(new PreClinicalReportEntry(pdxReportEntry, single));
                    }
                }
            }
        }
        return preclinicalReport;
    }

    private List<XnatImagesessiondata> filterImagingExperimentsForGivenSubjects(final List<XnatImagesessiondata> imagingExperiments, final List<String> subjectIds) {
        return imagingExperiments.stream().filter(exp -> subjectIds.contains(exp.getSubjectId())).collect(Collectors.toCollection(ArrayList::new));
    }

    private Hashtable<String, PreClinicalImagingReportEntry> getImagingSessionsByModality(final XnatProjectdata projectdata, final List<XnatImagesessiondata> allImagingExperiments, final List<String> subjectIds) {
        Hashtable<String, PreClinicalImagingReportEntry> experimentsByModality = new Hashtable<>();
        final String treatments = getTreatments(subjectIds);
        final List<XnatImagesessiondata> subjectSpecificImagingExperiments =  filterImagingExperimentsForGivenSubjects(allImagingExperiments, subjectIds);
        for (XnatExperimentdata exp : subjectSpecificImagingExperiments) {
            final XnatImagesessiondata imagesessiondata = ((XnatImagesessiondata)exp);
            List<XnatImagescandataI> scans = imagesessiondata.getScans_scan();
            for (XnatImagescandataI sc : scans) {
                PreClinicalImagingReportEntry preClinicalImagingReportEntry = null;
                final String type = sc.getType();
                final String modality = sc.getModality();
                if (modality != null) {
                    if (experimentsByModality.containsKey(modality)) {
                        preClinicalImagingReportEntry = experimentsByModality.get(modality);
                        setContrastSequenceUsed(preClinicalImagingReportEntry, type, exp);
                        preClinicalImagingReportEntry.setTreatments(treatments);
                    } else {
                        preClinicalImagingReportEntry = new PreClinicalImagingReportEntry();
                        preClinicalImagingReportEntry.setModality(modality);
                        preClinicalImagingReportEntry.setTreatments(treatments);
                        setContrastSequenceUsed(preClinicalImagingReportEntry, type, exp);
                        experimentsByModality.put(modality, preClinicalImagingReportEntry);
                    }
                }
            }
        }
        return experimentsByModality;
    }

    private void setContrastSequenceUsed(PreClinicalImagingReportEntry preClinicalImagingReportEntry, String type, XnatExperimentdata exp) {
        if ((exp instanceof XnatPetsessiondata) || exp instanceof XnatPetmrsessiondata) {
            try {
                preClinicalImagingReportEntry.addContrastSequenceUsed(((XnatPetsessiondata) exp).getTracer_name());
            } catch (ClassCastException cce) {
                preClinicalImagingReportEntry.addContrastSequenceUsed(((XnatPetmrsessiondata) exp).getTracer_name());
            }
        } else {
            preClinicalImagingReportEntry.addContrastSequenceUsed(type);
        }
    }


    private String getTreatments(final List<String> subjectIds) {
        String subjects = subjectIds.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(","));
        final String DRUG_NAME_QUERY = "SELECT STRING_AGG(drug, '+') AS drugnames from " +
                " (select distinct drug from pixi_drugtherapydata dt " +
                " left join xnat_subjectassessordata sa on dt.id = sa.id " +
                " left join xnat_subjectdata s on sa.subject_id = s.id " +
                " where s.id in (" + subjects +")) as drugs;";
        PoolDBUtils con = new PoolDBUtils();
        String drugNames = "Not provided";
        try {
            final XFTTable table = con.executeSelectPS(DRUG_NAME_QUERY);
            ArrayList<Object[]> rows  = table.rows();
            drugNames = (String)rows.get(0)[0];
        } catch (Exception e) {
            log.error("Could not get drug  names", e);
        } finally {
            con.closeConnection();
        }
        return (drugNames == null ? CMOUtils.NOT_PROVIDED : drugNames);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {NotFoundException.class})
    public String handleNotFound(final Exception e) {
        return e.getMessage();
    }


}
