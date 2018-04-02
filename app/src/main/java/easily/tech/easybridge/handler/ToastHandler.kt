package easily.tech.easybridge.handler

import android.content.Context
import android.util.Log
import android.widget.Toast
import easily.tech.easybridge.lib.ResultCallBack
import easily.tech.easybridge.lib.handler.BaseBridgeHandler

/**
 * Created by lemon on 30/03/2018.
 */
class ToastHandler(private val context: Context) : BaseBridgeHandler("toast") {
    override fun onCall(parameters: String?, callBack: ResultCallBack?) {
        Log.d("Toast",parameters)
        Toast.makeText(context, parameters, Toast.LENGTH_SHORT).show()
        callBack?.onResult(null)
    }
}