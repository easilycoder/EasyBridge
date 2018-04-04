package tech.easily.easybridge.lib;

/**
 * Created by lemon on 29/03/2018.
 */
final class CallBackMessage {

    static final int CODE_SUCCESS = 0;
    static final int CODE_NO_HANDLER = -1;
    static final int CODE_INVALID_HANDLER = -2;
    static final int CODE_SECURITY_FORBIDDEN = -3;

    private int code;
    private String description;
    private Object result;

    private CallBackMessage(int code, String description, Object result) {
        this.code = code;
        this.description = description;
        this.result = result;
    }


    static CallBackMessage generateErrorMessage(int code, String description) {
        return new CallBackMessage(code, description, null);
    }


    static CallBackMessage generateSuccessMessage(Object result) {
        return new CallBackMessage(CODE_SUCCESS, "success", result);
    }
}
