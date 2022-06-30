/*
 *  PIXI Upload Bli
 */

console.log('pixi-uploadBli.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});
XNAT.plugin.pixi.upload = upload = getObject(XNAT.plugin.pixi.upload || {});

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

    function importUrl(projectId) {
        return XNAT.url.csrfUrl(`/data/projects/${projectId}`)
    }

    XNAT.plugin.pixi.upload.validate = function() {
        console.debug('pixi-uploadBli.js: XNAT.plugin.pixi.upload.validate');





        return true;
    }

    XNAT.plugin.pixi.upload.bli = function(projectId, file) {
        console.debug('pixi-uploadBli.js: XNAT.plugin.pixi.upload.bli');

    }

}));