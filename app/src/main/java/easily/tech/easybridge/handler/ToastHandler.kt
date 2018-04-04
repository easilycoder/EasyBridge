package easily.tech.easybridge.handler

import android.widget.Toast
import easily.tech.easybridge.annotation.EasyBridgeHandler
import easily.tech.easybridge.lib.EasyBridgeWebView
import easily.tech.easybridge.lib.ResultCallBack
import easily.tech.easybridge.lib.handler.BaseBridgeHandler

/**
 * Created by lemon on 30/03/2018.
 */
@EasyBridgeHandler(name = "toast")
class ToastHandler(handlerName:String,webView: EasyBridgeWebView) : BaseBridgeHandler(handlerName,webView) {
    override fun onCall(parameters: String?, callBack: ResultCallBack?) {
        Toast.makeText(webView.context, parameters, Toast.LENGTH_SHORT).show()
        callBack?.onResult(null)
    }
}