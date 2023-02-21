/*
 *  PIXI Experiment Manager
 */

console.debug('pixi-experimentManager.js');

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
    
    console.debug('pixi-experimentManager.js - AbstractDataManager');
    
    
    let validationMixins = {
        isEmpty(item) {
            return item === null || item === undefined || item === '';
        },
        validateSubjectLabel(project, subject, method, callback) {
            if (validationMixins.isEmpty(subject)) {
                callback(false);
                return;
            }
            
            let thenCb, catchCb;
            
            switch (method) {
                case 'create':
                case 'new':
                    thenCb = false;
                    catchCb = true;
                    break;
                case 'update':
                case 'existing':
                    thenCb = true;
                    catchCb = false;
                    break;
            }
            
            if (validationMixins.projectSubjectCache.has(project)) {
                let subjects = validationMixins.projectSubjectCache.get(project);
                if (subjects.includes(subject)) {
                    callback(thenCb);
                } else {
                    callback(catchCb);
                }
            } else {
                XNAT.plugin.pixi.subjects.get(project, subject)
                    .then(() => callback(thenCb))
                    .catch(() => callback(catchCb));
            }
        },
        validateNewSubjectLabel(project, subject, callback) {
            validationMixins.validateSubjectLabel(project, subject, 'new', callback);
        },
        validateExistingSubjectLabel(project, subject, callback) {
            validationMixins.validateSubjectLabel(project, subject, 'update', callback);
        },
        projectSubjectCache: new Map(),
    };
    
    XNAT.plugin.pixi.abstractDataManager = class AbstractDataManager {
        containerId;
        container;
        
        #title;
        #subtitle;
        #instructions1;
        #instructions2;
        
        projectSelector;
        submitButton;
        
        messageComponent;
        
        hot;
        errorMessages;
        successMessages;
        lastKey;
        lastAction;
        
        constructor(heading, subheading, instructions1, instructions2) {
            if (new.target === AbstractDataManager) {
                throw new TypeError("Cannot construct Abstract instances directly");
            }
            
            this.#title = heading;
            this.#subtitle = subheading;
            this.#instructions1 = instructions1;
            this.#instructions2 = instructions2;
            
            this.errorMessages = new Map();
            this.successMessages = new Map();
            this.lastAction = 'create';
            
            Object.assign(this, validationMixins);
        }
        
        async submit() {
            throw new Error("Method 'submit()' must be implemented.");
        }
        
        async submitRow() {
            throw new Error("Method 'submitRow()' must be implemented.");
        }
        
        getSubjectColumnKey() {
            throw new Error("Method 'getSubjectColumnKey()' must be implemented.");
        }
        
        createActionLabel() {
            throw new Error("Method 'createActionLabel()' must be implemented.");
        }
        
        updateActionLabel() {
            throw new Error("Method 'updateActionLabel()' must be implemented.");
        }
        
        async getDataForSubject(subject) {
            throw new Error("Method 'getDataForSubjects()' must be implemented.");
        }
        
        getXsiType() {
            throw new Error("Method 'getXsiType()' must be implemented.");
        }
    
        getEmptyRow(subject) {
            throw new Error("Method 'getEmptyRow()' must be implemented.");
        }
        
        
        async init(containerId, hotSettings, project, subjects) {
            const self = this;
            
            this.containerId = containerId;
            this.container = document.getElementById(this.containerId);
            
            this.messageComponent = spawn('div', { id: 'table-msg', style: { display: 'none' } });
            
            let titleEl = spawn('h2', self.#title);
            
            let panel = spawn('div.container', [
                spawn('div.withColor containerTitle', self.#subtitle),
                spawn('div.containerBody', [
                    ...self.containerBody(),
                    spawn('div.hot-container.containerIterm', [spawn('div.hot-table')]),
                    self.messageComponent
                ])
            ]);
            
            this.submitButton = spawn('input.btn1.pull-right|type=button|value=Submit', {
                onclick: () => {
                    xmodal.confirm({
                                       title:    "Confirm Submission",
                                       height:   220,
                                       scroll:   false,
                                       content:  `<p>Are you ready to submit?</p>`,
                                       okAction: () => self.submit(),
                                   })
                }
            });
            
            let buttons = spawn('div.submit-right', [
                self.submitButton,
                ...self.additionalButtons(),
                spawn('div.clear')
            ])
            
            this.container.innerHTML = '';
            this.container.append(titleEl);
            this.container.append(panel);
            this.container.append(buttons);
            
            return XNAT.plugin.pixi.projects.populateSelectBox('project')
                       .then(() => {
                           this.hot = new Handsontable(this.container.querySelector('.hot-table'), hotSettings);
                           this.getDataAtRowProp = (row, prop) => this.hot.getDataAtRowProp(row, prop);
                           this.updateHeight();
                           this.hot.addHook('afterChange', (changes, source) => this.updateHeight());
                           this.hot.addHook('afterCreateRow', () => this.updateHeight());
                           this.hot.addHook('afterRemoveRow', () => this.updateHeight());
                           // Place cursor at first cell
                           this.hot.selectCell(0, 0, 0, 0);
                       })
                       .then(() => {
                           if (project) {
                               this.setProjectSelection(project)
                               this.disableProjectSelection();
    
                               XNAT.plugin.pixi.subjects.getAll(self.getProjectSelection())
                                   .then(subjects => subjects['ResultSet']['Result'])
                                   .then(subjects => subjects.map(subject => subject['label']))
                                   .then(subjects => {
                                       self.projectSubjectCache.set(self.getProjectSelection(), subjects);
                                   })
                           }
                       })
                       .then(() => this.populateSubjectSelector());
        }
        
        additionalButtons() {
            return [];
        }
        
        containerBody() {
            const self = this;
            
            this.projectSelector = spawn('div.form-component.col.containerItem.third', [
                spawn('label.required|for=\'project\'', 'Select a Project'),
                spawn('select.form-control', {
                          id:       'project',
                          name:     'project',
                          onchange: () => {
                              self.validateProjectSelection();
                              self.populateSubjectSelector()
                                  .then(() => self.subjectsSelector.populate(self.getProjectSelection()))
                                  .then(() => self.actionSelector.get() === 'update' ? self.updateData([{}, {}, {}, {}, {}]) : null)
                                  .then(() => self.validateCells())
                                  .then(() => document.dispatchEvent(new Event('project-changed')));
                              
                              XNAT.plugin.pixi.subjects.getAll(self.getProjectSelection())
                                  .then(subjects => subjects['ResultSet']['Result'])
                                  .then(subjects => subjects.map(subject => subject['label']))
                                  .then(subjects => {
                                      self.projectSubjectCache.set(self.getProjectSelection(), subjects);
                                  })
                          }
                      },
                      [spawn('option|', { selected: true, disabled: true, value: '' }, '')]),
                spawn('div.prj-error', { style: { display: 'none' } }, 'Please select a project')
            ])
    
            this.actionSelector = {
                elements: spawn('div.row', [
                    spawn('div.form-component.containerItem.half.action.create.active',
                          {
                              onclick: () => {
                                  if (this.isSubmitButtonEnabled()) {
                                      const action = 'create';
                                      if (this.lastAction !== action) {
                                          document.dispatchEvent(new Event('action-selected'));
                                          this.actionSelected(action);
                                      }
    
                                      this.lastAction = action;
                                  }
                              }
                          },
                          [
                              spawn('input.form-control.action.create|type=\'radio\'',
                                    {
                                        id:      'createAction',
                                        name:    'action',
                                        value:   'create',
                                        checked: true,
                                    }),
                              spawn('label|for=\'createAction\'', `${this.createActionLabel()}`),
                          ]),
                    spawn('div.form-component.containerItem.half.action.update.disabled',
                          {
                              onclick: () => {
                                  if (this.isSubmitButtonEnabled()) {
                                      const action = 'update';
                                      if (this.lastAction !== action) {
                                          document.dispatchEvent(new Event('action-selected'));
                                          this.actionSelected(action);
                                      }
    
                                      this.lastAction = action;
                                  }
                              }
                          },
                          [
                              spawn('input.form-control.action.update|type=\'radio\'',
                                    {
                                        id:   'updateAction',
                                        name: 'action',
                                        value: 'update'
                                    }),
                              spawn('label|for=\'updateAction\'', `${this.updateActionLabel()}`),
                          ])
                ]),
                get: () => {
                    const radioGroup = document.getElementsByName('action');
                    const selected = Array.from(radioGroup).find(radio => radio.checked);
                    return selected ? selected.value : null;
                },
            }
            
            this.subjectsSelector = {
                elements: spawn('div.subjects-component.form-component.col.containerItem.third',
                                { style: { display: 'none' } },
                                [
                                    spawn('label|for=\'subjects\'', 'Select Subjects'),
                                    spawn('select.form-control|', {
                                        id:       'subjects',
                                        name:     'subjects',
                                        multiple: true,
                                        onchange: async () => {
                                            document.dispatchEvent(new Event('subjects-selected'));
                            
                                            let subjectsEl = document.getElementById('subjects');
                                            let subjects = Array.from(subjectsEl.selectedOptions).map(option => option.text);
                            
                                            return this.subjectsSelected(subjects);
                                        },
                                    }),
                                ]),
                disable:  () => document.getElementById('subjects').disabled = true,
                enable:   () => document.getElementById('subjects').disabled = false,
                clear:   () => document.getElementById('subjects').value = '',
                hide: () => this.subjectsSelector.elements.style.display = 'none',
                show: () => this.subjectsSelector.elements.style.display = '',
                populate: async (project) => {
                    const element = document.getElementById('subjects');
                    
                    while (element.options.length > 0) {
                        element.options.remove(element.options.length - 1);
                    }
                    
                    if (this.isEmpty(project)) {
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
                                       element.options.add(new Option(subject['label'], subject['id']))
                                   });
                               })
                },
                populateAndShow: async (project) => {
                    return this.subjectsSelector.populate(project).then(() => this.subjectsSelector.show());
                }
            }
            
            return [
                this.actionSelector.elements,
                spawn('hr'),
                spawn('div.containerItem', [self.#instructions1, self.#instructions2]),
                spawn('hr'),
                spawn('div.row', [this.projectSelector, this.subjectsSelector.elements])
            ];
        }
        
        actionSelected(action) {
            document.querySelectorAll(`.form-component.action input.action`).forEach(input => {
                input.disabled = !input.classList.contains(action);
                input.checked = input.classList.contains(action);
            })
            
            document.querySelectorAll(`.form-component.action`).forEach(input => {
                if (input.classList.contains(action)) {
                    // Selected
                    input.classList.add('active');
                    input.classList.remove('disabled');
                    input.disabled = false;
                } else {
                    input.classList.add('disabled');
                    input.classList.remove('active');
                    input.disabled = true;
                }
            })
    
            document.querySelectorAll(`.instructions.action`).forEach(input => {
                if (input.classList.contains(action)) {
                    // Selected
                    input.style.display = '';
                } else {
                    input.style.display = 'none';
                }
            })
    
            this.updateData([{}, {}, {}, {}, {}]);
            this.clearAndHideMessage();
            this.subjectsSelector.clear();
            this.hot.selectCell(0, 0, 0, 0);
        }
        
        async subjectsSelected(subjects) {
            XNAT.ui.dialog.static.wait('Retrieving data from XNAT');
            
            // Copy the source data
            let hotData = JSON.parse(JSON.stringify(this.hot.getSourceData()));
            
            // Remove deselected subjects
            hotData = hotData.filter(row => subjects.contains(row[this.getSubjectColumnKey()]))
            
            // Add newly selected subjects
            let currentSubjects = hotData.map(row => row[this.getSubjectColumnKey()]);
            
            for (const subject of subjects) {
                if (!currentSubjects.contains(subject)) {
                    const data = this.actionSelector.get() === 'update' ? await this.getDataForSubject(subject) : this.getEmptyRow(subject);
                    hotData.push(...data);
                }
            }
            
            hotData = hotData.sort(XNAT.plugin.pixi.compareGenerator(this.getSubjectColumnKey()));
            
            this.updateData(hotData);
            this.updateHeight();
            
            this.validateCells();
    
            XNAT.ui.dialog.closeAll();
        }
        
        
        addKeyboardShortCuts() {
            const self = this;
            
            // Add new keyboard shortcut for inserting a row
            this.hot.updateSettings({
                                        afterDocumentKeyDown: function (e) {
                                            if (self.lastKey === 'Control' && e.key === 'n') {
                                                let row = self.hot.getSelected()[0][0];
                                                self.hot.alter('insert_row_above', (row + 1), 1)
                                            }
                                            self.lastKey = e.key;
                                        }
                                    });
        }
        
        removeKeyboardShortCuts() {
            const self = this;
            
            // Remove keyboard shortcut for inserting a row
            this.hot.updateSettings({
                                        afterDocumentKeyDown: function (e) {
                                            self.lastKey = e.key;
                                        }
                                    });
        }
        
        getProjectSelection() {
            return this.projectSelector.getElementsByTagName('select')[0].value;
        }
        
        disableProjectSelection() {
            this.projectSelector.getElementsByTagName('select')[0].disabled = true;
        }
        
        setProjectSelection(project) {
            let options = this.projectSelector.getElementsByTagName('option');
            
            for (let i = 0; i < options.length; i++) {
                let option = options[i];
                if (option.value === project) {
                    option.selected = true;
                    break;
                }
            }
        }
        
        validateProjectSelection() {
            if (this.getProjectSelection() === '') {
                this.projectSelector.classList.add('invalid')
                this.projectSelector.querySelector('.prj-error').style.display = '';
                return false;
            } else {
                this.projectSelector.classList.remove('invalid');
                this.projectSelector.querySelector('.prj-error').style.display = 'none'
                return true;
            }
        }
        
        async populateSubjectSelector() {
            return this.subjectsSelector.populateAndShow(this.getProjectSelection());
        }
        
        enableSubmitButton() {
            this.submitButton.disabled = false;
        }
        
        disableSubmitButton() {
            this.submitButton.disabled = true;
        }
        
        isSubmitButtonEnabled() {
            return !this.submitButton.disabled;
        }
        
        clearAndHideMessage() {
            this.messageComponent.style.display = 'none';
            this.messageComponent.innerHTML = '';
            this.messageComponent.classList.remove('success');
            this.messageComponent.classList.remove('error');
            this.messageComponent.classList.remove('warning');
            this.messageComponent.classList.remove('info');
        }
        
        displayMessage(type, message) {
            this.messageComponent.style.display = '';
            this.messageComponent.innerHTML = '';
            this.messageComponent.classList.add(type);
            this.messageComponent.append(message)
        }
        
        isHotEmpty() {
            return (this.hot.countEmptyRows() === this.hot.countRows())
        }
        
        updateData(data) {
            this.hot.updateData(data);
            this.updateHeight();
        }
        
        getColumns() {
            let settings = this.hot.getSettings();
            return settings['columns'];
        }
        
        getColumn(data) {
            let columns = this.getColumns();
            return columns.find(col => col['data'] === data);
        }
        
        updateColumns() {
            this.hot.updateSettings({ columns: this.getColumns() });
        }
        
        validateCells(callback) {
            if (!this.isHotEmpty()) {
                this.hot.validateCells(callback);
            }
        }
        
        removeEmptyRows() {
            for (let i = this.hot.countRows() - 1; i >= 0; i--) {
                if (this.hot.isEmptyRow(i)) {
                    this.hot.alter('remove_row', i, 1);
                }
            }
        }
        
        formatDate(inputDate) {
            let date, month, year;
            
            date = inputDate.getDate();
            month = inputDate.getMonth() + 1;
            year = inputDate.getFullYear();
            
            date = date
                .toString()
                .padStart(2, '0');
            
            month = month
                .toString()
                .padStart(2, '0');
            
            return `${month}/${date}/${year}`;
        }
        
        updateHeight() {
            const numRows = this.hot.countRows();
            const height = 26 + 23 * (numRows + 2);
            const container = this.container.querySelector('.hot-container');
            const minHeight = 200;
            
            container.style.height = height > minHeight ? `${height}px` : `${minHeight}px`;
            
            this.hot.render();
        }
    }
    
    console.debug('pixi-experimentManager.js - AbstractExperimentManager');
    
    XNAT.plugin.pixi.abstractExperimentManager = class AbstractExperimentManager extends XNAT.plugin.pixi.abstractDataManager {
        
        constructor(heading, subheading, description, instructions2) {
            super(heading, subheading, description, instructions2);
        }
        
        getSubjectColumnKey() {
            return 'subjectId'
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
                
                XNAT.ui.dialog.static.wait('Submitting to XNAT', { id: "submit" });
                
                let submissionReports = [];
                
                for (let iRow = 0; iRow < this.hot.countRows(); iRow++) {
                    await this.submitRow(iRow)
                              .then(submissionReport => submissionReports.push(submissionReport))
                              .catch(submissionReport => submissionReports.push(submissionReport));
                }
    
                let successfulRows = submissionReports.filter(submissionReport => !('error' in submissionReport));
                let failedRows = submissionReports.filter(submissionReport => 'error' in submissionReport);
                
                XNAT.ui.dialog.close('submit');
                
                document.dispatchEvent(new Event('submit'));
                
                // Disable new inputs to successful rows
                this.hot.updateSettings({
                                            cells:       function (row, col) {
                                                var cellProperties = {};
                        
                                                if (successfulRows.map(submissionReport => submissionReport['row']).contains(
                                                    row)) {
                                                    cellProperties.readOnly = true;
                                                }
                        
                                                return cellProperties;
                                            },
                                            contextMenu: ['copy', 'cut'],
                                        });
                
                this.removeKeyboardShortCuts();
                this.disableProjectSelection();
                this.subjectsSelector.disable();
                
                // TODO set experiment IDs after submit
                // experiments.forEach(experiment => {
                //     this.hot.setDataAtRowProp(experiment['row'], 'experimentId', experiment['experimentId']);
                // })
    
                let message;
                
                if (failedRows.length === 0) {
                    // Success
                    message = spawn('div.success', [
                        spawn('p', 'Review successful submissions:'),
                        spawn('ul', successfulRows.map(submissionReport => {
                            return spawn('li', [
                                spawn(`a`, {
                                    href:   submissionReport['url'],
                                    target: '_BLANK'
                                }, submissionReport['urlText'])
                            ])
                        }))
                    ])
                    
                    // Disable resubmissions
                    this.disableSubmitButton();
    
                    XNAT.ui.dialog.open({
                                            title: 'Success',
                                            content: message,
                                            closeBtn: true,
                                            buttons: [
                                                {
                                                    label: 'Return to Project',
                                                    isDefault: true,
                                                    close: false,
                                                    action: function (obj) {
                                                        XNAT.ui.dialog.closeAll();
                                                        window.location = XNAT.url.rootUrl('/data/projects/' + self.getProjectSelection());
                                                    }
                                                }
                                            ]
                                        });
                } else if (successfulRows.length === 0 && failedRows.length > 0) {
                    // All submissions in error
                    message = spawn('div', [
                        spawn('p', ''),
                        spawn('p', 'There were errors with your submission. Correct the issues and try resubmitting.'),
                        spawn('ul',
                              failedRows.map(submissionReport => spawn('li',
                                                                       `Row: ${submissionReport['row'] + 1} ${submissionReport['error']}`))),
                    ])
                    
                    this.displayMessage('error', message);
                } else if (successfulRows.length > 0 && failedRows.length > 0) {
                    // Some submitted successfully, some failed
                    message = spawn('div', [
                        spawn('p', 'There were errors with your submission. Correct the issues and try resubmitting.'),
                        spawn('p', 'Error(s):'),
                        spawn('ul',
                              failedRows.map(submissionReport => spawn('li',
                                                                       `Row: ${submissionReport['row'] + 1} ${submissionReport['error']}`))),
                        spawn('p', 'Successful submissions:'),
                        spawn('ul', successfulRows.map(submissionReport => spawn('li', [spawn(`a`, {
                            href:   submissionReport['url'],
                            target: '_BLANK'
                        }, submissionReport['urlText'])])))
                    ])
                    
                    this.displayMessage('warning', message);
                }
    

                
                XNAT.ui.dialog.close('submit');
                
            })
        }
        
        validateDate(date, callback) {
            let dateRegex = /^\d{1,2}\/\d{1,2}\/\d{4}$/;
            dateRegex.test(date) ? callback(true) : callback(false);
        }
        
        changeDate(dateColumn, changes, source) {
            if (changes) {
                changes.forEach(change => {
                    if (change[1] === dateColumn && change[3]) {
                        let date = new Date(change[3]);
                        if (!isNaN(date)) {
                            change[3] = date.toLocaleDateString();
                        }
                    }
                });
            }
        }
        
    }
    
}));
