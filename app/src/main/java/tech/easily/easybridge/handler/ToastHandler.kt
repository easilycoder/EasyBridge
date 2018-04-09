package tech.easily.easybridge.handler

import android.widget.Toast
import tech.easily.easybridge.annotation.EasyBridgeHandler
import tech.easily.easybridge.lib.EasyBridgeWebView
import tech.easily.easybridge.lib.ResultCallBack
import tech.easily.easybridge.lib.handler.BaseBridgeHandler

/**
 * Created by lemon on 30/03/2018.
 */
@EasyBridgeHandler(name = "toast")
class ToastHandler(handlerName:String,webView: EasyBridgeWebView) : BaseBridgeHandler(handlerName,webView) {
    override fun onCall(parameters: String?, callBack: ResultCallBack?) {
        Toast.makeText(webView.context, parameters, Toast.LENGTH_SHORT).show()
        callBack?.onResult("data send to js")
    }
}