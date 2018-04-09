package tech.easily.easybridge.lib;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.Date;

/**
 * Created by lemon on 2018/4/2.
 */
public class EasyBridgeWebChromeClient extends WebChromeClient {

    private static final String BRIDGE_SCRIPT_PATH = "easybridge.js";
    public static final String SECURITY_CHECK_PARAMETERS = "inject bridge script";
    private boolean hasCalled;
    private EasyBridgeWebView easyBridgeWebView;
    private String injectScript;
    private InjectBridgeTask injectBridgeTask;


    public EasyBridgeWebChromeClient(EasyBridgeWebView webView) {
        this.easyBridgeWebView = webView;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (injectBridgeTask == null) {
            injectBridgeTask = new InjectBridgeTask(this);
        }
        // inject the bridge code when the progress above 25% to make sure it worked
        if (newProgress <= 25) {
            hasCalled = false;
            return;
        }
        if (!hasCalled) {
            hasCalled = true;
            // reset the injected status label before injecting a new bridge to the page
            if (easyBridgeWebView != null) {
                easyBridgeWebView.setInjected(false);
            }
            // TODO: 2018/4/3 it seems that the url received here possibly not the same as the real page url we look forward to
            if (easyBridgeWebView.checkSecurityGlobally(view.getUrl(), SECURITY_CHECK_PARAMETERS)) {
                injectBridgeTask.start();
            } else {
                deleteBridge();
            }
        }
    }

    private void injectBridge() {
        if (easyBridgeWebView == null) {
            return;
        }
        if (TextUtils.isEmpty(injectScript)) {
            injectScript = Utils.readAssetFile(easyBridgeWebView.getContext(), BRIDGE_SCRIPT_PATH);
            injectScript = "var bridgeName = " + "\"" + easyBridgeWebView.getBridgeName() + "\"" + ";" + injectScript;
        }
        easyBridgeWebView.evaluateJavascript(injectScript);
    }


    private boolean isInjected() {
        return easyBridgeWebView != null && easyBridgeWebView.isInjected();
    }

    /**
     * as EasyBridge will inject an object named '_easybridge' into JavaScript context using addJavaScriptInterface()
     * we need to removed it when security forbidden
     */
    private void deleteBridge() {
        if (easyBridgeWebView == null) {
            return;
        }
        String script = "delete " + EasyBridgeWebView.MAPPING_JS_INTERFACE_NAME + ";";
        easyBridgeWebView.evaluateJavascript(script);
    }

    /**
     * a task using to inject bridge ,
     * it will try to re-inject the bridge every 300ms(at most 5 times) if the status of bridgeInjected is false
     */
    private static class InjectBridgeTask implements Runnable {

        private static final int RETRY_COUNT = 5;
        private static final long RETRY_DELAY_INTERVAL = 300;
        private int retryCount;
        private EasyBridgeWebChromeClient webChromeClient;
        private Handler mainHandler;

        InjectBridgeTask(EasyBridgeWebChromeClient webChromeClient) {
            this.webChromeClient = webChromeClient;
            this.mainHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void run() {
            if (!webChromeClient.isInjected() && retryCount <= RETRY_COUNT) {
                webChromeClient.injectBridge();
                retryCount++;
                mainHandler.postDelayed(this, RETRY_DELAY_INTERVAL);
            } else {
                reset();
            }
        }

        private void reset() {
            mainHandler.removeCallbacks(this);
            retryCount = 0;
        }

        public void start() {
            mainHandler.post(this);
        }
    }
}
