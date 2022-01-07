/*!
 * Site-wide Admin UI functions for PIXI
 */

console.log('pixi-siteAdmin.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = getObject(XNAT.plugin.pixi || {});

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

    /* ================ *
     * GLOBAL FUNCTIONS *
     * ================ */

    console.log('pixi-siteAdmin.js - Global Functions');

    let undefined,
        restUrl = XNAT.url.restUrl

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        const errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function() {
                        if (closeAll) {
                            xmodal.closeAll();

                        }
                    }
                }
            ]
        });
    }

    function getUrlParams(){
        const paramObj = {};

        // get the querystring param, redacting the '?', then convert to an array separating on '&'
        let urlParams = window.location.search.substr(1,window.location.search.length);
        urlParams = urlParams.split('&');

        urlParams.forEach(function(param) {
            // iterate over every key=value pair, and add to the param object
            param = param.split('=');
            paramObj[param[0]] = param[1];
        });

        return paramObj;
    }

    function getProjectId() {
        if (XNAT.data.context.projectID.length > 0) return XNAT.data.context.projectID;
        return getUrlParams().id;
    }

    function speciesValidator(inputs) {
        const errorMsg = [];

        if (inputs.length) {
            inputs.forEach(function($input) {
                if (!$input.val()) {
                    errorMsg.push('<b>' + $input.prop('name') + '</b> requires a value.');
                    $input.addClass('invalid');
                }
            });
        }

        return errorMsg;
    }

    function displayErrors(errorMsg) {
        var errors = [];
        errorMsg.forEach(function(msg) { errors.push(spawn('li',msg)) });

        return spawn('div',[
            spawn('p', 'Errors found:'),
            spawn('ul', errors)
        ]);
    }

    /* =========================== *
     * Species Preference Manager  *
     * =========================== */

    console.log('pixi-siteAdmin.js - Species Preference Manager');

    let speciesPreferenceManager;

    XNAT.plugin.pixi.speciesPreferenceManager = speciesPreferenceManager = getObject(XNAT.plugin.pixi.speciesPreferenceManager || {});

    function speciesPreferencesUrl() {
        let url = '/xapi/pixi/preferences/species';
        // const projectId = getProjectId();
        // url = projectId ? url + '/projects/' + projectId : url;
        return restUrl(url);
    }

    // get the list of species
    speciesPreferenceManager.getSpecies = speciesPreferenceManager.getAll = function(callback) {
        callback = isFunction(callback) ? callback : function() {};
        return XNAT.xhr.get({
            url: speciesPreferencesUrl(),
            dataType: 'json',
            success: function(data) {
                speciesPreferenceManager.data = data;
                callback.apply(this, arguments);
            }
        });
    };

    // dialog to create/edit species
    speciesPreferenceManager.dialog = function(item, isNew) {
        const doWhat = (isNew) ? 'Create' : 'Edit';
        item = item || {};

        XNAT.dialog.open({
            title: doWhat + ' Species',
            content: spawn('form'),
            maxBtn: true,
            width: 600,
            beforeShow: function(obj) {
                // spawn species form
                speciesPreferenceManager.nconstraints = 0;
                const $formContainer = obj.$modal.find('.xnat-dialog-content');
                $formContainer.addClass('panel');
                obj.$modal.find('form').append(
                    spawn('!', [
                        XNAT.ui.panel.input.text({
                            name: 'scientificName',
                            label: 'Scientific Name',
                            description: 'The scientific name of the species. This will be stored in the species field for subjects.'
                        }).element,
                        XNAT.ui.panel.input.text({
                            name: 'commonName',
                            label: 'Common Name',
                            description: 'The common name of the species. This will be displayed to users when selecting the species for a subject.'
                        }).element
                    ])
                );

                // fill in form if editing
                if (item) {
                    $formContainer.find('form').setValues(item);
                }
            },
            buttons: [
                {
                    label: 'Save',
                    isDefault: true,
                    close: false,
                    action: function(obj) {
                        // on save
                        // get inputs
                        const $form = obj.$modal.find('form');
                        const $scientificName = $form.find('input[name=scientificName]');
                        const $commonName = $form.find('input[name=commonName]');
                        const $toValidate = [$scientificName, $commonName];

                        // remove errors from previous save attempt
                        $form.find(':input').removeClass('invalid');

                        // validate
                        const errors = speciesValidator($toValidate);

                        if (errors.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: displayErrors(errors)
                            })
                        } else {
                            // no errors -> send to xnat
                            let speciesToSave = {
                                id: undefined,
                                scientificName: undefined,
                                commonName: undefined
                            };

                            if (!isNew && item) {
                                // Edit
                                speciesToSave.id = item['id'];
                                speciesToSave.scientificName = $scientificName.val();
                                speciesToSave.commonName = $commonName.val();

                                // Update existing species
                                let idx = speciesPreferenceManager.data.findIndex(obj => obj.id === item.id);
                                speciesPreferenceManager.data[idx] = speciesToSave;
                            } else if (isNew) {
                                // Create
                                // Find the max id and increment by 1
                                const maxID = speciesPreferenceManager.data
                                    .map(el => el.id)
                                    .reduce((el1, el2) => Math.max(el1, el2));

                                speciesToSave.id = maxID + 1;
                                speciesToSave.scientificName = $scientificName.val();
                                speciesToSave.commonName = $commonName.val();

                                // add new species
                                speciesPreferenceManager.data.push(speciesToSave);
                            }

                            XNAT.xhr.put({
                                url: speciesPreferencesUrl(),
                                data: JSON.stringify(speciesPreferenceManager.data), // Submit all data, not just the item
                                contentType: 'application/json',
                                success: function () {
                                    XNAT.ui.banner.top(1000, '<b>"' + speciesToSave['scientificName'] + '"</b> saved.', 'success');
                                    speciesPreferenceManager.refreshTable();
                                    xmodal.closeAll();
                                    XNAT.ui.dialog.closeAll();
                                },
                                fail: function (e) {
                                    errorHandler(e, 'Could not ' + doWhat.toLocaleLowerCase() + ' species');
                                }
                            })
                        }
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        });
    };

    // table listing of species
    speciesPreferenceManager.table = function(container, callback) {

        // initialize the table
        const speciesTable = XNAT.table({
            className: 'species xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        })

        // add table header row
        speciesTable.tr()
            .th('<b>ID #</b>')
            .th('<b>Scientific Name</b>')
            .th('<b>Common Name</b>')
            .th('<b>Actions</b>')

        function editButton(item) {
            return spawn('button.btn.sm.edit', {
                onclick: function(e) {
                    e.preventDefault();
                    speciesPreferenceManager.dialog(item, false);
                }
            }, 'Edit');
        }

        function deleteButton(item) {
            return spawn('button.btn.sm.delete', {
                onclick: function() {
                    xmodal.confirm({
                        height: 220,
                        scroll: false,
                        content: "" +
                            "<p>Are you sure you'd like to delete this species?</p>" +
                            "<p><b>This action cannot be undone.</b></p>",
                        okAction: function() {
                            let newData = speciesPreferenceManager.data
                                                                  .filter(el => el.id !== item.id)
                                                                  .map((el, idx) => {
                                                                      el.id = idx + 1;
                                                                      return el;
                                                                  });

                            XNAT.xhr.put({
                                url: speciesPreferencesUrl(),
                                data: JSON.stringify(newData),
                                contentType: 'application/json',
                                success: function() {
                                    XNAT.ui.banner.top(1000, '<b>"'+ item['scientificName'] + '"</b> deleted.', 'success');
                                    speciesPreferenceManager.refreshTable();
                                },
                                fail: function(e) {
                                    errorHandler(e, 'Could not delete species');
                                }
                            });
                        }
                    })
                },
                disabled: speciesPreferenceManager.data.length <= 1,
                title: speciesPreferenceManager.data.length <= 1 ? "Cannot delete the only species" : "Delete Species",
            }, [ spawn('i.fa.fa-trash') ]);
        }

        speciesPreferenceManager.getAll().done(function(data) {
            // Sort table by id.
            data.sort((a,b) => a.id - b.id)

            // create row for each species
            data.forEach(item => {
                speciesTable.tr()
                    .td([ spawn('div.center', [item['id']]) ])
                    .td([ spawn('div.center', [item['scientificName']]) ])
                    .td([ spawn('div.center', [item['commonName']]) ])
                    .td([ spawn('div.center', [editButton(item), spacer(10), deleteButton(item)]) ])
            })

            if (container) {
                $$(container).append(speciesTable.table);
            }

            if (isFunction(callback)) {
                callback(speciesTable.table);
            }
        })

        speciesPreferenceManager.$table = $(speciesTable.table);

        return speciesTable.table;
    }

    speciesPreferenceManager.init = function(container) {

        const $manager = $$(container || 'div#species-preferences-manager');
        const $footer = $('#species-preferences-manager').parents('.panel').find('.panel-footer');

        speciesPreferenceManager.$container = $manager;

        $manager.append(speciesPreferenceManager.table());

        const newSpecies = spawn('button.new-container-host.btn.btn-sm.submit', {
            html: 'New Species',
            onclick: function() {
                speciesPreferenceManager.dialog(null, true);
            }
        });

        // add the 'add new' button to the panel footer
        $footer.append(spawn('div.pull-right', [newSpecies]));
        $footer.append(spawn('div.clear.clearFix'));

        return {
            element: $manager[0],
            spawned: $manager[0],
            get: function() {
                return $manager[0]
            }
        };
    }

    speciesPreferenceManager.refresh = speciesPreferenceManager.refreshTable = function() {
        speciesPreferenceManager.$table.remove();
        speciesPreferenceManager.table(null, function(table) {
            speciesPreferenceManager.$container.prepend(table);
        });
    };

    speciesPreferenceManager.init();
}));