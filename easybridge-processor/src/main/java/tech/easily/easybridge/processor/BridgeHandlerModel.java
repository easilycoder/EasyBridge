package tech.easily.easybridge.processor;

/**
 * Created by lemon on 04/04/2018.
 */

final class BridgeHandlerModel {
    String handlerName;
    // the class name of the handler,using it to construct an instance
    String className;

    BridgeHandlerModel(String handlerName, String className) {
        this.handlerName = handlerName;
        this.className = className;
    }
}
