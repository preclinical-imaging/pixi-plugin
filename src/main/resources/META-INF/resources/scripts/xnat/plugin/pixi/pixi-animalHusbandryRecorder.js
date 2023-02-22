/*
 * PIXI Animal Husbandry Recorder
 */
console.log('pixi-animalHusbandryRecorder.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = getObject(XNAT.plugin.pixi || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {
    
    XNAT.plugin.pixi.animalHusbandryRecorder = class AnimalHusbandryRecorder extends XNAT.plugin.pixi.abstractExperimentManager {
        
        constructor() {
            super("PIXI Animal Husbandry Recorder",
                "Record and update animal husbandry data for small animal subjects within a project",
                "<div class='instructions action create whole'><ol>\n" +
                "  <li>Select the project from the list of available projects in XNAT.</li>\n" +
                "  <li>Select the subject(s) from the subject list that you wish to record animal husbandry data for.</li>\n" +
                "  <li>Enter the animal husbandry data for the selected subject(s).</li>\n" +
                "  <li>Review the input data and ensure that it is accurate and consistent.</li>\n" +
                "  <li>Click the 'Submit' button to save the animal husbandry data to XNAT.</li>\n" +
                "</ol></div>",
                "<div class='instructions action update whole' style='display: none'><ol>\n" +
                "  <li>Select the project from the list of available projects in XNAT.</li>\n" +
                "  <li>Select the subject(s) from the subject list that you wish to update animal husbandry data for.</li>\n" +
                "  <li>Enter the updated animal husbandry data for the selected subject(s).</li>\n" +
                "  <li>Review the input data and ensure that it is accurate and consistent.</li>\n" +
                "  <li>Click the 'Submit' button to save the updated animal husbandry data to XNAT.</li>\n" +
                "</ol></div>");
        }
        
        static async create(containerId, project = null, subjects = []) {
            let animalHusbandryRecorder = new AnimalHusbandryRecorder();
            
            let colHeaders = [
                "Subject ID *",
                "Experiment ID",
                "Experiment Label",
                "Animal Feed",
                "Feed Source",
                "Feed Manufacturer",
                "Feed Product Name",
                "Feed Product Code",
                "Feeding Method",
                "Water Type",
                "Water Delivery",
                "Num Animals Within Same Housing Unit",
                "Sex of Animals Within Same Housing Unit",
                "Environmental Temperature (C)",
                "Housing Humidity (%)",
                "Notes"
            ]
    
            let colWidths = [175, 100, 150, 100, 225, 150, 150, 150, 130, 320, 150, 255, 265, 200, 150, 150];
            
            let columns = [
                {
                    data:         'subjectId',
                    readOnly:     true,
                },
                {
                    data: 'experimentId'
                },
                {
                    data: 'experimentLabel'
                },
                {
                    data: 'animalFeed',
                    type: 'autocomplete',
                    source: ['NIH07', 'NIH31', 'AIN76', 'AIN93G', 'AIN93M'],
                    strict: false
                },
                {
                    data: 'feedSource',
                    type: 'autocomplete',
                    source: ['Commercial product', 'Locally manufactured product'],
                    strict: false
                },
                {
                    data: 'feedManufacturer',
                },
                {
                    data: 'feedProductName',
                },
                {
                    data: 'feedProductCode',
                },
                {
                    data: 'feedingMethod',
                    type: 'autocomplete',
                    source: ['ab libitum', 'Restricted diet', 'Food treat', 'Gavage'],
                    strict: false
                },
                {
                    data: 'waterType',
                    type: 'autocomplete',
                    source: ['Tap water', 'Distilled water', 'Reverse osmosis purified water', 'Reverse osmosis purified, HCl acidified water'],
                    strict: false
                },
                {
                    data: 'waterDelivery',
                    type: 'autocomplete',
                    source: ['ab libitum', 'Restricted diet', 'Food treat', 'Gavage'],
                    strict: false
                },
                {
                    data: 'numberOfAnimalsWithinSameHousingUnit',
                    type: 'numeric',
                    allowEmpty: true,
                    allowInvalid: true,
                    validator: (value, callback) => {
                        // Empty value or positive integer
                        if (value === '' || value === null || value === undefined) {
                            callback(true);
                        } else {
                            callback(Number.isInteger(value) && value >= 0);
                        }
                    }
                },
                {
                    data: 'sexOfAnimalsWithinSameHousingUnit',
                    type: 'autocomplete',
                    source: ['Male', 'Female', 'Both'],
                    strict: false,
                },
                {
                    data: 'environmentalTemperature',
                    type: 'numeric',
                    allowEmpty: true,
                    allowInvalid: true,
                    validator: (value, callback) => {
                        // Empty value or positive decimal number
                        if (value === '' || value === null || value === undefined) {
                            callback(true);
                        } else {
                            callback(Number.isFinite(value));
                        }
                    }
                },
                {
                    data: 'housingHumidity',
                    type: 'numeric',
                    allowEmpty: true,
                    allowInvalid: true,
                    validator: (value, callback) => {
                        // Empty value or positive decimal number between 0 and 100
                        if (value === '' || value === null || value === undefined) {
                            callback(true);
                        } else {
                            callback(Number.isFinite(value) && value >= 0 && value <= 100);
                        }
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
            
            return animalHusbandryRecorder.init(containerId, hotSettings, project, subjects)
                                          .then(() => animalHusbandryRecorder);
        }
        
        getXsiType() {
            return 'pixi:animalHusbandryData'
        }
        
        createActionLabel() {
            return 'Record animal husbandry data'
        }
        
        updateActionLabel() {
            return 'Update existing animal husbandry data'
        }
    
        async submitRow(row) {
            console.debug(`Submitting animal husbandry data for row ${row}`);
        
            let animalHusbandryData = {
                project: this.getProjectSelection(),
                subject: this.hot.getDataAtRowProp(row, 'subjectId'),
                experimentId: this.hot.getDataAtRowProp(row, 'experimentId'),
                experimentLabel: this.hot.getDataAtRowProp(row, 'experimentLabel'),
                animalFeed: this.hot.getDataAtRowProp(row, 'animalFeed'),
                feedSource: this.hot.getDataAtRowProp(row, 'feedSource'),
                feedManufacturer: this.hot.getDataAtRowProp(row, 'feedManufacturer'),
                feedProductName: this.hot.getDataAtRowProp(row, 'feedProductName'),
                feedProductCode: this.hot.getDataAtRowProp(row, 'feedProductCode'),
                feedingMethod: this.hot.getDataAtRowProp(row, 'feedingMethod'),
                waterType: this.hot.getDataAtRowProp(row, 'waterType'),
                waterDelivery: this.hot.getDataAtRowProp(row, 'waterDelivery'),
                numberOfAnimalsWithinSameHousingUnit: this.hot.getDataAtRowProp(row, 'numberOfAnimalsWithinSameHousingUnit'),
                sexOfAnimalsWithinSameHousingUnit: this.hot.getDataAtRowProp(row, 'sexOfAnimalsWithinSameHousingUnit'),
                environmentalTemperature: this.hot.getDataAtRowProp(row, 'environmentalTemperature'),
                housingHumidity: this.hot.getDataAtRowProp(row, 'housingHumidity'),
                notes: this.hot.getDataAtRowProp(row, 'notes')
            }
        
            return XNAT.plugin.pixi.experiments.animalHusbandry.createOrUpdate(animalHusbandryData)
                       .then(id => {
                           return {
                               'subject': animalHusbandryData.subject,
                               'experimentId': id,
                               'row': row,
                               'url': `/data/projects/${animalHusbandryData.project}/experiments/${id}?format=html`,
                               'urlText': `${animalHusbandryData.subject}`,
                           }
                       })
                       .catch(error => {
                           return {
                               'subject': animalHusbandryData.subject,
                               'row': row,
                               'error': error,
                           }
                       })
        }
        
        getEmptyRow(subject) {
            return [{
                'subjectId':       subject,
                'experimentId':    '',
                'experimentLabel': '',
                'animalFeed':      '',
                'feedSource':      '',
                'feedManufacturer': '',
                'feedProductName': '',
                'feedProductCode': '',
                'feedingMethod':   '',
                'waterType':       '',
                'waterDelivery':   '',
                'numberOfAnimalsWithinSameHousingUnit': '',
                'sexOfAnimalsWithinSameHousingUnit': '',
                'environmentalTemperature': '',
                'housingHumidity': '',
                'notes':           '',
            }];
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
                    'animalFeed':      data_fields['animalFeed'],
                    'feedSource':      data_fields['feedSource'],
                    'feedManufacturer': data_fields['feedManufacturer'],
                    'feedProductName': data_fields['feedProductName'],
                    'feedProductCode': data_fields['feedProductCode'],
                    'feedingMethod':   data_fields['feedingMethod'],
                    'waterType':       data_fields['waterType'],
                    'waterDelivery':   data_fields['waterDelivery'],
                    'numberOfAnimalsWithinSameHousingUnit': data_fields['numberOfAnimalsWithinSameHousingUnit'],
                    'sexOfAnimalsWithinSameHousingUnit': data_fields['sexOfAnimalsWithinSameHousingUnit'],
                    'environmentalTemperature': data_fields['environmentalTemperature'],
                    'housingHumidity': data_fields['housingHumidity'],
                    'notes':           data_fields['note']
                }
                
                data.push(experiment);
            }
            
            return Promise.resolve(data);
        }
    }
}));

