/*
 *  PIXI Subject Data Manager
 *
 *  This script depends on functions in pixi-module.js
 */

console.log('pixi-subjectDataManager.js');

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
    
    class SubjectDataManager extends XNAT.plugin.pixi.abstractDataManager {

        constructor() {
            super("PIXI Subject Data Manager",
                  "Create and update small animal subjects for a project",
                  "<div class='instructions action create whole'><ol>\n" +
                      "  <li>Select the project from the list of available projects in XNAT that you want to add the subject(s) to.</li>\n" +
                      "  <li>Enter the number of subjects you wish to create in the \"Number of Subjects\" field.</li>\n" +
                      "  <li>In the Subject ID column, create a unique identifier for the subject. This should be a single word or acronym, it should not contain spaces or special characters. And it should be unique for the selected project.</li>\n" +
                      "  <li>In the designated columns, record the subject's research group, species, sex, date of birth and litter ID if applicable.</li>\n" +
                      "  <li>In the appropriate columns, record the subject's strain, vendor, stock number, humanization type, and any genetic modifications if applicable.</li>\n" +
                      "  <li>Repeat steps 3-5 for each subject you wish to create.</li>\n" +
                      "  <li>Review all the entries and ensure that they are accurate and consistent.</li>\n" +
                      "  <li>Create the subjects by clicking the \"Submit\" button.</li>\n" +
                      "</ol></div>",
                  "<div class='instructions action update whole' style='display: none'><ol>\n" +
                      "  <li>In the project selection menu, select the project that contains the subject(s) you wish to update.</li>\n" +
                      "  <li>In the subject selection box, select the subject(s) you wish to update.</li>\n" +
                      "  <li>The selected subject(s) will be automatically loaded into the table.</li>\n" +
                      "  <li>Note that the Subject ID column is read-only. If you need to update the subject's ID, you must navigate to each subject individually.</li>\n" +
                      "  <li>In the designated columns, update the subject's research group, species, sex, date of birth and litter ID if applicable.</li>\n" +
                      "  <li>In the appropriate columns, update the subject's strain, vendor, stock number, humanization type, and any genetic modifications if applicable.</li>\n" +
                      "  <li>Repeat steps 5-6 for each subject you wish to update.</li>\n" +
                      "  <li>Review all the entries and ensure that they are accurate and consistent.</li>\n" +
                      "  <li>Update the subjects by clicking the \"Submit\" button.</li>\n" +
                    "</ol></div>")
        }
    
        getXsiType() { return 'xnat:subjectData'; }
        createActionLabel() { return 'Create new subjects' }
        updateActionLabel() { return 'Update existing subjects' }
        getSubjectColumnKey() { return 'label' }

        async init(containerId, project = null, subjects = []) {
            let colHeaders = [
                "Subject ID *",
                "Research Group",
                "Species",
                "Sex",
                "Date of Birth",
                "Litter ID",
                "Strain",
                "Vendor",
                "Stock #",
                "Humanization Type",
                "Genetic Mods",
                "Genetic Mods (non-std)",
                "Date of Death",
                "Endpoint",
            ];

            let colWidths = [175, 115, 150, 50, 100, 100, 150, 150, 75, 135, 150, 150, 100, 200];

            // Columns
            let columns = [
                {
                    data: 'label',
                    validator: (value, callback) => this.validateNewSubjectLabel(this.getProjectSelection(), value, callback),
                    allowInvalid: true
                },
                {data: 'group'},
                {
                    data: 'species',
                    type: 'autocomplete',
                    filter: true,
                    strict: false,
                    source: []
                },
                {
                    data: 'sex',
                    type: 'dropdown',
                    source: ['', 'F', 'M']
                },
                {
                    data: 'dateOfBirth',
                    type: 'date',
                    dateFormat: 'MM/DD/YYYY'
                },
                {data: 'litter'},
                {data: 'strain'},
                {
                    data: 'source',
                    type: 'autocomplete',
                    filter: true,
                    strict: false,
                    source: []
                },
                {data: 'stockNumber'},
                {data: 'strainImmuneSystemHumanizationType'},
                {data: 'geneticModifications'},
                {data: 'geneticModificationsSecondary'},
                {
                    data: 'dateOfDeath',
                    type: 'date',
                    dateFormat: 'MM/DD/YYYY'
                },
                {
                    data: 'endpoint',
                    type: 'autocomplete',
                    filter: true,
                    strict: false,
                    source: []
                },
            ]
            
            const initData = new Array(5).fill(undefined).map(u => ({
                'id': '',
                'label': '',
                'project': '',
                'group': '',
                'sex': '',
                'dateOfBirth': '',
                'litter': '',
                'strain': '',
                'source': '',
                'stockNumber': '',
                'strainImmuneSystemHumanizationType': '',
                'geneticModifications': '',
                'geneticModificationsSecondary': '',
                'dateOfDeath': '',
                'endpoint': ''
            }))
            
            let hotSettings = {
                data: initData,
                colHeaders: colHeaders,
                colWidths: colWidths,
                columns: columns,
                rowHeaders: true,
                manualColumnResize: true,
                contextMenu: ['row_above', 'row_below', '---------', 'remove_row', '---------', 'undo', 'redo', '---------', 'copy', 'cut'],
                width: '100%',
                licenseKey: 'non-commercial-and-evaluation',
                minRows: 1,
                fixedColumnsStart: 1
            }
    
            return super.init(containerId, hotSettings, project, subjects)
                        .then(() => this.subjectsSelector.hide())
                        .then(() => this.numSubjectsInput.show())
                        .then(() => this.addKeyboardShortCuts())
                        .then(() => this.initSpeciesSelector())
                        .then(() => this.initVendorSelector())
                        .then(() => this.initEndpointSelector())
                        .then(() => this.hot.addHook('beforeChange', (changes, source) => this.beforeChange(changes, source)))
                        .then(() => this.hot.addHook('afterValidate', (isValid, value, row, prop) => this.afterValidate(isValid, value, row, prop)))
                        .then(() => this);
        }
        
        async populateSubjectSelector() {
            const self = this;
        
            let project = self.getProjectSelection();
            
            let subjectSelectEl = document.getElementById('subjects');
            
            while (subjectSelectEl.options.length > 0) {
                subjectSelectEl.options.remove(subjectSelectEl.options.length - 1);
            }
            
            if (project === null || project === undefined || project === '') {
                return;
            }
        
            return XNAT.plugin.pixi.subjects.getAll(project)
                       .then(resultSet => resultSet['ResultSet']['Result'])
                       .then(subjects => {
                           // remove subject with 'label' = 'hotel'
                           return subjects.filter(subject => subject['label'].toLowerCase() !== 'hotel');
                       })
                       .then(subjects => {
                           subjects.sort(pixi.compareGenerator('label'));
                           subjects.forEach(subject => {
                               subjectSelectEl.options.add(new Option(subject['label'], subject['id']))
                           });
                       })
        }
        
        async getDataForSubject(subject) {
            const subjectDetails = await XNAT.plugin.pixi.subjects.get(this.getProjectSelection(), subject);
            
            // yyyy-mm-dd to mm/dd/yyyy
            if (subjectDetails['dateOfBirth']) {
                subjectDetails['dateOfBirth'] = subjectDetails['dateOfBirth'].replace(/(\d{4})-(\d{2})-(\d{2})/, "$2/$3/$1");
            }

            if (subjectDetails['dateOfDeath']) {
                subjectDetails['dateOfDeath'] = subjectDetails['dateOfDeath'].replace(/(\d{4})-(\d{2})-(\d{2})/, "$2/$3/$1");
            }
            
            return Promise.resolve([subjectDetails]);
        }
        
        actionSelected(action) {
            if (action === 'create') {
                this.subjectsSelector.hide();
                this.numSubjectsInput.show();
                this.numSubjectsInput.enable();
                this.numSubjectsInput.set(5);
    
                const subjectLabelColumn = this.getColumn(this.getSubjectColumnKey());
                subjectLabelColumn.validator = (value, callback) => this.validateNewSubjectLabel(this.getProjectSelection(), value, callback);
                subjectLabelColumn['readOnly'] = false;
                this.updateColumns();
                
            } else if (action === 'update') {
                this.subjectsSelector.show();
                this.numSubjectsInput.hide();
                this.numSubjectsInput.disable();
                
                const subjectLabelColumn = this.getColumn(this.getSubjectColumnKey());
                subjectLabelColumn.validator = (value, callback) => this.validateExistingSubjectLabel(this.getProjectSelection(), value, callback);
                subjectLabelColumn['readOnly'] = true;
                this.updateColumns();

            }
            
            super.actionSelected(action);
        }
    
        initSpeciesSelector() {
            XNAT.plugin.pixi.speices.get().then(species => {
                let options = [];

                species.sort(pixi.compareGenerator('scientificName'))
                species.forEach(specie => {
                    options.push(specie['scientificName'])
                });

                let columns = this.getColumns();
                columns[2]['source'] = options;
                this.hot.updateSettings({columns: columns});
            })
        }

        initVendorSelector() {
            XNAT.plugin.pixi.vendors.get().then(vendors => {
                let options = [];

                vendors.sort(pixi.compareGenerator('vendor'));
                vendors.forEach(vendor => options.push(vendor['vendor']));

                let columns = this.getColumns();
                columns[7]['source'] = options;
                this.hot.updateSettings({columns: columns});
            });
        }

        initEndpointSelector() {
            XNAT.plugin.pixi.animalEndpoints.get().then(endpoints => {
                let options = Array.from(endpoints).sort();
                let columns = this.getColumns();
                columns[13]['source'] = options;
                this.hot.updateSettings({columns: columns});
            });
        }
        
        numSubjectsChanged(source) {
            super.numSubjectsChanged(source);
            
            if (this.numSubjectsInput.isDisabled()) {
                return;
            }
            
            if (source === 'hot') {
                this.numSubjectsInput.set(this.hot.countRows());
            }
            else if (source === 'num-subjects-input') {
                // update hot with new number of subjects
                let numSubjects = this.numSubjectsInput.get();
                let data = this.hot.getSourceData();
                let numSubjectsToAdd = numSubjects - data.length;
                if (numSubjectsToAdd > 0) {
                    this.hot.alter('insert_row_above', data.length, numSubjectsToAdd);
                } else if (numSubjectsToAdd < 0) {
                    this.hot.alter('remove_row', data.length + numSubjectsToAdd, -numSubjectsToAdd);
                }
            }
        }
    
        beforeChange(changes, source) {
            for (let i = changes.length - 1; i >= 0; i--) {
                if (changes[i][1] === 'label') {
                    if (changes[i][3] !== null && changes[i][3] !== undefined && changes[i][3] !== '') {
                        // Remove spaces and special characters from subject ids
                        changes[i][3] = changes[i][3].replaceAll(' ', '_');
                        changes[i][3] = changes[i][3].replaceAll(/[!@#&?<>()*$%]/g, "_");
    
                        if (changes[i][2] !== changes[i][3]) { // If the data changed
                            // increment the subject id if it already exists
                            while (this.hot.getDataAtProp('label').includes(changes[i][3])) {
                                if (changes[i][3].match(/[0-9]+$/)) {
                                    changes[i][3] = changes[i][3].replace(/[0-9]+$/, (match) => {
                                        return parseInt(match) + 1;
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }

        afterValidate(isValid, value, row, prop) {
            let key = `${prop}.${row}`;

            if (!isValid) {
                let displayRow = row + 1;

                if (prop === "label") {
                    if (value !== null && value !== undefined && value !== '') {
                        // Failed because it matches an existing subject
                        this.errorMessages.set(key, `${value} matches an existing subject id in 
                            ${XNAT.app.displayNames.singular.project.toLowerCase()} ${this.getProjectSelection()}.`);
                    } else {
                        this.errorMessages.delete(key);
                    }
                }
            } else {
                this.errorMessages.delete(key);
            }

            if (this.errorMessages.size > 0) {
                let message = spawn('div', [
                    spawn('p', 'Errors found:'),
                    spawn('ul', Array.from(this.errorMessages.values()).map(msg => spawn('li', msg)))
                ])

                this.displayMessage('error', message);
            } else {
                this.clearAndHideMessage();
            }
        }

        async submit() {
            const self = this;
            
            let validProject = this.validateProjectSelection(),
                isEmpty      = this.isHotEmpty();
    
            if (!validProject) {
                return Promise.reject('Invalid project selection');
            } else if (isEmpty) {
                return Promise.reject('Empty');
            }
    
            this.removeEmptyRows();

            this.validateCells(async (valid) => {
                if (!valid) {
                    let message = spawn('div', [
                        spawn('p', 'Invalid inputs. Please correct before resubmitting.'),
                    ])

                    this.displayMessage('error', message);

                    return;
                }

                // Everything is valid, remove old messages
                this.clearAndHideMessage();

                XNAT.ui.dialog.static.wait('Submitting to XNAT', {id: "submit_form"});

                let projectId = this.getProjectSelection();
                let subjects = [];

                let successfulRows = [];
                let failedRows = [];

                for (let iRow = 0; iRow < this.hot.countRows(); iRow++) {
                    let subjectLabel = this.hot.getDataAtRowProp(iRow, 'label');

                    let group = this.hot.getDataAtRowProp(iRow, 'group');
                    let species = this.hot.getDataAtRowProp(iRow, 'species');
                    let sex = this.hot.getDataAtRowProp(iRow, 'sex');
                    let dob = this.hot.getDataAtRowProp(iRow, 'dateOfBirth');
                    let litter = this.hot.getDataAtRowProp(iRow, 'litter');
                    let strain = this.hot.getDataAtRowProp(iRow, 'strain');
                    let source = this.hot.getDataAtRowProp(iRow, 'source');
                    let stockNumber = this.hot.getDataAtRowProp(iRow, 'stockNumber');
                    let humanizationType = this.hot.getDataAtRowProp(iRow, 'strainImmuneSystemHumanizationType');
                    let geneticModifications = this.hot.getDataAtRowProp(iRow, 'geneticModifications');
                    let geneticModificationsNonStd = this.hot.getDataAtRowProp(iRow, 'geneticModificationsSecondary');
                    let dateOfDeath = this.hot.getDataAtRowProp(iRow, 'dateOfDeath');
                    let endpoint = this.hot.getDataAtRowProp(iRow, 'endpoint');

                    await XNAT.plugin.pixi.subjects.createOrUpdate(projectId, subjectLabel, group, species,
                        sex, dob, litter, strain, source, stockNumber, humanizationType, geneticModifications,
                        geneticModificationsNonStd, dateOfDeath, endpoint)
                        .then(url => {
                            successfulRows.push(iRow)
                            subjects.push({
                                'subjectId': subjectLabel,
                                'url': url
                            });

                            return url;
                        })
                        .catch(error => {
                            console.error(`Error creating subject: ${error}`);

                            failedRows.push(
                                {
                                    'subjectId': subjectLabel,
                                    'row': iRow,
                                    'error': error
                                }
                            )

                            return error;
                        })
                }

                XNAT.ui.dialog.close('submit_form');

                // Disable new inputs to successful rows
                this.hot.updateSettings({
                    cells: function (row, col) {
                        var cellProperties = {};

                        if (successfulRows.contains(row)) {
                            cellProperties.readOnly = true;
                        }

                        return cellProperties;
                    },
                    contextMenu: ['copy', 'cut'],
                });

                this.removeKeyboardShortCuts();
                this.disableProjectSelection();
    
                if (failedRows.length === 0) {
                    // Success
                    let message = spawn('div.success', [
                        spawn('p', 'Review successful submissions:'),
                        spawn('ul', subjects.map(subject => spawn('li', [spawn(`a`, {
                            href: subject['url'],
                            target: '_BLANK'
                        }, subject['subjectId'])])))
                    ])

                    // Disable resubmissions
                    this.disableSubmitButton();
    
                    XNAT.ui.dialog.open({
                                            title: 'Success',
                                            content: message,
                                            closeBtn: true,
                                            width: 600,
                                            buttons: [
                                                {
                                                    label: 'Return to Project',
                                                    isDefault: true,
                                                    close: false,
                                                    className: 'btn btn-success',
                                                    action: function (obj) {
                                                        XNAT.ui.dialog.closeAll();
                                                        window.location = XNAT.url.rootUrl('/data/projects/' + self.getProjectSelection());
                                                    }
                                                },
                                                {
                                                    label: 'Record PDX Details',
                                                    isDefault: false,
                                                    close: false,
                                                    action: function () {
                                                        XNAT.ui.dialog.closeAll();
    
                                                        let project = self.getProjectSelection();
                                                        let subjects = self.hot.getDataAtProp('label').filter(s => Boolean(s));
    
                                                        (async () => {
                                                            window.pdxExperimentManager = await XNAT.plugin.pixi.pdxExperimentManager.create(self.containerId, project, subjects);
                                                        })();
                                                    }
                                                },
                                                {
                                                    label: 'Record Cell Line Details',
                                                    isDefault: false,
                                                    close: false,
                                                    action: function () {
                                                        XNAT.ui.dialog.closeAll();
            
                                                        let project = self.getProjectSelection();
                                                        let subjects = self.hot.getDataAtProp('label').filter(s => Boolean(s));
    
                                                        (async () => {
                                                            window.cellLineExperimentManager = await XNAT.plugin.pixi.cellLineExperimentManager.create(self.containerId, project, subjects);
                                                        })();
                                                    }
                                                }
                                            ]
                                        });
                } else if (successfulRows.length === 0 && failedRows.length > 0) {
                    // All submissions in error
                    let message = spawn('div', [
                        spawn('p', ''),
                        spawn('p', 'There were errors with your submission. Correct the issues and try resubmitting.'),
                        spawn('ul', failedRows.map(subject => spawn('li', `Row: ${subject['row'] + 1} ${XNAT.app.displayNames.singular.subject} ID: ${subject['subjectId']} ${subject['error']}`))),
                    ])

                    this.displayMessage('error', message);
                } else if (successfulRows.length > 0 && failedRows.length > 0) {
                    // Some submitted successfully, some failed
                    let message = spawn('div', [
                        spawn('p', 'There were errors with your submission. Correct the issues and try resubmitting.'),
                        spawn('p', 'Error(s):'),
                        spawn('ul', failedRows.map(subject => spawn('li', `Row: ${subject['row'] + 1} ${XNAT.app.displayNames.singular.subject} ID: ${subject['subjectId']} ${subject['error']}`))),
                        spawn('p', 'Successful submissions:'),
                        spawn('ul', subjects.map(subject => spawn('li', [spawn(`a`, {
                            href: subject['url'],
                            target: '_BLANK'
                        }, subject['subjectId'])])))
                    ])

                    this.displayMessage('warning', message);
                }

                XNAT.ui.dialog.close('submit_form');
            });
        }
    }
    
    XNAT.plugin.pixi.subjectDataManager = new SubjectDataManager();
    
}));
