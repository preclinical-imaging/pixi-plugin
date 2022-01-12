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

    XNAT.plugin.pixi.serverErrorHandler = function(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        const errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>' : e;
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

    XNAT.plugin.pixi.urlValidator = function(inputs) {
        const errorMsg = [];

        if (inputs.length) {
            inputs.forEach(function($input) {
                // no error for empty url
                if ($input.val()) {
                    // https://stackoverflow.com/questions/5717093/check-if-a-javascript-string-is-a-url
                    let url;

                    try {
                        url = new URL($input.val());

                        if (!(url.protocol === "http:" || url.protocol === "https:")) {
                            errorMsg.push('<b>' + $input.prop('name') + '</b> is not a valid URL.');
                            $input.addClass('invalid');
                        }
                    } catch (_) {
                        errorMsg.push('<b>' + $input.prop('name') + '</b> is not a valid URL.');
                        $input.addClass('invalid');
                    }
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
}));