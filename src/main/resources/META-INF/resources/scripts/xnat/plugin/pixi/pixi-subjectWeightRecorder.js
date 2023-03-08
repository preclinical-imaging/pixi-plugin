/*
 *  PIXI Subject Weight Recorder
 */
console.log('pixi-subjectWeightRecorder.js');

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
    
    XNAT.plugin.pixi.subjectWeightRecorder = class SubjectWeightRecorder extends XNAT.plugin.pixi.abstractExperimentManager {
        
        constructor() {
            super("PIXI Small Animal Subject Weight Recorder",
                  "Record and update subject weight measurements without the need for an imaging session",
                  "<div class='instructions action create whole'><ol>\n" +
                      "  <li>Select the project from the list of available projects in XNAT.</li>\n" +
                      "  <li>Select the subject(s) to record weight measurements for from the list of available subjects.</li>\n" +
                      "  <li>Record the subject's weight, the date of the measurement, and the technician who performed the measurement in the designated columns.</li>\n" +
                      "  <li>Any additional information or observations regarding the subject belongs in the notes column.</li>\n" +
                      "  <li>Repeat steps 3-4 for each subject you wish to record weights for.</li>\n" +
                      "  <li>Verify that all entries are accurate and consistent before submitting by clicking the \"Submit\" button.</li>\n" +
                      "</ol></div>",
                  "<div class='instructions action update whole' style='display: none'><ol>\n" +
                      "  <li>From the project selection menu, select the project that contains the subjects and weight measurements you wish to update.</li>\n" +
                      "  <li>In the subject selection box, select the subject(s) whose weight measurements you wish to update.</li>\n" +
                      "  <li>The existing weight measurements for the selected subject(s) will be automatically load into the table.</li>\n" +
                      "  <li>Update the subject's weight, the date of the measurement, and the technician who is performing the measurement in the designated columns.</li>\n" +
                      "  <li>Any additional information or observations regarding the subject belongs in the notes column.</li>\n" +
                      "  <li>Repeat steps 4-5 for each subject/measurement you wish to update.</li>\n" +
                      "  <li>Note that removing a row from the table will not delete the weight measurement.</li>" +
                      "  <li>Verify that all entries are accurate and consistent before submitting by clicking the \"Submit\" button.</li>\n" +
                      "</ol></div>");
        }
        
        static async create(containerId, project = null, subjects = []) {
            let subjectWeightRecorder = new SubjectWeightRecorder();
            
            let colHeaders = [
                "Subject ID *",
                "Experiment ID",
                "Experiment Label",
                "Subject Weight (g) *",
                "Date *",
                "Time",
                "Technician",
                "Notes"
            ]
            
            let colWidths = [175, 100, 100, 150, 100, 100, 130, 175];
            
            let columns = [
                {
                    data:         'subjectId',
                    readOnly: true
                },
                { data: 'experimentId' },
                { data: 'experimentLabel' },
                {
                    data:       'weight',
                    type:       'numeric',
                    allowEmpty: false,
                },
                {
                    data:         'date',
                    type:         'date',
                    allowEmpty:   true,
                    allowInvalid: true,
                    dateFormat:   'MM/DD/YYYY',
                    validator:    (value, callback) => subjectWeightRecorder.validateDate(value, callback)
                },
                {
                    data:          'time',
                    type:          'time',
                    timeFormat:    'h:mm:ss a',
                    correctFormat: true
                },
                {
                    data:       'technician',
                },
                { data: 'notes' }
            ]
            
            let hotSettings = {
                colHeaders:         colHeaders,
                colWidths:          colWidths,
                columns:            columns,
                rowHeaders:         true,
                manualColumnResize: true,
                contextMenu:        ['undo', 'redo', '---------', 'copy', 'cut'],
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
            
            return subjectWeightRecorder.init(containerId, hotSettings, project, subjects)
                                        .then(() => subjectWeightRecorder.hot.addHook('beforeChange', (changes, source) => subjectWeightRecorder.changeDate('date', changes, source)))
                                        .then(() => subjectWeightRecorder);
        }
        
        getXsiType() {
            return 'pixi:weightData'
        }
        
        createActionLabel() {
            return 'Record subject weight measurements'
        }
        
        updateActionLabel() {
            return 'Update existing subject weight measurements'
        }
        
        async submitRow(row) {
            console.debug(`Submitting subject weight measurements for row ${row}`);
            
            let subjectWeightExperiment = {
                project: this.getProjectSelection(),
                subject: this.hot.getDataAtRowProp(row, 'subjectId'),
                experimentId: this.hot.getDataAtRowProp(row, 'experimentId'),
                experimentLabel: this.hot.getDataAtRowProp(row, 'experimentLabel'),
                date: this.hot.getDataAtRowProp(row, 'date'),
                time: this.hot.getDataAtRowProp(row, 'time'),
                technician: this.hot.getDataAtRowProp(row, 'technician'),
                subjectWeight: this.hot.getDataAtRowProp(row, 'weight'),
                notes: this.hot.getDataAtRowProp(row, 'notes')
            }
            
            return XNAT.plugin.pixi.experiments.subjectWeight.createOrUpdate(subjectWeightExperiment)
                       .then(id => {
                           return {
                               'subject':      subjectWeightExperiment.subject,
                               'experimentId': id,
                               'row':          row,
                               'url':          `/data/projects/${subjectWeightExperiment.project}/experiments/${id}?format=html`,
                               'urlText':      `${subjectWeightExperiment.subject}`,
                           }
                       })
                       .catch(error => {
                           return {
                               'subject': subjectWeightExperiment.subject,
                               'row':     row,
                               'error':   error,
                           }
                       })
        }
        
        getEmptyRow(subject) {
            return [{
                'subjectId':       subject,
                'experimentId':    '',
                'experimentLabel': '',
                'weight':          '',
                'date':            '',
                'time':            '',
                'technician':      '',
                'notes':           ''
            }]
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
                    'date':            data_fields['date'] ? data_fields['date'].replace(/(\d{4})-(\d{2})-(\d{2})/, '$2/$3/$1') : '',
                    'time':            data_fields['time'] ? data_fields['time'] : '',
                    'technician':      data_fields['technician'],
                    'weight':          data_fields['weight'],
                    'notes':           data_fields['note']
                }
                
                data.push(experiment);
            }
            
            return Promise.resolve(data);
        }
    }
}));
