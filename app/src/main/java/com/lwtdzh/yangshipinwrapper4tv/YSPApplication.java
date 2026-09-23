package com.lwtdzh.yangshipinwrapper4tv;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.multidex.MultiDex;

public class YSPApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("YSPApp", "onCreate sdk=" + Build.VERSION.SDK_INT);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (Build.VERSION.SDK_INT <= 20) {
            MultiDex.install(this);
            Log.i("YSPApp", "multidex_installed sdk=" + Build.VERSION.SDK_INT);
        }
    }
}