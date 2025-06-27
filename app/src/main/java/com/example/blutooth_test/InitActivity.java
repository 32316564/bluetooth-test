package com.example.blutooth_test;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.blutooth_test.utils.ActManager;
import com.example.blutooth_test.utils.PermissionUtils;
import com.example.blutooth_test.web.WebAppInterface;
import com.example.blutooth_test.web.WebViewManager;

public class InitActivity extends AppCompatActivity {
    private static final String TAG = "InitActivity";
    private WebAppInterface webAppInterface;
    private WebViewManager webViewManager;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActManager.getAppManager().addActivity(this);
        setContentView(R.layout.activity_init);

        // 初始化视图
        initViews();

        // 初始化WebView
        initWebView();

        LottieAnimationView animationView = findViewById(R.id.animation_view);

        // 监听动画状态
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.i(TAG, "onAnimationStart: 动画开始");
                // 动画开始
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.i(TAG, "onAnimationEnd: 动画结束");
                // 动画结束
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.i(TAG, "onAnimationCancel: 动画取消");
                // 动画取消
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.i(TAG, "onAnimationRepeat: 动画重复");
                // 动画重复
            }
        });

        // 加载页面
        webView.loadUrl("file:///android_asset/init.html");

        // 检查权限
        checkPermissions();
    }

    private void initViews() {
        webView = findViewById(R.id.initView);

    }

    private void initWebView() {
        webViewManager = new WebViewManager();
        webViewManager.setupWebView(webView);
        webAppInterface = new WebAppInterface(this, webViewManager);
        webView.addJavascriptInterface(webAppInterface, "InitObj");

        // 设置WebView客户端
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());
    }

    private void checkPermissions() {
        if (!PermissionUtils.checkBluetoothPermissions(this)) {
            PermissionUtils.requestBluetoothPermissions(this);
        }
    }

    // 淡入显示Lottie动画
    private void fadeInLottieAnimation() {
        LottieAnimationView animationView = findViewById(R.id.animation_view);
        animationView.setAlpha(0f);
        animationView.setVisibility(View.VISIBLE);
        animationView.animate()
                .alpha(1f)
                .setDuration(300)
                .withStartAction(() -> animationView.playAnimation())
                .start();
    }

    // 淡出隐藏Lottie动画
    private void fadeOutLottieAnimation() {
        LottieAnimationView animationView = findViewById(R.id.animation_view);
        animationView.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    animationView.cancelAnimation();
                    animationView.setVisibility(View.GONE);
                })
                .start();
    }

    // 显示Lottie动画
    private void showLottieAnimation() {
        LottieAnimationView animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation(); // 如果需要自动播放
    }

    // 隐藏Lottie动画
    private void hideLottieAnimation() {
        LottieAnimationView animationView = findViewById(R.id.animation_view);
        animationView.cancelAnimation(); // 停止动画
        animationView.setVisibility(View.GONE);
    }

    /**
     * 自定义WebViewClient处理页面加载事件
     */
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            showLottieAnimation();
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            fadeOutLottieAnimation();
            super.onPageFinished(view, url);
            Log.d(TAG, "页面加载完成: " + url);
        }
    }

    /**
     * 自定义WebChromeClient处理进度变化
     */
    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            super.onProgressChanged(view, newProgress);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        // 清理资源
        if (webView != null) {
            webView.removeJavascriptInterface("InitObj");
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.destroy();
            webView = null;
        }
        ActManager.getAppManager().finishActivity(this);
        super.onDestroy();
    }
}