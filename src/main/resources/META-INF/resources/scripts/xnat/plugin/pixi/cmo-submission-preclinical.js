console.log('cmo-submission-preclinical.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});
XNAT.plugin.pixi.cmoSubmissionTemplateGenerator = getObject(XNAT.plugin.pixi.cmoSubmissionTemplateGenerator || {});

(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function() {

  XNAT.plugin.pixi.cmoSubmissionTemplateGenerator = cmoSubmissionTemplateGenerator = getObject(XNAT.plugin.pixi.cmoSubmissionTemplateGenerator || {});

    var report = [];

       cmoSubmissionTemplateGenerator.errorHandler = errorHandler = function(e, title, closeAll){
            console.log(e);
            title = (title) ? 'Error Found: '+ title : 'Error';
            closeAll = (closeAll === undefined) ? true : closeAll;
            var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>' : e;
            XNAT.dialog.open({
                width: 450,
                title: title,
                content: errormsg,
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true,
                        action: function(){
                            if (closeAll) {
                                xmodal.closeAll();

                            }
                        }
                    }
                ]
            });
        }


    cmoSubmissionTemplateGenerator.fetchReport = function(projectId) {
                var reportDetailsUrl = XNAT.url.restUrl('xapi/cmo/template/preclinical/project/'+ projectId,{},false,false);
                XNAT.xhr.get({
                    url: reportDetailsUrl,
                    async: false,
                    dataType: 'json',
                    success: function(data) {
                        report = data;
                        console.log(JSON.stringify(report));
                    },
                    error: function(e) {
                        errorHandler(e);
                    }
                });
                return report;
    }

    cmoSubmissionTemplateGenerator.show = function($form) {
                let columnIds = ["model_id", "passage_number",	"engraftment_site",	"treatment",	"modality",	"contrast_sequence_used",	"number_of_images",	"project_name",	"project_url", "description"];
                let labelMap = {
                    model_id: {
                        label: "model_id",
                        checkboxes: false,
                        id: "model_id"
                    },
                    passage_number: {
                        label: "passage_number",
                        checkboxes: false,
                        id: "passage_number"
                    },
                    engraftment_site: {
                        label: "engraftment_site",
                        checkboxes: false,
                        id: "engraftment_site"
                    },
                    treatment: {
                        label: "treatment",
                        checkboxes: false,
                        id: "treatment"
                    },
                    modality: {
                        label: "modality",
                        checkboxes: false,
                        id: "modality"
                    },
                    contrast_sequence_used: {
                        label: "contrast_sequence_used",
                        checkboxes: false,
                        id: "contrast_sequence_used"
                    },
                    number_of_images: {
                        label: "number_of_images",
                        checkboxes: false,
                        id: "number_of_images"
                    },
                    project_name: {
                        label: "project_name",
                        checkboxes: false,
                        id: "project_name"
                    },
                    project_url: {
                        label: "project_url",
                        checkboxes: false,
                        id: "project_url"
                    },
                    description: {
                        label: "description",
                        checkboxes: false,
                        id: "description"
                    }
                };

                var reportTable = XNAT.table({
                    className: 'report-table xnat-table data-table fixed-header clean',
                    style: {
                        width: 'auto'
                    }
                });
                var $dataRows = [];
                var dataRows = [];

                reportTable.thead().tr();
                $.each(columnIds, function(i, c) {
                    reportTable.th('<b>' + labelMap[c].label + '</b>');
                });
                reportTable.tbody({
                    classes: 'table-body'
                });
                $.each(report['preClinicalReportEntryList'], function(i, e) {
                    let sequenceCountsObj = e['preClinicalOtherReportEntry']['sequenceOrTracerCounts'];
                    let sequenceCounts = JSON.parse(JSON.stringify(sequenceCountsObj));
                    reportTable.tr();
                    reportTable.td({
                        classes: columnIds[0],
                    }, e['pdxReportEntry']['sourceId']);
                    reportTable.td({
                        classes: columnIds[1]
                    }, e['pdxReportEntry']['passageNumber']);
                    reportTable.td({
                        classes: columnIds[2]
                    }, e['pdxReportEntry']['engraftmentSite']);
                    reportTable.td({
                        classes: columnIds[3]
                    }, e['preClinicalOtherReportEntry']['treatments']);
                    reportTable.td({
                        classes: columnIds[4]
                    }, e['preClinicalOtherReportEntry']['modality']);
                    Object.keys(sequenceCounts).forEach(function(key) {
                        reportTable.td({
                            classes: columnIds[5]
                        }, key); // Using index as the key
                        reportTable.td({
                            classes: columnIds[6]
                        }, sequenceCounts[key]); // Using the value at the index
                    });
                    reportTable.td({
                        classes: columnIds[7]
                    }, report['projectTitle']);
                    reportTable.td({
                        classes: columnIds[8]
                    }, report['projectUrl']);
                    reportTable.td({
                        classes: columnIds[8]
                    }, report['description']);
                });
                $form.append(reportTable.table);
                cmoSubmissionTemplateGenerator.container = $form;
                cmoSubmissionTemplateGenerator.$table = $(reportTable.table);
   }
   cmoSubmissionTemplateGenerator.init = function(projectId){
     cmoSubmissionTemplateGenerator.fetchReport(projectId);
   }

}));
