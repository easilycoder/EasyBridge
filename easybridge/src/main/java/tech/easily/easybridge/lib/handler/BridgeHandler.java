package tech.easily.easybridge.lib.handler;

import tech.easily.easybridge.lib.ResultCallBack;
import tech.easily.easybridge.lib.SecurityPolicyChecker;

/**
 * Created by lemon on 29/03/2018.
 */
public interface BridgeHandler {

    String getHandlerName();

    /**
     * async call from JavaScript
     * <p>
     *
     * @param parameters value passed from JavaScript
     * @param callBack   when code execute finish ,callback
     */
    void onCall(String parameters, ResultCallBack callBack);

    /**
     * sync call from JavaScript
     * keep in mind that this method is not invoked in main thread
     *
     * @param parameters value passed from JavaScript
     * @return
     */
    Object onCall(String parameters);

    SecurityPolicyChecker securityPolicyChecker();
}
