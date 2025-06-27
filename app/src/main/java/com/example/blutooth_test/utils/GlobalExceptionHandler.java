package com.example.blutooth_test.utils;


import android.content.Context;
import android.util.Log;


/**
 * 全局异常捕获
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final String LOG_TAG = "GlobalExceptionHandler";

    private Thread.UncaughtExceptionHandler defaultUEH;
    private Context context;

    public GlobalExceptionHandler(Context context) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.i("uncaughtException", "uncaughtException: " + throwable);
        // 自定义的异常处理逻辑
        handleException(throwable);

        // 如果存在默认的异常处理器，继续调用它
        if (defaultUEH != null) {
            Log.i("uncaughtException", "uncaughtException: 捕获到了异常" + defaultUEH);
            defaultUEH.uncaughtException(thread, throwable);
        } else {
            Log.i("uncaughtException", "uncaughtException: 结束应用");
            // 杀死进程
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(2); // 使用2作为状态码表示异常终止
        }
    }

    private void handleException(Throwable throwable) {
        // 例如：记录日志，显示提示，重启应用等
        // 这里可以调用MyApplication中的异常捕获逻辑
        String exception = Log.getStackTraceString(throwable);
            Log.i(LOG_TAG, "uploadExceptionToServer异常信息: " + exception);
            if (exception == null) {
                Log.i(LOG_TAG, "出现异常但是异常信息为空");
                return;
            }
    }

}
