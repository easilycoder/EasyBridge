//you should inject the code below in Java ,if the bridgeName should defined customly
//var bridgeName = "easyBridge";
//define and invoke the function immediately to inject the bridge object
(function () {
    var uniqueId = 1;
    var callbackArray = {};

    //return directly,if the bridge had been injected
    if (window[bridgeName]) {
        return;
    }
    //the function that should be invoke if the javascript want to invoke the java code
    function callHandler(handlerName, args, callback) {
        //check whether the parameters existed
        var callbackId = '';

        if (typeof args == 'function') {
            callback = args;
            args = {};
        }

        if (typeof callback == 'function') {
            callbackId = 'cb_' + (uniqueId++) + '_' + new Date().getTime();
            callbackArray[callbackId] = callback;
        }

        var parameters;
        if (typeof args == 'string') {
            parameters = args;
        } else {
            parameters = args || {};
            parameters = JSON.stringify(parameters);
        }
        //the name '_easybridge' is an java object that mapping to a javascrip onject,using addJavaInterface in Java code
        if (window._easybridge) {
            setTimeout(function () {
                _easybridge.enqueue(handlerName, location.href, parameters, callbackId);
            }, 0);
        } else {
            console.error(bridgeName + ':' + "the mapping object '_easybridge' had not been added any more");
        }
    }

    //the function for the java code to dispatch the result
    function _dispatchResult(callbackId, result) {
        //find the target callback function and invoke it
        if (callbackId && callbackArray[callbackId]) {
            setTimeout(function () {
                result = _resolveResult(result);
                if (result.code == 0) {
                    console.log(bridgeName + ':success,' + result.result);
                    callbackArray[callbackId](result.result);
                } else {
                    console.error(bridgeName + ':' + result.description);
                }
                delete callbackArray[callbackId];
            }, 0);

        } else {
            console.error('did not find the callback match with the id:' + callbackId);
        }
    }

    function _resolveResult(result) {
        console.log(result);
        if (result) {
            result = typeof result == 'object' ? JSON.stringify(result) : JSON.parse(result.replace(/\n/g, '\\\\n'));
        }
        return result;
    }

    //init the bridge object
    window[bridgeName] = {
        callHandler: callHandler,
        _dispatchResult: _dispatchResult,
    };
    //notify to javascript that the bridge had been init
    var doc = document;
    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('WebViewJavascriptBridgeReady');
    readyEvent.bridge = window[bridgeName];
    doc.dispatchEvent(readyEvent);

})();