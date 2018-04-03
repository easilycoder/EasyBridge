package easily.tech.easybridge.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import easily.tech.easybridge.lib.handler.BridgeHandler;

/**
 * this class is the main entrance of the library,you are supposed to do things as bellow:
 * 1. register handler that to be call by JavaScript:{@link #registerHandler(BridgeHandler)}
 * 2. set a global security checker:{@link #setPolicyChecker(SecurityPolicyChecker)}
 *    before inject a bridge,you will receive a check request with the parameters:{@link EasyBridgeWebChromeClient#SECURITY_CHECK_PARAMETERS},make your idea about it;
 * 3. you can make a global security check by:{@link #checkSecurityGlobally(String, String)}
 * 4. you can call the JavaScript function with:{@link #callHandler(String, String, ResultCallBack)},
 *    but make sure before:you had register a JavaScript handler using the bridge,the code is like below:
 *    "window.easyBridge.registerHandler(handlerName,realFunction)"
 * <p>
 * Created by lemon on 29/03/2018.
 */
public class EasyBridgeWebView extends WebView {

    static final String MAPPING_JS_INTERFACE_NAME = "_easybridge";
    private static final String DEFAULT_BRIDGE_NAME = "easyBridge";
    private final EasyBridge easyBridge;
    private String bridgeName = DEFAULT_BRIDGE_NAME;
    protected SecurityPolicyChecker policyChecker;

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
            easyBridge.callHandler(handlerName, parameters, resultCallBack);
        }
    }

    public void clear() {
        if (easyBridge != null) {
            easyBridge.clear();
        }
    }

    public String getBridgeName() {
        return bridgeName;
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (easyBridge != null) {
            easyBridge.destroy();
        }
    }
}
