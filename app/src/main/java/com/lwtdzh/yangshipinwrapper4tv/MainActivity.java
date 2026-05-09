package com.lwtdzh.yangshipinwrapper4tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
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
    private static final String PLAYBACK_MODE_HW = "hw";
    private static final String PLAYBACK_MODE_SW = "sw";
    private static final String[] QUALITY_ORDER = new String[]{"hd", "shd", "fhd"};
    private static final String PLAYBACK_HELP_TEXT = "按上下键换台，按OK键打开频道列表，按左右切换清晰度";
    private static final int MENU_PAGE_MAIN = 0;
    private static final int MENU_PAGE_CHANNELS = 1;
    private static final int MENU_PAGE_SETTINGS = 2;
    private static final int MAIN_MENU_SETTINGS = 0;
    private static final int MAIN_MENU_CHANNELS = 1;
    private static final String[] MAIN_MENU_ITEMS = new String[]{"Settings", "Channels"};

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
    private int menuSelection = 0;
    private int mainMenuSelection = MAIN_MENU_CHANNELS;
    private int settingsSelection = 0;
    private int menuPage = MENU_PAGE_CHANNELS;
    private int bridgeAttempts = 0;
    private boolean channelsLoaded = false;
    private String playbackMode = PLAYBACK_MODE_HW;
    private int touchSlop;
    private final StringBuilder numberBuffer = new StringBuilder();

    private final Runnable hideOverlayRunnable = new Runnable() {
        @Override
        public void run() {
            overlayText.setVisibility(View.GONE);
        }
    };

    private final Runnable numberCommitRunnable = new Runnable() {
        @Override
        public void run() {
            commitNumberInput();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        preferredQuality = preferences.getString(PREF_QUALITY, "fhd");
        if (qualityIndex(preferredQuality) < 0) {
            preferredQuality = "fhd";
        }
        playbackMode = preferences.getString(PREF_PLAYBACK_MODE, PLAYBACK_MODE_HW);
        if (!PLAYBACK_MODE_SW.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_HW;
        }

        buildUi();
        setupProtocolBridge();
        showOverlay("Loading Yangshipin...", false);
        updateStatus();
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
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        root.addView(overlayText, overlayParams);

        statusText = new TextView(this);
        statusText.setVisibility(View.GONE);

        buildMenu();
        gestureTraceView = new GestureTraceView(this);
        root.addView(gestureTraceView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
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
        menuHeader.setText("Channels");
        menuHeader.setGravity(Gravity.CENTER_VERTICAL);
        menuPanel.addView(menuHeader, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(48)));

        channelListView = new ListView(this);
        channelListView.setDivider(new ColorDrawable(0x33FFFFFF));
        channelListView.setDividerHeight(1);
        channelListView.setCacheColorHint(Color.TRANSPARENT);
        channelListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        channelAdapter = new ChannelAdapter(this);
        channelListView.setAdapter(channelAdapter);
        menuPanel.addView(channelListView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1));

        int menuWidth = Math.min(dp(470), (int) (getResources().getDisplayMetrics().widthPixels * 0.86f));
        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
                menuWidth,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.LEFT);
        root.addView(menuPanel, menuParams);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setupProtocolBridge() {
        WebView.setWebContentsDebuggingEnabled(true);
        bridgeWebView = new WebView(this);
        bridgeWebView.setFocusable(false);
        bridgeWebView.setBackgroundColor(Color.BLACK);
        applyPlaybackModeToWebView();
        WebSettings settings = bridgeWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadsImagesAutomatically(false);
        settings.setBlockNetworkImage(true);
        settings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36");
        bridgeWebView.setWebChromeClient(new WebChromeClient());
        bridgeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                scheduleBridgeInjection(2500);
            }
        });
        bridgeWebView.addJavascriptInterface(new BridgeCallbacks(), "YspAndroid");
        FrameLayout.LayoutParams bridgeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(bridgeWebView, 0, bridgeParams);
        bridgeWebView.loadUrl(YSP_HOME_URL);
    }

    private void applyPlaybackModeToWebView() {
        if (bridgeWebView == null) {
            return;
        }
        // Android WebView video is backed by a separate accelerated surface. Forcing
        // the WebView itself into a software layer leaves the official player with
        // audio but a black video surface on TV/emulator builds.
        bridgeWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void scheduleBridgeInjection(long delayMs) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                injectBridge();
            }
        }, delayMs);
    }

    private void injectBridge() {
        if (bridgeWebView == null) {
            return;
        }
        bridgeAttempts++;
        try {
            bridgeWebView.evaluateJavascript(loadAsset("ysp_bridge.js"), null);
        } catch (Exception e) {
            showOverlay("Protocol bridge failed: " + e.getMessage(), false);
        }
        if (!channelsLoaded && bridgeAttempts < 12) {
            scheduleBridgeInjection(2500);
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
        } finally {
            input.close();
        }
    }

    private void onChannelsLoaded(String json) {
        if (channelsLoaded) {
            return;
        }
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
                showOverlay("No free Yangshipin channels found.", false);
                return;
            }
            channelsLoaded = true;
            Log.i(TAG, "channels_loaded count=" + channels.size());
            String savedPid = preferences.getString(PREF_CHANNEL_PID, "");
            currentIndex = findChannelIndexByPid(savedPid);
            if (currentIndex < 0) {
                currentIndex = 0;
            }
            menuSelection = currentIndex;
            channelAdapter.notifyDataSetChanged();
            updateMenuHeader();
            requestCurrentStream("initial");
        } catch (Exception e) {
            showOverlay("Failed to parse channel list: " + e.getMessage(), false);
        }
    }

    private int findChannelIndexByPid(String pid) {
        if (pid == null || pid.length() == 0) {
            return -1;
        }
        for (int i = 0; i < channels.size(); i++) {
            if (pid.equals(channels.get(i).pid)) {
                return i;
            }
        }
        return -1;
    }

    private void requestCurrentStream() {
        requestCurrentStream("channel");
    }

    private void requestCurrentStream(String reason) {
        if (channels.isEmpty() || bridgeWebView == null) {
            return;
        }
        Channel channel = channels.get(currentIndex);
        preferences.edit().putString(PREF_CHANNEL_PID, channel.pid).apply();
        showPlaybackOverlay();
        updateStatus();
        String requestId = String.valueOf(++requestCounter);
        activeRequestId = requestId;
        Log.i(TAG, "stream_request id=" + requestId + " index=" + currentIndex
                + " name=" + channel.name + " pid=" + channel.pid
                + " streamId=" + channel.streamId + " quality=" + preferredQuality);
        String js = "window.YspTvBridge && window.YspTvBridge.playChannel("
                + quoteJs(requestId) + ","
                + quoteJs(channel.pid) + ","
                + quoteJs(channel.streamId) + ","
                + quoteJs(preferredQuality) + ","
                + quoteJs(reason) + ");";
        bridgeWebView.evaluateJavascript(js, null);
    }

    private void onPlaybackResult(String requestId, String json) {
        if (!activeRequestId.equals(requestId)) {
            return;
        }
        try {
            JSONObject object = new JSONObject(json);
            if (!object.optBoolean("ok", false)) {
                Log.w(TAG, "web_playback id=" + requestId + " ok=false error=" + object.optString("error"));
                showOverlay("Playback failed: " + object.optString("error"), false);
                return;
            }
            String actualQuality = object.optString("quality", preferredQuality);
            if (qualityIndex(actualQuality) >= 0) {
                preferredQuality = actualQuality;
                preferences.edit().putString(PREF_QUALITY, preferredQuality).apply();
            }
            updateStatus();
            Log.i(TAG, "web_playback id=" + requestId + " ok=true quality=" + preferredQuality);
        } catch (Exception e) {
            showOverlay("Failed to parse playback result: " + e.getMessage(), false);
        }
    }

    private String quoteJs(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private void changeChannel(int delta) {
        if (channels.isEmpty()) {
            return;
        }
        currentIndex = (currentIndex + delta + channels.size()) % channels.size();
        Log.i(TAG, "channel_change index=" + currentIndex + " name=" + channels.get(currentIndex).name);
        menuSelection = currentIndex;
        channelAdapter.notifyDataSetChanged();
        requestCurrentStream("channel");
    }

    private void changeQuality(int delta) {
        int index = qualityIndex(preferredQuality);
        if (index < 0) {
            index = qualityIndex("fhd");
        }
        index = (index + delta + QUALITY_ORDER.length) % QUALITY_ORDER.length;
        preferredQuality = QUALITY_ORDER[index];
        Log.i(TAG, "quality_change quality=" + preferredQuality);
        preferences.edit().putString(PREF_QUALITY, preferredQuality).apply();
        requestCurrentStream("quality");
    }

    private int qualityIndex(String quality) {
        for (int i = 0; i < QUALITY_ORDER.length; i++) {
            if (QUALITY_ORDER[i].equals(quality)) {
                return i;
            }
        }
        return -1;
    }

    private String qualityLabel(String quality) {
        if ("hd".equals(quality)) {
            return "540P HD";
        }
        if ("shd".equals(quality)) {
            return "720P SHD";
        }
        return "1080P Blu-ray";
    }

    private String qualityOverlayLabel(String quality) {
        if ("hd".equals(quality)) {
            return "540P";
        }
        if ("shd".equals(quality)) {
            return "720P";
        }
        return "1080P";
    }

    private String playbackModeLabel() {
        return PLAYBACK_MODE_SW.equals(playbackMode) ? "SW" : "HW";
    }

    private void updateStatus() {
        String channelName = channels.isEmpty() ? "" : channels.get(currentIndex).name;
        statusText.setText(qualityLabel(preferredQuality) + (channelName.length() > 0 ? "  " + channelName : ""));
    }

    private void showPlaybackOverlay() {
        if (channels.isEmpty()) {
            return;
        }
        String title = channels.get(currentIndex).name + "  " + qualityOverlayLabel(preferredQuality);
        String text = title + "\n" + PLAYBACK_HELP_TEXT;
        SpannableString overlay = new SpannableString(text);
        overlay.setSpan(new RelativeSizeSpan(0.45f), title.length() + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        showOverlay(overlay, true);
    }

    private void showOverlay(CharSequence text, boolean autoHide) {
        handler.removeCallbacks(hideOverlayRunnable);
        overlayText.setText(text);
        overlayText.setVisibility(View.VISIBLE);
        if (autoHide) {
            handler.postDelayed(hideOverlayRunnable, 5000);
        }
    }

    private void toggleMenu() {
        if (menuPanel.getVisibility() == View.VISIBLE) {
            return;
        } else {
            showMenu();
        }
    }

    private void showMenu() {
        if (channels.isEmpty()) {
            showOverlay("Channel list is still loading.", true);
            return;
        }
        showChannelsMenu();
    }

    private void showMainMenu(int selectedItem) {
        menuPage = MENU_PAGE_MAIN;
        mainMenuSelection = selectedItem;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        Log.i(TAG, "menu_main selection=" + mainMenuSelection);
        channelAdapter.notifyDataSetChanged();
        channelListView.setSelection(mainMenuSelection);
    }

    private void showChannelsMenu() {
        menuPage = MENU_PAGE_CHANNELS;
        menuSelection = currentIndex;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        Log.i(TAG, "menu_channels");
        channelAdapter.notifyDataSetChanged();
        channelListView.setSelection(menuSelection);
    }

    private void showSettingsMenu() {
        menuPage = MENU_PAGE_SETTINGS;
        settingsSelection = 0;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        Log.i(TAG, "menu_settings playbackMode=" + playbackMode);
        channelAdapter.notifyDataSetChanged();
        channelListView.setSelection(settingsSelection);
    }

    private void hideMenu() {
        menuPanel.setVisibility(View.GONE);
        Log.i(TAG, "menu_hide");
        hideSystemUi();
    }

    private void updateMenuHeader() {
        if (menuHeader != null) {
            if (menuPage == MENU_PAGE_MAIN) {
                menuHeader.setText("Menu");
            } else if (menuPage == MENU_PAGE_SETTINGS) {
                menuHeader.setText("Settings");
            } else {
                menuHeader.setText("Channels  " + (channels.isEmpty() ? "0" : String.valueOf(channels.size())));
            }
        }
    }

    private void moveMenuSelection(int delta) {
        if (menuPage == MENU_PAGE_MAIN) {
            mainMenuSelection = (mainMenuSelection + delta + MAIN_MENU_ITEMS.length) % MAIN_MENU_ITEMS.length;
            Log.i(TAG, "menu_main_selection index=" + mainMenuSelection);
            channelListView.setSelection(mainMenuSelection);
            channelAdapter.notifyDataSetChanged();
            return;
        }
        if (menuPage == MENU_PAGE_SETTINGS) {
            settingsSelection = 0;
            channelListView.setSelection(settingsSelection);
            channelAdapter.notifyDataSetChanged();
            return;
        }
        if (channels.isEmpty()) {
            return;
        }
        menuSelection = (menuSelection + delta + channels.size()) % channels.size();
        Log.i(TAG, "menu_selection index=" + menuSelection + " name=" + channels.get(menuSelection).name);
        channelListView.setSelection(menuSelection);
        channelAdapter.notifyDataSetChanged();
    }

    private void enterMainMenuSelection() {
        if (mainMenuSelection == MAIN_MENU_SETTINGS) {
            showSettingsMenu();
        } else {
            showChannelsMenu();
        }
    }

    private void togglePlaybackMode() {
        playbackMode = PLAYBACK_MODE_SW.equals(playbackMode) ? PLAYBACK_MODE_HW : PLAYBACK_MODE_SW;
        preferences.edit().putString(PREF_PLAYBACK_MODE, playbackMode).apply();
        applyPlaybackModeToWebView();
        Log.i(TAG, "playback_mode_change mode=" + playbackMode);
        channelAdapter.notifyDataSetChanged();
    }

    private void selectMenuChannel() {
        if (channels.isEmpty()) {
            return;
        }
        currentIndex = menuSelection;
        Log.i(TAG, "menu_select index=" + currentIndex + " name=" + channels.get(currentIndex).name);
        channelAdapter.notifyDataSetChanged();
        channelListView.setSelection(currentIndex);
        requestCurrentStream();
    }

    private void selectMenuItemAt(int position) {
        if (menuPage == MENU_PAGE_MAIN) {
            if (position >= 0 && position < MAIN_MENU_ITEMS.length) {
                mainMenuSelection = position;
                enterMainMenuSelection();
            }
            return;
        }
        if (menuPage == MENU_PAGE_SETTINGS) {
            togglePlaybackMode();
            return;
        }
        if (position >= 0 && position < channels.size()) {
            menuSelection = position;
            selectMenuChannel();
            Log.i(TAG, "touch_menu_select index=" + position + " name=" + channels.get(position).name);
        }
    }

    private void appendNumber(int digit) {
        handler.removeCallbacks(numberCommitRunnable);
        if (numberBuffer.length() >= 3) {
            numberBuffer.setLength(0);
        }
        numberBuffer.append(digit);
        showOverlay("Channel " + numberBuffer.toString(), true);
        handler.postDelayed(numberCommitRunnable, 1200);
    }

    private void commitNumberInput() {
        if (numberBuffer.length() == 0 || channels.isEmpty()) {
            return;
        }
        try {
            int oneBased = Integer.parseInt(numberBuffer.toString());
            numberBuffer.setLength(0);
            if (oneBased >= 1 && oneBased <= channels.size()) {
                currentIndex = oneBased - 1;
                menuSelection = currentIndex;
                Log.i(TAG, "number_select channel=" + oneBased + " index=" + currentIndex
                        + " name=" + channels.get(currentIndex).name);
                channelAdapter.notifyDataSetChanged();
                channelListView.setSelection(currentIndex);
                requestCurrentStream();
            } else {
                showOverlay("Channel " + oneBased + " is out of range.", true);
            }
        } catch (NumberFormatException ignored) {
            numberBuffer.setLength(0);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return true;
        }
        int keyCode = event.getKeyCode();
        Log.i(TAG, "key_down code=" + keyCode);
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            appendNumber(keyCode - KeyEvent.KEYCODE_0);
            return true;
        }
        if (menuPanel.getVisibility() == View.VISIBLE) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                moveMenuSelection(-1);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                moveMenuSelection(1);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (menuPage == MENU_PAGE_CHANNELS) {
                    showMainMenu(MAIN_MENU_CHANNELS);
                } else if (menuPage == MENU_PAGE_SETTINGS) {
                    showMainMenu(MAIN_MENU_SETTINGS);
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (menuPage == MENU_PAGE_MAIN) {
                    enterMainMenuSelection();
                } else if (menuPage == MENU_PAGE_SETTINGS) {
                    togglePlaybackMode();
                }
                return true;
            }
            if (isOkKey(keyCode)) {
                if (menuPage == MENU_PAGE_MAIN) {
                    enterMainMenuSelection();
                } else if (menuPage == MENU_PAGE_SETTINGS) {
                    togglePlaybackMode();
                } else {
                    selectMenuChannel();
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hideMenu();
                return true;
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            changeChannel(-1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            changeChannel(1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            changeQuality(-1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            changeQuality(1);
            return true;
        }
        if (isOkKey(keyCode) || keyCode == KeyEvent.KEYCODE_MENU) {
            toggleMenu();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
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
                if (position >= 0) {
                    selectMenuItemAt(position);
                }
            } else {
                Log.i(TAG, "touch_menu_outside_hide");
                hideMenu();
            }
            return;
        }
        Log.i(TAG, "touch_tap_ok");
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
            if (menuPage == MENU_PAGE_CHANNELS) {
                showMainMenu(MAIN_MENU_CHANNELS);
            } else if (menuPage == MENU_PAGE_SETTINGS) {
                showMainMenu(MAIN_MENU_SETTINGS);
            }
            return;
        }
        Log.i(TAG, "touch_menu_swipe_right");
        if (menuPage == MENU_PAGE_MAIN) {
            enterMainMenuSelection();
        } else if (menuPage == MENU_PAGE_SETTINGS) {
            togglePlaybackMode();
        }
    }

    private boolean isPointInsideMenu(float x, float y) {
        return menuPanel.getVisibility() == View.VISIBLE
                && x >= menuPanel.getLeft()
                && x <= menuPanel.getRight()
                && y >= menuPanel.getTop()
                && y <= menuPanel.getBottom();
    }

    private int pointToMenuPosition(float x, float y) {
        Rect rect = getListRectInRoot();
        if (!rect.contains((int) x, (int) y)) {
            return -1;
        }
        int position = channelListView.pointToPosition(
                (int) (x - rect.left),
                (int) (y - rect.top));
        if (position < 0 || position >= channelAdapter.getCount()) {
            return -1;
        }
        return position;
    }

    private Rect getListRectInRoot() {
        int left = menuPanel.getLeft() + channelListView.getLeft();
        int top = menuPanel.getTop() + channelListView.getTop();
        return new Rect(left, top, left + channelListView.getWidth(), top + channelListView.getHeight());
    }

    private void hideSystemUi() {
        root.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onChannelsLoaded(json);
                }
            });
        }

        @JavascriptInterface
        public void onError(final String scope, final String message) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if ("channels".equals(scope) && !channelsLoaded && bridgeAttempts < 12) {
                        return;
                    }
                    Log.w(TAG, "protocol_error scope=" + scope + " message=" + message);
                    showOverlay("Protocol error (" + scope + "): " + message, false);
                }
            });
        }

        @JavascriptInterface
        public void onPlayback(final String requestId, final String json) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "web_playback id=" + requestId + " result=" + json);
                    onPlaybackResult(requestId, json);
                }
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
            this.name = name;
            this.pid = pid;
            this.streamId = streamId;
            this.type = type;
            this.is4K = is4K;
        }
    }

    private final class ChannelAdapter extends BaseAdapter {
        private final Context context;

        ChannelAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            if (menuPage == MENU_PAGE_MAIN) {
                return MAIN_MENU_ITEMS.length;
            }
            if (menuPage == MENU_PAGE_SETTINGS) {
                return 1;
            }
            return channels.size();
        }

        @Override
        public Object getItem(int position) {
            if (menuPage == MENU_PAGE_MAIN) {
                return MAIN_MENU_ITEMS[position];
            }
            if (menuPage == MENU_PAGE_SETTINGS) {
                return playbackMode;
            }
            return channels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

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
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(54)));
            }
            boolean selected;
            if (menuPage == MENU_PAGE_MAIN) {
                textView.setText(MAIN_MENU_ITEMS[position]);
                selected = position == mainMenuSelection;
            } else if (menuPage == MENU_PAGE_SETTINGS) {
                textView.setText("Decoder Mode  " + playbackModeLabel());
                selected = position == settingsSelection;
            } else {
                Channel channel = channels.get(position);
                String typeLabel = "weishi".equals(channel.type) ? "SAT" : "CCTV";
                textView.setText(String.valueOf(position + 1) + ". " + channel.name + "  " + typeLabel);
                selected = position == menuSelection;
            }
            if (selected) {
                textView.setBackgroundColor(0xFF1D6FFF);
            } else if (menuPage == MENU_PAGE_CHANNELS && position == currentIndex) {
                textView.setBackgroundColor(0x66333333);
            } else {
                textView.setBackgroundColor(Color.TRANSPARENT);
            }
            return textView;
        }
    }

    private final class GestureTraceView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private float downX;
        private float downY;
        private float lastY;
        private boolean downInMenu;
        private boolean moved;

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
            setFocusable(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
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
                    boolean isSwipe = moved && Math.max(Math.abs(dx), Math.abs(dy)) >= dp(48);
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        if (isSwipe) {
                            handleTouchSwipe(dx, dy, downInMenu);
                        } else {
                            handleTouchTap(x, y);
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
