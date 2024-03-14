console.log('pixi-animalEndpoints.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});
XNAT.plugin.pixi.animalEndpoints = getObject(XNAT.plugin.pixi.animalEndpoints || {});

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

    XNAT.plugin.pixi.animalEndpoints = {
        endpoints: new Set(),
        url: `/xapi/pixi/preferences/endpoints`,
        get: async function() {
            console.debug('XNAT.plugin.pixi.endpoints.get()');

            const url = XNAT.url.csrfUrl(`${this.url}`);

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error("Failed to get endpoints.");
            }

            let data = await response.json();
            XNAT.plugin.pixi.animalEndpoints.endpoints = new Set(data['endpoints'])
            return XNAT.plugin.pixi.animalEndpoints.endpoints;
        },
        add: async function(endpoint) {
            console.debug('XNAT.plugin.pixi.endpoints.add()');

            XNAT.plugin.pixi.animalEndpoints.endpoints.add(endpoint);

            return this.update();
        },
        remove: async function(endpoint) {
            console.debug('XNAT.plugin.pixi.endpoints.remove()', endpoint);

            XNAT.plugin.pixi.animalEndpoints.endpoints.delete(endpoint);

            return this.update();
        },
        update: async function() {
            console.debug('XNAT.plugin.pixi.endpoints.update()');

            const url = XNAT.url.csrfUrl(`${this.url}`);

            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(Array.from(XNAT.plugin.pixi.animalEndpoints.endpoints))
            });

            if (!response.ok) {
                throw new Error("Failed to update endpoints.");
            }

            document.dispatchEvent(new Event('endpoints-updated'));
        },
        editor: async function(endpoint, action) {
            console.debug('XNAT.plugin.pixi.endpoints.editor()');

            let isNew  = action === 'create',
                isEdit = action === 'edit',
                title  = isNew ? 'Add Animal Endpoint' : 'Edit Animal Endpoint';

            XNAT.dialog.open({
                title: title,
                content: spawn('div.endpoint-editor'),
                width: 500,
                beforeShow: () => {
                    const formContainer = document.querySelector('div.endpoint-editor');
                    formContainer.classList.add('panel');

                    let form = spawn('!', [
                        XNAT.ui.panel.input.text({
                            name: 'endpoint',
                            label: 'Endpoint',
                            value: endpoint || '',
                            required: true,
                            description: 'The animal endpoint method.'
                        }),
                    ]);

                    formContainer.appendChild(form);

                },
                buttons: [
                    {
                        label: 'Cancel',
                        close: true
                    },
                    {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: async function(obj) {
                            let form = document.querySelector('div.endpoint-editor');

                            let validator = XNAT.validate(form.querySelector('#endpoint'))
                                .reset().chain()
                                .required()
                                .is('notEmpty')
                                .failure('Endpoint is required')

                            let isValid = validator.check();

                            if (!isValid) {
                                XNAT.dialog.open({
                                    title: 'Error',
                                    width: 400,
                                    content: '<ul><li>' + validator.messages.join('</li><li>') + '</li></ul>',
                                })
                                return;
                            }

                            let newEndpoint = form.querySelector('#endpoint').value;

                            let response;
                            if (isNew) { // add the new endpoint
                                XNAT.plugin.pixi.animalEndpoints.endpoints.add(newEndpoint);
                            } else { // remove the old endpoint then add the new one
                                XNAT.plugin.pixi.animalEndpoints.endpoints.delete(endpoint);
                                XNAT.plugin.pixi.animalEndpoints.endpoints.add(newEndpoint);
                            }

                            XNAT.plugin.pixi.animalEndpoints.update().then(() => {
                                XNAT.ui.banner.top(2000, 'Endpoint added.', 'success');
                                obj.close();
                            }).catch(err => {
                                XNAT.ui.banner.top(2000, 'Failed to add endpoint.', 'error');
                                console.error(err);
                            });
                        }
                    }
                ]
            })
        },
        manager: function(querySelector) {
            console.debug('XNAT.plugin.pixi.endpoints.manager()');

            let container, footer;

            const init = () => {
                container = document.querySelector(querySelector);
                container.innerHTML = '<div class="loading"><i class="fa fa-spinner fa-spin"></i> Loading...</div>'

                container.style.display = 'flex';
                container.style.flexDirection = 'row';
                container.style.justifyContent = 'center';

                let newButton = spawn('div', [
                    spawn('div.pull-right', [
                        spawn('button.btn.btn-sm submit', { html: 'New Endpoint' , onclick: () => XNAT.plugin.pixi.animalEndpoints.editor(null, 'create') } ),
                    ]),
                    spawn('div.clear.clearFix')
                ]);

                footer = container.closest('.panel').querySelector('.panel-footer');
                footer.innerHTML = '';
                footer.appendChild(newButton);

                // add event listeners
                document.addEventListener('endpoints-updated', () => {
                    refresh();
                });

                refresh();
            }

            const clear = () => {
                container.innerHTML = '';
            }

            const refresh = async () => {
                XNAT.plugin.pixi.animalEndpoints.get().then(data => {
                    clear();

                    if(data.length === 0) {
                        container.innerHTML = '<div class="empty">No endpoints found.</div>';
                    } else {
                        table();
                    }
                })
            }

            const remove = async (endpoint) => {
                return XNAT.plugin.pixi.animalEndpoints.remove(endpoint).then(() => refresh())
            }

            const table = () => {
                const columns = {
                    endpoint: {
                        label: '<b>Endpoint</b>',
                        th: { className: 'left'},
                        apply: function() {
                            return spawn('div.left', [
                                spawn('span', {}, this.toString())
                            ]);
                        }
                    },
                    actions: {
                        label: '<b>Actions</b>',
                        th: { style: { width: '150px' } },
                        apply: function() {
                            return spawn('div.center', [
                                spawn('button.btn.btn-sm', { onclick: () => XNAT.plugin.pixi.animalEndpoints.editor(this.toString(), 'edit') }, '<i class="fa fa-pencil" title="Edit"></i>'),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm', { onclick: () => remove(this.toString())}, '<i class="fa fa-trash" title="Delete"></i>')
                            ]);
                        }
                    }
                }

                let data = Array.from(XNAT.plugin.pixi.animalEndpoints.endpoints).sort();

                const table = XNAT.table.dataTable(data, {
                    header: true,
                    sortable: 'endpoint',
                    columns: columns,
                });

                clear();
                table.render(`${querySelector}`);
            }

            init();

            return {
                init: init,
                clear: clear,
                refresh: refresh,
                remove: remove,
            }
        }
    }
}));