package com.lwtdzh.yangshipinwrapper4tv;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import androidx.multidex.MultiDex;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class YSPApplication extends Application {
    private static final String TAG = "YSPApp";
    private static YSPApplication sInstance;
    private static ExecutorService sGlobalThreadPool;

    public static YSPApplication getInstance() { return sInstance; }
    public static ExecutorService getGlobalThreadPool() { return sGlobalThreadPool; }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Log.i(TAG, "onCreate sdk=" + Build.VERSION.SDK_INT);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e(TAG, "uncaught_exception thread=" + t.getName(), e);
                writeCrashLog(e);
                android.os.Process.killProcess(Process.myPid());
                System.exit(1);
            }
        });

        sGlobalThreadPool = new ThreadPoolExecutor(
                2,
                4,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(128),
                new ThreadFactory() {
                    private final AtomicInteger idx = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "YSP-Worker-" + idx.getAndIncrement());
                        t.setPriority(Thread.NORM_PRIORITY - 1);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private void writeCrashLog(Throwable e) {
        try {
            File dir = new File(getExternalFilesDir(null), "crash");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "crash_" + System.currentTimeMillis() + ".txt");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            pw.close();
            FileWriter fw = new FileWriter(file);
            fw.write(sw.toString());
            fw.close();
        } catch (Throwable ignored) {}
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (Build.VERSION.SDK_INT <= 20) {
            MultiDex.install(this);
            Log.i(TAG, "multidex_installed sdk=" + Build.VERSION.SDK_INT);
        }
    }
}