package tech.easily.easybridge.lib;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import tech.easily.easybridge.lib.handler.BridgeHandler;

import static tech.easily.easybridge.lib.CallBackMessage.CODE_SECURITY_FORBIDDEN;

/**
 * the object will mapping to a js object when webView loaded.
 * it is the java bridge object which link the java layer and js layer.
 * <p>
 * Created by lemon on 29/03/2018.
 */
final class EasyBridge {

    private static final String CALLBACK_FUNCTION = "%s._dispatchResult(\"%s\",\'%s\')";
    private static final String EXECUTE_SCRIPT = "%s._executeScript(\'%s\',\'%s\',\'%s\')";

    private Map<String, BridgeHandler> registerHandlerMap;
    private Map<String, ResultCallBack> jsCallbackMap;
    private Handler callBackHandler;
    private SoftReference<EasyBridgeWebView> bridgeWebView;
    private String bridgeName;
    private static long uniqueId = 1;

    EasyBridge(EasyBridgeWebView webView, String bridgeName) {
        this.bridgeName = bridgeName;
        registerHandlerMap = new HashMap<>();
        jsCallbackMap = new HashMap<>();
        this.bridgeWebView = new SoftReference<>(webView);
        this.callBackHandler = new Handler(Looper.getMainLooper());
    }


    /**
     * js invoke java async
     *
     * @param handlerName name
     * @param parameters  values from js
     * @param callbackId  the unique id to invoke js callback function
     */
    @JavascriptInterface
    public void enqueue(String handlerName, String currentPageUrl, final String parameters, String callbackId) {
        Logger.debug(String.format("receive call from JavaScript,[handlerName]:%s [currentPage]:%s [parameters]:%s [requestId]:%s", handlerName, currentPageUrl, parameters, callbackId));
        final Gson gson = new GsonBuilder().create();
        final ResultCallBack callBack = new ResultCallBack(callbackId) {
            @Override
            public void onResult(Object result) {
                dispatchResult(getCallbackId(), gson.toJson(CallBackMessage.generateSuccessMessage(result)));
            }
        };
        if (TextUtils.isEmpty(handlerName)) {
            String errorMessage = "the [handlerName]:" + handlerName + " is not invalid";
            callBack.onResult(CallBackMessage.generateErrorMessage(CallBackMessage.CODE_INVALID_HANDLER, errorMessage));
            Logger.error(errorMessage);
            return;
        }
        final BridgeHandler handler = findTargetHandler(handlerName);
        if (handler == null) {
            String errorMessage = "the [handlerName]:" + handlerName + " is not registered in Java code";
            callBack.onResult(CallBackMessage.generateErrorMessage(CallBackMessage.CODE_NO_HANDLER, errorMessage));
            Logger.error(errorMessage);
            return;
        }
        // global security check
        if (!checkGlobalSecurity(currentPageUrl, parameters)) {
            String errorMessage = "the [handlerName]:" + handlerName + " is not allowed to invoke in [page]:" + currentPageUrl + " by the global Security Checker,with [parameters]:" + parameters;
            callBack.onResult(CallBackMessage.generateErrorMessage(CODE_SECURITY_FORBIDDEN, errorMessage));
            Logger.error(errorMessage);
            return;
        }
        // handler security check
        if (handler.securityPolicyChecker() != null && !handler.securityPolicyChecker().check(currentPageUrl, parameters)) {
            String errorMessage = "the [handlerName]:" + handlerName + " is not allowed to invoke in [page]:" + currentPageUrl + ",with [parameters]:" + parameters;
            callBack.onResult(CallBackMessage.generateErrorMessage(CODE_SECURITY_FORBIDDEN, errorMessage));
            Logger.error(errorMessage);
            return;
        }
        // invoke the handler in main thread
        callBackHandler.post(new Runnable() {
            @Override
            public void run() {
                handler.onCall(parameters, callBack);
            }
        });
    }

    /**
     * a method using to return the result from the Java Code execute the JavaScript
     *
     * @param callbackId the uniqueId for request
     * @param result     the result from JavaScript
     */
    @JavascriptInterface
    public void onExecuteJSCallback(String callbackId, final String result) {
        if (jsCallbackMap == null || jsCallbackMap.isEmpty() || TextUtils.isEmpty(callbackId)) {
            return;
        }
        final ResultCallBack resultCallBack = jsCallbackMap.get(callbackId);
        if (resultCallBack != null) {
            jsCallbackMap.remove(callbackId);
            callBackHandler.post(new Runnable() {
                @Override
                public void run() {
                    resultCallBack.onResult(result);
                }
            });
            Logger.debug(String.format("receive result from JavaScript,[callbackId]:%s [result]:%s", callbackId, result));
        }
    }

    void callHandler(String handlerName, String parameters, ResultCallBack resultCallBack) {
        String callbackId = "";
        if (resultCallBack != null) {
            callbackId = "cb_" + (uniqueId++) + System.currentTimeMillis();
            jsCallbackMap.put(callbackId, resultCallBack);
        }
        String executeScript = String.format(EXECUTE_SCRIPT, bridgeName, handlerName, parameters, callbackId);
        executeScriptInMain(executeScript);
        Logger.debug(String.format("call JavaScript in Java,[handlerName]:%s [parameters]:%s [requestId]:%s", handlerName, parameters, callbackId));
    }


    /**
     * if js invoke the java async ,that the result will be returned by execute a script in the bridge
     * since this method may be invoked not in the main thread ,we send a message using the {@link Handler} and resolve it in main thread
     *
     * @param callbackId uniqueId which match to an callback function in js
     * @param parameters value that will be pass to js
     */
    private void dispatchResult(String callbackId, String parameters) {
        if (bridgeWebView == null || bridgeWebView.get() == null) {
            return;
        }
        final String callBackScript = String.format(CALLBACK_FUNCTION, bridgeName, callbackId, parameters);
        executeScriptInMain(callBackScript);
        Logger.debug(String.format("dispatchResult to JavaScript,[callbackId]:%s [result]:%s", callbackId, parameters));
    }

    private void executeScriptInMain(final String script) {
        if (bridgeWebView == null || bridgeWebView.get() == null) {
            return;
        }
        callBackHandler.post(new Runnable() {
            @Override
            public void run() {
                bridgeWebView.get().evaluateJavascript(script);
            }
        });
    }

    /**
     * find the suitable handler
     *
     * @param handlerName name
     * @return instance of {@link BridgeHandler},may be null if the match handler had not been registered yet
     */
    private BridgeHandler findTargetHandler(String handlerName) {
        if (registerHandlerMap == null || registerHandlerMap.isEmpty()) {
            return null;
        }
        for (BridgeHandler handler : registerHandlerMap.values()) {
            if (TextUtils.equals(handlerName, handler.getHandlerName())) {
                return handler;
            }
        }
        return null;
    }

    private boolean checkGlobalSecurity(String url, String parameters) {
        //can not reach the policyChecker,return true directly
        if (bridgeWebView == null || bridgeWebView.get() == null || bridgeWebView.get().policyChecker == null) {
            return true;
        }
        SecurityPolicyChecker checker = bridgeWebView.get().policyChecker;
        return checker.check(url, parameters);
    }

    void registerHandler(BridgeHandler handler) {
        if (handler == null || TextUtils.isEmpty(handler.getHandlerName())) {
            return;
        }
        registerHandlerMap.put(handler.getHandlerName(), handler);
    }

    void unregisterHandler(String handlerName) {
        if (TextUtils.isEmpty(handlerName) || registerHandlerMap == null || registerHandlerMap.isEmpty()) {
            return;
        }
        registerHandlerMap.remove(handlerName);
    }

    void clear() {
        if (registerHandlerMap != null) {
            registerHandlerMap.clear();
        }
    }

    Map<String, BridgeHandler> getRegisterHandlerMap() {
        if (registerHandlerMap == null) {
            return new HashMap<>();
        }
        return registerHandlerMap;
    }

    void destroy() {
        if (registerHandlerMap != null) {
            registerHandlerMap.clear();
        }
        if (jsCallbackMap != null) {
            jsCallbackMap.clear();
        }
    }

}
