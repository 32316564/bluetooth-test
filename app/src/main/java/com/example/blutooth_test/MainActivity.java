package com.example.blutooth_test;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.blutooth_test.web.WebAppInterface;
import com.example.blutooth_test.web.WebViewManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private WebViewManager webViewManager;
    private WebAppInterface webAppInterface;
    private WebView webView;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainHandler = new Handler(Looper.getMainLooper());

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

        // 显示进度条
        //showProgressBar();
        fadeInLottieAnimation();

        // 延迟初始化WebView（让出UI线程时间片）
        mainHandler.postDelayed(this::initWebView, 300);
    }

    private void initWebView() {
        try {
            long startTime = System.currentTimeMillis();

            // 初始化WebView
            webViewManager = new WebViewManager();
            webView = findViewById(R.id.mainView);
            webViewManager.setupWebView(webView);
            webAppInterface = new WebAppInterface(this, webViewManager);
            webView.addJavascriptInterface(webAppInterface, "Android");

            Log.d(TAG, "WebView初始化耗时: " + (System.currentTimeMillis() - startTime) + "ms");

            // 延迟加载HTML（进一步让出时间片）
            mainHandler.postDelayed(() -> {
                if (isFinishing() || isDestroyed()) return;

                long loadStartTime = System.currentTimeMillis();
                webView.loadUrl("file:///android_asset/index.html");
                Log.d(TAG, "WebView加载URL耗时: " + (System.currentTimeMillis() - loadStartTime) + "ms");

                // 隐藏进度条
                //hideProgressBar();
                fadeOutLottieAnimation();
            }, 200);

        } catch (Exception e) {
            Log.e(TAG, "WebView初始化失败", e);
            //hideProgressBar();
            fadeOutLottieAnimation();
        }
    }

    //private void showProgressBar() {
    //    findViewById(R.id.progressBar).setVisibility(android.view.View.VISIBLE);
    //}
    //
    //private void hideProgressBar() {
    //    findViewById(R.id.progressBar).setVisibility(android.view.View.GONE);
    //}

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保在WebView销毁前取消所有待处理的消息
        mainHandler.removeCallbacksAndMessages(null);

        if (webViewManager != null) {
            webViewManager.cleanup();
        }
        if (webAppInterface != null) {
            webAppInterface.releaseResources();
        }
    }
}