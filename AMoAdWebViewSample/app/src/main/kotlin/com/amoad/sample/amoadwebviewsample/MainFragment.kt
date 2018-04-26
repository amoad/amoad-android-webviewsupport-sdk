package com.amoad.sample.amoadwebviewsample

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.amoad.webviewsupport.AMoAdWebViewSupport

class MainFragment : Fragment() {

    private val url = "file:///android_asset/ad.html"

    private lateinit var webView: WebView
    private lateinit var support: AMoAdWebViewSupport

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_main, container, false)

        this.webView = v.findViewById(R.id.webview)

        // WebView のタッチイベントを ScrollView に奪われないための設定
        this.webView.setOnTouchListener { _, _ ->
            this.webView.requestDisallowInterceptTouchEvent(true)
            false
        }

        if (savedInstanceState == null) {
            this.webView.loadUrl(this.url)
        }
        else {
            this.webView.restoreState(savedInstanceState)
        }

        this.support = AMoAdWebViewSupport(webView)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.support.activity = activity
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        this.webView.saveState(outState)
    }
}
