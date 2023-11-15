console.debug('pixi-bli-importer.js')

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});
XNAT.plugin.pixi.bli = getObject(XNAT.plugin.pixi.bli || {});
XNAT.plugin.pixi.bli.importer = getObject(XNAT.plugin.pixi.bli.importer || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {

    const mappings = {
        get: async (name) => {
            console.debug(`Getting importer mappings ${name}`)

            const url = XNAT.url.csrfUrl(`/xapi/pixi/bli/import/mappings/${name}`);

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`Failed to get importer mapping ${name}`);
            }
        },
        getAll: async () => {
            console.debug(`Getting all importer mappings`)

            const url = XNAT.url.csrfUrl(`/xapi/pixi/bli/import/mappings`);

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`Failed to get importer mappings`);
            }
        },
        createOrUpdate: async (name, mapping) => {
            console.debug(`Updating importer mapping ${name}`)

            const url = XNAT.url.csrfUrl(`/xapi/pixi/bli/import/mappings/${name}`);

            const response = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(mapping)
            });

            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`Failed to update importer mapping ${name}`);
            }
        },
        delete: async (name) => {
            console.debug(`Deleting importer mapping ${name}`)

            const url = XNAT.url.csrfUrl(`/xapi/pixi/bli/import/mappings/${name}`);

            const response = await fetch(url, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to delete importer mapping ${name}`);
            }
        },
        getDefault: async () => {
            return XNAT.plugin.pixi.preferences.get('defaultBliImporterMapping')
                                               .then(preference => preference['defaultBliImporterMapping']);
        },
        setDefault: async (name) => {
            return XNAT.plugin.pixi.preferences.set('defaultBliImporterMapping', name);
        }
    }

    const manager =  (containerId) => {
        let container,
            footer;

        const init = () => {
            container = document.getElementById(containerId);
            container.innerHTML = '<div class="panel-body"><div class="loading"><i class="fa fa-spinner fa-spin"></i> Loading...</div></div>'

            container.style.display = 'flex';
            container.style.flexDirection = 'row';
            container.style.justifyContent = 'center';

            footer = container.closest('.panel').querySelector('.panel-footer');
            footer.innerHTML = '';

            footer.appendChild(newButton('Add', () => {
                console.debug('Add button clicked')
                editor();
            }));

            refresh();
        }

        const clear = () => {
            container.innerHTML = '';
        }

        const newButton = (label, action) => {
            return spawn('div', [
                spawn('div.pull-right', [
                    spawn('button.btn.btn-sm.submit', { html: label, onclick: action })
                ]),
                spawn('div.clear.clearFix')
            ])
        }

        const refresh = async () => {
            console.debug('refreshing');

            const mappings = await XNAT.plugin.pixi.bli.importer.mappings.getAll();

            if (mappings.length === 0) {
                clear();
                container.innerHTML = '<div class="panel-body left">No mappings found</div>';
                return;
            }

            return table(mappings);
        }

        const editor = async (item) => {
            console.debug('editor');

            const doWhat = item ? 'Update' : 'Create';
            const isNew = !item;
            item = item || {};

            let itemName = item?.name || '';

            XNAT.dialog.open({
                title: `${doWhat} BLI Importer Mapping`,
                content: spawn('form#editor-form'),
                maxBtn: true,
                width: 600,
                beforeShow: function() {
                    const formContainer = document.getElementById('editor-form');
                    formContainer.classList.add('panel');

                    let name = itemName;
                    let isDefault = false;
                    let subjectLabelField = item?.subjectLabelField || '';
                    let subjectLabelRegex = item?.subjectLabelRegex || '';
                    let hotelSession = item?.hotelSession || false;
                    let hotelSubjectSeparator = item?.hotelSubjectSeparator || '';
                    let sessionLabelField = item?.sessionLabelField || '';
                    let sessionLabelRegex = item?.sessionLabelRegex || '';
                    let scanLabelField = item?.scanLabelField || '';
                    let scanLabelRegex = item?.scanLabelRegex || '';

                    const nameElement =  XNAT.ui.panel.input.text({
                        label: 'Name',
                        id: 'name',
                        name: 'name',
                        description: 'A unique name for this mapping. Users will select this name when importing BLI data.',
                        value: name
                    }).element;

                    const isDefaultElement =  XNAT.ui.panel.input.checkbox({
                        label: 'Default',
                        id: 'defaultMapping',
                        name: 'defaultMapping',
                        description: 'If true, this mapping will be selected by default when importing BLI data.',
                        value: isDefault
                    }).element;

                    // Update the default mapping on opening the editor
                    XNAT.plugin.pixi.bli.importer.mappings.getDefault().then((defaultMapping) => {
                        if (defaultMapping === name) {
                            isDefaultElement.querySelector('input').checked = true;
                        }
                    });

                    const userLabelNameSetOptions = (selected) => {
                        return [
                            { value: '', label: 'Please select', selected: selected === '' },
                            { value: 'user', label: 'User', selected: selected === 'user' },
                            { value: 'group', label: 'Group', selected: selected === 'group' },
                            { value: 'experiment', label: 'Experiment', selected: selected === 'experiment' },
                            { value: 'comment1', label: 'Comment1', selected: selected === 'comment1' },
                            { value: 'comment2', label: 'Comment2', selected: selected === 'comment2' },
                            { value: 'timePoint', label: 'TimePoint', selected: selected === 'timePoint' },
                            { value: 'animalNumber', label: 'AnimalNumber', selected: selected === 'animalNumber' },
                            { value: 'sex', label: 'Sex', selected: selected === 'sex' },
                            { value: 'view', label: 'View', selected: selected === 'view' },
                            { value: 'clickNumber', label: 'ClickNumber', selected: selected === 'clickNumber' },
                        ]
                    };

                    const subjectLabelFieldElement = XNAT.ui.panel.select.single({
                        label: 'Subject Label Field',
                        id: 'subjectLabelField',
                        name: 'subjectLabelField',
                        description: 'The field in the AnalyzedClickInfo.txt file that contains the XNAT subject label.',
                        options: userLabelNameSetOptions(subjectLabelField),
                    });

                    const subjectLabelRegexElement =  XNAT.ui.panel.input.text({
                        label: 'Subject Label Regex',
                        id: 'subjectLabelRegex',
                        name: 'subjectLabelRegex',
                        description: 'A regex to extract the XNAT subject label from the specified field.',
                        value: subjectLabelRegex
                    }).element;

                    const hotelSessionElement =  XNAT.ui.panel.input.checkbox({
                        label: 'Hotel Session',
                        id: 'hotelSession',
                        name: 'hotelSession',
                        description: 'If true, the subject label will be be set to "Hotel".',
                        value: hotelSession
                    }).element;

                    const hotelSubjectSeparatorElement =  XNAT.ui.panel.input.text({
                        label: 'Hotel Subject Separator',
                        id: 'hotelSubjectSeparator',
                        name: 'hotelSubjectSeparator',
                        description: 'If specified, hotel subjects will be separated from the captured subject label by this string.',
                        value: hotelSubjectSeparator
                    }).element;

                    // Not using right now
                    hotelSubjectSeparatorElement.style.display = 'none';

                    hotelSessionElement.querySelector('input').addEventListener('change', function() {
                        hotelSubjectSeparatorElement.querySelector('input').disabled = !this.checked;
                    });

                    hotelSubjectSeparatorElement.querySelector('input').disabled = !hotelSessionElement.querySelector('input').checked;

                    const sessionLabelFieldElement =  XNAT.ui.panel.select.single({
                        label: 'Session Label Field',
                        id: 'sessionLabelField',
                        name: 'sessionLabelField',
                        description: 'The field in the AnalyzedClickInfo.txt file that contains the XNAT session label.',
                        options: userLabelNameSetOptions(sessionLabelField),
                    });

                    const sessionLabelRegexElement =  XNAT.ui.panel.input.text({
                        label: 'Session Label Regex',
                        id: 'sessionLabelRegex',
                        name: 'sessionLabelRegex',
                        description: 'A regex to extract the XNAT session label from the specified field.',
                        value: sessionLabelRegex
                    }).element;

                    const scanLabelFieldElement =  XNAT.ui.panel.select.single({
                        label: 'Scan Label Field',
                        id: 'scanLabelField',
                        name: 'scanLabelField',
                        description: 'The field in the AnalyzedClickInfo.txt file that contains the XNAT scan label.',
                        options: userLabelNameSetOptions(scanLabelField),
                    });

                    const scanLabelRegexElement = XNAT.ui.panel.input.text({
                        label: 'Scan Label Regex',
                        id: 'scanLabelRegex',
                        name: 'scanLabelRegex',
                        description: 'A regex to extract the XNAT scan label from the specified field.',
                        value: scanLabelRegex
                    }).element;

                    formContainer.appendChild(spawn('!', [
                        nameElement,
                        isDefaultElement,
                        subjectLabelFieldElement,
                        subjectLabelRegexElement,
                        hotelSessionElement,
                        hotelSubjectSeparatorElement,
                        sessionLabelFieldElement,
                        sessionLabelRegexElement,
                        scanLabelFieldElement,
                        scanLabelRegexElement
                    ]));
                },
                buttons: [
                    {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: function() {
                            console.debug('Save button clicked');

                            const form = document.getElementById('editor-form');

                            const nameElement = form.querySelector('#name');
                            const subjectLabelFieldElement = form.querySelector('#subjectLabelField');
                            const subjectLabelRegexElement = form.querySelector('#subjectLabelRegex');
                            const hotelSessionElement = form.querySelector('#hotelSession');
                            const hotelSubjectSeparatorElement = form.querySelector('#hotelSubjectSeparator');
                            const sessionLabelFieldElement = form.querySelector('#sessionLabelField');
                            const sessionLabelRegexElement = form.querySelector('#sessionLabelRegex');
                            const scanLabelFieldElement = form.querySelector('#scanLabelField');
                            const scanLabelRegexElement = form.querySelector('#scanLabelRegex');

                            const validators = [];

                            validators.push(
                                XNAT.validate(nameElement)
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Name is required')
                            );

                            validators.push(
                                XNAT.validate(subjectLabelFieldElement)
                                    .reset().chain()
                                    .required()
                                    .failure('Subject Label Field is required')
                            );

                            validators.push(
                                XNAT.validate(subjectLabelRegexElement)
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Subject Label Regex is required')
                            );

                            validators.push(
                                XNAT.validate(sessionLabelFieldElement)
                                    .reset().chain()
                                    .required()
                                    .failure('Session Label Field is required')
                            );

                            validators.push(
                                XNAT.validate(sessionLabelRegexElement)
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Session Label Regex is required')
                            );

                            validators.push(
                                XNAT.validate(scanLabelFieldElement)
                                    .reset().chain()
                                    .required()
                                    .failure('Scan Label Field is required')
                            );

                            validators.push(
                                XNAT.validate(scanLabelRegexElement)
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Scan Label Regex is required')
                            );

                            let errorMessages = [];

                            validators.forEach((validator) => {
                                if (!validator.check()) {
                                    validator.messages.forEach(message => errorMessages.push(message));
                                }
                            });

                            if (errorMessages.length > 0) {
                                XNAT.dialog.open({
                                    title: 'Error',
                                    width: 400,
                                    content: '<ul><li>' + errorMessages.join('</li><li>') + '</li></ul>',
                                })
                                return;
                            }

                            (async () => {

                                const mapping = {
                                    name: nameElement.value,
                                    subjectLabelField: subjectLabelFieldElement.value,
                                    subjectLabelRegex: subjectLabelRegexElement.value,
                                    hotelSession: hotelSessionElement.checked,
                                    hotelSubjectSeparator: hotelSubjectSeparatorElement.value,
                                    sessionLabelField: sessionLabelFieldElement.value,
                                    sessionLabelRegex: sessionLabelRegexElement.value,
                                    scanLabelField: scanLabelFieldElement.value,
                                    scanLabelRegex: scanLabelRegexElement.value,
                                };

                                try {
                                    await mappings.createOrUpdate(
                                        isNew ? mapping.name : itemName,
                                        mapping
                                    );

                                    if (document.getElementById('defaultMapping').checked) {
                                        await mappings.setDefault(mapping.name);
                                    } else {
                                        const defaultMapping = await mappings.getDefault();
                                        if (defaultMapping === mapping.name) {
                                            await mappings.setDefault('');
                                        }
                                    }

                                    XNAT.ui.banner.top(2000, 'Saved', 'success');
                                    XNAT.dialog.closeAll();
                                    refresh();
                                } catch (error) {
                                    XNAT.ui.banner.top(2000, 'Failed to save', 'error');
                                    console.error(error);
                                }
                            })();
                        }
                    },
                    {
                        label: 'Cancel',
                        close: true,
                    }
                ]
            })
        }

        const remove = async (name) => {
            console.debug('remove');

            return mappings.delete(name).then(() => {
                XNAT.ui.banner.top(2000, 'Deleted', 'success');
                refresh();
            }).catch((error) => {
                XNAT.ui.banner.top(2000, 'Failed to delete', 'error');
                console.error(error);
            });
        }

        const table = async (mappings) => {
            const defaultMapping = await XNAT.plugin.pixi.bli.importer.mappings.getDefault();

            // sort by mapping name
            mappings.sort((a, b) => {
                return a.name.toUpperCase() < b.name.toUpperCase() ? -1 : a.name.toUpperCase() > b.name.toUpperCase() ? 1 : 0;
            });

            const table = XNAT.table.dataTable(mappings, {
                header: true,
                sortable: 'name',
                columns: {
                    name: {
                        label: 'Name',
                        th: { className: 'left' },
                    },
                    default: {
                        label: 'Default',
                        th: { style: { width: '50px' } },
                        apply: function() {
                            return spawn('div.center', [
                                spawn('span', {}, this['name'] === defaultMapping ? '<i class="fa fa-check"></i>' : '')
                            ]);
                        }
                    },
                    actions: {
                        label: 'Actions',
                        th: { style: { width: '150px' } },
                        apply: function() {
                            return spawn('div.center', [
                                spawn(
                                    'button.btn.btn-sm',
                                    { onclick: () => editor(this) },
                                    '<i class="fa fa-pencil" title="Edit"></i>'
                                ),
                                spawn('span', { style: { display: 'inline-block', width: '10px' } }),
                                spawn('button.btn.btn-sm',
                                    { onclick: () => remove(this['name']) },
                                    '<i class="fa fa-trash" title="Delete"></i>'
                                ),
                            ]);
                        }
                    }
                }
            })

            table.element.style.width = '70%';

            clear();
            table.render(`#${containerId}`);
        }

        init();
    }

    XNAT.plugin.pixi.bli.importer = {
        mappings: mappings,
        manager: manager
    };

}));

