package tech.easily.easybridge.lib;

/**
 * Created by lemon on 2018/4/8.
 */
class EasyBridgeInvokeException extends IllegalStateException {

    int errorCode;

    public EasyBridgeInvokeException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
