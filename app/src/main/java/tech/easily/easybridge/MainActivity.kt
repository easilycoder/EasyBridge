package tech.easily.easybridge

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.google.gson.Gson
import tech.easily.easybridge.lib.EBHandlerManager
import tech.easily.easybridge.lib.EasyBridgeWebChromeClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_performance.*
import tech.easily.easybridge.lib.ResultCallBack
import tech.easily.easybridge.model.PerformanceTiming
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val injectPerformanceHandler = "window.easyBridge.registerHandler('getPerformance', function (parameters, callback) {\n" +
            "                    if (typeof callback == 'function') {\n" +
            "                        callback(window.performance.timing);\n" +
            "                    }\n" +
            "                });"

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
        tvCallJS.setOnClickListener {
            webView.callHandler("resultBack", "this is the value pass from Java", object : ResultCallBack() {
                override fun onResult(result: Any?) {
                    Toast.makeText(this@MainActivity, result?.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }
        EBHandlerManager.register(webView)
        webView.setDebuggable(BuildConfig.DEBUG)
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                llPerformance.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.postDelayed({
                    getPerformanceData()
                },500)
            }
        }
        // set a global security checker,all the url with scheme https/http is not allowed to using the bridge
        webView.setPolicyChecker(({ url, parameters ->
            val page = Uri.parse(url)
            when (parameters) {
            // control the bridge inject action
                EasyBridgeWebChromeClient.SECURITY_CHECK_PARAMETERS -> when (page.scheme) {
                    "http" -> false
                    "https" -> true
                    else -> true
                }
                else -> true
            }
        }))
        // inject a performance js interface
        webView.setBridgeInjectedListener {
            webView.loadUrl("javascript:$injectPerformanceHandler")
        }
    }

    private fun getPerformanceData() {
        webView.callHandler("getPerformance", null, object : ResultCallBack() {
            override fun onResult(result: Any?) {
                try {
                    val performanceTimingJson = result as String
                    val performanceTiming = Gson().fromJson<PerformanceTiming>(performanceTimingJson, PerformanceTiming::class.java)
                    renderPerformance(performanceTiming)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun renderPerformance(performanceTiming: PerformanceTiming) {
        llPerformance.visibility = View.VISIBLE
        with(performanceTiming) {
            tvNet.text = (responseEnd - fetchStart).toString() + " ms"
            tvRender.text = (loadEventEnd - responseEnd).toString() + " ms"
            tvTotal.text = (loadEventEnd - fetchStart).toString() + " ms"
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
