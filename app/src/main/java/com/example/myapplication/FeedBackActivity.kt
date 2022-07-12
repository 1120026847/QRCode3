package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class FeedBackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_back)
        val mWebView= WebView(this)
        val webview:WebView=findViewById(R.id.webview)
       webview.getSettings().setJavaScriptEnabled(true);
       webview.getSettings().setDomStorageEnabled(true);
        val url="https://support.qq.com/product/417639"
        val webViewClient: WebViewClient =object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
     webview.setWebViewClient(webViewClient);

     webview.loadUrl(url);
    }
}