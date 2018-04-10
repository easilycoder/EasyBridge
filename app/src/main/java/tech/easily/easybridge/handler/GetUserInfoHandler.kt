package tech.easily.easybridge.handler

import tech.easily.easybridge.annotation.EasyBridgeHandler
import tech.easily.easybridge.lib.EasyBridgeWebView
import tech.easily.easybridge.lib.ResultCallBack
import tech.easily.easybridge.lib.handler.BaseBridgeHandler
import tech.easily.easybridge.model.User

/**
 * Created by hzyangjiehao on 2018/4/10.
 */
@EasyBridgeHandler(name = "getUserInfo")
class GetUserInfoHandler(handlerName: String, webView: EasyBridgeWebView) : BaseBridgeHandler(handlerName, webView) {
    override fun onCall(parameters: String?, callBack: ResultCallBack?) {
        val user = User("userName", 13, "Hangzhou")
        callBack?.onResult(user)
    }
}