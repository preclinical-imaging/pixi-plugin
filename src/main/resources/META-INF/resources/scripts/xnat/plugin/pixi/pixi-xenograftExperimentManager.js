/*
 *  PIXI Xenograft Experiment Manager
 *
 *  This script depends on functions in pixi-module.js
 */
console.log('pixi-xenograftExperimentManager.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {
    
    XNAT.plugin.pixi.pdxExperimentManager = class PdxExperimentManager extends XNAT.plugin.pixi.abstractExperimentManager {
        
        constructor() {
            super("PIXI Patient-derived Tumor Engraftment Data Manager",
                  "Record and update patient-derived tumor tissue engraftments for small animal subjects within a project.",
                  "<div class='instructions action create whole'><ol>\n" +
                      "  <li>Select the project from the list of available projects in XNAT that contains the subject(s) for which you want to record patient-derived tumor engraftments for.</li>\n" +
                      "  <li>In the Subject ID column, type the subject's ID or select the subject from the drop down menu.</li>\n" +
                      "  <li>In the designated columns, record the date, site, type of the engraftment, as well as the number of cells engrafted.</li>\n" +
                      "  <li>In the appropriate columns, optionally record the PDX passage and PDX passage method.</li>\n" +
                      "  <li>In the notes column, add any additional information or observations regarding the subject.</li>\n" +
                      "  <li>Repeat steps 2-5 for each subject you wish to record a tumor engraftment for. You may record multiple engraftments for a subject.</li>\n" +
                      "  <li>Review all the entries and ensure that they are accurate and consistent.</li>\n" +
                      "  <li>Save the entries by clicking the \"Submit\" button.</li>\n" +
                      "</ol></div>",
                  "<div class='instructions action update whole' style='display: none'><ol>\n" +
                      "  <li>Select the project from the list of available projects that contains the subjects and engraftments you wish to update.</li>\n" +
                      "  <li>In the subject selection box, choose the subject(s) whose engraftments you wish to update.</li>\n" +
                      "  <li>The existing engraftments for the selected subject(s) will be automatically loaded into the table.</li>\n" +
                      "  <li>In the designated columns, update the date, site, type of engraftment, and number of cells engrafted.</li>\n" +
                      "  <li>In the appropriate columns, update the PDX passage and PDX passage method if necessary.</li>\n" +
                      "  <li>In the notes column, add any additional information or observations regarding the subject.</li>\n" +
                      "  <li>Repeat steps 4-6 for each subject/engraftment you wish to update.</li>\n" +
                      "  <li>Note that removing a row from the table will not delete the patient-derived tumor engraftment.</li>" +
                      "  <li>Review all the entries and ensure that they are accurate and consistent.</li>\n" +
                      "  <li>Save the updated engraftments by clicking the \"Submit\" button.</li>\n" +
                      "</ol></div>");
        }
        
        static async create(containerId, project = null, subjects = []) {
            let pdxExperimentManager = new PdxExperimentManager();
            
            let initData = subjects.length ?
                subjects.map(subject => ({
                    'subjectId':        subject,
                    'experimentId':     '',
                    'sourceId':         '',
                    'injectionDate':    '',
                    'injectionSite':    '',
                    'injectionType':    '',
                    'numCellsInjected': '',
                    'passage':          '',
                    'passageMethod':    '',
                    'notes':            ''
                }))
                : new Array(5).fill({
                                        'subjectId':        '',
                                        'experimentId':     '',
                                        'sourceId':         '',
                                        'injectionDate':    '',
                                        'injectionSite':    '',
                                        'injectionType':    '',
                                        'numCellsInjected': '',
                                        'passage':          '',
                                        'passageMethod':    '',
                                        'notes':            ''
                                    });
            
            
            let colHeaders = [
                "Subject ID *",
                "Experiment ID",
                "PDX ID *",
                "Injection Date *",
                "Injection Site",
                "Injection Type",
                "Num Cells Injected",
                "Passage",
                "Passage Method",
                "Notes"
            ];
            
            let colWidths = [150, 100, 150, 100, 150, 150, 150, 150, 115, 130];
    
            const pdxs = await XNAT.plugin.pixi.pdxs.get()
                                   .then(pdxs => pdxs.sort(pixi.compareGenerator('sourceId')))
                                   .then(pdxs => pdxs.map(pdx => pdx['sourceId']));
            
            let columns = [
                {
                    data:         'subjectId',
                    type:         'autocomplete',
                    filter:       true,
                    strict:       true,
                    source:       [],
                    allowEmpty:   true,
                    allowInvalid: true,
                    validator:    (value, callback) => pdxExperimentManager.validateExistingSubjectLabel(pdxExperimentManager.getProjectSelection(), value, callback)
                },
                {
                    data: 'experimentId'
                },
                {
                    data:         'sourceId',
                    type:         'autocomplete',
                    filter:       true,
                    strict:       false,
                    source:       pdxs,
                    allowEmpty:   true,
                    allowInvalid: true,
                    validator:    (value, callback) => value ? callback(true) : callback(false),
                },
                {
                    data:         'injectionDate',
                    type:         'date',
                    allowEmpty:   true,
                    allowInvalid: true,
                    dateFormat:   'MM/DD/YYYY',
                    validator:    (value, callback) => pdxExperimentManager.validateDate(value, callback)
                },
                { data: 'injectionSite' },
                {
                    data:   'injectionType',
                    type:   'autocomplete',
                    filter: true,
                    strict: false,
                    source: ['Subcutaneous', 'Orthotopic']
                },
                {
                    data: 'numCellsInjected',
                    type: 'numeric'
                },
                { data: 'passage' },
                { data: 'passageMethod' },
                { data: 'notes' }
            ];
            
            let hotSettings = {
                data:               initData,
                colHeaders:         colHeaders,
                colWidths:          colWidths,
                columns:            columns,
                rowHeaders:         true,
                manualColumnResize: true,
                contextMenu:        ['row_above', 'row_below', '---------', 'remove_row', '---------', 'undo', 'redo', '---------', 'copy', 'cut'],
                width:              '100%',
                licenseKey:         'non-commercial-and-evaluation',
                minRows:            1,
                hiddenColumns:      {
                    columns: [1],
                    // show UI indicators to mark hidden columns
                    indicators: false
                },
                fixedColumnsStart: 1
            }
            
            return pdxExperimentManager.init(containerId, hotSettings, project, subjects)
                                       .then(() => pdxExperimentManager.hot.addHook('beforeChange', (changes, source) => pdxExperimentManager.changeDate('injectionDate', changes, source)))
                                       .then(() => pdxExperimentManager);
        }
        
        getXsiType() {
            return 'pixi:pdxData';
        }
        
        createActionLabel() {
            return 'New patient-derived tumor engraftments';
        }
        
        updateActionLabel() {
            return 'Update existing engraftments';
        }
        
        async submitRow(row) {
            console.debug(`Submitting pdx experiment for row ${row}`);
            
            let experiment = {
                project:          this.getProjectSelection(),
                subject:          this.getDataAtRowProp(row, 'subjectId'),
                experimentId:     this.getDataAtRowProp(row, 'experimentId'),
                sourceId:         this.getDataAtRowProp(row, 'sourceId'),
                date:             this.getDataAtRowProp(row, 'injectionDate'),
                injectionSite:    this.getDataAtRowProp(row, 'injectionSite'),
                injectionType:    this.getDataAtRowProp(row, 'injectionType'),
                numCellsInjected: this.getDataAtRowProp(row, 'numCellsInjected'),
                passage:          this.getDataAtRowProp(row, 'passage'),
                passageMethod:    this.getDataAtRowProp(row, 'passageMethod'),
                notes:            this.getDataAtRowProp(row, 'notes'),
            }
            
            return XNAT.plugin.pixi.experiments.pdx.createOrUpdate(experiment)
                       .then(id => {
                           return {
                               'subject':      experiment.subject,
                               'experimentId': id,
                               'row':          row,
                               'url':          `/data/projects/${experiment.project}/experiments/${id}?format=html`,
                               'urlText':      `${experiment.subject}`,
                           }
                       })
                       .catch(error => {
                           return {
                               'subject': experiment.subject,
                               'row':     row,
                               'error':   error,
                           }
                       })
        }
        
        async getDataForSubject(subject) {
            const response = await XNAT.plugin.pixi.experiments.get(this.getProjectSelection(),
                                                                    subject,
                                                                    '',
                                                                    this.getXsiType());
            
            // Skip subjects without experiments
            if (response['ResultSet']['Result'].length === 0) {
                return Promise.resolve([]);
            }
            
            let data = []
            
            for (const result of response['ResultSet']['Result']) {
                const response = await XNAT.plugin.pixi.experiments.get('', '', result['ID'], '');
                let data_fields = response['items'][0]['data_fields']
                
                let experiment = {
                    'subjectId':        subject,
                    'experimentId':     result['ID'],
                    'sourceId':         data_fields['sourceId'],
                    'injectionDate':    data_fields['date'] ? data_fields['date'].replace(/(\d{4})-(\d{2})-(\d{2})/, '$2/$3/$1') : '',
                    'injectionSite':    data_fields['injectionSite'],
                    'injectionType':    data_fields['injectionType'],
                    'numCellsInjected': data_fields['numCellsInjected'],
                    'passage':          data_fields['passage'],
                    'passageMethod':    data_fields['passageMethod'],
                    'notes':            data_fields['note']
                }
                
                data.push(experiment);
            }
            
            return Promise.resolve(data);
        }
    }
    
    XNAT.plugin.pixi.cellLineExperimentManager = class CellLineExperimentManager extends XNAT.plugin.pixi.abstractExperimentManager {
        
        constructor() {
            super("PIXI Cell Line Data Manager",
                  "Record and update cell line injections for small animal subjects within a project.",
                  "<div class='instructions action create whole'><ol>\n" +
                      "  <li>Select the project from the list of available projects in XNAT that contains the subject(s) for which you want to record cell line injections for.</li>\n" +
                      "  <li>In the Subject ID column, type the subject's ID or select the subject from the drop down menu.</li>\n" +
                      "  <li>In the designated columns, record the date, site, type of injections, as well as the number of cells injected.</li>\n" +
                      "  <li>In the notes column, add any additional information or observations regarding the subject.</li>\n" +
                      "  <li>Repeat steps 2-4 for each subject you wish to record a cell line injection for. You may record multiple injections for a subject.</li>\n" +
                      "  <li>Review all the entries and ensure that they are accurate and consistent.</li>\n" +
                      "  <li>Save the entries by clicking the \"Submit\" button.</li>\n" +
                      "</ol></div>",
                  "<div class='instructions action update whole' style='display: none'><ol>\n" +
                      "  <li>Select the project from the list of available projects that contains the subjects and engraftments you wish to update.</li>\n" +
                      "  <li>In the subject selection box, choose the subject(s) whose cell line injecitons you wish to update.</li>\n" +
                      "  <li>The existing injections for the selected subject(s) will be automatically loaded into the table.</li>\n" +
                      "  <li>In the designated columns, update the date, site, type of injections, and number of cells injected.</li>\n" +
                      "  <li>In the notes column, add any additional information or observations regarding the subject.</li>\n" +
                      "  <li>Repeat steps 4-5 for each subject/injection you wish to update.</li>\n" +
                      "  <li>Note that removing a row from the table will not delete the cell line injection.</li>" +
                      "  <li>Review all the entries and ensure that they are accurate and consistent.</li>\n" +
                      "  <li>Save the updated injections by clicking the \"Submit\" button.</li>\n" +
                      "</ol></div>");
        }
        
        static async create(containerId, project = null, subjects = []) {
            let cellLineExperimentManager = new CellLineExperimentManager();
            
            let initData = subjects.length ?
                subjects.map(subject => ({
                    'subjectId':        subject,
                    'experimentId':     '',
                    'sourceId':         '',
                    'injectionDate':    '',
                    'injectionSite':    '',
                    'injectionType':    '',
                    'numCellsInjected': '',
                    'notes':            ''
                }))
                : new Array(5).fill({
                                        'subjectId':        '',
                                        'experimentId':     '',
                                        'sourceId':         '',
                                        'injectionDate':    '',
                                        'injectionSite':    '',
                                        'injectionType':    '',
                                        'numCellsInjected': '',
                                        'notes':            ''
                                    });
            
            
            let colHeaders = [
                "Subject ID *",
                "Experiment ID",
                "Cell Line ID *",
                "Injection Date *",
                "Injection Site",
                "Injection Type",
                "Num Cells Injected",
                "Notes"
            ];
            
            let colWidths = [150, 100, 150, 100, 150, 150, 150, 130];
    
            const cellLines = await XNAT.plugin.pixi.cellLines.get()
                                   .then(cellLines => cellLines.sort(pixi.compareGenerator('sourceId')))
                                   .then(cellLines => cellLines.map(cellLine => cellLine['sourceId']));
            
            let columns = [
                {
                    data:         'subjectId',
                    type:         'autocomplete',
                    filter:       true,
                    strict:       true,
                    source:       [],
                    allowEmpty:   true,
                    allowInvalid: true,
                    validator:    (value, callback) => cellLineExperimentManager.validateExistingSubjectLabel(cellLineExperimentManager.getProjectSelection(), value, callback),
                },
                {
                    data: 'experimentId'
                },
                {
                    data:         'sourceId',
                    type:         'autocomplete',
                    filter:       true,
                    strict:       false,
                    source:       cellLines,
                    allowEmpty:   true,
                    allowInvalid: true,
                    validator:    (value, callback) => value ? callback(true) : callback(false),
                },
                {
                    data:         'injectionDate',
                    type:         'date',
                    allowEmpty:   true,
                    allowInvalid: true,
                    dateFormat:   'MM/DD/YYYY',
                    validator:    (value, callback) => cellLineExperimentManager.validateDate(value, callback)
                },
                { data: 'injectionSite' },
                {
                    data:   'injectionType',
                    type:   'autocomplete',
                    filter: true,
                    strict: false,
                    source: ['Subcutaneous', 'Orthotopic']
                },
                {
                    data: 'numCellsInjected',
                    type: 'numeric'
                },
                { data: 'notes' }
            ];
            
            let hotSettings = {
                data:               initData,
                colHeaders:         colHeaders,
                colWidths:          colWidths,
                columns:            columns,
                rowHeaders:         true,
                manualColumnResize: true,
                contextMenu:        ['row_above', 'row_below', '---------', 'remove_row', '---------', 'undo', 'redo', '---------', 'copy', 'cut'],
                width:              '100%',
                licenseKey:         'non-commercial-and-evaluation',
                minRows:            1,
                hiddenColumns:      {
                    columns: [1],
                    // show UI indicators to mark hidden columns
                    indicators: false
                },
                fixedColumnsStart: 1
            }
            
            return cellLineExperimentManager.init(containerId, hotSettings, project, subjects)
                                            .then(() => cellLineExperimentManager.hot.addHook('beforeChange', (changes, source) => cellLineExperimentManager.changeDate('injectionDate', changes, source)))
                                            .then(() => cellLineExperimentManager);
        }
        
        getXsiType() {
            return 'pixi:cellLineData';
        }
        
        createActionLabel() {
            return 'New cell line injections';
        }
        
        updateActionLabel() {
            return 'Update existing cell line injections';
        }
        
        async submitRow(row) {
            console.debug(`Submitting cell line experiment for row ${row}`);
            
            let experiment = {
                project:          this.getProjectSelection(),
                subject:          this.getDataAtRowProp(row, 'subjectId'),
                experimentId:     this.getDataAtRowProp(row, 'experimentId'),
                sourceId:         this.getDataAtRowProp(row, 'sourceId'),
                date:             this.getDataAtRowProp(row, 'injectionDate'),
                injectionSite:    this.getDataAtRowProp(row, 'injectionSite'),
                injectionType:    this.getDataAtRowProp(row, 'injectionType'),
                numCellsInjected: this.getDataAtRowProp(row, 'numCellsInjected'),
                notes:            this.getDataAtRowProp(row, 'notes'),
            }
            
            return XNAT.plugin.pixi.experiments.cellLine.createOrUpdate(experiment)
                       .then(id => {
                           return {
                               'subject':      experiment.subject,
                               'experimentId': id,
                               'row':          row,
                               'url':          `/data/projects/${experiment.project}/experiments/${id}?format=html`,
                               'urlText':      `${experiment.subject}`,
                           }
                       })
                       .catch(error => {
                           return {
                               'subject': experiment.subject,
                               'row':     row,
                               'error':   error,
                           }
                       })
        }
        
        async getDataForSubject(subject) {
            const response = await XNAT.plugin.pixi.experiments.get(this.getProjectSelection(), subject, '', this.getXsiType());
            
            // Skip subjects without experiments
            if (response['ResultSet']['Result'].length === 0) {
                return Promise.resolve([]);
            }
            
            let data = []
            
            for (const result of response['ResultSet']['Result']) {
                const response = await XNAT.plugin.pixi.experiments.get('', '', result['ID'], '');
                let data_fields = response['items'][0]['data_fields']
    
                let date = data_fields['date'];
                date;
                
                let experiment = {
                    'subjectId':        subject,
                    'experimentId':     result['ID'],
                    'sourceId':         data_fields['sourceId'],
                    'injectionDate':    data_fields['date'] ? data_fields['date'].replace(/(\d{4})-(\d{2})-(\d{2})/, '$2/$3/$1') : '',
                    'injectionSite':    data_fields['injectionSite'],
                    'injectionType':    data_fields['injectionType'],
                    'numCellsInjected': data_fields['numCellsInjected'],
                    'notes':            data_fields['note']
                }
                
                data.push(experiment);
            }
            
            return Promise.resolve(data);
        }
    }
}));
