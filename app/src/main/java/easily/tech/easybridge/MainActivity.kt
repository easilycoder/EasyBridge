package easily.tech.easybridge

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import easily.tech.easybridge.handler.ToastHandler
import easily.tech.easybridge.lib.EasyBridgeWebChromeClient
import easily.tech.easybridge.lib.ResultCallBack
import easily.tech.easybridge.lib.SecurityPolicyChecker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContentView(R.layout.activity_main)
        webView.registerHandler(ToastHandler(this))
        webView.loadUrl("file:///android_asset/demo.html")
        // call JavaScript From Java
        webView.postDelayed({
            webView.callHandler("resultBack", "this is the value pass from Java", object : ResultCallBack() {
                override fun onResult(result: Any?) {
                    Toast.makeText(this@MainActivity, result?.toString(), Toast.LENGTH_SHORT).show()
                }

            })
        }, 5000)
    }
}
