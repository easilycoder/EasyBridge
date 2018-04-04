package tech.easily.easybridge.lib.handler;

import tech.easily.easybridge.lib.ResultCallBack;
import tech.easily.easybridge.lib.SecurityPolicyChecker;

/**
 * Created by lemon on 29/03/2018.
 */
public interface BridgeHandler {

    String getHandlerName();

    /**
     * async call from js
     * <p>
     *
     * @param parameters value passed from js
     * @param callBack   when code execute finish ,callback
     */
    void onCall(String parameters, ResultCallBack callBack);

    SecurityPolicyChecker securityPolicyChecker();
}
