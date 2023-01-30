/*
 *  PIXI Drug Therapy Recorder
 */
console.log('pixi-drugTherapyRecorder.js');

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
    
    XNAT.plugin.pixi.drugTherapyRecorder = class DrugTherapyRecorder extends XNAT.plugin.pixi.abstractExperimentManager {
        
        constructor() {
            super("PIXI Drug Therapy Recorder",
                  "Record and update drug therapies administered to small animal subjects",
                  "<div class='instructions action create whole'><ol>\n" +
                      "  <li>Select the project from the list of available projects in XNAT that contains the subject(s) for which you want to record drug therapies.</li>\n" +
                      "  <li>In the Subject ID column, type the subject's ID or select the subject from the drop down menu.</li>\n" +
                      "  <li>In the designated columns, record the drug name, dose, dose units, route of administration and the site of administration. Optionally, record the drug lot number.</li>\n" +
                      "  <li>In the appropriate columns, record the subject's weight at the time of administration, the date of administration and the technician who is administering the drug.</li>\n" +
                      "  <li>In the notes column, add any additional information or observations regarding the subject or the administration.</li>\n" +
                      "  <li>Repeat steps 2-5 for each subject you wish to record a drug therapy for.</li>\n" +
                      "  <li>Review all the entries and ensure that they are accurate and consistent.</li>\n" +
                      "  <li>Save the drug therapies by clicking the \"Submit\" button.</li>\n" +
                      "  <li>Repeat the process at regular intervals or as needed for longitudinal studies.</li>\n" +
                      "</ol></div>",
                  "<div class='instructions action update whole' style='display: none'><ol>\n" +
                      "  <li>In the project selection menu, select the project that contains the subjects and drug therapies you wish to update.</li>\n" +
                      "  <li>In the subject selection box, select the subject(s) whose drug therapies you wish to update.</li>\n" +
                      "  <li>The existing drug therapies for the selected subject(s) will be automatically loaded into the table.</li>\n" +
                      "  <li>In the designated columns, update the drug name, dose, dose units, route of administration, the site of administration, and the optional drug lot number.</li>\n" +
                      "  <li>In the appropriate columns, update the subject's weight at the time of administration, the date of administration and the technician who is administering the drug.</li>\n" +
                      "  <li>In the notes column, add any additional information or observations regarding the subject or the administration.</li>\n" +
                      "  <li>Repeat steps 4-6 for each subject/drug therapy you wish to update.</li>\n" +
                      "  <li>Note that removing a row from the table will not delete the drug therapy.</li>" +
                      "  <li>Review all the entries and ensure that they are accurate and consistent.</li>\n" +
                      "  <li>Save the updated drug therapies by clicking the \"Submit\" button.</li>\n" +
                      "</ol></div>");
        }
        
        static async create(containerId, project = null, subjects = []) {
            let drugTherapyRecorder = new DrugTherapyRecorder();
            
            let colHeaders = [
                "Subject ID *",
                "Experiment ID",
                "Experiment Label",
                "Drug *",
                "Dose *",
                "Dose Unit *",
                "Route *",
                "Site *",
                "Lot Number",
                "Subject Weight (g)",
                "Date *",
                "Time",
                "Technician *",
                "Notes"
            ]
            
            let colWidths = [175, 100, 150, 100, 100, 100, 120, 60, 80, 150, 100, 100, 130, 175];
            
            let columns = [
                {
                    data:         'subjectId',
                    type:         'autocomplete',
                    filter:       true,
                    strict:       true,
                    source:       [],
                    allowEmpty:   true,
                    allowInvalid: true,
                    validator:    (value, callback) => drugTherapyRecorder.validateExistingSubjectLabel(drugTherapyRecorder.getProjectSelection(), value, callback),
                },
                { data: 'experimentId' },
                { data: 'experimentLabel' },
                {
                    data:       'drug',
                    type:       'text',
                    allowEmpty: false,
                    validator:  (value, callback) => value ? callback(true) : callback(false)
                },
                {
                    data:       'dose',
                    type:       'numeric',
                    allowEmpty: false,
                },
                {
                    data:       'doseUnit',
                    type:       'text',
                    allowEmpty: false,
                    validator:  (value, callback) => value ? callback(true) : callback(false)
                },
                {
                    data:       'route',
                    type:       'text',
                    allowEmpty: false,
                    validator:  (value, callback) => value ? callback(true) : callback(false)
                },
                {
                    data:       'site',
                    type:       'text',
                    allowEmpty: false,
                    validator:  (value, callback) => value ? callback(true) : callback(false)
                },
                { data: 'lotNumber' },
                {
                    data: 'subjectWeight',
                    type: 'numeric'
                },
                {
                    data:         'treatmentDate',
                    type:         'date',
                    allowEmpty:   true,
                    allowInvalid: true,
                    dateFormat:   'MM/DD/YYYY',
                    validator:    (value, callback) => drugTherapyRecorder.validateDate(value, callback)
                },
                {
                    data:          'time',
                    type:          'time',
                    timeFormat:    'h:mm:ss a',
                    correctFormat: true
                },
                {
                    data:       'technician',
                    type:       'text',
                    allowEmpty: false,
                    validator:  (value, callback) => {
                        value ? callback(true) : callback(false)
                    }
                },
                { data: 'notes' }
            ]
            
            let hotSettings = {
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
                    columns: [1, 2], // Experiment ID and Experiment Label
                    // show UI indicators to mark hidden columns
                    indicators: false
                },
                fixedColumnsStart: 1
            }
            
            return drugTherapyRecorder.init(containerId, hotSettings, project, subjects)
                                       .then(() => drugTherapyRecorder.hot.addHook('beforeChange', (changes, source) => drugTherapyRecorder.changeDate('treatmentDate', changes, source)))
                                       .then(() => drugTherapyRecorder);
        }
        
        getXsiType() {
            return 'pixi:drugTherapyData'
        }
        
        createActionLabel() {
            return 'Record new drug therapies'
        }
        
        updateActionLabel() {
            return 'Update existing drug therapies'
        }
        
        async submitRow(row) {
            console.debug(`Submitting drug therapy experiment for row ${row}`);
            
            let drugTherapy = {
                project:         this.getProjectSelection(),
                subject:         this.hot.getDataAtRowProp(row, 'subjectId'),
                experimentId:    this.hot.getDataAtRowProp(row, 'experimentId'),
                experimentLabel: this.hot.getDataAtRowProp(row, 'experimentLabel'),
                date:            this.hot.getDataAtRowProp(row, 'treatmentDate'),
                time:            this.hot.getDataAtRowProp(row, 'time'),
                technician:      this.hot.getDataAtRowProp(row, 'technician'),
                drug:            this.hot.getDataAtRowProp(row, 'drug'),
                dose:            this.hot.getDataAtRowProp(row, 'dose'),
                doseUnit:        this.hot.getDataAtRowProp(row, 'doseUnit'),
                route:           this.hot.getDataAtRowProp(row, 'route'),
                site:            this.hot.getDataAtRowProp(row, 'site'),
                lotNumber:       this.hot.getDataAtRowProp(row, 'lotNumber'),
                subjectWeight:   this.hot.getDataAtRowProp(row, 'subjectWeight'),
                notes:           this.hot.getDataAtRowProp(row, 'notes'),
            }
            
            return XNAT.plugin.pixi.experiments.drugTherapy.createOrUpdate(drugTherapy)
                       .then(id => {
                           return {
                               'subject':      drugTherapy.subject,
                               'experimentId': id,
                               'row':          row,
                               'url':          `/data/projects/${drugTherapy.project}/experiments/${id}?format=html`,
                               'urlText':      `${drugTherapy.subject}`,
                           }
                       })
                       .catch(error => {
                           return {
                               'subject': drugTherapy.subject,
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
    
                let experiment = {
                    'subjectId':       subject,
                    'experimentId':    result['ID'],
                    'experimentLabel': data_fields['label'],
                    'treatmentDate':   data_fields['date'] ? data_fields['date'].replace(/(\d{4})-(\d{2})-(\d{2})/, '$2/$3/$1') : '',
                    'time':            data_fields['time'] ? data_fields['time'] : '',
                    'technician':      data_fields['technician'],
                    'drug':            data_fields['drug'],
                    'dose':            data_fields['dose'],
                    'doseUnit':        data_fields['doseUnit'],
                    'route':           data_fields['route'],
                    'site':            data_fields['site'],
                    'lotNumber':       data_fields['lotNumber'],
                    'subjectWeight':   data_fields['weight'],
                    'notes':           data_fields['note']
                }
            
                data.push(experiment);
            }
        
            return Promise.resolve(data);
        }
    }
    
}));
