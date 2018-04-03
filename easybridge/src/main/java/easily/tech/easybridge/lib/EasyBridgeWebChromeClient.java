package easily.tech.easybridge.lib;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by hzyangjiehao on 2018/4/2.
 */
public class EasyBridgeWebChromeClient extends WebChromeClient {

    private static final String BRIDGE_SCRIPT_PATH = "easybridge.js";
    private static final String JAVA_SCRIPT_PROTOCOL = "javascript:";
    public static final String SECURITY_CHECK_PARAMETERS = "inject bridge script";
    private boolean isInjected;
    private EasyBridgeWebView easyBridgeWebView;

    public EasyBridgeWebChromeClient(EasyBridgeWebView webView) {
        this.easyBridgeWebView = webView;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        // inject the bridge code when the progress above 25% to make sure it worked
        if (newProgress <= 25) {
            isInjected = false;
            return;
        }
        if (!isInjected) {
            // TODO: 2018/4/3 it seems that the url received here possibly not the same as the real page url we look forward to
            if (easyBridgeWebView.checkSecurityGlobally(view.getUrl(), SECURITY_CHECK_PARAMETERS)) {
                isInjected = true;
                injectBridge(view);
            } else {
                deleteBridge(view);
            }
        }
    }

    private void injectBridge(WebView view) {
        String bridgeScript = Utils.readAssetFile(view.getContext(), BRIDGE_SCRIPT_PATH);
        String executeScript = "var bridgeName = " + "\"" + easyBridgeWebView.getBridgeName() + "\"" + ";" + bridgeScript;
        view.loadUrl(String.format("%s%s", JAVA_SCRIPT_PROTOCOL, executeScript));
    }

    private void deleteBridge(WebView view) {
        // execute script below to remove the bridge had set before :
        // delete _easybridge;
        String script = "delete " + EasyBridgeWebView.MAPPING_JS_INTERFACE_NAME + ";";
        view.loadUrl(String.format("%s%s", JAVA_SCRIPT_PROTOCOL, script));
    }
}
