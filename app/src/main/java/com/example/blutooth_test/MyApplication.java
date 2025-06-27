package com.example.blutooth_test;


import android.app.Application;
import com.example.blutooth_test.utils.GlobalExceptionHandler;

public class MyApplication extends Application {
    public static final String TAG = "MyApplication";


    @Override
    public void onCreate() {
        super.onCreate();
        // 设置自定义的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }
}