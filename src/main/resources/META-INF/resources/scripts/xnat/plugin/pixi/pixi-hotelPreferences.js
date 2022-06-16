/*!
* PIXI Hotel Preferences
*/

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

    let restUrl = XNAT.url.restUrl

    let hotelPreferenceManager;
    XNAT.plugin.pixi.hotelPreferenceManager = hotelPreferenceManager = getObject(XNAT.plugin.pixi.hotelPreferenceManager || {});

    hotelPreferenceManager.containerId = 'hotel-preferences-manager';
    hotelPreferenceManager.hotelPositionTableContainerId = 'hotel-positions-table-container';
    hotelPreferenceManager.hotelPositionTableId = 'hotel-positions-table';

    function hotelUrl(append) {
        let url = '/xapi/pixi/hotels'
        url =  append ? `${url}/${append}` : url;
        return restUrl(url);
    }

    // get the list of hotels
    hotelPreferenceManager.getHotels = function(callback) {
        callback = isFunction(callback) ? callback : function() {};
        return XNAT.xhr.get({
            url: hotelUrl(),
            dataType: 'json',
            success: function(data) {
                hotelPreferenceManager.hotels = data;
                callback.apply(this, arguments);
            }
        });
    };

    hotelPreferenceManager.emptyHotelPosition = function() {
        return {
            name: '',
            x: '',
            y: '',
            z: ''
        };
    }

    hotelPreferenceManager.emptyHotel = function() {
        return {
            name: '',
            positions: []
        }
    }

    // dialog to edit an individual hotel position
    hotelPreferenceManager.hotelPositionDialog = function(hotel, hotelPosition, isNew) {
        const doWhat = (isNew) ? 'Create' : 'Edit';
        hotelPosition = hotelPosition || hotelPreferenceManager.emptyHotelPosition();

        const dialogId = 'hotel-position-dialog';

        XNAT.dialog.open({
            title: `${doWhat} Hotel Position`,
            content: spawn('form#hotel-position-form', {addClass: 'panel'}),
            maxBtn: false,
            width: 500,
            beforeShow: function(obj) {
                // spawn hotel position form
                obj.$modal.find('#hotel-position-form').append(
                    spawn('!', [
                        XNAT.ui.panel.input.text({
                            name: 'positionName',
                            id: 'positionName',
                            label: 'Position Name *',
                            value: hotelPosition['name'],
                            description: 'A name which describes the hotel position. This will be displayed to the user instead of its x/y relative location. Examples: Left, Top Left, Bottom Right, Middle.'
                        }).element,
                        XNAT.ui.panel.input.number({
                            name: 'x',
                            id:'x',
                            label: 'X *',
                            value: hotelPosition['x'],
                            min: 1,
                            max: 4,
                            step: 1,
                            description: 'X Position: This value starts at one for the left most subject holder and monotonically increases by one for each successive subject holder towards the right.'
                        }).element,
                        XNAT.ui.panel.input.number({
                            name: 'y',
                            id: 'y',
                            label: 'Y *',
                            value: hotelPosition['y'],
                            min: 1,
                            max: 4,
                            step: 1,
                            description: 'Y Position: This value starts at one for the top most subject holder and monotonically increases by one for each successively lower subject holder.'
                        }).element,
                        // Z is not supported by hotel splitter. Lets keep the input but hide it from the user.
                        spawn(['div|hidden=true', [
                                                    XNAT.ui.panel.input.number({
                                                        name: 'z',
                                                        id: 'z',
                                                        label: 'Z *',
                                                        value: isNew ? 1 : hotelPosition['z'],
                                                        min: 1,
                                                        max: 4,
                                                        step: 1,
                                                        description: 'Z Position: This value starts at one for the outer most subject holder and monotonically increases by one for each successive subject holder inwards (i.e., increasing values from the front to the back of the gantry along the direction orthogonal to the first two dimensions, usually the long axis of the table).'
                                                    })]
                        ])
                    ])
                )
            },
            buttons: [
                {
                    label: 'Save',
                    isDefault: true,
                    close: false,
                    action: function() {
                        // get inputs
                        const positionNameEl = document.getElementById("positionName");
                        const xEl = document.getElementById('x');
                        const yEl = document.getElementById('y');
                        const zEl = document.getElementById('z');

                        // validator for position name
                        let validatePositionName = XNAT.validate(positionNameEl).reset().chain();
                        validatePositionName.is('notEmpty').failure('Position name is a required field.');

                        // validator for x, y, and z positions
                        let validateX = XNAT.validate(xEl).reset().chain();
                        validateX.is('integer').failure('X must be an integer.');
                        validateX.is('greaterThan', 0).failure('X must be greater than 0');

                        let validateY = XNAT.validate(yEl).reset().chain();
                        validateY.is('integer').failure('Y must be an integer.');
                        validateY.is('greaterThan', 0).failure('Y must be greater than 0');

                        let validateZ = XNAT.validate(zEl).reset().chain();
                        validateZ.is('integer').failure('Z must be an integer.');
                        validateZ.is('greaterThan', 0).failure('Z must be greater than 0');

                        // validate fields
                        let errorMessages = [];
                        [validatePositionName, validateX, validateY, validateZ].forEach(validator => {
                            validator.check();
                            validator.messages.forEach(message => errorMessages.push(message));
                        })

                        if (errorMessages.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: pixi.clientErrorHandler(errorMessages)
                            })

                            return;
                        }

                        // Validate uniqueness
                        hotel['positions'].filter(position => position['name'] !== hotelPosition['name']).forEach(position => {
                            // X Y Z must be unique amongst all positions
                            if (position['x'] === parseInt(xEl.value) && position['y'] === parseInt(yEl.value) && position['z'] === parseInt(zEl.value)) {
                                errorMessages.push('X and Y match another hotel position. Positions must be unique.');
                                xEl.classList.add('invalid');
                                yEl.classList.add('invalid');
                                zEl.classList.add('invalid');
                            }

                            // Name matches another position name
                            if (position['name'] === positionNameEl.value) {
                                errorMessages.push('Position name matches another hotel position name. Positions names must be unique.');
                                positionNameEl.classList.add('invalid');
                            }
                        })

                        if (errorMessages.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: pixi.clientErrorHandler(errorMessages)
                            })

                            return;
                        }

                        // Update the hotel position
                        hotelPosition.name = positionNameEl.value
                        hotelPosition.x = parseInt(xEl.value);
                        hotelPosition.y = parseInt(yEl.value);
                        hotelPosition.z = parseInt(zEl.value);

                        // Add position to hotel if new
                        if (isNew) {
                            hotel['positions'].push(hotelPosition);
                        }

                        // refresh table and exit edit dialog
                        hotelPreferenceManager.hotelPositionsTable(hotel);
                        XNAT.dialog.close(dialogId);

                        console.debug("pixi-hotelPreferences.js - Hotel position saved")
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        })
    }

    // generate table to display the hotel positions
    hotelPreferenceManager.hotelPositionsTable = function(hotel,
                                                          containerId= hotelPreferenceManager.hotelPositionTableContainerId,
                                                          tableId = hotelPreferenceManager.hotelPositionTableId) {

        function editButton(hotelPosition) {
            return spawn('button.btn.sm.edit', {
                onclick: function(e) {
                    e.preventDefault();
                    hotelPreferenceManager.hotelPositionDialog(hotel, hotelPosition, false)
                }
            }, '<i class="fa fa-pencil" title="Edit Position"></i>');
        }

        function deleteButton(hotelPosition) {
            return spawn('button.btn.sm.delete', {
                onclick: function(e) {
                    e.preventDefault();
                    // remove position from hotel
                    hotel['positions'] = hotel['positions'].filter(position => position['name'] !== hotelPosition['name']);
                    // refresh the table
                    hotelPreferenceManager.hotelPositionsTable(hotel);

                    console.debug("pixi-hotelPreferences.js - Hotel position deleted.")
                },
                title: hotel['positions'].length <= 2 ? "Cannot delete, hotel's must contain at least two positions." : "Delete" ,
                disabled: hotel['positions'].length <= 2,
            }, [ spawn('i.fa.fa-trash') ]);
        }

        let hotelPositionTable = XNAT.ui.table.dataTable(hotel['positions'], {
            name: 'hotel-positions-table',
            table: {
                id: tableId
            },
            items: {
                name: 'Position',
                x: 'X',
                y: 'Y',
                z: '~!', // Hide Z from user as it's not supported by hotel splitter.
                actions: {
                    label: 'Actions',
                    apply: function() {
                            // 'this' refers to each hotel position in the array of hotel positions
                            return spawn('div.center', [editButton(this), pixi.spacer(10), deleteButton(this)])
                        }
                    }
            },
            sortable: "name, x, y"
        }).element

        // Reset table
        if (document.getElementById(containerId)) {
            let containerEl = document.getElementById(containerId)

            // Clear old table
            while (containerEl.firstChild) {
                containerEl.removeChild(containerEl.firstChild);
            }

            // Append new table
            containerEl.appendChild(hotelPositionTable);
        }

        console.debug("pixi-hotelPreferences.js - Hotel position table initialized")

        return hotelPositionTable;
    }

    // dialog to create/edit a mouse hotel
    hotelPreferenceManager.hotelDialog = function(hotel, isNew) {
        const doWhat = (isNew) ? 'Create' : 'Edit';
        hotel = hotel || hotelPreferenceManager.emptyHotel();

        XNAT.dialog.open({
            title: `${doWhat} Hotel`,
            content: spawn('form'),
            maxBtn: true,
            width: 750,
            beforeShow: function(obj) {
                //spawn hotel form
                const $formContainer = obj.$modal.find('.xnat-dialog-content');
                $formContainer.addClass('panel');
                obj.$modal.find('form').append(
                    spawn('!', [
                        XNAT.ui.panel.input.text({
                            name: 'hotelName',
                            id: 'hotelName',
                            label: 'Hotel Name *',
                            value: hotel['name'],
                            description: 'Provide a name for this hotel.'
                        }).element,
                        spawn(`div#${hotelPreferenceManager.hotelPositionTableContainerId}`)
                    ])
                );

                hotelPreferenceManager.hotelPositionsTable(hotel);
            },
            buttons: [
                {
                    label: 'Save',
                    isDefault: true,
                    close: false,
                    action: function() {
                        // Validation
                        // Individual positions are validated by the hotel position dialog

                        // validate hotelName
                        const hotelNameEl = document.getElementById("hotelName");
                        const validateHotelName = XNAT.validate(hotelNameEl).reset().chain();
                        validateHotelName.is('notEmpty').failure('Hotel name is a required field');

                        validateHotelName.check()
                        let errorMessages = validateHotelName.messages;

                        if (errorMessages.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: pixi.clientErrorHandler(errorMessages)
                            })

                            return;
                        }

                        // validate name does not match other names
                        hotelPreferenceManager.hotels.filter(h => h['name'] !== hotel['name']).forEach(h => {
                            // Name matches another hotel name
                            if (h['name'] === hotelNameEl.value) {
                                errorMessages.push('Hotel name matches another hotel name. Hotel names must be unique.');
                                hotelNameEl.classList.add('invalid');
                            }
                        })

                        // Hotels must have two hotel positions, otherwise it wouldn't be a hotel.
                        if (hotel['positions'].length <=1) {
                            errorMessages.push('Hotels must have at least two animal positions.');
                        }

                        if (errorMessages.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: pixi.clientErrorHandler(errorMessages)
                            })

                            return;
                        }

                        // No errors can submit
                        // Hotel positions have already been updated, just need to update the name
                        const oldHotelName = hotel['name'];
                        hotel['name'] = hotelNameEl.value;

                        XNAT.xhr.ajax({
                            url: isNew ? hotelUrl() : hotelUrl(oldHotelName),
                            data: JSON.stringify(hotel),
                            method: isNew ? 'POST' : 'PUT',
                            contentType: 'application/json',
                            success: function () {
                                XNAT.ui.banner.top(1000, '<b>"' + hotel['name'] + '"</b> saved.', 'success');
                                xmodal.closeAll();
                                XNAT.ui.dialog.closeAll();
                                hotelPreferenceManager.refreshTable();
                            },
                            fail: function (e) {
                                console.error(e)
                                pixi.serverErrorHandler(e, 'Could not ' + doWhat.toLocaleLowerCase() + ' ' + hotel['name']);
                            }
                        })
                    }
                },
                {
                    label: 'Canel',
                    close: true,
                    action: function() {
                        hotelPreferenceManager.refreshTable();
                    }
                },
                {
                    label: 'Add Hotel Position',
                    close: false,
                    className: 'pull-left',
                    action: function() {
                        hotelPreferenceManager.hotelPositionDialog(hotel, hotelPreferenceManager.emptyHotelPosition(), true);
                    }
                }
            ]
        })
    }

    // table listing of hotels
    hotelPreferenceManager.table = function(container, callback) {

        // initialize the table
        const hotelTable = XNAT.table({
            className: 'hotels xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        })

        // add table header row
        hotelTable.tr()
            .th({ addClass: 'left', html: '<b>Hotel</b>' })
            .th('<b>Actions</b>')

        function editButton(item) {
            return spawn('button.btn.sm.edit', {
                onclick: function(e) {
                    e.preventDefault();
                    hotelPreferenceManager.hotelDialog(item, false);
                }
            }, '<i class="fa fa-pencil" title="Edit Hotel"></i>');
        }

        function deleteButton(item) {
            return spawn('button.btn.sm.delete', {
                onclick: function() {
                    xmodal.confirm({
                        height: 220,
                        scroll: false,
                        content: "" +
                            "<p>Are you sure you'd like to delete this hotel?</p>" +
                            "<p><b>This action cannot be undone.</b></p>",
                        okAction: function() {
                            XNAT.xhr.delete({
                                url: hotelUrl(item['name']),
                                contentType: 'application/json',
                                success: function() {
                                    XNAT.ui.banner.top(1000, '<b>"'+ item['name'] + '"</b> deleted.', 'success');
                                    hotelPreferenceManager.refreshTable();
                                },
                                fail: function(e) {
                                    console.error(e)
                                    pixi.serverErrorHandler(e, 'Could not delete ' + item['name']);
                                }
                            });
                        }
                    })
                },
                title: "Delete",
            }, [ spawn('i.fa.fa-trash') ]);
        }

        hotelPreferenceManager.getHotels().done(function(hotels) {
            // Sort table by hotel name.
            hotels.sort(pixi.compareGenerator('name'))

            // create row for each hotel
            hotels.forEach(item => {
                hotelTable.tr()
                    .td([ spawn('div.left', [item['name']]) ])
                    .td([ spawn('div.center', [editButton(item), pixi.spacer(10), deleteButton(item)]) ])
            })

            if (container) {
                $$(container).append(hotelTable.table);
            }

            if (isFunction(callback)) {
                callback(hotelTable.table);
            }
        })

        hotelPreferenceManager.$table = $(hotelTable.table);

        console.debug("pixi-hotelPreferences.js - Hotel table created")

        return hotelTable.table;
    }

    // initialize the hotel preference manager
    hotelPreferenceManager.init = function(container) {

        const $manager = $$(container || `div#${hotelPreferenceManager.containerId}`);
        const $footer = $(`#${hotelPreferenceManager.containerId}`).parents('.panel').find('.panel-footer');

        hotelPreferenceManager.$container = $manager;

        $manager.append(hotelPreferenceManager.table());

        const newHotel = spawn('button.new-container-host.btn.btn-sm.submit', {
            html: 'New Hotel',
            onclick: function() {
                hotelPreferenceManager.hotelDialog(null, true);
            }
        });

        // add the 'add new' button to the panel footer
        $footer.append(spawn('div.pull-right', [newHotel]));
        $footer.append(spawn('div.clear.clearFix'));

        console.debug("pixi-hotelPreferences.js - Hotel panel initialized")

        return {
            element: $manager[0],
            spawned: $manager[0],
            get: function() {
                return $manager[0]
            }
        };
    }

    hotelPreferenceManager.refresh = hotelPreferenceManager.refreshTable = function() {
        hotelPreferenceManager.$table.remove();
        hotelPreferenceManager.table(null, function(table) {
            hotelPreferenceManager.$container.prepend(table);
        });
        console.debug("pixi-hotelPreferences.js - Hotel table refreshed")
    };

}));