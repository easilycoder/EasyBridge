package easily.tech.easybridge.lib;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by hzyangjiehao on 2018/4/2.
 */
public class EasyBridgeWebChromeClient extends WebChromeClient {

    private static final String BRIDGE_SCRIPT_PATH = "easybridge.js";
    private static final String JAVA_SCRIPT_PROTOCOL = "javascript:";


    protected SecurityPolicyChecker securityPolicyChecker;

    private boolean isInjected;
    private EasyBridgeWebView easyBridgeWebView;

    public EasyBridgeWebChromeClient(EasyBridgeWebView webView) {
        this.easyBridgeWebView = webView;
        this.securityPolicyChecker = webView.getPolicyChecker();
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        // inject the bridge code when the progress above 25% to make sure it worked
        if (newProgress <= 25) {
            isInjected = false;
        } else if (!isInjected) {
            if (securityPolicyChecker == null || securityPolicyChecker.check(view.getUrl(), "")) {
                isInjected = true;
                String bridgeScript = Utils.readAssetFile(view.getContext(), BRIDGE_SCRIPT_PATH);
                String executeScript = "var bridgeName = " + "\"" + easyBridgeWebView.getBridgeName() + "\"" + ";" + bridgeScript;
                view.loadUrl(String.format("%s%s", JAVA_SCRIPT_PROTOCOL, executeScript));
            }
        }
    }
}
