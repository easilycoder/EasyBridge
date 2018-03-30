package easily.tech.easybridge.lib.handler;

import android.support.annotation.NonNull;

import easily.tech.easybridge.lib.SecurityPolicyChecker;

/**
 * Created by lemon on 30/03/2018.
 */
public abstract class BaseBridgeHandler implements BridgeHandler {

    protected String name;
    protected SecurityPolicyChecker checker;

    public BaseBridgeHandler(String name) {
        this(name, null);
    }

    public BaseBridgeHandler(String name, SecurityPolicyChecker securityPolicyChecker) {
        this.name = name;
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
}
