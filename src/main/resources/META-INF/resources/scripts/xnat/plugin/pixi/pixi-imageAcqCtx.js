/*
 *  PIXI Image Acquisition Context
 */

console.debug('pixi-imageAcqCtx.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});
XNAT.plugin.pixi.imageAcqCtx = getObject(XNAT.plugin.pixi.imageAcqCtx || {});
XNAT.plugin.pixi.imageAcqCtx.getConfig = getObject(XNAT.plugin.pixi.imageAcqCtx.getConfig || {});
XNAT.plugin.pixi.imageAcqCtx.fasting = getObject(XNAT.plugin.pixi.imageAcqCtx.fasting || {});
XNAT.plugin.pixi.imageAcqCtx.anesthesia = getObject(XNAT.plugin.pixi.imageAcqCtx.anesthesia || {});
XNAT.plugin.pixi.imageAcqCtx.heatingConditions = getObject(XNAT.plugin.pixi.imageAcqCtx.heatingConditions || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {
    
    XNAT.plugin.pixi.imageAcqCtx.configs = {
        get: async (type, scope, entityId) => {
            let url = `/xapi/pixi/image-acq-ctx-config/${type}`;
            
            if (scope && entityId) {
                url += `?scope=${scope}&entityId=${entityId}`
            } else if (scope) {
                url += `?scope=${scope}`
            } else {
                throw new Error('Missing scope and/or entityId');
            }
            
            url = XNAT.url.csrfUrl(url);
            let response = await fetch(url, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            })
            
            if (!response.ok) {
                throw new Error(response.statusText);
            }
            
            return response.json();
        },
        put: async (type, scope, entityId, config) => {
            let url = `/xapi/pixi/image-acq-ctx-config/${type}`;
            
            if (scope && entityId) {
                url += `?scope=${scope}&entityId=${entityId}`
            } else if (scope) {
                url += `?scope=${scope}`
            } else {
                throw new Error('Missing scope and/or entityId');
            }
            
            url = XNAT.url.csrfUrl(url);
            let response = await fetch(url, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(config)
            })
            
            if (!response.ok) {
                throw new Error(response.statusText);
            }
            
            return response.json();
        },
    };
    
    XNAT.plugin.pixi.imageAcqCtx.fasting = {
        get: async (scope, entityId) => {
            return XNAT.plugin.pixi.imageAcqCtx.configs.get('fasting', scope, entityId);
        },
        put: async (scope, entityId, fastingConfig) => {
            return XNAT.plugin.pixi.imageAcqCtx.configs.put('fasting', scope, entityId, fastingConfig);
        },
        manager: (containerId) => {
            let container,
                footer,
                config;
            
            const init = () => {
                container = document.getElementById(containerId);
                container.innerHTML = '';
                
                footer = container.closest('.panel').querySelector('.panel-footer');
                footer.innerHTML = '';
                
                footer.appendChild(newButton('Add', () => {
                    console.debug('Add button clicked');
                    editor();
                }));
                
                refresh();
            }
            
            const clearContainer = () => container.innerHTML = '';
            
            const newButton = (label, action) => {
                return spawn('div', [
                    spawn('div.pull-right', [
                        spawn('button.btn.btn-sm.submit', { html: label, onclick: action })
                    ]),
                    spawn('div.clear.clearFix')
                ])
            }
            
            const refresh = async () => {
                const scope = XNAT.data.context.project ? 'Project' : 'Site';
                const entityId = XNAT.data.context.project || null;
                config = await XNAT.plugin.pixi.imageAcqCtx.fasting.get(scope, entityId);
                
                if (config['fastingTemplates'].length === 0) {
                    clearContainer();
                    container.innerHTML = '<p>No fasting templates defined.</p>'
                    return;
                }
                
                return table(config);
            }
            
            const remove = async (templateName) => {
                const scope = XNAT.data.context.project ? 'Project' : 'Site';
                const entityId = XNAT.data.context.project || null;
                
                const newTemplates = config['fastingTemplates'].filter(template => template.name !== templateName);
                
                config['fastingTemplates'] = newTemplates;
                
                await XNAT.plugin.pixi.imageAcqCtx.fasting.put(scope, entityId, config);
                
                refresh();
            }
            
            const editor = (item) => {
                const doWhat = item ? 'Update' : 'Create';
                const isNew = !item;
                item = item || {};
                
                XNAT.dialog.open({
                    title: `${doWhat} Fasting Template`,
                    content: spawn('form#template-form'),
                    maxBtn: true,
                    width: 600,
                    beforeShow: function () {
                        const formContainer = document.getElementById('template-form');
                        formContainer.classList.add('panel');
                        
                        let templateName = isNew ? null : item.name || null;
                        let defaultTemplate = isNew ? false : item.defaultTemplate || false;
                        let fastingStatus = isNew ? false : item.fasting.fastingStatus || false;
                        let fastingDuration = isNew ? null : item.fasting.fastingDuration || null;
                        
                        const fastingTemplateNameElement = XNAT.ui.panel.input.text({
                            label: 'Fasting Template Name',
                            id: 'templateName',
                            name: 'templateName',
                            validation: 'required',
                            description: 'The name of this fasting template.',
                            value: templateName,
                        }).element;
                        
                        const defaultTemplateElement = XNAT.ui.panel.input.checkbox({
                            label: 'Default Template',
                            id: 'defaultTemplate',
                            name: 'defaultTemplate',
                            description: 'Whether or not this is the default fasting template.',
                            value: defaultTemplate,
                        });
                        
                        const fastingStatusElement = XNAT.ui.panel.input.checkbox({
                            label: 'Fasting Status',
                            id: 'fastingStatus',
                            name: 'fastingStatus',
                            description: 'Whether or not fasting occurs.',
                            value: fastingStatus,
                        }).element;
                        
                        const fastingDurationElement = XNAT.ui.panel.input.text({
                            label: 'Fasting Duration (hours)',
                            id: 'fastingDuration',
                            name: 'fastingDuration',
                            description: 'The duration of the fasting period.',
                            value: fastingDuration,
                        }).element;
                        
                        // null values are being converted to 'null' strings, resulting in validation errors
                        // this is a workaround
                        fastingTemplateNameElement.querySelector('input').value = templateName;
                        fastingDurationElement.querySelector('input').value = fastingDuration;
                        
                        if (!isNew) {
                            fastingTemplateNameElement.querySelector('input').disabled = true;
                        }
                        
                        fastingDurationElement.querySelector('input').disabled = !fastingStatus;
                        
                        // required fields, not sure how to do this in the XNAT.ui.panel.input methods
                        fastingTemplateNameElement.querySelector('input').required = true;

                        fastingStatusElement.querySelector('input').addEventListener('change', function () {
                            const input = fastingDurationElement.querySelector('input');
                            input.disabled = !this.checked;
                            input.value = this.checked ? input.value : null;
                        });
                        
                        formContainer.appendChild(spawn('!', [
                            fastingTemplateNameElement,
                            defaultTemplateElement,
                            fastingStatusElement,
                            fastingDurationElement,
                        ]));
                        
                    },
                    buttons: [
                        {
                            label: 'Save',
                            isDefault: true,
                            close: false,
                            action: function () {
                                console.debug('Save');
                                const form = document.getElementById('template-form');
                                
                                if (!form.checkValidity()) {
                                    form.reportValidity();
                                    return;
                                } else {
                                    const templateName = document.getElementById('templateName').value;
                                    const defaultTemplate = document.getElementById('defaultTemplate').checked;
                                    const fastingStatus = document.getElementById('fastingStatus').checked;
                                    const fastingDuration = document.getElementById('fastingDuration').value;
                                    
                                    // if this is the default template, set all other templates to not be the default
                                    if (defaultTemplate) {
                                        config['fastingTemplates'].forEach((template) => {
                                            template.defaultTemplate = false;
                                        });
                                    }
                                    
                                    if (isNew) {
                                        config['fastingTemplates'].push({
                                            name: templateName,
                                            defaultTemplate: defaultTemplate,
                                            fasting: {
                                                fastingStatus: fastingStatus,
                                                fastingDuration: fastingDuration,
                                            }
                                        });
                                    } else {
                                        config['fastingTemplates'].forEach((template) => {
                                            if (template.name === item.name) {
                                                template.name = templateName;
                                                template.defaultTemplate = defaultTemplate;
                                                template.fasting.fastingStatus = fastingStatus;
                                                template.fasting.fastingDuration = fastingDuration;
                                            }
                                        });
                                    }
                                    
                                    const scope = XNAT.data.context.project ? 'Project' : 'Site';
                                    const entityId = XNAT.data.context.project || null;
                                    
                                    XNAT.plugin.pixi.imageAcqCtx.fasting.put(scope, entityId, config)
                                        .then((response) => {
                                            XNAT.ui.banner.top(2000, 'Fasting template saved successfully', 'success');
                                            refresh();
                                            XNAT.dialog.closeAll();
                                        })
                                        .catch((error) => {
                                            console.error('Error', error);
                                            XNAT.ui.banner.top(2000, 'Fasting template could not be saved', 'error');
                                        });
                                }
                            }
                        },
                        {
                            label: 'Cancel',
                            close: true
                        }
                    ]
                });
            }
            
            const table = async (data) => {
                const templateTable = XNAT.table.dataTable(data['fastingTemplates'], {
                    header: true,
                    sortable: 'name',
                    columns: {
                        name: {
                            label: 'Template Name',
                        },
                        fastingStatus: {
                            label: 'Fasting Status',
                            apply: function () {
                                return spawn('div.center', this['fasting']['fastingStatus'] ? 'Yes' : 'No');
                            }
                        },
                        fastingDuration: {
                            label: 'Fasting Duration (hours)',
                            apply: function () {
                                return spawn('div.center', this['fasting']['fastingDuration'] ? this['fasting']['fastingDuration'] : '');
                            }
                        },
                        defaultTemplate: {
                            label: 'Default',
                            apply: function () {
                                return spawn('div.center', this['defaultTemplate'] ? 'X' : ' ');
                            }
                        },
                        actions: {
                            label: 'Actions',
                            th: { style: { width: '150px' } },
                            apply: function () {
                                return spawn('div.center', [
                                    spawn('button.btn.btn-sm',
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
                
                clearContainer();
                templateTable.render(`#${containerId}`);
            }
            
            init();
            
            return {
                refresh: refresh
            }
        },
    }
    
    XNAT.plugin.pixi.imageAcqCtx.anesthesia = {
        get: async (scope, entityId) => {
            return XNAT.plugin.pixi.imageAcqCtx.configs.get('anesthesia', scope, entityId);
        },
        put: async (scope, entityId, config) => {
            return XNAT.plugin.pixi.imageAcqCtx.configs.put('anesthesia', scope, entityId, config);
        },
        manager: (containerId) => {
            let container,
                footer,
                config;
            
            const init = () => {
                container = document.getElementById(containerId);
                container.innerHTML = '';
                
                footer = container.closest('.panel').querySelector('.panel-footer');
                footer.innerHTML = '';
                
                footer.appendChild(newButton('Add', () => {
                    console.debug('Add button clicked');
                    editor();
                }));
                
                refresh();
            }
            
            const clearContainer = () => container.innerHTML = '';
            
            const newButton = (label, action) => {
                return spawn('div', [
                    spawn('div.pull-right', [
                        spawn('button.btn.btn-sm.submit', { html: label, onclick: action })
                    ]),
                    spawn('div.clear.clearFix')
                ])
            }
            
            const refresh = async () => {
                const scope = XNAT.data.context.project ? 'Project' : 'Site';
                const entityId = XNAT.data.context.project || null;
                config = await XNAT.plugin.pixi.imageAcqCtx.anesthesia.get(scope, entityId);
                
                if (config['anesthesiaTemplates'].length === 0) {
                    clearContainer();
                    container.innerHTML = '<p>No anesthesia templates defined.</p>'
                    return;
                }
                
                return table(config);
            }
            
            const remove = async (templateName) => {
                const scope = XNAT.data.context.project ? 'Project' : 'Site';
                const entityId = XNAT.data.context.project || null;
                
                config['anesthesiaTemplates'] = config['anesthesiaTemplates'].filter(template => template.name !== templateName);
                
                await XNAT.plugin.pixi.imageAcqCtx.anesthesia.put(scope, entityId, config);
                
                refresh();
            }
            
            const editor = (item) => {
                const doWhat = item ? 'Update' : 'Create';
                const isNew = !item;
                item = item || {};
                
                XNAT.dialog.open({
                    title: `${doWhat} Anesthesia Template`,
                    content: spawn('form#template-form'),
                    maxBtn: true,
                    width: 600,
                    beforeShow: function () {
                        const formContainer = document.getElementById('template-form');
                        formContainer.classList.add('panel');
                        
                        let templateName = isNew ? null : item.name || null;
                        let defaultTemplate = isNew ? false : item.defaultTemplate || false;
                        let anesthesia = isNew ? null : item.anesthesia.anesthesia || null;
                        let routeOfAdministration = isNew ? null : item.anesthesia.routeOfAdministration || null;
                        
                        const templateNameElement = XNAT.ui.panel.input.text({
                            label: 'Anesthesia Template Name',
                            id: 'templateName',
                            name: 'templateName',
                            validation: 'required',
                            description: 'The name of this anesthesia template.',
                            value: templateName,
                        }).element;
                        
                        const defaultTemplateElement = XNAT.ui.panel.input.checkbox({
                            label: 'Default Template',
                            id: 'defaultTemplate',
                            name: 'defaultTemplate',
                            description: 'Whether or not this is the default anesthesia template.',
                            value: defaultTemplate,
                        });
                        
                        const anesthesiaDataList = spawn('datalist#anesthesias-list', [
                            spawn('option', { value: 'Alphachloralose' }),
                            spawn('option', { value: 'Alphadolone' }),
                            spawn('option', { value: 'Alphaxalone' }),
                            spawn('option', { value: 'Azaperone' }),
                            spawn('option', { value: 'Butabarbital' }),
                            spawn('option', { value: 'Chloral hydrate' }),
                            spawn('option', { value: 'Diazepam' }),
                            spawn('option', { value: 'Droperidol' }),
                            spawn('option', { value: 'Etomidate' }),
                            spawn('option', { value: 'Fluanisone' }),
                            spawn('option', { value: 'Ketamine' }),
                            spawn('option', { value: 'Methohexital' }),
                            spawn('option', { value: 'Metomidate' }),
                            spawn('option', { value: 'Midazolam' }),
                            spawn('option', { value: 'Pentobarbital' }),
                            spawn('option', { value: 'Propofol' }),
                            spawn('option', { value: 'Thiamylal' }),
                            spawn('option', { value: 'Thiopental' }),
                            spawn('option', { value: 'Tiletamine' }),
                            spawn('option', { value: 'Tribromoethanol' }),
                            spawn('option', { value: 'Urethane (ethyl carbamate)' }),
                            spawn('option', { value: 'Xylazine' }),
                            spawn('option', { value: 'Zolazepam' }),
                            spawn('option', { value: 'Carbon dioxide' }),
                            spawn('option', { value: 'Chloroform' }),
                            spawn('option', { value: 'Desflurane' }),
                            spawn('option', { value: 'Diethyl ether' }),
                            spawn('option', { value: 'Enflurane' }),
                            spawn('option', { value: 'HalothaneHalothane' }),
                            spawn('option', { value: 'Isoflurane' }),
                            spawn('option', { value: 'Methoxyflurane' }),
                            spawn('option', { value: 'Sevoflurane' }),
                            spawn('option', { value: 'Acepromazine' }),
                            spawn('option', { value: 'Chlorpromazine' }),
                            spawn('option', { value: 'Succinylcholine' }),
                            spawn('option', { value: 'Pancuronium' }),
                            spawn('option', { value: 'Bupivacaine' }),
                            spawn('option', { value: 'Lidocaine + Prilocaine' }),
                            spawn('option', { value: 'Lidocaine' })
                        ]);
                        
                        const anesthesiaElement = spawn('div.panel-element', [
                            spawn('label.element-label|for=anesthesia', 'Anesthesia Used'),
                            spawn('div.element-wrapper', [
                                spawn('input|list=anesthesias-list', {
                                    id: 'anesthesia',
                                    name: 'anesthesia',
                                    value: anesthesia,
                                }),
                                spawn('div.description', 'Specify the anesthesia used'),
                            ])
                        ]);
                        
                        const routesOfAdministrationDataList = spawn('datalist#routes-of-administration-list', [
                            spawn('option', { value: 'Intraperitoneal route' }),
                            spawn('option', { value: 'By inhalation' }),
                            spawn('option', { value: 'Intravenous route' }),
                            spawn('option', { value: 'Per rectum' }),
                            spawn('option', { value: 'Intramuscular route' })
                        ]);
                        
                        const routeOfAdministrationElement = spawn('div.panel-element', [
                            spawn('label.element-label|for=routeOfAdministration', 'Route of Administration'),
                            spawn('div.element-wrapper', [
                                spawn('input|list=routes-of-administration-list', {
                                    id: 'routeOfAdministration',
                                    name: 'routeOfAdministration',
                                    value: routeOfAdministration,
                                }),
                                spawn('div.description', 'Specify the route of administration of the anesthesia.'),
                            ])
                        ]);
                        
                        // null values are being converted to 'null' strings, resulting in validation errors
                        // this is a workaround
                        templateNameElement.querySelector('input').value = templateName;
                        anesthesiaElement.querySelector('input').value = anesthesia;
                        routeOfAdministrationElement.querySelector('input').value = routeOfAdministration;
                        
                        if (!isNew) {
                            templateNameElement.querySelector('input').disabled = true;
                        }
                        
                        // required fields, not sure how to do this in the XNAT.ui.panel.input methods
                        templateNameElement.querySelector('input').required = true;

                        formContainer.appendChild(spawn('!', [
                            templateNameElement,
                            defaultTemplateElement,
                            anesthesiaDataList,
                            anesthesiaElement,
                            routesOfAdministrationDataList,
                            routeOfAdministrationElement,
                        ]));
                    },
                    buttons: [
                        {
                            label: 'Save',
                            isDefault: true,
                            close: false,
                            action: function () {
                                console.debug('Save');
                                const form = document.getElementById('template-form');
                                
                                if (!form.checkValidity()) {
                                    form.reportValidity();
                                    return;
                                } else {
                                    const templateName = document.getElementById('templateName').value;
                                    const defaultTemplate = document.getElementById('defaultTemplate').checked;
                                    const anesthesia = document.getElementById('anesthesia').value;
                                    const routeOfAdministration = document.getElementById('routeOfAdministration').value;
                                    
                                    // if this is the default template, set all other templates to not be the default
                                    if (defaultTemplate) {
                                        config.anesthesiaTemplates.forEach(template => {
                                            template.defaultTemplate = false;
                                        });
                                    }
                                    
                                    if (isNew) {
                                        config.anesthesiaTemplates.push({
                                            name: templateName,
                                            defaultTemplate: defaultTemplate,
                                            anesthesia: {
                                                anesthesia: anesthesia,
                                                routeOfAdministration: routeOfAdministration,
                                            }
                                        });
                                    } else {
                                        const template = config.anesthesiaTemplates.find(template => template.name === templateName);
                                        template.defaultTemplate = defaultTemplate;
                                        template.anesthesia.anesthesia = anesthesia;
                                        template.anesthesia.routeOfAdministration = routeOfAdministration;
                                    }
                                }
                                
                                const scope = XNAT.data.context.project ? 'Project' : 'Site';
                                const entityId = XNAT.data.context.project || null;
                                
                                XNAT.plugin.pixi.imageAcqCtx.anesthesia.put(scope, entityId, config)
                                    .then(() => {
                                        XNAT.ui.banner.top(2000, 'Anesthesia template saved successfully', 'success');
                                        refresh();
                                        XNAT.dialog.closeAll();
                                    })
                                    .catch(err => {
                                        console.error(err);
                                        XNAT.ui.banner.top(2000, 'Anesthesia template could not be saved', 'error');
                                    });
                            }
                        },
                        {
                            label: 'Cancel',
                            close: true
                        }
                    ]
                });
            }
            
            const table = (data) => {
                const templateTable = XNAT.table.dataTable(data['anesthesiaTemplates'], {
                    header: true,
                    sortable: 'name',
                    columns: {
                        name: {
                            label: 'Template Name',
                        },
                        anesthesia: {
                            label: 'Anesthesia',
                            apply: function () {
                                return spawn('div.center', this['anesthesia']['anesthesia'] ? this['anesthesia']['anesthesia'] : '');
                            }
                        },
                        routeOfAdministration: {
                            label: 'Route Of Administration',
                            apply: function () {
                                return spawn('div.center', this['anesthesia']['routeOfAdministration'] ? this['anesthesia']['routeOfAdministration'] : '');
                            }
                        },
                        defaultTemplate: {
                            label: 'Default',
                            apply: function () {
                                return spawn('div.center', this['defaultTemplate'] ? 'X' : ' ');
                            }
                        },
                        actions: {
                            label: 'Actions',
                            th: { style: { width: '150px' } },
                            apply: function () {
                                return spawn('div.center', [
                                    spawn('button.btn.btn-sm',
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
                
                clearContainer();
                templateTable.render(`#${containerId}`);
            }
            
            init();
            
            return {
                refresh: refresh
            }
        }
    }
    
    XNAT.plugin.pixi.imageAcqCtx.heatingConditions = {
        get: async (scope, entityId) => {
            console.debug(`Getting heating conditions config for ${scope} ${entityId}`);
            return XNAT.plugin.pixi.imageAcqCtx.configs.get('heating-conditions', scope, entityId);
        },
        put: async (scope, entityId, config) => {
            console.debug(`Saving heating conditions config for ${scope} ${entityId}`);
            return XNAT.plugin.pixi.imageAcqCtx.configs.put('heating-conditions', scope, entityId, config);
        },
        manager: (containerId) => {
            console.debug(`Initializing heating conditions manager`);
            let container,
                footer,
                config;
            
            const init = () => {
                container = document.getElementById(containerId);
                container.innerHTML = '';
                
                footer = container.closest('.panel').querySelector('.panel-footer');
                footer.innerHTML = '';
                
                footer.appendChild(newButton('Add', () => {
                    console.debug('Add button clicked');
                    editor();
                }));
                
                refresh();
            }
            
            const clearContainer = () => container.innerHTML = '';
            
            const newButton = (label, action) => {
                return spawn('div', [
                    spawn('div.pull-right', [
                        spawn('button.btn.btn-sm.submit', { html: label, onclick: action })
                    ]),
                    spawn('div.clear.clearFix')
                ])
            }
            
            const refresh = async () => {
                const scope = XNAT.data.context.project ? 'Project' : 'Site';
                const entityId = XNAT.data.context.project || null;
                config = await XNAT.plugin.pixi.imageAcqCtx.heatingConditions.get(scope, entityId);
                
                if (config['templates'].length === 0) {
                    clearContainer();
                    container.innerHTML = '<p>No heating condition templates defined.</p>'
                    return;
                }
                
                return table(config);
            }
            
            const remove = async (templateName) => {
                const scope = XNAT.data.context.project ? 'Project' : 'Site';
                const entityId = XNAT.data.context.project || null;
                
                config['templates'] = config['templates'].filter(template => template.name !== templateName);
                
                await XNAT.plugin.pixi.imageAcqCtx.heatingConditions.put(scope, entityId, config);
                await refresh();
            }
            
            const editor = (item) => {
                const doWhat = item ? 'Update' : 'Create';
                const isNew = !item;
                item = item || {};
                
                XNAT.dialog.open({
                    title: `${doWhat} Heating Conditions Template`,
                    content: spawn('form#template-form'),
                    maxBtn: true,
                    width: 600,
                    beforeShow: function () {
                        const formContainer = document.getElementById('template-form');
                        formContainer.classList.add('panel');
                        
                        let templateName = isNew ? '' : item.name || '';
                        let defaultTemplate = isNew ? false : item.defaultTemplate || false;
                        let procedurePhase = isNew ? '' : item.heatingConditions.procedurePhase || '';
                        let heatingMethod = isNew ? '' : item.heatingConditions.heatingMethod || '';
                        let feedbackTemperatureRegulation =
                                  isNew ? '' :
                                  item.heatingConditions.feedbackTemperatureRegulation === true ? '1' :
                                  item.heatingConditions.feedbackTemperatureRegulation === false ? '0' :
                                  '';
                        let temperatureSensorDeviceComponent = isNew ? '' : item.heatingConditions.temperatureSensorDeviceComponent || '';
                        let setpointTemperature = isNew ? '' : item.heatingConditions.setpointTemperature || '';
                        
                        const templateNameElement = XNAT.ui.panel.input.text({
                            label: 'Template Name',
                            id: 'templateName',
                            name: 'templateName',
                            validation: 'required',
                            description: 'The name of this heating conditions template.',
                            value: templateName,
                        }).element;
                        
                        const defaultTemplateElement = XNAT.ui.panel.input.checkbox({
                            label: 'Default Template',
                            id: 'defaultTemplate',
                            name: 'defaultTemplate',
                            description: 'Whether or not this is the default heating conditions template.',
                            value: defaultTemplate,
                        });
                        
                        // select element
                        const procedurePhaseElement = XNAT.ui.panel.select.single({
                            id: 'procedurePhase',
                            name: 'procedurePhase',
                            label: 'Procedure Phase',
                            description: 'The phase of the procedure during which these heating conditions are applied.',
                            options: [
                                { value: '', label: '(SELECT)', selected: procedurePhase === '' },
                                { value: 'Intraoperative', label: 'Intraoperative' , selected: procedurePhase === 'Intraoperative' },
                                { value: 'Preoperative', label: 'Preoperative' , selected: procedurePhase === 'Preoperative' },
                                { value: 'Postoperative', label: 'Postoperative' , selected: procedurePhase === 'Postoperative' },
                            ]
                        });
                        
                        const heatingMethodsDataList = spawn('datalist#heating-methods-list', [
                            spawn('option', { value: 'Air heating pad' }),
                            spawn('option', { value: 'Electric blanket' }),
                            spawn('option', { value: 'Electric heating pad' }),
                            spawn('option', { value: 'Forced air heater' }),
                            spawn('option', { value: 'Forced air warming blanket' }),
                            spawn('option', { value: 'Heated imaging device' }),
                            spawn('option', { value: 'Heated patient support' }),
                            spawn('option', { value: 'Heated water blanket' }),
                            spawn('option', { value: 'Heat lamp' }),
                            spawn('option', { value: 'Non-electric heating pad' }),
                            spawn('option', { value: 'Pre-heated pad' }),
                            spawn('option', { value: 'Unheated' }),
                            spawn('option', { value: 'Warmer device' }),
                            spawn('option', { value: 'Warming blanket' }),
                        ]);
                        
                        const heatingMethodElement = spawn('div.panel-element', [
                            spawn('label.element-label|for=heatingMethod', 'Heating Method'),
                            spawn('div.element-wrapper', [
                                spawn('input|list=heating-methods-list', {
                                    id: 'heatingMethod',
                                    name: 'heatingMethod',
                                    value: heatingMethod,
                                }),
                                spawn('div.description', 'Specify the heating method used'),
                            ])
                        ]);
                        
                        const feedbackTemperatureRegulationElement = XNAT.ui.panel.select.single({
                            label: 'Feedback Temperature Regulation',
                            id: 'feedbackTemperatureRegulation',
                            name: 'feedbackTemperatureRegulation',
                            description: 'Indicates whether temperature is regulated by feedback from a temperature sensor used to control an active heating or cooling device.',
                            options: [
                                { value: '', label: 'Select...', selected: feedbackTemperatureRegulation === '' },
                                { value: '1', label: 'Yes', selected: feedbackTemperatureRegulation === '1' },
                                { value: '0', label: 'No', selected: feedbackTemperatureRegulation === '0' },
                            ]
                        });
                        
                        const temperatureSensorDeviceComponentsDataList = spawn('datalist#temperature-sensor-device-components-list', [
                            spawn('option', { value: 'Rectal temperature' }),
                            spawn('option', { value: 'Thermography' }),
                            spawn('option', { value: 'Carrier temperature sensor' }),
                        ]);
                        
                        const temperatureSensorDeviceComponentElement = spawn('div.panel-element', [
                            spawn('label.element-label|for=temperatureSensorDeviceComponent', 'Temperature Sensor Device Component'),
                            spawn('div.element-wrapper', [
                                spawn('input|list=temperature-sensor-device-components-list', {
                                    id: 'temperatureSensorDeviceComponent',
                                    name: 'temperatureSensorDeviceComponent',
                                    value: temperatureSensorDeviceComponent,
                                }),
                                spawn('div.description', 'Specify the temperature sensor device component used'),
                            ])
                        ]);
                        
                        const setpointTemperatureElement = XNAT.ui.panel.input.number({
                            label: 'Setpoint Temperature',
                            id: 'setpointTemperature',
                            name: 'setpointTemperature',
                            description: 'The temperature setpoint for the heating conditions.',
                            value: setpointTemperature,
                            step: "0.01",
                        }).element;
                        
                        if (!isNew) {
                            templateNameElement.querySelector('input').disabled = true;
                        }

                        // required fields, not sure how to do this in the XNAT.ui.panel.input methods
                        templateNameElement.querySelector('input').required = true;

                        formContainer.appendChild(spawn('!', [
                            templateNameElement,
                            defaultTemplateElement,
                            procedurePhaseElement,
                            heatingMethodsDataList,
                            heatingMethodElement,
                            feedbackTemperatureRegulationElement,
                            temperatureSensorDeviceComponentsDataList,
                            temperatureSensorDeviceComponentElement,
                            setpointTemperatureElement,
                        ]));
                    },
                    buttons: [
                        {
                            label: 'Save',
                            isDefault: true,
                            close: false,
                            action: function () {
                                console.debug('Save');
                                const form = document.getElementById('template-form');
                                
                                if (!form.checkValidity()) {
                                    form.reportValidity();
                                    return;
                                } else {
                                    const templateName = document.getElementById('templateName').value;
                                    const defaultTemplate = document.getElementById('defaultTemplate').checked;
                                    const procedurePhase = document.getElementById('procedurePhase').value;
                                    const heatingMethod = document.getElementById('heatingMethod').value;
                                    const feedbackTemperatureRegulation = document.getElementById(
                                        'feedbackTemperatureRegulation').value;
                                    const temperatureSensorDeviceComponent = document.getElementById(
                                        'temperatureSensorDeviceComponent').value;
                                    const setpointTemperature = document.getElementById('setpointTemperature').value;
                                    
                                    // if this is the default template, set all other templates to not be the default
                                    if (defaultTemplate) {
                                        config.templates.forEach(template => {
                                            template.defaultTemplate = false;
                                        });
                                    }
                                    
                                    if (isNew) {
                                        config.templates.push({
                                            name: templateName,
                                            defaultTemplate: defaultTemplate,
                                            heatingConditions: {
                                                procedurePhase: procedurePhase,
                                                heatingMethod: heatingMethod,
                                                feedbackTemperatureRegulation:
                                                    feedbackTemperatureRegulation === '1' ? true :
                                                    feedbackTemperatureRegulation === '0' ? false :
                                                    null,
                                                temperatureSensorDeviceComponent: temperatureSensorDeviceComponent,
                                                setpointTemperature: setpointTemperature,
                                            }
                                        });
                                    } else {
                                        const template = config.templates.find(template => template.name === templateName);
                                        template.defaultTemplate = defaultTemplate;
                                        template.heatingConditions.procedurePhase = procedurePhase;
                                        template.heatingConditions.heatingMethod = heatingMethod;
                                        template.heatingConditions.feedbackTemperatureRegulation =
                                            feedbackTemperatureRegulation === '1' ? true :
                                            feedbackTemperatureRegulation === '0' ? false :
                                            null;
                                        template.heatingConditions.temperatureSensorDeviceComponent = temperatureSensorDeviceComponent;
                                        template.heatingConditions.setpointTemperature = setpointTemperature;
                                    }
                                    
                                    const scope = XNAT.data.context.project ? 'Project' : 'Site';
                                    const entityId = XNAT.data.context.project || null;
                                    
                                    XNAT.plugin.pixi.imageAcqCtx.heatingConditions.put(scope, entityId, config)
                                        .then(() => {
                                            XNAT.ui.banner.top(2000,
                                                'Heating conditions template saved successfully',
                                                'success'
                                            );
                                            refresh();
                                            XNAT.dialog.closeAll();
                                        })
                                        .catch(err => {
                                            console.error(err);
                                            XNAT.ui.banner.top(2000,
                                                'Heating conditions template could not be saved',
                                                'error'
                                            );
                                        });
                                }
                            }
                        },
                        {
                            label: 'Cancel',
                            close: true,
                        }
                    ]
                });
            }
            
            const table = async (data) => {
                const templateTable = XNAT.table.dataTable(data['templates'], {
                    header: true,
                    sortable: 'name',
                    columns: {
                        name: {
                            label: 'Template Name',
                        },
                        heatingMethod: {
                            label: 'Heating Method',
                            apply: function () {
                                return spawn('div.center', this['heatingConditions']['heatingMethod'] ? this['heatingConditions']['heatingMethod'] : '');
                            }
                        },
                        feedbackTemperatureRegulation: {
                            label: 'Temp Regulation',
                            apply: function () {
                                return spawn('div.center',
                                    this['heatingConditions']['feedbackTemperatureRegulation'] ? 'Yes' :
                                    this['heatingConditions']['feedbackTemperatureRegulation'] === false ? 'No' : ''
                                );
                            }
                        },
                        temperatureSensorDeviceComponent: {
                            label: 'Temp Sensor Device',
                            apply: function () {
                                return spawn('div.center',
                                    this['heatingConditions']['temperatureSensorDeviceComponent'] ? this['heatingConditions']['temperatureSensorDeviceComponent'] : ''
                                );
                            }
                        },
                        setpointTemperature: {
                            label: 'Setpoint Temp (&#8451;)',
                            apply: function () {
                                return spawn('div.center',
                                    this['heatingConditions']['setpointTemperature'] ? this['heatingConditions']['setpointTemperature']: ''
                                );
                            }
                        },
                        defaultTemplate: {
                            label: 'Default',
                            apply: function () {
                                return spawn('div.center', this['defaultTemplate'] ? 'X' : ' ');
                            }
                        },
                        actions: {
                            label: 'Actions',
                            th: { style: { width: '150px' } },
                            apply: function () {
                                return spawn('div.center', [
                                    spawn('button.btn.btn-sm',
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
                
                clearContainer();
                templateTable.render(`#${containerId}`);
            }
            
            
            init();
            
            return {
                refresh: refresh
            }
        }
    }
    
}));