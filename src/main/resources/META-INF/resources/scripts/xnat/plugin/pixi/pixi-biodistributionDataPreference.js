/*!
 * PIXI Biodistribution Data Preference
 */

console.log('pixi-biodistributionDataPreference.js');

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

    var restUrl = XNAT.url.restUrl

    var biodistributionDataPreferenceManager;
    XNAT.plugin.pixi.biodistributionDataPreferenceManager = biodistributionDataPreferenceManager = getObject(XNAT.plugin.pixi.biodistributionDataPreferenceManager || {});


    biodistributionDataPreferenceManager.getBiodistributionDataPreferenceUrl = function() {
        const projectID = XNAT.data.context.project;
        return restUrl(`/xapi/pixi/preferences/biodistributionAcceptedSampleTypes/projects/${projectID}`);
    }

    biodistributionDataPreferenceManager.setActionUrl = function(formElementId) {
        let form$ = document.getElementById(formElementId);
        form$.action = biodistributionDataPreferenceManager.getBiodistributionDataPreferenceUrl();
    }

    biodistributionDataPreferenceManager.initPreference = function(preferenceElementId) {
        XNAT.xhr.get({
            url: biodistributionDataPreferenceManager.getBiodistributionDataPreferenceUrl(),
            dataType: 'json',
            success: function(data) {
                $('#biodistribution-accepted-sample-types').val(data['biodistributionAcceptedSampleTypes']);
            }
        });
    }

    biodistributionDataPreferenceManager.init = function() {
        biodistributionDataPreferenceManager.initPreference('biodistributionAcceptedSampleTypes');
        biodistributionDataPreferenceManager.setActionUrl("biodistributionUploadForm")
    }
}));