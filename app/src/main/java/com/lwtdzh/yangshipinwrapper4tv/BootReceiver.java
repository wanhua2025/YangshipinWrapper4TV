package com.lwtdzh.yangshipinwrapper4tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "YSPTV";
    private static final String PREFS = "yangshipin_tv";
    private static final String PREF_AUTO_START = "auto_start_on_boot";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();

        boolean isBootAction = Intent.ACTION_BOOT_COMPLETED.equals(action)
                || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)
                || "android.intent.action.QUICKBOOT_POWERON".equals(action)
                || "com.htc.intent.action.QUICKBOOT_POWERON".equals(action);

        if (!isBootAction) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean autoStart = prefs.getBoolean(PREF_AUTO_START, false);
        Log.i(TAG, "boot_receiver action=" + action + " auto_start=" + autoStart);

        if (!autoStart) return;

        launchAppWithDelay(context, 3000L);
    }

    private void launchAppWithDelay(final Context context, long delayMs) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent launch = new Intent(context, MainActivity.class);
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(launch);
                    Log.i(TAG, "boot_receiver startActivity OK");
                } catch (Throwable t) {
                    Log.e(TAG, "boot_receiver launch failed", t);
                }
            }
        }, delayMs);
    }
}