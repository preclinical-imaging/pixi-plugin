var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = getObject(XNAT.plugin.pixi || {});
console.log('pixi-editScanRecord.js');

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

    const project = XNAT.data.context.project;
    var hotelScanRecords, setLabel;

    XNAT.plugin.pixi.handleHotelSelection = function(){
        // reset hotel subjects
        $('#hotel-units').empty();

        const createHotelUnit = function (i, position) {
            // i = the unit index

            return spawn('div.column', [
                spawn('div.panel.panel-default.hotel-unit', [
                    spawn('div.panel-body', [
                        spawn('h4', 'Position: <span class="hotel-unit-position">' + position.name + '</span>'),
                        spawn('input', {
                            type: 'hidden',
                            name: 'pixi:hotelScanRecord/hotel_subjects/subject[' + i + ']/position',
                            value: position.name
                        }),
                        XNAT.ui.panel.select.single({
                            className: 'stacked subject-selector',
                            name: 'pixi:hotelScanRecord/hotel_subjects/subject[' + i + ']/subject_id',
                            label: 'Subject ID',
                            options: XNAT.plugin.pixi.pixiSubjects
                        }),
                        spawn('input', {
                            type: 'hidden',
                            className: 'subject-label-selector',
                            name: 'pixi:hotelScanRecord/hotel_subjects/subject[' + i + ']/subject_label',
                        }),
                        XNAT.ui.panel.select.single({
                            className: 'stacked',
                            name: 'pixi:hotelScanRecord/hotel_subjects/subject[' + i + ']/orientation',
                            label: 'Orientation',
                            options: {
                                HFP:'Head First-Prone',
                                HFS:'Head First-Supine',
                                HFDR:'Head First-Decubitus Right',
                                HFDL:'Head First-Decubitus Left',
                                FFDR:'Feet First-Decubitus Right',
                                FFDL:'Feet First-Decubitus Left',
                                FFP: 'Feet First-Prone',
                                FFS: 'Feet First-Supine',
                                LFP: 'Left First-Prone',
                                LFS: 'Left First-Supine',
                                RFP: 'Right First-Prone',
                                RFS: 'Right First-Supine',
                                AFDR: 'Anterior First-Decubitus Right',
                                AFDL: 'Anterior First-Decubitus Left',
                                PFDR: 'Posterior First-Decubitus Right',
                                PFDL: 'Posterior First-Decubitus Left'
                            }
                        }),
                        XNAT.ui.panel.input({
                            className: 'stacked',
                            name: 'pixi:hotelScanRecord/hotel_subjects/subject[' + i + ']/weight',
                            label: 'Weight (g)'
                        }),
                        XNAT.ui.panel.input({
                            className: 'stacked',
                            type: 'time',
                            step: '10',
                            name: 'pixi:hotelScanRecord/hotel_subjects/subject[' + i + ']/injection_time',
                            label: 'Injection Time',
                            description: 'Specify HH:MM:SS and AM/PM'
                        }),
                        XNAT.ui.panel.input({
                            className: 'stacked',
                            name: 'pixi:hotelScanRecord/hotel_subjects/subject[' + i + ']/activity',
                            label: 'Activity (mCi)',
                            description: 'Enter units in millicuries'
                        }),
                        XNAT.ui.panel.textarea({
                            className: 'stacked',
                            name: 'pixi:hotelScanRecord/hotel_subjects/subject[' + i + ']/notes',
                            label: 'Notes',
                            rows: 6
                        }).spawned
                    ])
                ])
            ])
        };

        // Get the selected hotel
        const selectedHotelName = document.getElementById("pixi:hotelScanRecord/hotel").value;
        const selectedHotel = XNAT.plugin.pixi.hotels.find(hotel => hotel.name === selectedHotelName);

        // format hotel unit display
        if (selectedHotel.positions.length % 3 === 0) {
            $('#hotel-units').removeClass('col-2').addClass('col-3');
        }
        else {
            $('#hotel-units').removeClass('col-3').addClass('col-2');
        }

        // Create units for each position in the hotel
        selectedHotel.positions.forEach(function (position, i) {
            $('#hotel-units').append(createHotelUnit(i, position));
        })
    };
    XNAT.plugin.pixi.hotelScanRecords = hotelScanRecords = [];

    XNAT.plugin.pixi.updateScanRecord = function(status, scanRecordLabel, msg){
        if (status === 'Ready To Split') {
            XNAT.xhr.ajax({
                method: 'PUT',
                url: XNAT.url.csrfUrl('/xapi/pixi/hotelscanrecords/'+scanRecordLabel+'/project/'+project+'/status'),
                data: status,
                contentType: 'text/plain',
                success: function(data){
                    $('#status-updater').html(
                        spawn('div.success',[
                            'Session found in project archive! Status updated to "'+status+'". ',
                            spawn('a', { href: '.' },'Reload the page'),
                            ' to update.'
                        ])
                    );
                },
                fail: function(e){
                    function showError() {
                        XNAT.ui.dialog.open({ title: 'Error', content: e.responseText });
                    }
                    $('#status-updater').html(
                        spawn('div.warning',[
                            'Session found in project archive, but could not update status. ',
                            spawn('a', { href: '#!', onclick: 'showError()' },'View error report'),
                            '.'
                        ])
                    );
                }
            })
        }
    };

    XNAT.plugin.pixi.checkSession = function(e,sessionLabel,scanRecordLabel) {
        if (e) {
            e.preventDefault();
            if (!sessionLabel)  window.location.assign(e.target.href);
        }

        var sessionFound = false,
            sessionStatus = false,
            statusDiv = $('#status-updater');

        statusDiv.html(spawn('div.note',[
            spawn('i.fa.fa-clock-o.pull-left', {
                style: { 'margin-left': '-20px' }
            }),
            'Searching for image session...'
        ]));

        // first, check to see if session is archived
        XNAT.xhr.getJSON({
            url: XNAT.url.rootUrl('/data/projects/'+ project +'/experiments/'+sessionLabel ),
            async: false,
            success: function (data,e){
                if (e === 'success') {
                    sessionFound = true;
                    sessionStatus = 'Ready To Split';
                }
            }
        });

        if (sessionFound) {
            window.setTimeout(function(){
                XNAT.plugin.pixi.updateScanRecord(sessionStatus,scanRecordLabel, 'Image session found in Project Archive');
            },1200);
            return false;
        }

        // second, see if session is in the listing of Prearchive items for this project
        XNAT.xhr.getJSON({
            url: XNAT.url.rootUrl('/data/prearchive/projects/'+project),
            async: false,
            success: function (data){
                var prearchiveSessions = data.ResultSet.Result;
                if (prearchiveSessions.length) {
                    var prearcSession = prearchiveSessions.filter((session) => { return session.name === sessionLabel })
                    if (prearcSession.length){
                        sessionFound = true;
                        sessionStatus = prearcSession[0].status;
                    }
                }
            }
        });

        if (sessionFound) {
            var msg = 'This image session was found in the Prearchive with a status of '+sessionStatus+'. ',
                okayLink = '<a href="'+ XNAT.url.rootUrl('/app/template/XDATScreen_prearchives.vm') +'">Go to the Prearchive</a>',
                messageType = 'note';
            switch (sessionStatus) {
                case 'READY' :
                    msg+='It is ready to be archived. '+ okayLink +' to add it to your project archive.';
                    messageType = 'success';
                    break;
                case 'RECEIVING' :
                    msg+='It is still receiving data. We recommend checking back when the session has a status of "READY". ';
                    messageType = 'message';
                    break;
                case 'ERROR' :
                    msg+= okayLink + ' to determine what to do next.';
                    messageType = 'warning';
                    break;
                default:
                    msg += okayLink + '.';
                    break;
            }

            window.setTimeout(function(){
                statusDiv.html(spawn('div', { addClass: messageType, html: msg }));
            }, 1200);
        }
        else {
            window.setTimeout(function(){
                statusDiv.html(spawn('div.message', 'No record of this image session was found in the prearchive or the archive. You might continue waiting, or double-check your session name.'));
            }, 1200);

        }

    };

    XNAT.plugin.pixi.initScanRecord = function(newExperiment = true){
        // get all known scan records
        XNAT.xhr.getJSON({
            url: XNAT.url.rootUrl('/data/projects/' + project + '/experiments?xsiType=pixi:hotelScanRecord'),
            async: false,
            success: function (data) {
                if (data.ResultSet.Result.length) {
                    data.ResultSet.Result.forEach((record) => {
                        hotelScanRecords.push({
                            label: record.label,
                            session: record.label.split('_scan_record')[0]
                        });
                    });
                }
            },
            fail: function (e) {
                console.warn(e)
            }
        });

        // populate the image session selector with all known image sessions in this project
        XNAT.xhr.getJSON({
            url: XNAT.url.rootUrl('/data/projects/'+project+'/experiments?xsiType=xnat:imageSessionData'),
            async: false,
            success: function (data) {
                let sessions = data.ResultSet.Result;
                if (sessions.length) {
                    // remove image sessions that have already been split and sort alphabetically
                    sessions = sessions
                        .filter((session) => { return session.label.indexOf('_split_')<0 })
                        .sort((a, b) => {return(a.label < b.label) ? -1 : 1 });
                    sessions.forEach(session => {
                        $('select#session-selector').append(
                            spawn('option', {
                                data: {sessiondate: session.date, sessionid: session['session_ID']},
                                value: session.label,
                                html: session.label
                            }))
                    });

                    if (!newExperiment){
                        // replace the placeholder session label text with proper label name
                        var selectedSessionId = document.getElementById("pixi:hotelScanRecord/session_label").value;
                        var selectedSession = sessions.filter((session) => {return session['session_ID'] === selectedSessionId})[0];
                        $('.selected-session-label').html(selectedSession.label);
                    }
                }
            }
        });

        if (newExperiment){
            let hotels;

            XNAT.xhr.get({
                url: XNAT.url.rootUrl("/xapi/pixi/hotels/"),
                dataType: 'json',
                async: false,
                success: function (data) {
                    XNAT.plugin.pixi.hotels = hotels = data;
                    setHotelOptions(hotels);
                    // handleHotelSelection();
                }
            });

            function setHotelOptions(hotels) {
                let hotelSelectorEl = document.getElementById("pixi:hotelScanRecord/hotel");

                hotels.forEach(hotel => {
                    let hotelName = hotel.name;
                    hotelSelectorEl.options.add(new Option(hotelName, hotelName))
                })
            }
        }
    };

    XNAT.plugin.pixi.toggleSessionSource = function(source){
        // toggle the source of the image session -- either already in the project, or awaiting import
        $('.session-source').each(function(){
            if ($(this).data('source') === source) {
                $(this).removeClass('disabled').addClass('active');
                $(this).find('.panel-element').removeClass('disabled');
                $(this).find('input').prop('disabled',false);
                $(this).find('select').prop('disabled',false);
                $(this).find('input.session-source-toggle').prop('checked','checked');
            }
            else {
                $(this).removeClass('active').addClass('disabled');
                $(this).find('.panel-element').addClass('disabled');
                $(this).find('input').prop('disabled','disabled');
                $(this).find('select').prop('disabled','disabled');
                $(this).find('input.session-source-toggle').prop('checked',false);
            }
        });

        // toggle Scan Record Status based on Session source
        var statusEl = document.getElementById('pixi:hotelScanRecord/status');
        if (['Split Begun','Complete','Error'].indexOf(statusEl.value) <0 ) {
            statusEl.value = (source === 'existing') ? 'Ready to Split' : 'Waiting for Session';
        }

    };

    XNAT.plugin.pixi.setLabel = setLabel = function(session){
        // check for existence of ID field, which means these fields should not be edited
        var idField = document.getElementById('pixi:hotelScanRecord/ID');

        if (!idField.value) {
            // check for existence of other scan records for this session
            var existingRecords = hotelScanRecords.filter((record) => { return record.session === session });
            var iterator = (existingRecords.length) ? '_'+existingRecords.length : '';

            document.getElementById('pixi:hotelScanRecord/label').value = session+'_scan_record'+iterator;
            document.getElementById('pixi:hotelScanRecord.ID').value = session+'_scan_record'+iterator+'_id';
        }
    };

    $(document).on('click','.session-source',function(){
        if (!$(this).hasClass('active')) {
            var source = $(this).data('source');
            XNAT.plugin.pixi.toggleSessionSource(source);
        }
    });

    $(document).on('change','input.session-source-toggle',function(){
        var source = $(this).val();
        XNAT.plugin.pixi.toggleSessionSource(source);
    });

    $(document).on('change','#session-selector',function(){
        var session = $(this).find('option:selected');
        setLabel(session.val());

        var sessionDate = session.data('sessiondate');
        $('.selected-session-date').html(sessionDate);
        $('.session-source.active').find('input[name="pixi:hotelScanRecord/date"]').val(sessionDate);
    });

    $(document).on('change','.subject-selector',function(){
        var subjectLabel = $(this).find('option:selected').html();
        $(this).parents('.hotel-unit').find('input.subject-label-selector').val(subjectLabel);
    });

    $(document).on('blur','#new-session-label',function(){
        setLabel($(this).val());
    });

}));