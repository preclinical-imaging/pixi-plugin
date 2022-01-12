/*!
 * Site-wide Admin UI functions for PIXI
 *
 * This script depends on pixi-speciesPreferences.js, pixi-vendorPreferences.js, and pixi-xenograft.js
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

    XNAT.plugin.pixi.speciesPreferenceManager.init('#species-preferences-manager');
    XNAT.plugin.pixi.vendorPreferenceManager.init('#vendor-preferences-manager');
    XNAT.plugin.pixi.pdxManager.init('#pdx-manager');
    XNAT.plugin.pixi.cellLineManager.init('#cell-line-manager');

}));