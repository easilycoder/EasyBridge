package tech.easily.easybridge.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import tech.easily.easybridge.lib.handler.BaseBridgeHandler;
import tech.easily.easybridge.lib.handler.BridgeHandler;

/**
 * this class is the main entrance of the library,you are supposed to do things as bellow:
 * 1. register handler that to be call by JavaScript:{@link #registerHandler(BridgeHandler)}
 * 2. set a global security checker:{@link #setPolicyChecker(SecurityPolicyChecker)}
 * before inject a bridge,you will receive a check request with the parameters:{@link EasyBridgeWebChromeClient#SECURITY_CHECK_PARAMETERS},make your idea about it;
 * 3. you can make a global security check by:{@link #checkSecurityGlobally(String, String)}
 * 4. you can call the JavaScript function with:{@link #callHandler(String, String, ResultCallBack)},
 * but make sure before:you had register a JavaScript handler using the bridge,the code is like below:
 * "window.easyBridge.registerHandler(handlerName,realFunction)"
 * <p>
 * Created by lemon on 29/03/2018.
 */
public class EasyBridgeWebView extends WebView {

    public interface OnBridgeInjectedListener {
        /**
         * observe the bridge injected event of each page
         */
        void onInjected();
    }

    private static final String JAVA_SCRIPT_PROTOCOL = "javascript:";
    static final String MAPPING_JS_INTERFACE_NAME = "_easybridge";
    private static final String DEFAULT_BRIDGE_NAME = "easyBridge";
    private static final String REGISTER_INJECT_FINISHED = "injectFinished";
    private final EasyBridge easyBridge;
    private String bridgeName = DEFAULT_BRIDGE_NAME;
    protected SecurityPolicyChecker policyChecker;
    // whether the bridge had been injected to the currentPage
    private volatile boolean isInjected;
    private OnBridgeInjectedListener listener;

    public EasyBridgeWebView(Context context, String bridgeName) {
        this(context, (AttributeSet) null);
        this.bridgeName = bridgeName;
    }

    public EasyBridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EasyBridgeWebView);
            if (typedArray.hasValue(R.styleable.EasyBridgeWebView_bridgeName)) {
                bridgeName = typedArray.getString(R.styleable.EasyBridgeWebView_bridgeName);
            }
            typedArray.recycle();
        }
        easyBridge = new EasyBridge(this, bridgeName);
        initWebView();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView() {
        // 开启JavaScript的支持
        WebSettings webSettings = getSettings();
        if (webSettings != null) {
            webSettings.setJavaScriptEnabled(true);
        }
        addJavascriptInterface(easyBridge, MAPPING_JS_INTERFACE_NAME);
        EasyBridgeWebChromeClient webChromeClient = new EasyBridgeWebChromeClient(this);
        setWebChromeClient(webChromeClient);
        //register a default handler to subscribe the event of bridge injected
        registerHandler(new BaseBridgeHandler(REGISTER_INJECT_FINISHED, this) {
            @Override
            public void onCall(String parameters, ResultCallBack callBack) {
                Logger.debug("inject bridge success in page:" + getUrl());
                if (listener != null) {
                    listener.onInjected();
                }
                setInjected(true);
            }
        });
    }

    public void registerHandler(BridgeHandler handler) {
        if (easyBridge != null) {
            easyBridge.registerHandler(handler);
        }
    }

    public void unregisterHandler(String handlerName) {
        if (easyBridge != null) {
            easyBridge.unregisterHandler(handlerName);
        }
    }

    // use to execute JavaScript
    public void callHandler(String handlerName, String parameters, ResultCallBack resultCallBack) {
        if (easyBridge != null) {
            if (parameters == null) {
                parameters = "";
            }
            easyBridge.callHandler(handlerName, parameters, resultCallBack);
        }
    }

    public Map<String, BridgeHandler> getAllRegisterHandlers() {
        if (easyBridge == null) {
            return new HashMap<>();
        }
        return easyBridge.getRegisterHandlerMap();
    }

    public void clear() {
        if (easyBridge != null) {
            easyBridge.clear();
        }
    }

    public String getBridgeName() {
        return bridgeName;
    }

    public boolean isInjected() {
        return isInjected;
    }

    void setInjected(boolean injected) {
        isInjected = injected;
    }

    public boolean checkSecurityGlobally(String url, String parameters) {
        return policyChecker == null || policyChecker.check(url, parameters);
    }

    /**
     * make sure that ,the instance {@link SecurityPolicyChecker} set here is working globally,
     * if the security checked failed ,the bridge is not allowed to using (it will remove the bridge inner)
     *
     * @param policyChecker a global {@link SecurityPolicyChecker} instance
     */
    public void setPolicyChecker(SecurityPolicyChecker policyChecker) {
        this.policyChecker = policyChecker;
    }

    public void setBridgeInjectedListener(OnBridgeInjectedListener listener) {
        this.listener = listener;
    }

    public EasyBridgeWebView setDebuggable(boolean debuggable) {
        Logger.setDebuggable(debuggable);
        return this;
    }

    public void evaluateJavascript(String script) {
        evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });
    }

    @Override
    public void evaluateJavascript(String script, @Nullable ValueCallback<String> resultCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super.evaluateJavascript(script, resultCallback);
        } else {
            loadUrl(String.format("%s%s", JAVA_SCRIPT_PROTOCOL, script));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        policyChecker = null;
        if (easyBridge != null) {
            easyBridge.destroy();
        }
    }
}
