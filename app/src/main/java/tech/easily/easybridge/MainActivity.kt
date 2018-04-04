package tech.easily.easybridge

import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import tech.easily.easybridge.lib.EBHandlerManager
import tech.easily.easybridge.lib.EasyBridgeWebChromeClient
import tech.easily.easybridge.lib.ResultCallBack
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContentView(R.layout.activity_main)
        init()
        webView.loadUrl("file:///android_asset/demo.html")

    }

    private fun init() {
        EBHandlerManager.register(webView)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }
        }
        // set a global security checker,all the url with scheme https/http is not allowed to using the bridge
        webView.setPolicyChecker(({ url, parameters ->
            val page = Uri.parse(url)
            when (parameters) {
            // control the bridge inject action
                EasyBridgeWebChromeClient.SECURITY_CHECK_PARAMETERS -> when (page.scheme) {
                    "http" -> false
                    "https" -> false
                    else -> true
                }
                else -> true
            }
        }))
        // call JavaScript From Java
        webView.postDelayed({
            webView.callHandler("resultBack", "this is the value pass from Java", object : ResultCallBack() {
                override fun onResult(result: Any?) {
                    Toast.makeText(this@MainActivity, result?.toString(), Toast.LENGTH_SHORT).show()
                }

            })
        }, 5000)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
