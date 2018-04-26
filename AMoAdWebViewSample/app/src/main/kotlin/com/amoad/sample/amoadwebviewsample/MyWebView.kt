package com.amoad.sample.amoadwebviewsample

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.ads.identifier.AdvertisingIdClient

class MyWebView : WebView {

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : super(context, attrs, defStyleAttr, defStyleRes)

    private var advertisingId: String = ""
    private var isOptOut: Int = 1

    private fun initAdvertisingId(context: Context) {
        Thread(Runnable {
            AdvertisingIdClient.getAdvertisingIdInfo(context)?.let {
                this.advertisingId = it.id
                this.isOptOut = if (it.isLimitAdTrackingEnabled) 1 else 0
            }
        }).start()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configure(webView: WebView) {
        // 動画自動再生の設定
        webView.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.settings.mediaPlaybackRequiresUserGesture = false
        }

        // WebView を PC Chrome でデバッグできるようにする
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // viewport 指定を有効にする
        webView.settings.useWideViewPort = true

        // WebView 内の target='_blank' をフックする
        webView.settings.setSupportMultipleWindows(true)

        webView.webViewClient = this.webViewClient
        webView.webChromeClient = this.webChromeClient
    }

    private val webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            url?.let {
                if (it.startsWith("amoadscheme://")) {
                    handleAMoAdScheme(view)
                }
            }
            return false
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            request?.let {
                return shouldOverrideUrlLoading(view, it.url.toString())
            }
            return false
        }
    }

    private fun handleAMoAdScheme(view: WebView?) {
        val script = """javascript:
            (function() {
                var message = { amoadOption: { idfa: '${this.advertisingId}', optout: '${this.isOptOut}' }};
                var target = '*';
                window.postMessage(message, target);
                for (var i = 0; i < window.frames.length; i++) {
                    window.frames[i].postMessage(message, target);
                }
            })();
        """
        view?.loadUrl(script)
    }

    private val webChromeClient = object : WebChromeClient() {
        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            val transport: WebView.WebViewTransport = resultMsg!!.obj as WebView.WebViewTransport
            transport.webView = createBlankTargetWebView(view!!.context)
            resultMsg.sendToTarget()
            return true
        }
    }

    /**
     * @return URL を外部ブラウザに飛ばすためのテンポラリな WebView
     */
    private fun createBlankTargetWebView(context: Context): WebView {
        return WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    openUrl(view!!.context, url!!)
                    view.destroy()
                    return true
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    openUrl(view!!.context, request!!.url.toString())
                    view.destroy()
                    return true
                }
            }
        }
    }

    private fun openUrl(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        })
    }

    init {
        initAdvertisingId(context)
        configure(this)
    }
}
