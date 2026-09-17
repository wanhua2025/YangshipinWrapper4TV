package com.lwtdzh.yangshipinwrapper4tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "YSPTV";
    private static final String YSP_HOME_URL = "https://www.yangshipin.cn/tv/home";
    private static final String PREFS = "yangshipin_tv";
    private static final String PREF_QUALITY = "quality";
    private static final String PREF_CHANNEL_PID = "channel_pid";
    private static final String PREF_PLAYBACK_MODE = "playback_mode";
    private static final String PREF_CHANNELS_JSON = "channels_json";
    private static final String PREF_AUTO_START = "auto_start_on_boot";
    private static final String PLAYBACK_MODE_DEFAULT = "default";
    private static final String PLAYBACK_MODE_HW = "hw";
    private static final String PLAYBACK_MODE_SW = "sw";
    private static final String[] QUALITY_ORDER = new String[]{"hd", "shd", "fhd"};
    private static final String PLAYBACK_HELP_TEXT = "上下换台 · 左开菜单 · 右切清晰度 · OK确定";
    private static final String[] SETTINGS_ITEMS = new String[]{"解码模式", "开机自启"};
    private static final int MENU_PAGE_CHANNELS = 0;
    private static final int MENU_PAGE_SETTINGS = 1;
    private static final int MENU_SELECTION_SETTINGS = 0;

    private static final long JS_HEARTBEAT_TIMEOUT_MS = 6000;
    private static final long FIRST_FRAME_TIMEOUT_MS = 15000;
    private static final long OVERLAY_HIDE_AFTER_FIRST_FRAME_MS = 2000;
    private static final int MAX_RECOVERY_LEVEL = 4;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Channel> channels = new ArrayList<Channel>();

    private FrameLayout root;
    private WebView bridgeWebView;
    private GestureTraceView gestureTraceView;
    private TextView overlayText;
    private TextView statusText;
    private LinearLayout menuPanel;
    private TextView menuHeader;
    private ListView channelListView;
    private ChannelAdapter channelAdapter;
    private SharedPreferences preferences;

    private String preferredQuality = "fhd";
    private String activeRequestId = "";
    private int requestCounter = 0;
    private int currentIndex = 0;
    private int menuSelection = 1;
    private int settingsSelection = 0;
    private int menuPage = MENU_PAGE_CHANNELS;
    private int bridgeAttempts = 0;
    private boolean channelsLoaded = false;
    private String playbackMode = PLAYBACK_MODE_DEFAULT;
    private boolean autoStartOnBoot = true;
    private boolean lowMemoryDevice = false;
    private int touchSlop;
    private final StringBuilder numberBuffer = new StringBuilder();

    private boolean overlayWaitingFirstFrame = false;
    private boolean firstFrameReceived = false;
    private boolean realFrameReceived = false;
    private int consecutiveBlackScreens = 0;
    private int recoveryLevel = 0;
    private long lastJsHeartbeatMs = 0;

    private final Runnable hideOverlayRunnable = new Runnable() {
        @Override
        public void run() {
            if (overlayWaitingFirstFrame) {
                showPlaybackOverlay();
                return;
            }
            showBridgeWebView();
            overlayText.setVisibility(View.GONE);
        }
    };

    private final Runnable firstFrameTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (overlayWaitingFirstFrame) {
                overlayWaitingFirstFrame = false;
                Log.w(TAG, "first_frame timeout, hiding overlay");
                showBridgeWebView();
                overlayText.setVisibility(View.GONE);
            }
        }
    };

    private final Runnable numberCommitRunnable = new Runnable() {
        @Override
        public void run() { commitNumberInput(); }
    };

    private final Runnable playbackTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!channels.isEmpty() && bridgeWebView != null && !playbackTimedOut) {
                playbackTimedOut = true;
                requestCurrentStream("timeout_retry");
            }
        }
    };

    private final Runnable blackScreenRecoveryRunnable = new Runnable() {
        @Override
        public void run() {
            runBlackScreenRecovery();
        }
    };

    private final Runnable jsHeartbeatMonitorRunnable = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastJsHeartbeatMs;
            if (lastJsHeartbeatMs == 0) {
                handler.postDelayed(jsHeartbeatMonitorRunnable, 2000);
                return;
            }
            if (elapsed > JS_HEARTBEAT_TIMEOUT_MS) {
                Log.e(TAG, "js_heartbeat_timeout elapsed=" + elapsed + "ms - JS may be frozen");
                consecutiveBlackScreens++;
                if (consecutiveBlackScreens >= 2) {
                    showOverlay("JS心跳丢失\n正在修复 (" + consecutiveBlackScreens + ")", true);
                    handler.removeCallbacks(blackScreenRecoveryRunnable);
                    handler.postDelayed(blackScreenRecoveryRunnable, 300);
                }
            }
            handler.postDelayed(jsHeartbeatMonitorRunnable, 2500);
        }
    };

    private boolean playbackTimedOut = false;
    private long appStartTimeMs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appStartTimeMs = System.currentTimeMillis();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        detectLowMemoryDevice();
        preferredQuality = preferences.getString(PREF_QUALITY, "fhd");
        if (qualityIndex(preferredQuality) < 0) preferredQuality = "fhd";
        playbackMode = preferences.getString(PREF_PLAYBACK_MODE, PLAYBACK_MODE_DEFAULT);
        if (!PLAYBACK_MODE_SW.equals(playbackMode) && !PLAYBACK_MODE_HW.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_DEFAULT;
        }
        if (lowMemoryDevice) {
            playbackMode = PLAYBACK_MODE_DEFAULT;
            Log.i(TAG, "low_memory_device=true forcing playback_mode=default");
        }
        autoStartOnBoot = preferences.getBoolean(PREF_AUTO_START, true);
        Log.i(TAG, "prefs auto_start_on_boot=" + autoStartOnBoot);

        buildUi();
        setupProtocolBridge(savedInstanceState);

        showOverlay("全力加载中...", false);
        updateStatus();

        lastJsHeartbeatMs = System.currentTimeMillis();
        handler.postDelayed(jsHeartbeatMonitorRunnable, 2000);
    }

    private void detectLowMemoryDevice() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long totalMem = mi.totalMem;
        int memClass = am.getMemoryClass();
        lowMemoryDevice = totalMem <= 1024L * 1024 * 1024 || memClass <= 128;
        Log.i(TAG, "device_memory total=" + (totalMem / (1024 * 1024)) + "MB memClass=" + memClass + "MB lowMem=" + lowMemoryDevice);
    }

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        overlayText = new TextView(this);
        overlayText.setTextColor(Color.WHITE);
        overlayText.setTextSize(44);
        overlayText.setGravity(Gravity.CENTER);
        overlayText.setBackgroundColor(0x99000000);
        overlayText.setPadding(dp(32), dp(20), dp(32), dp(20));
        overlayText.setLineSpacing(dp(8), 1.0f);
        overlayText.setVisibility(View.GONE);
        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        root.addView(overlayText, overlayParams);

        statusText = new TextView(this);
        statusText.setVisibility(View.GONE);

        buildMenu();
        gestureTraceView = new GestureTraceView(this);
        root.addView(gestureTraceView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        hideSystemUi();
    }

    private void buildMenu() {
        menuPanel = new LinearLayout(this);
        menuPanel.setOrientation(LinearLayout.VERTICAL);
        menuPanel.setBackgroundColor(0xE6101010);
        menuPanel.setPadding(dp(16), dp(18), dp(16), dp(18));
        menuPanel.setVisibility(View.GONE);

        menuHeader = new TextView(this);
        menuHeader.setTextColor(Color.WHITE);
        menuHeader.setTextSize(22);
        menuHeader.setText("频道列表");
        menuHeader.setGravity(Gravity.CENTER_VERTICAL);
        menuPanel.addView(menuHeader, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        channelListView = new ListView(this);
        channelListView.setDivider(new ColorDrawable(0x33FFFFFF));
        channelListView.setDividerHeight(1);
        channelListView.setCacheColorHint(Color.TRANSPARENT);
        channelListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        channelAdapter = new ChannelAdapter(this);
        channelListView.setAdapter(channelAdapter);
        menuPanel.addView(channelListView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        int menuWidth = Math.min(dp(470), (int) (getResources().getDisplayMetrics().widthPixels * 0.86f));
        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
                menuWidth, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT);
        root.addView(menuPanel, menuParams);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setupProtocolBridge(Bundle savedInstanceState) {
        WebView.setWebContentsDebuggingEnabled(false);
        bridgeWebView = new WebView(this);
        bridgeWebView.setFocusable(false);
        bridgeWebView.setBackgroundColor(Color.BLACK);
        bridgeWebView.setVisibility(View.VISIBLE);
        bridgeWebView.setAlpha(1.0f);
        Log.i(TAG, "bridgeWebView created visible=true alpha=1.0");
        applyPlaybackModeToWebView();

        WebSettings settings = bridgeWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadsImagesAutomatically(false);
        settings.setBlockNetworkImage(true);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSaveFormData(false);
        settings.setGeolocationEnabled(false);
        settings.setNeedInitialFocus(false);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36");

        if (lowMemoryDevice) {
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }

        bridgeWebView.setWebChromeClient(new WebChromeClient());
        bridgeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished url=" + url + " progress=" + view.getProgress());
                view.evaluateJavascript(
                    "(function(){console.log('[YSP_PAGE] url='+location.href+' title='+document.title+' readyState='+document.readyState+' vue='+(document.querySelectorAll('[class*=__vue__]').length));})()",
                    null);
                scheduleBridgeInjection(80);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading url=" + url);
                return false;
            }
        });

        bridgeWebView.addJavascriptInterface(new BridgeCallbacks(), "YspAndroid");

        FrameLayout.LayoutParams bridgeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(bridgeWebView, 0, bridgeParams);

        if (savedInstanceState != null) {
            bridgeWebView.restoreState(savedInstanceState);
        } else {
            bridgeWebView.loadUrl(YSP_HOME_URL);
            scheduleBridgeInjection(200);
        }
    }

    private void applyPlaybackModeToWebView() {
        if (bridgeWebView == null) return;
        bridgeWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        bridgeWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        bridgeWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        bridgeWebView.setHorizontalScrollBarEnabled(false);
        bridgeWebView.setVerticalScrollBarEnabled(false);
        try { bridgeWebView.getClass().getMethod("setEnableSmoothTransition", boolean.class).invoke(bridgeWebView, false); } catch (Throwable ignored) {}
        Log.i(TAG, "playback_mode setLayerType(HARDWARE) applied");
    }

    private void scheduleBridgeInjection(long delayMs) {
        handler.postDelayed(new Runnable() {
            @Override public void run() { injectBridge(); }
        }, delayMs);
    }

    private void injectBridge() {
        if (bridgeWebView == null) return;
        bridgeAttempts++;
        try {
            bridgeWebView.evaluateJavascript(loadAsset("ysp_bridge.js"), null);
        } catch (Exception e) {
            showOverlay("协议桥接失败: " + e.getMessage(), false);
        }
        if (!channelsLoaded && bridgeAttempts < 25) {
            scheduleBridgeInjection(300);
        }
    }

    private String loadAsset(String name) throws Exception {
        InputStream input = getAssets().open(name);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), Charset.forName("UTF-8"));
        } finally { input.close(); }
    }

    private void onChannelsLoaded(String json) {
        if (channelsLoaded) return;
        try {
            JSONArray array = new JSONArray(json);
            channels.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                channels.add(new Channel(
                        object.optString("name"),
                        object.optString("pid"),
                        object.optString("streamId"),
                        object.optString("type"),
                        object.optBoolean("is4K", false)));
            }
            if (channels.isEmpty()) {
                Log.w(TAG, "channels_loaded count=0");
                showOverlay("未找到免费节目", false);
                return;
            }
            channelsLoaded = true;
            preferences.edit().putString(PREF_CHANNELS_JSON, json).apply();
            Log.i(TAG, "channels_loaded count=" + channels.size() + " time=" + (System.currentTimeMillis() - appStartTimeMs) + "ms");
            String savedPid = preferences.getString(PREF_CHANNEL_PID, "");
            currentIndex = findChannelIndexByPid(savedPid);
            if (currentIndex < 0) currentIndex = 0;
            menuSelection = currentIndex;
            channelAdapter.notifyDataSetChanged();
            updateMenuHeader();
            requestCurrentStream("initial");
        } catch (Exception e) {
            showOverlay("频道列表解析失败: " + e.getMessage(), false);
        }
    }

    private int findChannelIndexByPid(String pid) {
        if (pid == null || pid.length() == 0) return -1;
        for (int i = 0; i < channels.size(); i++) {
            if (pid.equals(channels.get(i).pid)) return i;
        }
        return -1;
    }

    private long lastStreamRequestMs = 0;
    private static final long STREAM_REQUEST_COOLDOWN_MS = 400;
    private volatile boolean isChannelChanging = false;

    private void requestCurrentStream() { requestCurrentStream("channel"); }

    private void requestCurrentStream(String reason) {
        if (channels.isEmpty() || bridgeWebView == null) return;
        long now = System.currentTimeMillis();
        if (isChannelChanging && (now - lastStreamRequestMs) < STREAM_REQUEST_COOLDOWN_MS) {
            Log.i(TAG, "stream_request skipped (cooldown) reason=" + reason);
            return;
        }
        lastStreamRequestMs = now;
        isChannelChanging = true;
        Channel channel = channels.get(currentIndex);
        preferences.edit().putString(PREF_CHANNEL_PID, channel.pid).apply();
        showPlaybackOverlay();
        updateStatus();
        String requestId = String.valueOf(++requestCounter);
        activeRequestId = requestId;
        playbackTimedOut = false;
        consecutiveBlackScreens = 0;
        recoveryLevel = 0;
        handler.removeCallbacks(playbackTimeoutRunnable);
        long timeoutMs = lowMemoryDevice ? 9000 : 6000;
        handler.postDelayed(playbackTimeoutRunnable, timeoutMs);
        Log.i(TAG, "stream_request id=" + requestId + " idx=" + currentIndex
                + " name=" + channel.name + " pid=" + channel.pid
                + " streamId=" + channel.streamId + " q=" + preferredQuality
                + " reason=" + reason + " bridge_ready=" + channelsLoaded);
        String js = "(function(){"
                + "if(!window.YspTvBridge||typeof window.YspTvBridge.playChannel!=='function'){"
                + "  return 'bridge_not_ready';"
                + "}"
                + "window.YspTvBridge.playChannel("
                + quoteJs(requestId) + ","
                + quoteJs(channel.pid) + ","
                + quoteJs(channel.streamId) + ","
                + quoteJs(preferredQuality) + ","
                + quoteJs(reason) + ");"
                + "return 'ok';"
                + "})();";
        bridgeWebView.evaluateJavascript(js, new android.webkit.ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (value != null && value.contains("bridge_not_ready")) {
                    Log.w(TAG, "bridge not ready, retrying in 500ms...");
                    handler.removeCallbacks(playbackTimeoutRunnable);
                    handler.postDelayed(new Runnable() {
                        @Override public void run() {
                            injectBridge();
                            requestCurrentStream(reason + "_retry_bridge");
                        }
                    }, 500);
                }
            }
        });
    }

    private void onPlaybackResult(String requestId, String json) {
        if (!activeRequestId.equals(requestId)) return;
        handler.removeCallbacks(playbackTimeoutRunnable);
        isChannelChanging = false;
        try {
            JSONObject object = new JSONObject(json);
            if (!object.optBoolean("ok", false)) {
                Log.w(TAG, "web_playback id=" + requestId + " ok=false err=" + object.optString("error"));
                showOverlay("播放失败: " + object.optString("error"), false);
                handler.postDelayed(new Runnable() {
                    @Override public void run() { requestCurrentStream("auto_retry_after_fail"); }
                }, 2000);
                return;
            }
            String actualQuality = object.optString("quality", preferredQuality);
            if (qualityIndex(actualQuality) >= 0) {
                preferredQuality = actualQuality;
                preferences.edit().putString(PREF_QUALITY, preferredQuality).apply();
            }
            consecutiveBlackScreens = 0;
            recoveryLevel = 0;
            updateStatus();
            Log.i(TAG, "web_playback id=" + requestId + " ok=true q=" + preferredQuality);
        } catch (Exception e) {
            showOverlay("播放结果解析失败: " + e.getMessage(), false);
        }
    }

    private void onJsEvent(String name, String json) {
        try {
            JSONObject object = new JSONObject(json == null ? "{}" : json);
            switch (name) {
                case "first_frame":
                    boolean isReal = object.optBoolean("realFrame", false);
                    String note = object.optString("note", "");
                    onFirstFrame(isReal, note);
                    break;
                case "real_frame":
                    onRealFrame();
                    break;
                case "black_screen":
                    if (isChannelChanging) {
                        Log.i(TAG, "black_screen ignored during channel change reason=" + object.optString("reason"));
                        break;
                    }
                    onBlackScreenEvent(object.optString("reason", "unknown"));
                    break;
                case "stalled":
                    if (!isChannelChanging) Log.w(TAG, "js_event stalled");
                    break;
                case "ended":
                    if (!isChannelChanging) Log.w(TAG, "js_event ended");
                    break;
                case "no_video":
                    if (!isChannelChanging) Log.w(TAG, "js_event no_video");
                    break;
                case "heartbeat":
                    lastJsHeartbeatMs = System.currentTimeMillis();
                    break;
                case "video_debug":
                    Log.i(TAG, "video_debug: " + object.toString());
                    break;
            }
        } catch (Exception e) {
            Log.w(TAG, "onJsEvent parse error: " + e.getMessage());
        }
    }

    private void showBridgeWebView() {
        if (bridgeWebView != null && bridgeWebView.getAlpha() < 1.0f) {
            bridgeWebView.setAlpha(1.0f);
            bridgeWebView.setVisibility(View.VISIBLE);
            Log.i(TAG, "bridgeWebView setAlpha=1.0 (real frame arrived)");
        }
    }

    private void onFirstFrame(boolean realFrame, String note) {
        firstFrameReceived = true;
        if (realFrame) realFrameReceived = true;
        showBridgeWebView();
        if (overlayWaitingFirstFrame) {
            overlayWaitingFirstFrame = false;
            handler.removeCallbacks(firstFrameTimeoutRunnable);
            handler.removeCallbacks(hideOverlayRunnable);
            handler.postDelayed(hideOverlayRunnable, OVERLAY_HIDE_AFTER_FIRST_FRAME_MS);
            Log.i(TAG, "overlay_hide scheduled first_frame real=" + realFrame + " +" + OVERLAY_HIDE_AFTER_FIRST_FRAME_MS + "ms");
        }
    }

    private void onRealFrame() {
        realFrameReceived = true;
        showBridgeWebView();
        if (overlayWaitingFirstFrame) {
            overlayWaitingFirstFrame = false;
            handler.removeCallbacks(firstFrameTimeoutRunnable);
            handler.removeCallbacks(hideOverlayRunnable);
            handler.postDelayed(hideOverlayRunnable, OVERLAY_HIDE_AFTER_FIRST_FRAME_MS);
            Log.i(TAG, "overlay_hide scheduled canvas real_frame +" + OVERLAY_HIDE_AFTER_FIRST_FRAME_MS + "ms");
        }
    }

    private void onBlackScreenEvent(String reason) {
        consecutiveBlackScreens++;
        Log.w(TAG, "black_screen_event reason=" + reason + " count=" + consecutiveBlackScreens + " recoveryLevel=" + recoveryLevel);
        if (consecutiveBlackScreens >= 2) {
            showOverlay("线路问题\n正在修复 (" + consecutiveBlackScreens + ")", true);
            handler.removeCallbacks(blackScreenRecoveryRunnable);
            handler.postDelayed(blackScreenRecoveryRunnable, 400);
        }
    }

    private void runBlackScreenRecovery() {
        if (bridgeWebView == null) return;
        consecutiveBlackScreens = 0;
        recoveryLevel = Math.min(recoveryLevel + 1, MAX_RECOVERY_LEVEL);
        Log.w(TAG, "black_screen_recovery level=" + recoveryLevel);

        switch (recoveryLevel) {
            case 1: {
                showOverlay("修复 L1: 强制重绘...", false);
                try {
                    bridgeWebView.evaluateJavascript(
                            "window.YspTvBridge && window.YspTvBridge.forceRepaint();", null);
                } catch (Exception ignored) {}
                handler.postDelayed(new Runnable() {
                    @Override public void run() { requestCurrentStream("recovery_L1"); }
                }, 500);
                break;
            }
            case 2: {
                showOverlay("修复 L2: 重新注入桥接...", false);
                try {
                    bridgeWebView.evaluateJavascript(
                            "window.YspTvBridge && window.YspTvBridge.stopWatchdog && window.YspTvBridge.stopWatchdog();", null);
                } catch (Exception ignored) {}
                scheduleBridgeInjection(200);
                handler.postDelayed(new Runnable() {
                    @Override public void run() { requestCurrentStream("recovery_L2"); }
                }, 1200);
                break;
            }
            case 3: {
                showOverlay("修复 L3: 重载页面...", false);
                try {
                    bridgeWebView.evaluateJavascript(
                            "window.YspTvBridge && window.YspTvBridge.reloadPage();", null);
                } catch (Exception ignored) {}
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (bridgeWebView != null) {
                            bridgeWebView.reload();
                            bridgeAttempts = 0;
                            channelsLoaded = false;
                            scheduleBridgeInjection(1500);
                        }
                    }
                }, 800);
                break;
            }
            case 4:
            default:
                showOverlay("修复 L4: 重建 WebView...", false);
                rebuildWebView();
                break;
        }
    }

    private void rebuildWebView() {
        if (bridgeWebView == null) return;
        try {
            bridgeWebView.stopLoading();
            bridgeWebView.loadUrl("about:blank");
            bridgeWebView.removeJavascriptInterface("YspAndroid");
            bridgeWebView.clearHistory();
            bridgeWebView.clearCache(true);
            root.removeView(bridgeWebView);
            bridgeWebView.destroy();
        } catch (Exception e) { Log.e(TAG, "destroy old webview: " + e.getMessage()); }
        bridgeWebView = null;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupProtocolBridge(null);
                bridgeAttempts = 0;
                channelsLoaded = false;
                requestCurrentStream("recovery_L4");
            }
        }, 300);
    }

    private String quoteJs(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private void changeChannel(int delta) {
        if (channels.isEmpty()) return;
        currentIndex = (currentIndex + delta + channels.size()) % channels.size();
        menuSelection = currentIndex;
        channelAdapter.notifyDataSetChanged();
        requestCurrentStream("channel");
    }

    private void changeQuality(int delta) {
        int index = qualityIndex(preferredQuality);
        if (index < 0) index = qualityIndex("fhd");
        index = (index + delta + QUALITY_ORDER.length) % QUALITY_ORDER.length;
        preferredQuality = QUALITY_ORDER[index];
        preferences.edit().putString(PREF_QUALITY, preferredQuality).apply();
        requestCurrentStream("quality");
    }

    private int qualityIndex(String quality) {
        for (int i = 0; i < QUALITY_ORDER.length; i++) {
            if (QUALITY_ORDER[i].equals(quality)) return i;
        }
        return -1;
    }

    private String qualityLabel(String quality) {
        if ("hd".equals(quality)) return "540P HD";
        if ("shd".equals(quality)) return "720P SHD";
        return "1080P 蓝光";
    }

    private String qualityOverlayLabel(String quality) {
        if ("hd".equals(quality)) return "540P";
        if ("shd".equals(quality)) return "720P";
        return "1080P";
    }

    private String playbackModeLabel() {
        if (PLAYBACK_MODE_SW.equals(playbackMode)) return "SW兼容";
        if (PLAYBACK_MODE_HW.equals(playbackMode)) return "HW性能";
        return "默认推荐";
    }

    private static final java.util.Map<String, String> CCTV_GENRE = new java.util.HashMap<String, String>();
    static {
        CCTV_GENRE.put("CCTV-1", "综合");
        CCTV_GENRE.put("CCTV-2", "财经");
        CCTV_GENRE.put("CCTV-3", "综艺");
        CCTV_GENRE.put("CCTV-4", "中文国际");
        CCTV_GENRE.put("CCTV-5", "体育");
        CCTV_GENRE.put("CCTV-5+", "体育赛事");
        CCTV_GENRE.put("CCTV-6", "电影");
        CCTV_GENRE.put("CCTV-7", "国防军事");
        CCTV_GENRE.put("CCTV-8", "电视剧");
        CCTV_GENRE.put("CCTV-9", "纪录");
        CCTV_GENRE.put("CCTV-10", "科教");
        CCTV_GENRE.put("CCTV-11", "戏曲");
        CCTV_GENRE.put("CCTV-12", "社会与法");
        CCTV_GENRE.put("CCTV-13", "新闻");
        CCTV_GENRE.put("CCTV-14", "少儿");
        CCTV_GENRE.put("CCTV-15", "音乐");
        CCTV_GENRE.put("CCTV-16", "奥林匹克");
        CCTV_GENRE.put("CCTV-17", "农业农村");
    }

    private String formatChannelDisplayName(Channel ch) {
        if (ch == null) return "";
        String raw = ch.name == null ? "" : ch.name.trim();
        if (raw.isEmpty()) return raw;
        String key = raw.toUpperCase();
        if (key.startsWith("CCTV")) {
            String numPart = raw.substring(4);
            if (numPart.contains("4K")) return "CCTV-4K 4K超高清";
            if (numPart.contains("8K")) return "CCTV-8K 8K超高清";
            if (numPart.matches("\\d+\\+?")) {
                String dashed = "CCTV-" + numPart;
                String genre = CCTV_GENRE.get(dashed);
                if (genre != null) return dashed + " " + genre;
                return dashed;
            }
            return raw;
        }
        if (key.startsWith("CGTN")) return raw;
        return raw;
    }

    private void updateStatus() {
        String channelName = channels.isEmpty() ? "" : channels.get(currentIndex).name;
        statusText.setText(qualityLabel(preferredQuality) + (channelName.length() > 0 ? "  " + channelName : ""));
    }

    private void showPlaybackOverlay() {
        if (channels.isEmpty()) return;
        String title = channels.get(currentIndex).name + "  " + qualityOverlayLabel(preferredQuality);
        String text = title + "\n" + PLAYBACK_HELP_TEXT;
        SpannableString overlay = new SpannableString(text);
        overlay.setSpan(new RelativeSizeSpan(0.45f), title.length() + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        firstFrameReceived = false;
        realFrameReceived = false;
        overlayWaitingFirstFrame = true;
        handler.removeCallbacks(hideOverlayRunnable);
        handler.removeCallbacks(firstFrameTimeoutRunnable);
        if (bridgeWebView != null) {
            bridgeWebView.setAlpha(0f);
            bridgeWebView.setVisibility(View.VISIBLE);
            Log.i(TAG, "showPlaybackOverlay: bridgeWebView alpha=0 overlay VISIBLE channel=" + channels.get(currentIndex).name);
        }
        overlayText.setText(overlay);
        overlayText.setVisibility(View.VISIBLE);
        handler.postDelayed(firstFrameTimeoutRunnable, FIRST_FRAME_TIMEOUT_MS);
    }

    private void showOverlay(CharSequence text, boolean autoHide) {
        overlayWaitingFirstFrame = false;
        handler.removeCallbacks(firstFrameTimeoutRunnable);
        handler.removeCallbacks(hideOverlayRunnable);
        overlayText.setText(text);
        overlayText.setVisibility(View.VISIBLE);
        if (autoHide) handler.postDelayed(hideOverlayRunnable, 3000);
    }

    private void toggleMenu() {
        if (menuPanel.getVisibility() == View.VISIBLE) hideMenu();
        else showChannelsMenu();
    }

    private void showChannelsMenu() {
        menuPage = MENU_PAGE_CHANNELS;
        menuSelection = currentIndex + 1;
        if (menuSelection < 1) menuSelection = 1;
        if (menuSelection > channels.size()) menuSelection = 1;
        updateMenuHeader();
        channelListView.setSelection(menuSelection);
        channelAdapter.notifyDataSetChanged();
        Log.i(TAG, "menu_channels");
        menuPanel.setVisibility(View.VISIBLE);
    }

    private void showSettingsMenu() {
        menuPage = MENU_PAGE_SETTINGS;
        settingsSelection = 0;
        updateMenuHeader();
        channelListView.setSelection(settingsSelection);
        channelAdapter.notifyDataSetChanged();
        Log.i(TAG, "menu_settings mode=" + playbackMode);
        menuPanel.setVisibility(View.VISIBLE);
    }

    private void hideMenu() {
        menuPanel.setVisibility(View.GONE);
        Log.i(TAG, "menu_hide");
        hideSystemUi();
    }

    private void updateMenuHeader() {
        if (menuHeader != null) {
            if (menuPage == MENU_PAGE_SETTINGS) menuHeader.setText("设置");
            else menuHeader.setText("频道列表  " + (channels.isEmpty() ? "0" : String.valueOf(channels.size())));
        }
    }

    private void moveMenuSelection(int delta) {
        if (menuPage == MENU_PAGE_SETTINGS) {
            settingsSelection = (settingsSelection + delta + SETTINGS_ITEMS.length) % SETTINGS_ITEMS.length;
            Log.i(TAG, "menu_move_settings idx=" + settingsSelection);
            channelListView.setSelection(settingsSelection);
            channelAdapter.notifyDataSetChanged();
            return;
        }
        if (channels.isEmpty()) return;
        int total = channels.size() + 1;
        menuSelection = (menuSelection + delta + total) % total;
        if (menuSelection == MENU_SELECTION_SETTINGS) {
            Log.i(TAG, "menu_move -> 设置");
        } else {
            int chIdx = menuSelection - 1;
            Log.i(TAG, "menu_move_channels idx=" + chIdx + " name=" + channels.get(chIdx).name);
        }
        channelListView.setSelection(menuSelection);
        channelAdapter.notifyDataSetChanged();
    }

    private void cyclePlaybackMode() {
        if (PLAYBACK_MODE_DEFAULT.equals(playbackMode)) playbackMode = PLAYBACK_MODE_HW;
        else if (PLAYBACK_MODE_HW.equals(playbackMode)) playbackMode = PLAYBACK_MODE_SW;
        else playbackMode = PLAYBACK_MODE_DEFAULT;
        preferences.edit().putString(PREF_PLAYBACK_MODE, playbackMode).apply();
        applyPlaybackModeToWebView();
        Log.i(TAG, "playback_mode_change mode=" + playbackMode);
        channelAdapter.notifyDataSetChanged();
    }

    private void toggleAutoStart() {
        autoStartOnBoot = !autoStartOnBoot;
        preferences.edit().putBoolean(PREF_AUTO_START, autoStartOnBoot).apply();
        Log.i(TAG, "auto_start_toggle enabled=" + autoStartOnBoot);
        channelAdapter.notifyDataSetChanged();
    }

    private void selectMenuChannel() {
        if (channels.isEmpty()) return;
        currentIndex = menuSelection - 1;
        if (currentIndex < 0) currentIndex = 0;
        Log.i(TAG, "menu_select idx=" + currentIndex + " name=" + channels.get(currentIndex).name);
        hideMenu();
        requestCurrentStream();
    }

    private void selectMenuItemAt(int position) {
        if (menuPage == MENU_PAGE_SETTINGS) {
            if (position == 0) cyclePlaybackMode();
            else if (position == 1) toggleAutoStart();
            return;
        }
        if (position == MENU_SELECTION_SETTINGS) {
            showSettingsMenu();
        } else if (position >= 1 && position <= channels.size()) {
            menuSelection = position;
            selectMenuChannel();
        }
    }

    private void appendNumber(int digit) {
        handler.removeCallbacks(numberCommitRunnable);
        if (numberBuffer.length() >= 3) numberBuffer.setLength(0);
        numberBuffer.append(digit);
        showOverlay("频道 " + numberBuffer.toString(), true);
        handler.postDelayed(numberCommitRunnable, 1200);
    }

    private void commitNumberInput() {
        if (numberBuffer.length() == 0 || channels.isEmpty()) return;
        try {
            int oneBased = Integer.parseInt(numberBuffer.toString());
            numberBuffer.setLength(0);
            if (oneBased >= 1 && oneBased <= channels.size()) {
                currentIndex = oneBased - 1;
                menuSelection = currentIndex;
                channelAdapter.notifyDataSetChanged();
                channelListView.setSelection(currentIndex);
                requestCurrentStream();
            } else {
                showOverlay("频道 " + oneBased + " 超出范围", true);
            }
        } catch (NumberFormatException ignored) { numberBuffer.setLength(0); }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return true;
        int keyCode = event.getKeyCode();
        Log.i(TAG, "key_down code=" + keyCode);
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            appendNumber(keyCode - KeyEvent.KEYCODE_0);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_SETTINGS) {
            showSettingsMenu();
            return true;
        }
        if (menuPanel.getVisibility() == View.VISIBLE) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { moveMenuSelection(-1); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { moveMenuSelection(1); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { hideMenu(); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (menuPage == MENU_PAGE_SETTINGS) { cyclePlaybackMode(); hideMenu(); }
                return true;
            }
            if (isOkKey(keyCode)) {
                if (menuPage == MENU_PAGE_SETTINGS) {
                    if (settingsSelection == 0) cyclePlaybackMode();
                    else if (settingsSelection == 1) toggleAutoStart();
                    hideMenu();
                } else {
                    if (menuSelection == MENU_SELECTION_SETTINGS) showSettingsMenu();
                    else selectMenuChannel();
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (menuPage == MENU_PAGE_SETTINGS) showChannelsMenu();
                else hideMenu();
                return true;
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { changeChannel(-1); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { changeChannel(1); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            showChannelsMenu();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) { changeQuality(1); return true; }
        if (isOkKey(keyCode) || keyCode == KeyEvent.KEYCODE_MENU) { showChannelsMenu(); return true; }
        if (keyCode == KeyEvent.KEYCODE_BACK) { finish(); return true; }
        return super.dispatchKeyEvent(event);
    }

    private boolean isOkKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER;
    }

    private void handleTouchTap(float x, float y) {
        if (menuPanel.getVisibility() == View.VISIBLE) {
            if (isPointInsideMenu(x, y)) {
                int position = pointToMenuPosition(x, y);
                if (position >= 0) selectMenuItemAt(position);
            } else {
                hideMenu();
            }
            return;
        }
        toggleMenu();
    }

    private void handleTouchSwipe(float dx, float dy, boolean startedInMenu) {
        if (menuPanel.getVisibility() == View.VISIBLE && startedInMenu) {
            if (Math.abs(dx) > Math.abs(dy)) {
                handleMenuHorizontalSwipe(dx);
            }
            return;
        }
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) {
                Log.i(TAG, "touch_swipe_right");
                changeQuality(1);
            } else {
                Log.i(TAG, "touch_swipe_left");
                changeQuality(-1);
            }
        } else {
            if (dy > 0) {
                Log.i(TAG, "touch_swipe_down");
                changeChannel(1);
            } else {
                Log.i(TAG, "touch_swipe_up");
                changeChannel(-1);
            }
        }
    }

    private void handleMenuHorizontalSwipe(float dx) {
        if (dx < 0) {
            Log.i(TAG, "touch_menu_swipe_left");
            if (menuPage == MENU_PAGE_CHANNELS && menuSelection == MENU_SELECTION_SETTINGS) {
                showSettingsMenu();
            }
            return;
        }
        Log.i(TAG, "touch_menu_swipe_right");
        if (menuPage == MENU_PAGE_SETTINGS) {
            showChannelsMenu();
        } else if (menuPage == MENU_PAGE_CHANNELS) {
            changeQuality(1);
        }
    }

    private boolean isPointInsideMenu(float x, float y) {
        return menuPanel.getVisibility() == View.VISIBLE
                && x >= menuPanel.getLeft() && x <= menuPanel.getRight()
                && y >= menuPanel.getTop() && y <= menuPanel.getBottom();
    }

    private int pointToMenuPosition(float x, float y) {
        android.graphics.Rect rect = getListRectInRoot();
        if (!rect.contains((int) x, (int) y)) return -1;
        int position = channelListView.pointToPosition(
                (int) (x - rect.left), (int) (y - rect.top));
        if (position < 0 || position >= channelAdapter.getCount()) return -1;
        return position;
    }

    private android.graphics.Rect getListRectInRoot() {
        int left = menuPanel.getLeft() + channelListView.getLeft();
        int top = menuPanel.getTop() + channelListView.getTop();
        return new android.graphics.Rect(left, top, left + channelListView.getWidth(), top + channelListView.getHeight());
    }

    @SuppressWarnings("DEPRECATION")
    private void hideSystemUi() {
        int flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        root.setSystemUiVisibility(flags);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bridgeWebView != null) bridgeWebView.saveState(outState);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (bridgeWebView == null) return;
        if (level == TRIM_MEMORY_COMPLETE || level == TRIM_MEMORY_MODERATE) {
            bridgeWebView.loadUrl("about:blank");
            bridgeWebView.clearHistory();
            Log.i(TAG, "trim_memory level=" + level + " cleared WebView");
        } else if (level >= TRIM_MEMORY_BACKGROUND) {
            bridgeWebView.clearCache(true);
            Log.i(TAG, "trim_memory level=" + level + " cleared WebView cache");
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (bridgeWebView != null) {
            bridgeWebView.loadUrl("about:blank");
            bridgeWebView.clearHistory();
            bridgeWebView.clearCache(true);
            Log.i(TAG, "onLowMemory cleared WebView");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bridgeWebView != null && Build.VERSION.SDK_INT >= 11) {
            try { bridgeWebView.onPause(); } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bridgeWebView != null && Build.VERSION.SDK_INT >= 11) {
            try { bridgeWebView.onResume(); } catch (Exception ignored) {}
        }
        hideSystemUi();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (bridgeWebView != null) {
            bridgeWebView.stopLoading();
            bridgeWebView.loadUrl("about:blank");
            bridgeWebView.removeJavascriptInterface("YspAndroid");
            bridgeWebView.destroy();
            bridgeWebView = null;
        }
        Log.i(TAG, "destroy_cleanup_complete");
        super.onDestroy();
    }

    private final class BridgeCallbacks {
        @JavascriptInterface
        public void onChannels(final String json) {
            handler.post(new Runnable() { @Override public void run() { onChannelsLoaded(json); } });
        }

        @JavascriptInterface
        public void onError(final String scope, final String message) {
            handler.post(new Runnable() {
                @Override public void run() {
                    if ("channels".equals(scope) && !channelsLoaded && bridgeAttempts < 15) return;
                    Log.w(TAG, "protocol_error scope=" + scope + " msg=" + message);
                }
            });
        }

        @JavascriptInterface
        public void onPlayback(final String requestId, final String json) {
            handler.post(new Runnable() {
                @Override public void run() {
                    Log.i(TAG, "web_playback id=" + requestId + " result=" + json);
                    onPlaybackResult(requestId, json);
                }
            });
        }

        @JavascriptInterface
        public void onEvent(final String name, final String json) {
            handler.post(new Runnable() {
                @Override public void run() { onJsEvent(name, json); }
            });
        }
    }

    private static final class Channel {
        final String name;
        final String pid;
        final String streamId;
        final String type;
        final boolean is4K;
        Channel(String name, String pid, String streamId, String type, boolean is4K) {
            this.name = name; this.pid = pid; this.streamId = streamId; this.type = type; this.is4K = is4K;
        }
    }

    private final class ChannelAdapter extends BaseAdapter {
        private final Context context;
        ChannelAdapter(Context context) { this.context = context; }

        @Override public int getCount() {
            if (menuPage == MENU_PAGE_SETTINGS) return SETTINGS_ITEMS.length;
            return channels.size() + 1;
        }

        @Override public Object getItem(int position) {
            if (menuPage == MENU_PAGE_SETTINGS) return SETTINGS_ITEMS[position];
            if (position == MENU_SELECTION_SETTINGS) return "设置";
            return channels.get(position - 1);
        }

        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView instanceof TextView) {
                textView = (TextView) convertView;
            } else {
                textView = new TextView(context);
                textView.setTextSize(22);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setSingleLine(true);
                textView.setPadding(dp(18), 0, dp(14), 0);
                textView.setTextColor(Color.WHITE);
                textView.setLayoutParams(new ListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
            }
            boolean selected;
            if (menuPage == MENU_PAGE_SETTINGS) {
                if (position == 0) textView.setText("解码模式  " + playbackModeLabel());
                else if (position == 1) textView.setText("开机自启  " + (autoStartOnBoot ? "开" : "关"));
                else textView.setText(SETTINGS_ITEMS[position]);
                selected = position == settingsSelection;
            } else {
                if (position == MENU_SELECTION_SETTINGS) {
                    textView.setText("⚙ 设置");
                    selected = menuSelection == MENU_SELECTION_SETTINGS;
                } else {
                    Channel channel = channels.get(position - 1);
                    textView.setText(String.valueOf(position) + ". " + formatChannelDisplayName(channel));
                    selected = position == menuSelection;
                }
            }
            if (selected) textView.setBackgroundColor(0xFF1D6FFF);
            else if (menuPage == MENU_PAGE_CHANNELS && position > 0 && (position - 1) == currentIndex) textView.setBackgroundColor(0x66333333);
            else textView.setBackgroundColor(Color.TRANSPARENT);
            return textView;
        }
    }

    private final class GestureTraceView extends View {
        private final Path path = new Path();
        private final Paint paint = new Paint();
        private float lastY;
        private boolean downInMenu;
        private boolean moved;
        private float downX, downY;
        private long downTime;
        private final Runnable clearPathRunnable = new Runnable() {
            @Override
            public void run() {
                path.reset();
                invalidate();
            }
        };

        GestureTraceView(Context context) {
            super(context);
            paint.setColor(0xFF35D7FF);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(dp(4));
            setWillNotDraw(false);
            setClickable(true);
            setFocusable(false);
        }

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(clearPathRunnable);
                    downX = x;
                    downY = y;
                    downTime = System.currentTimeMillis();
                    lastY = y;
                    downInMenu = isPointInsideMenu(x, y);
                    moved = false;
                    path.reset();
                    path.moveTo(x, y);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(x, y);
                    float totalDx = x - downX;
                    float totalDy = y - downY;
                    if (Math.abs(totalDx) > touchSlop || Math.abs(totalDy) > touchSlop) {
                        moved = true;
                    }
                    if (menuPanel.getVisibility() == View.VISIBLE && downInMenu) {
                        float stepDy = y - lastY;
                        if (Math.abs(stepDy) >= 1f) {
                            channelListView.smoothScrollBy((int) -stepDy, 0);
                        }
                    }
                    lastY = y;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    path.lineTo(x, y);
                    invalidate();
                    float dx = x - downX;
                    float dy = y - downY;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    long duration = System.currentTimeMillis() - downTime;
                    boolean isSwipe = moved && Math.max(Math.abs(dx), Math.abs(dy)) >= dp(48);
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        if (isSwipe) {
                            handleTouchSwipe(dx, dy, downInMenu);
                        } else if (dist < touchSlop && duration < 300) {
                            handleTouchTap(downX, downY);
                        }
                    }
                    handler.postDelayed(clearPathRunnable, 450);
                    return true;
                default:
                    return true;
            }
        }
    }
}