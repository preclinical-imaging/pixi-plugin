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

    /* =========================== *
     * Species Preference Manager  *
     * =========================== */

    console.log('pixi-siteAdmin.js - Species Preference Manager');

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
                        const errors = pixi.inputsValidator($toValidate);

                        if (errors.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: pixi.clientErrorHandler(errors)
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
                    .td([ spawn('div.center', [item['scientificName']]) ])
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

    speciesPreferenceManager.init();

    /* =========================== *
     * Vendor Preference Manager  *
     * =========================== */

    console.log('pixi-siteAdmin.js - Vendor Preference Manager');

    let vendorPreferenceManager;

    XNAT.plugin.pixi.vendorPreferenceManager = vendorPreferenceManager = getObject(XNAT.plugin.pixi.vendorPreferenceManager || {});

    function vendorPreferencesUrl() {
        let url = '/xapi/pixi/preferences/vendors';
        return restUrl(url);
    }

    // get the list of vendors
    vendorPreferenceManager.getVendors = vendorPreferenceManager.getAll = function(callback) {
        callback = isFunction(callback) ? callback : function() {};
        return XNAT.xhr.get({
            url: vendorPreferencesUrl(),
            dataType: 'json',
            success: function(data) {
                vendorPreferenceManager.data = data;
                callback.apply(this, arguments);
            }
        });
    };

    // dialog to create/edit vendors
    vendorPreferenceManager.dialog = function(item, isNew) {
        const doWhat = (isNew) ? 'Create' : 'Edit';
        item = item || {};

        XNAT.dialog.open({
            title: doWhat + ' Vendor',
            content: spawn('form'),
            maxBtn: true,
            width: 600,
            beforeShow: function(obj) {
                // spawn vendor form
                vendorPreferenceManager.nconstraints = 0;
                const $formContainer = obj.$modal.find('.xnat-dialog-content');
                $formContainer.addClass('panel');
                obj.$modal.find('form').append(
                    spawn('!', [
                        XNAT.ui.panel.input.text({
                            name: 'vendor',
                            label: 'Vendor',
                            description: 'The name of the laboratory rodent vendor/supplier.'
                        }).element,
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
                        const $vendor = $form.find('input[name=vendor]');
                        const $toValidate = [$vendor];

                        // remove errors from previous save attempt
                        $form.find(':input').removeClass('invalid');

                        // validate
                        const errors = pixi.inputsValidator($toValidate);

                        if (errors.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: pixi.clientErrorHandler(errors)
                            })
                        } else {
                            // no errors -> send to xnat
                            let vendorToSave = {
                                id: undefined,
                                vendor: undefined
                            };

                            if (!isNew && item) {
                                // Edit
                                vendorToSave.id = item['id'];
                                vendorToSave.vendor = $vendor.val();

                                // Update existing vendor
                                let idx = vendorPreferenceManager.data.findIndex(obj => obj.id === item.id);
                                vendorPreferenceManager.data[idx] = vendorToSave;
                            } else if (isNew) {
                                // Create
                                // Find the max id and increment by 1
                                let maxID = 0;
                                if (vendorPreferenceManager.data.length > 0) {
                                    maxID = vendorPreferenceManager.data
                                        .map(el => el.id)
                                        .reduce((el1, el2) => Math.max(el1, el2));
                                }

                                vendorToSave.id = maxID + 1;
                                vendorToSave.vendor = $vendor.val();

                                // add new vendor
                                vendorPreferenceManager.data.push(vendorToSave);
                            }

                            XNAT.xhr.put({
                                url: vendorPreferencesUrl(),
                                data: JSON.stringify(vendorPreferenceManager.data), // Submit all data, not just the item
                                contentType: 'application/json',
                                success: function () {
                                    XNAT.ui.banner.top(1000, '<b>"' + vendorToSave['vendor'] + '"</b> saved.', 'success');
                                    vendorPreferenceManager.refreshTable();
                                    xmodal.closeAll();
                                    XNAT.ui.dialog.closeAll();
                                },
                                fail: function (e) {
                                    pixi.serverErrorHandler(e, 'Could not ' + doWhat.toLocaleLowerCase() + ' vendor');
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

    // table listing of vendors
    vendorPreferenceManager.table = function(container, callback) {

        // initialize the table
        const vendorTable = XNAT.table({
            className: 'vendors xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        })

        // add table header row
        vendorTable.tr()
            .th('<b>Vendor</b>')
            .th('<b>Actions</b>')

        function editButton(item) {
            return spawn('button.btn.sm.edit', {
                onclick: function(e) {
                    e.preventDefault();
                    vendorPreferenceManager.dialog(item, false);
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
                            "<p>Are you sure you'd like to delete this vendor?</p>" +
                            "<p><b>This action cannot be undone.</b></p>",
                        okAction: function() {
                            let newData = vendorPreferenceManager.data
                                .filter(el => el.id !== item.id)
                                .map((el, idx) => {
                                    el.id = idx + 1;
                                    return el;
                                });

                            XNAT.xhr.put({
                                url: vendorPreferencesUrl(),
                                data: JSON.stringify(newData),
                                contentType: 'application/json',
                                success: function() {
                                    XNAT.ui.banner.top(1000, '<b>"'+ item['vendor'] + '"</b> deleted.', 'success');
                                    vendorPreferenceManager.refreshTable();
                                },
                                fail: function(e) {
                                    pixi.serverErrorHandler(e, 'Could not delete vendor');
                                }
                            });
                        }
                    })
                },
                title: "Delete Vendor",
            }, [ spawn('i.fa.fa-trash') ]);
        }

        vendorPreferenceManager.getAll().done(function(data) {
            // Sort table by vendor.
            data.sort(pixi.compareGenerator('vendor'))

            // create row for each vendor
            data.forEach(item => {
                vendorTable.tr()
                    .td([ spawn('div.center', [item['vendor']]) ])
                    .td([ spawn('div.center', [editButton(item), pixi.spacer(10), deleteButton(item)]) ])
            })

            if (container) {
                $$(container).append(vendorTable.table);
            }

            if (isFunction(callback)) {
                callback(vendorTable.table);
            }
        })

        vendorPreferenceManager.$table = $(vendorTable.table);

        return vendorTable.table;
    }

    vendorPreferenceManager.init = function(container) {

        const $manager = $$(container || 'div#vendor-preferences-manager');
        const $footer = $('#vendor-preferences-manager').parents('.panel').find('.panel-footer');

        vendorPreferenceManager.$container = $manager;

        $manager.append(vendorPreferenceManager.table());

        const newVendor = spawn('button.new-container-host.btn.btn-sm.submit', {
            html: 'New Vendor',
            onclick: function() {
                vendorPreferenceManager.dialog(null, true);
            }
        });

        // add the 'add new' button to the panel footer
        $footer.append(spawn('div.pull-right', [newVendor]));
        $footer.append(spawn('div.clear.clearFix'));

        return {
            element: $manager[0],
            spawned: $manager[0],
            get: function() {
                return $manager[0]
            }
        };
    }

    vendorPreferenceManager.refresh = vendorPreferenceManager.refreshTable = function() {
        vendorPreferenceManager.$table.remove();
        vendorPreferenceManager.table(null, function(table) {
            vendorPreferenceManager.$container.prepend(table);
        });
    };

    vendorPreferenceManager.init();

    XNAT.plugin.pixi.pdxManager.init('#pdx-manager');
    XNAT.plugin.pixi.cellLineManager.init('#cell-line-manager');

}));