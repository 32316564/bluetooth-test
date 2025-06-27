package com.example.blutooth_test.web;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewManager {
    //private final Context context;
    private WebView webView;

    //public WebViewManager(Context context) {
    //    this.context = context;
    //}

    public void setupWebView(WebView webView) {
        this.webView = webView;
        if (webView == null) {
            return;
        }

        WebSettings webSettings = webView.getSettings();
        setupWebSettings(webSettings);
        setupWebViewClient();
        setupWebChromeClient();
    }

    private void setupWebSettings(WebSettings settings) {
        // 启用JavaScript
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled(true);

        // 启用调试模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // 其他设置...
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        //settings.setAppCacheEnabled(true);
    }

    private void setupWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面加载完成后可执行JS测试
                view.evaluateJavascript("javascript:console.log('页面加载完成')", null);
            }
        });
    }

    private void setupWebChromeClient() {
        webView.setWebChromeClient(new WebChromeClient());
    }

    public void evaluateJavascript(String script) {
        Log.i("evaluateJavascript", "evaluateJavascript: " + script);
        if (webView == null) {
            return;
        }

        webView.post(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(script, result -> {
                    // 可在此处处理JS执行结果
                    Log.d("123", "JS执行结果: " + result);
                });
            } else {
                Log.d("123", "JS执行结果: result" );
                webView.loadUrl("javascript:" + script);
            }
        });
    }

    public void cleanup() {
        if (webView != null) {
            // 清除缓存和数据
            webView.clearCache(true);
            webView.clearHistory();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(null);
            } else {
                CookieManager.getInstance().removeAllCookie();
            }

            // 销毁WebView
            webView.destroy();
            webView = null;
        }
    }
}