/*
 *  PIXI Species Preferences Manager
 *
 *  This script depends on functions in pixi-module.js
 */

console.log('pixi-speciesPreferences.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});

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

    let undefined,
        restUrl = XNAT.url.restUrl

    let speciesPreferenceManager;
    XNAT.plugin.pixi.speciesPreferenceManager = speciesPreferenceManager = getObject(XNAT.plugin.pixi.speciesPreferenceManager || {});

    function speciesPreferencesUrl() {
        let url = '/xapi/pixi/preferences/species';
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
                            id: 'scientificName',
                            label: 'Scientific Name',
                            description: 'The scientific name of the species. This will be stored in the species field for subjects.'
                        }).element,
                        XNAT.ui.panel.input.text({
                            name: 'commonName',
                            id: 'commonName',
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
                    action: function() {
                        // on save
                        // get inputs
                        const scientificNameEl = document.getElementById("scientificName");
                        const commonNameEl = document.getElementById("commonName");

                        // validator for scientific name
                        let validateScientificName = XNAT.validate(scientificNameEl).reset().chain();
                        validateScientificName.minLength(1).failure('Scientific Name is required.');
                        validateScientificName.is('alphaDashSpace').failure('Scientific Name can only contain alphabetical characters, hyphens, and spaces.');

                        // validator for common name
                        let validateCommonName = XNAT.validate(commonNameEl).reset().chain();
                        validateCommonName.minLength(1).failure('Common Name is required.');
                        validateCommonName.is('alphaDashSpace').failure('Common Name can only contain alphabetical characters, hyphens, and spaces.');

                        // validate fields
                        let errorMessages = [];

                        [validateScientificName, validateCommonName].forEach(validator => {
                            validator.check();
                            validator.messages.forEach(message => errorMessages.push(message))
                        })

                        if (errorMessages.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: pixi.clientErrorHandler(errorMessages)
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
                                speciesToSave.scientificName = scientificNameEl.value;
                                speciesToSave.commonName = commonNameEl.value;

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
                                speciesToSave.scientificName = scientificNameEl.value;
                                speciesToSave.commonName = commonNameEl.value;

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
                                    pixi.serverErrorHandler(e, 'Could not ' + doWhat.toLocaleLowerCase() + ' species');
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
            .th({ addClass: 'left', html: '<b>Scientific Name</b>' })
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
                                    pixi.serverErrorHandler(e, 'Could not delete species');
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
            // Sort table by scientificName.
            data.sort(pixi.compareGenerator('scientificName'))

            // create row for each species
            data.forEach(item => {
                speciesTable.tr()
                    .td([ spawn('div.left', [item['scientificName']]) ])
                    .td([ spawn('div.center', [item['commonName']]) ])
                    .td([ spawn('div.center', [editButton(item), pixi.spacer(10), deleteButton(item)]) ])
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

}));