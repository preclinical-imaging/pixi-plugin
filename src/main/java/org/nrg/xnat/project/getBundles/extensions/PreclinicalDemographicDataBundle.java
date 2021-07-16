package org.nrg.xnat.project.getBundles.extensions;

import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.nrg.xdat.om.PixiPreclinicaldemographicdata;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.search.CriteriaCollection;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;

import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

public class PreclinicalDemographicDataBundle implements  BaseXnatProjectdata.AddBundlesI{
    static Logger logger = Logger.getLogger("org.nrg.xnatx.plugins.pixi");

//    @Override
//    public void execute(UserI user, List<XdatStoredSearch> storedSearches, BaseXnatProjectdata proj) throws Exception {
//        XFTTable pdd = XFTTable.Execute("select label from xnat_subjectdata RIGHT JOIN pixi_preclinicaldemographicdata ON demographics_xnat_abstractdemographicdata_id=xnat_abstractdemographicdata_id WHERE project='"+ proj.getId() + "';", null, null);
//
//        if (pdd.getNumRows() == 0) {
//            // Do not add stored search if no preclinical subjects exist for this project
//            return;
//        }
//
//        final DisplaySearch search = new DisplaySearch();
//        search.setRootElement(XnatSubjectdata.SCHEMA_ELEMENT_NAME);
//
//
//        final CriteriaCollection cc = new CriteriaCollection("AND");
//        cc.addClause(XnatSubjectdata.SCHEMA_ELEMENT_NAME + ".PROJECT", "=", proj.getId());
//        cc.addClause(PixiPreclinicaldemographicdata.SCHEMA_ELEMENT_NAME + ".SPECIES", "IS NOT NULL");
//        search.addCriteria(cc);
//
//        final String id = proj.getId() + "_" + PixiPreclinicaldemographicdata.SCHEMA_ELEMENT_NAME;
//        final XdatStoredSearch storedSearch = search.convertToStoredSearch(id);
//
//        // Remove default Subject Fields
//        storedSearch.removeSearchField(6);
//        storedSearch.removeSearchField(5);
//        storedSearch.removeSearchField(4);
//        storedSearch.removeSearchField(3);
//        storedSearch.removeSearchField(2);
//        storedSearch.removeSearchField(1);
//        storedSearch.removeSearchField(0);
//
//        storedSearch.setId(id);
//        storedSearch.setBriefDescription("Preclinical Subjects");
//        storedSearch.setSecure(false);
//        storedSearch.setAllowDiffColumns(false);
//        storedSearch.setTag(proj.getId());
//
//        // Subject Label
//        XdatSearchField searchField = new XdatSearchField(user);
//        searchField.setElementName(XnatSubjectdata.SCHEMA_ELEMENT_NAME);
//        searchField.setFieldId("SUBJECT_LABEL");
//        searchField.setHeader("Subject");
//        searchField.setType("string");
//        searchField.setSequence(0);
//        storedSearch.setSearchField(searchField);
//
//        // Species
//        searchField = new XdatSearchField(user);
//        searchField.setElementName(PixiPreclinicaldemographicdata.SCHEMA_ELEMENT_NAME);
//        searchField.setFieldId("SPECIES");
//        searchField.setHeader("Species");
//        searchField.setType("string");
//        searchField.setSequence(1);
//        storedSearch.setSearchField(searchField);
//
//        // Strain
//        searchField = new XdatSearchField(user);
//        searchField.setElementName(PixiPreclinicaldemographicdata.SCHEMA_ELEMENT_NAME);
//        searchField.setFieldId("STRAIN");
//        searchField.setHeader("Strain");
//        searchField.setType("string");
//        searchField.setSequence(2);
//        storedSearch.setSearchField(searchField);
//
//        SaveItemHelper.authorizedSave(storedSearch, user, true, true,
//                EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.TYPE.PROCESS, "Registered Preclinical View")
//        );
//
//        storedSearches.add(storedSearch);
//    }

//    @Override
//    public void execute(UserI user, List<XdatStoredSearch> storedSearches, BaseXnatProjectdata proj) throws Exception {
//        XFTTable pdd = XFTTable.Execute("select label from xnat_subjectdata RIGHT JOIN pixi_preclinicaldemographicdata ON demographics_xnat_abstractdemographicdata_id=xnat_abstractdemographicdata_id WHERE project='"+ proj.getId() + "';", null, null);
//
//        if (pdd.getNumRows() == 0) {
//            // Do not add stored search if no preclinical subjects exist for this project
//            return;
//        }
//
//        final DisplaySearch search = new DisplaySearch();
//        search.setRootElement(PixiPreclinicaldemographicdata.SCHEMA_ELEMENT_NAME);
//        search.setDisplay("project_bundle");
//
//        final CriteriaCollection cc = new CriteriaCollection("AND");
//        cc.addClause(XnatSubjectdata.SCHEMA_ELEMENT_NAME + ".PROJECT", "=", proj.getId());
//        search.addCriteria(cc);
//
//        final String id = proj.getId() + "_" + PixiPreclinicaldemographicdata.SCHEMA_ELEMENT_NAME;
//        final XdatStoredSearch storedSearch = search.convertToStoredSearch(id);
//
//        storedSearch.setId(id);
//        storedSearch.setBriefDescription("Preclinical Subjects");
//        storedSearch.setSecure(false);
//        storedSearch.setAllowDiffColumns(false);
//        storedSearch.setTag(proj.getId());
//
//        // Subject Label
//        XdatSearchField searchField = new XdatSearchField(user);
//        searchField.setElementName(XnatSubjectdata.SCHEMA_ELEMENT_NAME);
//        searchField.setFieldId("SUBJECT_LABEL");
//        searchField.setHeader("Subject");
//        searchField.setType("string");
//        searchField.setSequence(0);
//        storedSearch.setSearchField(searchField);
//
//
//        SaveItemHelper.authorizedSave(storedSearch, user, true, true,
//                EventUtils.newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.TYPE.PROCESS, "Registered Preclinical View")
//        );
//
//        storedSearches.add(storedSearch);
//    }


    @Override
    public void execute(UserI user, List<XdatStoredSearch> storedSearches, BaseXnatProjectdata proj) throws Exception {
        XFTTable pdd = XFTTable.Execute("select label from xnat_subjectdata RIGHT JOIN pixi_preclinicaldemographicdata ON demographics_xnat_abstractdemographicdata_id=xnat_abstractdemographicdata_id WHERE project='"+ proj.getId() + "';", null, null);

        if (pdd.getNumRows() == 0) {
            // Do not add stored search if no preclinical subjects exist for this project
            return;
        }

        final DisplaySearch search = new DisplaySearch();
        search.setRootElement(PixiPreclinicaldemographicdata.SCHEMA_ELEMENT_NAME);

        // @ -> default search
        final String id = "@" + PixiPreclinicaldemographicdata.SCHEMA_ELEMENT_NAME;
        final XdatStoredSearch storedSearch = search.convertToStoredSearch(id);

        storedSearch.setId(id);
        storedSearch.setTag(proj.getId());
        storedSearch.setBriefDescription("Preclinical Subjects");

        storedSearches.add(storedSearch);
    }
}
