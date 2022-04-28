/*
 *  PIXI Module
 *
 *  Contains commonly used functions.
 */

console.log('pixi-module.js');

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

    let undefined;
    let restUrl = XNAT.url.restUrl

    XNAT.plugin.pixi.serverErrorHandler = function(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        const errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + '</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function() {
                        if (closeAll) {
                            xmodal.closeAll();
                        }
                    }
                }
            ]
        });
    }

    XNAT.plugin.pixi.clientErrorHandler = function(errorMsg) {
        var errors = [];
        errorMsg.forEach(function(msg) { errors.push(spawn('li',msg)) });

        return spawn('div',[
            spawn('p', 'Errors found:'),
            spawn('ul', errors)
        ]);
    }

    XNAT.plugin.pixi.inputsValidator = function(inputs) {
        const errorMsg = [];

        if (inputs.length) {
            inputs.forEach(function($input) {
                if (!$input.val()) {
                    errorMsg.push('<b>' + $input.prop('name') + '</b> requires a value.');
                    $input.addClass('invalid');
                }
            });
        }

        return errorMsg;
    }

    XNAT.plugin.pixi.spacer = function(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    XNAT.plugin.pixi.compareGenerator = function(property) {
        return function(a,b) {
            const aValue = a[property].toUpperCase()
            const bValue = b[property].toUpperCase()

            return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        }
    }

    XNAT.plugin.pixi.getPreference = function(preference, callback) {
        callback = isFunction(callback) ? callback : function() {};
        return XNAT.xhr.get({
            url: restUrl(`/xapi/pixi/preferences/${preference}`),
            dataType: 'json',
            success: function(data) {
                callback.apply(this, arguments);
            }
        });
    };

}));