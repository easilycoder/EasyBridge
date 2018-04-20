package tech.easily.easybridge.lib;

import android.util.Log;

/**
 * Created by hzyangjiehao on 2018/4/20.
 */
final class Logger {

    static boolean debuggable;
    private static final String TAG = "EasyBridge";

    public static void setDebuggable(boolean debuggable) {
        Logger.debuggable = debuggable;
    }

    static void debug(String message) {
        if (!debuggable) {
            return;
        }
        Log.d(TAG, message);
    }

    static void error(String message) {
        if (!debuggable) {
            return;
        }
        Log.e(TAG, message);
    }
}
