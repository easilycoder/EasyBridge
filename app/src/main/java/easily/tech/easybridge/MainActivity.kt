package easily.tech.easybridge

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import easily.tech.easybridge.handler.ToastHandler
import easily.tech.easybridge.lib.EasyBridgeWebChromeClient
import easily.tech.easybridge.lib.SecurityPolicyChecker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContentView(R.layout.activity_main)
        webView.webChromeClient = object : EasyBridgeWebChromeClient(webView) {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                val script = String.format("%s.callHandler('toast','current progress:%s')", webView.bridgeName, newProgress)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(script) {
                    }
                }
            }
        }
        webView.registerHandler(ToastHandler(this))
        webView.loadUrl("file:///android_asset/demo.html")
    }
}
