package easily.tech.easybridge.lib;

/**
 * a security policy checker
 * you can define the security policy of each the handler according to the current page which invoke the native function
 * <p>
 * Created by lemon on 30/03/2018.
 */
public interface SecurityPolicyChecker {
    /**
     * security policy checker
     *
     * @param url        the page which invoke the native function
     * @param parameters the values passed from js
     * @return return true if the check passed
     */
    boolean check(String url, String parameters);
}
