package easily.tech.easybridge.lib;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by lemon on 29/03/2018.
 */
public class EasyBridgeWebViewClient extends WebViewClient {

    private static final String BRIDGE_SCRIPT_PATH = "easybridge.js";
    private static final String JAVA_SCRIPT_PROTOCOL = "javascript:";

    private String bridgeName;

    protected SecurityPolicyChecker securityPolicyChecker;

    public EasyBridgeWebViewClient(String bridgeName) {
        this.bridgeName = bridgeName;
    }

    public EasyBridgeWebViewClient(String bridgeName, SecurityPolicyChecker securityPolicyChecker) {
        this.bridgeName = bridgeName;
        this.securityPolicyChecker = securityPolicyChecker;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (securityPolicyChecker != null && securityPolicyChecker.check(url, "")) {
            String bridgeScript = Utils.readAssetFile(view.getContext(), BRIDGE_SCRIPT_PATH);
            String executeScript = "var bridgeName = " + "\"" + bridgeName + "\"" + ";" + bridgeScript;
            view.loadUrl(String.format("%s%s", JAVA_SCRIPT_PROTOCOL, executeScript));
        }
    }

    public void setSecurityPolicyChecker(SecurityPolicyChecker securityPolicyChecker) {
        this.securityPolicyChecker = securityPolicyChecker;
    }
}
