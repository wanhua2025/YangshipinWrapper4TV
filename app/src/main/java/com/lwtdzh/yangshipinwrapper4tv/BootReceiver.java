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
    private static final String PREF_AUTO_START = "auto_start";

    private static final String ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON";
    private static final String ACTION_QUICKBOOT_COMPLETED = "android.intent.action.QUICKBOOT_COMPLETED";
    private static final String ACTION_HTC_QUICKBOOT = "com.htc.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "BootReceiver received: " + action);

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean autoStart = prefs.getBoolean(PREF_AUTO_START, true);
        if (!autoStart) {
            Log.i(TAG, "auto_start disabled, skip boot launch");
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)
                || ACTION_QUICKBOOT_POWERON.equals(action)
                || ACTION_QUICKBOOT_COMPLETED.equals(action)
                || ACTION_HTC_QUICKBOOT.equals(action)) {

            final PendingResult pending = goAsync();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent launch = new Intent(context, MainActivity.class);
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(launch);
                        Log.i(TAG, "BootReceiver launched MainActivity");
                    } catch (Exception e) {
                        Log.e(TAG, "BootReceiver launch failed: " + e.getMessage());
                    } finally {
                        if (pending != null) pending.finish();
                    }
                }
            }, 2500);
        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            final PendingResult pending = goAsync();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent launch = new Intent(context, MainActivity.class);
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(launch);
                        Log.i(TAG, "BootReceiver launched after update");
                    } catch (Exception e) {
                        Log.e(TAG, "BootReceiver update launch failed: " + e.getMessage());
                    } finally {
                        if (pending != null) pending.finish();
                    }
                }
            }, 1500);
        }
    }
}