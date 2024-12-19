/*
 *  PIXI Xenograft Management
 *
 *  This script depends on functions in pixi-module.js
 */

console.log('pixi-xenograft.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});
XNAT.plugin.pixi.cellLines = getObject(XNAT.plugin.pixi.cellLines || {});
XNAT.plugin.pixi.pdxs = getObject(XNAT.plugin.pixi.pdxs || {});


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

    console.log('pixi-xenograft.js - Xenograft Manager');

    let restUrl = XNAT.url.restUrl

    XNAT.plugin.pixi.cellLines.get = async function() {
        console.debug(`pixi-xenograft.js: XNAT.plugin.pixi.cellLines.get`);

        const response = await fetch('/xapi/pixi/cellline', {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error("Failed to fetch cell lines.");
        }

        return await response.json();
    }

    XNAT.plugin.pixi.pdxs.get = async function() {
        console.debug(`pixi-xenograft.js: XNAT.plugin.pixi.pdxs.get`);

        const response = await fetch('/xapi/pixi/pdx', {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        });

        if (!response.ok) {
            throw new Error("Failed to fetch pdxs.");
        }

        return await response.json();
    }

    class XenograftManager {
        constructor(xenograftType, urlRoot) {
            this.xenograftType = xenograftType;
            this.urlRoot = urlRoot;
            this.data = [];
        }

        url(append) {
            let url = this.urlRoot
            url = append ? url + append : url;
            return restUrl(url);
        }

        getAll(callback) {
            callback = isFunction(callback) ? callback : function() {};
            const self = this;
            return XNAT.xhr.get({
                url: this.url(),
                dataType: 'json',
                success: function(data) {
                    self.data = data.sort(pixi.compareGenerator('sourceId'));
                    callback.apply(this, arguments);
                }
            });
        }

        getSpawnerElements() {
            const self = this;

            return [
                XNAT.ui.panel.input.text({
                    name: 'sourceId',
                    label: self.xenograftType + ' ID *',
                    id: 'sourceId',
                    description: 'Required: The original ID at the source providing this ' + self.xenograftType + ' model.'
                }).element,
                XNAT.ui.panel.input.text({
                    name: 'source',
                    label: 'Source *',
                    id: 'source',
                    description: 'Required: The source providing this ' + self.xenograftType + ' model.'
                }).element,
                XNAT.ui.panel.input.text({
                    name: 'sourceURL',
                    label: self.xenograftType + ' URL',
                    id: 'sourceURL',
                    description: 'Optional: A link to this ' + self.xenograftType + ' at the data source provider (if available).'
                }).element
            ];
        }

        // dialog to create/edit xenograft
        dialog(item, isNew, successCallback) {
            const self = this;
            const doWhat = (isNew) ? 'Create' : 'Edit';
            item = item || {};

            XNAT.dialog.open({
                title: doWhat + ' ' + this.xenograftType,
                content: spawn('form'),
                maxBtn: true,
                width: 600,
                beforeShow: function(obj) {
                    // spawn xenograft form
                    const $formContainer = obj.$modal.find('.xnat-dialog-content');
                    $formContainer.addClass('panel');

                    let spawnerElements = self.getSpawnerElements();

                    obj.$modal.find('form').append(
                        spawn('!', spawnerElements)
                    );

                    // fill in form if editing
                    if (item) {
                        $formContainer.find('form').setValues(item);
                    }
                },
                buttons: [
                    {
                        label: 'Submit',
                        isDefault: true,
                        close: false,
                        action: function() {
                            // on save
                            // get inputs
                            const sourceIdEl = document.getElementById("sourceId");
                            const sourceEl = document.getElementById("source");
                            const sourceURLEl = document.getElementById("sourceURL");

                            // validator for sourceId (i.e. PDX ID and Cell Line ID)
                            let validateSourceId = XNAT.validate(sourceIdEl).reset().chain();
                            validateSourceId.minLength(1).failure(`${self.xenograftType} ID is required.`);

                            // validator for source
                            let validateSource = XNAT.validate(sourceEl).reset().chain();
                            validateSource.minLength(1).failure('Source is required.');

                            // validator for sourceURL
                            let validateSourceURL = XNAT.validate(sourceURLEl).reset().chain();
                            validateSourceURL.allowEmpty = true;
                            validateSourceURL.is('url').failure('Invalid url.');

                            // validate fields
                            let errorMessages = [];

                            [validateSourceId, validateSource, validateSourceURL].forEach(validator => {
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
                                let xenograftToSubmit = {
                                    sourceId: sourceIdEl.value,
                                    source: sourceEl.value,
                                    sourceURL: sourceURLEl.value
                                };

                                if (self.xenograftType === "PDX") {
                                    const storageEl = document.getElementById("storage");
                                    xenograftToSubmit['storage'] = storageEl.value;
                                }

                                XNAT.xhr.ajax({
                                    url: isNew ? self.url() : self.url(item['sourceId']),
                                    data: JSON.stringify(xenograftToSubmit),
                                    method: isNew ? 'POST' : 'PUT',
                                    contentType: 'application/json',
                                    success: function () {
                                        XNAT.ui.banner.top(1000, '<b>"' + xenograftToSubmit['sourceId'] + '"</b> saved.', 'success');
                                        xmodal.closeAll();
                                        XNAT.ui.dialog.closeAll();

                                        if (isFunction(successCallback)) {
                                            successCallback.apply(self);
                                        }
                                    },
                                    fail: function (e) {
                                        pixi.serverErrorHandler(e, 'Could not ' + doWhat.toLocaleLowerCase() + ' ' + self.xenograftType);
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

        // table listing of xenografts
        table(container, callback) {
            const self = this;

            // initialize the table
            const xenograftTable = XNAT.table({
                className: 'xenografts xnat-table',
                style: {
                    width: '100%',
                    marginTop: '15px',
                    marginBottom: '15px'
                }
            })

            // add table header row
            xenograftTable.tr()
                .th({ addClass: 'left', html: '<b>' + this.xenograftType + ' ID</b>' })
                .th('<b>Source</b>')
                .th('<b>Link to Source Details</b>')
                .th('<b>Actions</b>')

            function editButton(item) {
                return spawn('button.btn.sm.edit', {
                    onclick: function(e) {
                        e.preventDefault();
                        self.dialog(item, false, self.refreshTable.bind(self));
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
                                "<p>Are you sure you'd like to delete this " + self.xenograftType + "?</p>" +
                                "<p><b>This action cannot be undone.</b></p>",
                            okAction: function() {
                                XNAT.xhr.delete({
                                    url: self.url(item['sourceId']),
                                    contentType: 'application/json',
                                    success: function() {
                                        XNAT.ui.banner.top(1000, '<b>"'+ item['sourceId'] + '"</b> deleted.', 'success');
                                        self.refreshTable();
                                    },
                                    fail: function(e) {
                                        pixi.serverErrorHandler(e, 'Could not delete ' + self.xenograftType);
                                    }
                                });
                            }
                        })
                    },
                    title: "Delete " + self.xenograftType,
                }, [ spawn('i.fa.fa-trash') ]);
            }

            function detailsButton(item) {
                // only make button if link exists
                if (item['sourceURL']) {
                    return spawn('a.link|href=#!', {
                        onclick: function(e){
                            e.preventDefault();
                            window.open(item['sourceURL']).focus();
                        }
                    }, [['b', 'Details']]);
                }
            }

            this.getAll().done(function(data) {
                // create row for each xenograft
                data.forEach(item => {
                    xenograftTable.tr()
                        .td([ spawn('div.left', [item['sourceId']]) ])
                        .td([ spawn('div.center', [item['source']]) ])
                        .td([ spawn('div.center', [detailsButton(item)]) ])
                        .td([ spawn('div.center', [editButton(item), pixi.spacer(10), deleteButton(item)]) ])
                })

                if (container) {
                    $$(container).append(xenograftTable.table);
                }

                if (isFunction(callback)) {
                    callback(xenograftTable.table);
                }
            })

            this.$table = $(xenograftTable.table);

            return xenograftTable.table;
        }

        init(container) {
            const self = this;
            const $manager = $$(container);
            const $footer = $(container).parents('.panel').find('.panel-footer');

            this.$container = $manager;

            $manager.append(this.table());

            const newXenograftButton = spawn('button.new-container-host.btn.btn-sm.submit', {
                html: 'New ' + this.xenograftType,
                onclick: function() {
                    self.dialog(null, true, self.refreshTable.bind(self));
                }
            });

            // add the 'add new' button to the panel footer
            $footer.append(spawn('div.pull-right', [newXenograftButton]));
            $footer.append(spawn('div.clear.clearFix'));

            return {
                element: $manager[0],
                spawned: $manager[0],
                get: function() {
                    return $manager[0]
                }
            };
        }

        refreshTable() {
            const self = this;

            this.$table.remove();
            this.table(null, function(table) {
                self.$container.prepend(table);
            });
        };

    }

    class PDXManager extends XenograftManager {
        constructor() {
            super('Patient-Derived Tumor', '/xapi/pixi/pdx/');
        }

        getSpawnerElements() {
            let spawnerElements = super.getSpawnerElements();

            spawnerElements.push(
                XNAT.ui.panel.input.text({
                name: 'storage',
                label: 'Storage',
                id: 'storage',
                description: 'Optional: Storage method for this tumor model.'
            }).element)

            return spawnerElements;
        }
    }

    class CellLineManager extends XenograftManager {
        constructor() {
            super('Cell Line', '/xapi/pixi/cellline/');
        }

        getSpawnerElements() {
            return super.getSpawnerElements();
        }
    }

    console.log('pixi-xenograft.js - PDX & Cell Line Managers');

    XNAT.plugin.pixi.pdxManager = new PDXManager();
    XNAT.plugin.pixi.cellLineManager = new CellLineManager();

}));