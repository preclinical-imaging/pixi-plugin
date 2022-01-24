/*
 *  PIXI Xenograft Management
 *
 *  This script depends on functions in pixi-module.js
 */

console.log('pixi-xenograft.js');

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

    console.log('pixi-xenograft.js - Xenograft Manager');

    let restUrl = XNAT.url.restUrl

    class XenograftManager {
        constructor(xenograftType) {
            this.xenograftType = xenograftType;
            this.data = [];
        }

        url(append) {
            let url = '/xapi/pixi/' + this.xenograftType.toLowerCase().replaceAll(/\s/g,'') + '/';
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
                    self.data = data.sort(pixi.compareGenerator('externalID'));
                    callback.apply(this, arguments);
                }
            });
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
                    obj.$modal.find('form').append(
                        spawn('!', [
                            XNAT.ui.panel.input.text({
                                name: 'externalID',
                                label: self.xenograftType + ' ID *',
                                id: 'externalID',
                                description: 'Required: The original ID at the data source providing this ' + self.xenograftType + ' model. Example: WHIM20.'
                            }).element,
                            XNAT.ui.panel.input.text({
                                name: 'dataSource',
                                label: 'Source *',
                                id: 'dataSource',
                                description: 'Required: The source providing this ' + self.xenograftType + ' model. Example: WUSTL.'
                            }).element,
                            XNAT.ui.panel.input.text({
                                name: 'dataSourceURL',
                                label: self.xenograftType + ' URL',
                                id: 'dataSourceURL',
                                description: 'A link to this ' + self.xenograftType + ' at the data source provider (if available).'
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
                        label: 'Submit',
                        isDefault: true,
                        close: false,
                        action: function(obj) {
                            // on save
                            // get inputs
                            const $form = obj.$modal.find('form');
                            const $externalID = $form.find('input[name=externalID]');
                            const $dataSource = $form.find('input[name=dataSource]');
                            const $dataSourceURL = $form.find('input[name=dataSourceURL]');
                            const $toValidate = [$externalID, $dataSource];
                            const $urlToValidate = [$dataSourceURL];

                            // remove errors from previous save attempt
                            $form.find(':input').removeClass('invalid');

                            // validate
                            let errors = pixi.inputsValidator($toValidate);
                            let urlErrors = pixi.urlValidator($urlToValidate);
                            errors = errors.concat(urlErrors);

                            if (errors.length) {
                                // errors?
                                XNAT.dialog.open({
                                    title: 'Validation Error',
                                    width: 300,
                                    content: pixi.clientErrorHandler(errors)
                                })
                            } else {
                                // no errors -> send to xnat
                                let xenograftToSubmit = {
                                    externalID: $externalID.val(),
                                    dataSource: $dataSource.val(),
                                    dataSourceURL: $dataSourceURL.val()
                                };

                                XNAT.xhr.ajax({
                                    url: isNew ? self.url() : self.url(item['externalID']),
                                    data: JSON.stringify(xenograftToSubmit),
                                    method: isNew ? 'POST' : 'PUT',
                                    contentType: 'application/json',
                                    success: function () {
                                        XNAT.ui.banner.top(1000, '<b>"' + xenograftToSubmit['externalID'] + '"</b> saved.', 'success');
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
                .th('<b>Details at Source</b>') // TODO Display Links properly
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
                                    url: self.url(item['externalID']),
                                    contentType: 'application/json',
                                    success: function() {
                                        XNAT.ui.banner.top(1000, '<b>"'+ item['externalID'] + '"</b> deleted.', 'success');
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
                if (item['dataSourceURL']) {
                    return spawn('a.link|href=#!', {
                        onclick: function(e){
                            e.preventDefault();
                            window.open(item['dataSourceURL']).focus();
                        }
                    }, [['b', 'Details']]);
                }
            }

            this.getAll().done(function(data) {
                // create row for each xenograft
                data.forEach(item => {
                    xenograftTable.tr()
                        .td([ spawn('div.left', [item['externalID']]) ])
                        .td([ spawn('div.center', [item['dataSource']]) ])
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
            super('PDX');
        }
    }

    class CellLineManager extends XenograftManager {
        constructor() {
            super('Cell Line');
        }
    }

    console.log('pixi-xenograft.js - PDX & Cell Line Managers');

    XNAT.plugin.pixi.pdxManager = new PDXManager();
    XNAT.plugin.pixi.cellLineManager = new CellLineManager();

}));