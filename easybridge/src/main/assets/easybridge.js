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

        var parameters = args || "";
        if (typeof parameters == 'object') {
            parameters = JSON.stringify(parameters);
        }
        //the name '_easybridge' is an java object that mapping to a javascript object,using addJavaInterface in Java code
        if (window._easybridge) {
            //the function enqueue is the pubic method from Java Code,using to call Java logic
            _easybridge.enqueue(handlerName, location.href, parameters, callbackId);
        } else {
            console.error(bridgeName + ':' + "the mapping object '_easybridge' had not been added any more");
        }
    }

    //the function for JavaScript to register handler that Java Code Can Executed
    function registerHandler(handlerName, handler) {
        window[bridgeName][handlerName] = handler;
    }

    //the function for Java Code to invoked JavaScript function 
    function _executeScript(handlerName, parameters, callbackId) {
        if (!handlerName) {
            console.error('invalid handlerName from Java code');
            return;
        }
        var handler = window[bridgeName][handlerName];
        if (!handler) {
            console.error('the handler with name \'' + handlerName + '\' to be invoked is not existed');
            return;
        }
        try {
            if (callbackId) {
                callbackFunc = function (result) {
                    if (window._easybridge) {
                        //the method onExecuteJSCallback is a Java Code,using to dispatch result after execute the JavaScript function from Java
                        _easybridge.onExecuteJSCallback(callbackId, result);
                    } else {
                        console.error(bridgeName + ':' + "the mapping object '_easybridge' had not been added any more");
                    }
                };
                handler(parameters, callbackFunc);
            } else {
                handler(parameters);
            }
        } catch (exception) {
            console.error(exception);
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
        if (typeof result == 'string') {
            result = JSON.parse(result.replace(/\n/g, '\\\\n'));
        }
        return result;
    }

    //init the bridge object
    window[bridgeName] = {
        callHandler: callHandler,
        registerHandler: registerHandler,
        _executeScript: _executeScript,
        _dispatchResult: _dispatchResult,

    };
    //notify to native that the bridge had been injected finished
    window[bridgeName].callHandler('injectFinished');
    //notify to javascript that the bridge had been init
    var doc = document;
    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('WebViewJavascriptBridgeReady');
    readyEvent.bridge = window[bridgeName];
    doc.dispatchEvent(readyEvent);

})();