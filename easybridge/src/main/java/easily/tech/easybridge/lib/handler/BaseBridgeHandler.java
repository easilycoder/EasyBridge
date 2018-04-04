package easily.tech.easybridge.lib.handler;

import android.support.annotation.NonNull;

import easily.tech.easybridge.lib.EasyBridgeWebView;
import easily.tech.easybridge.lib.SecurityPolicyChecker;

/**
 * Created by lemon on 30/03/2018.
 */
public abstract class BaseBridgeHandler implements BridgeHandler {

    protected String name;
    protected SecurityPolicyChecker checker;
    protected EasyBridgeWebView webView;

    public BaseBridgeHandler(String name, EasyBridgeWebView webView) {
        this(name, webView, null);
    }

    public BaseBridgeHandler(String name, EasyBridgeWebView webView, SecurityPolicyChecker securityPolicyChecker) {
        this.name = name;
        this.webView = webView;
        this.checker = securityPolicyChecker;
    }

    @Override
    public String getHandlerName() {
        return name;
    }

    @NonNull
    @Override
    public SecurityPolicyChecker securityPolicyChecker() {
        return checker;
    }

    public void setSecurityPolicyChecker(SecurityPolicyChecker securityPolicyChecker) {
        this.checker = securityPolicyChecker;
    }

    public void destroy() {
        webView = null;
    }
}
