package easily.tech.easybridge.lib;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import easily.tech.easybridge.lib.handler.BridgeHandler;

import static easily.tech.easybridge.lib.CallBackMessage.CODE_SECURITY_FORBIDDEN;

/**
 * the object will mapping to a js object when webView loaded.
 * it is the java bridge object which link the java layer and js layer.
 * <p>
 * Created by lemon on 29/03/2018.
 */
final class EasyBridge {

    private static class CallBackHandler extends Handler {
    }

    private static final String JAVA_SCRIPT_PROTOCOL = "javascript:";
    private static final String CALLBACK_FUNCTION = "%s._dispatchResult(\"%s\",\'%s\')";

    private Map<String, BridgeHandler> registerHandlerMap;
    private CallBackHandler callBackHandler;
    private SoftReference<WebView> bridgeWebView;
    private String bridgeName;

    EasyBridge(WebView webView, String bridgeName) {
        this.bridgeName = bridgeName;
        registerHandlerMap = new HashMap<>();
        this.bridgeWebView = new SoftReference<>(webView);
        this.callBackHandler = new CallBackHandler();
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
        final Gson gson = new GsonBuilder().create();
        final ResultCallBack callBack = new ResultCallBack(callbackId) {
            @Override
            public void onResult(Object result) {
                dispatchResult(getCallbackId(), gson.toJson(CallBackMessage.generateSuccessMessage(result)));
            }
        };
        if (TextUtils.isEmpty(handlerName)) {
            callBack.onResult(CallBackMessage.generateErrorMessage(CallBackMessage.CODE_INVALID_HANDLER, "the handlerName is not invalid"));
            return;
        }
        final BridgeHandler handler = findTargetHandler(handlerName);
        if (handler == null) {
            callBack.onResult(CallBackMessage.generateErrorMessage(CallBackMessage.CODE_NO_HANDLER, "handler with name " + handlerName + " is not registered in Java code"));
            return;
        }
        if (handler.securityPolicyChecker() != null && !handler.securityPolicyChecker().check(currentPageUrl, parameters)) {
            callBack.onResult(CallBackMessage.generateErrorMessage(CODE_SECURITY_FORBIDDEN, "handler with name " + handlerName + " is not allowed to invoke in page:" + currentPageUrl));
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
        callBackHandler.post(new Runnable() {
            @Override
            public void run() {
                executeScriptInMain(callBackScript);
            }
        });
    }

    private void executeScriptInMain(String script) {
        // 如果系统版本在android4.4及以上，则使用evaluateJavascript，这个方法可以拿到js执行的返回值，否则使用loadUrl
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bridgeWebView.get().evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        } else {
            bridgeWebView.get().loadUrl(String.format("%s%s", JAVA_SCRIPT_PROTOCOL, script));
        }
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

}
