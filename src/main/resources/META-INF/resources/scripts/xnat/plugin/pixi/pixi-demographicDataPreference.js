/*!
 * PIXI Demographic Data Preference
 */

console.log('pixi-demographicDataPreference.js');

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

    let demographicDataPreferenceManager;
    XNAT.plugin.pixi.demographicDataPreferenceManager = demographicDataPreferenceManager = getObject(XNAT.plugin.pixi.demographicDataPreferenceManager || {});


    demographicDataPreferenceManager.getDemographicDataPreferenceUrl = function() {
        const projectID = XNAT.data.context.project;
        return restUrl(`/xapi/pixi/preferences/demographic-data-impl/projects/${projectID}`);
    }

    // URL are hard coded into the spawner configuration, need to set after spawner has created the page
    demographicDataPreferenceManager.setActionUrl = function(formElementId) {
        let form$ = document.getElementById(formElementId);
        form$.action = demographicDataPreferenceManager.getDemographicDataPreferenceUrl();
        console.debug('pixi-demographicDataPreference.js - action url set');
    }

    // After the URL is set then spawner will refresh correctly after submission. But the URL is not initially known
    // when spawner sets up the page so we need to set the preference manually initially.
    demographicDataPreferenceManager.initPreference = function(preferenceElementId) {
        XNAT.xhr.get({
            url: demographicDataPreferenceManager.getDemographicDataPreferenceUrl(),
            dataType: 'json',
            success: function(data) {
                let preference = data['demographicDataImpl'];
                let options = document.getElementById(preferenceElementId)

                for (let i = 0; i < options.length; i++) {
                    if (options[i].value === preference) {
                        options[i].selected = true;
                        console.debug('pixi-demographicDataPreference.js - preference initialized');
                    }
                }
            }
        });
    }

    demographicDataPreferenceManager.init = function() {
        demographicDataPreferenceManager.initPreference('demographicDataImpl');
        demographicDataPreferenceManager.setActionUrl("demographicDataImplProjForm")
    }

    demographicDataPreferenceManager.init();
}));