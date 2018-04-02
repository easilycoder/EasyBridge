package easily.tech.easybridge.lib;

/**
 * Created by lemon on 29/03/2018.
 */
public abstract class ResultCallBack {
    /**
     * each async call from js had a uniqueId
     */
    private String callbackId;

    public ResultCallBack() {
    }

    ResultCallBack(String callbackId) {
        this.callbackId = callbackId;
    }

    String getCallbackId() {
        return callbackId;
    }

    public abstract void onResult(Object result);
}
