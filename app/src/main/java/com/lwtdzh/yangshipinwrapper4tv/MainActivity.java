package com.lwtdzh.yangshipinwrapper4tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String TAG = "YSPTV";
    private static final String YSP_HOME_URL = "https://www.yangshipin.cn/tv/home";
    private static final String M3U_URL = "http://124.223.198.234:1905/interface.m3u";
    private static final String PREFS = "yangshipin_tv";
    private static final String PREF_QUALITY = "quality";
    private static final String PREF_CHANNEL_PID = "channel_pid";
    private static final String PREF_PLAYBACK_MODE = "playback_mode";
    private static final String PREF_AUTO_START = "auto_start_on_boot";
    private static final String PLAYBACK_MODE_HW = "hw";
    private static final String PLAYBACK_MODE_SW = "sw";
    private static final String[] QUALITY_ORDER = new String[]{"hd", "shd", "fhd"};
    private static final String PLAYBACK_HELP_TEXT = "按上下键换台，按OK键打开频道列表，按左右切换清晰度";
    private static final int MENU_PAGE_GROUPS = 2;
    private static final int MENU_PAGE_GROUP_CHANNELS = 3;
    private static final int MENU_PAGE_CHANNELS = 0;
    private static final int MENU_PAGE_SETTINGS = 1;
    private static final int SETTINGS_ITEM_COUNT = 2;
    private static final int SETTINGS_IDX_DECODER = 0;
    private static final int SETTINGS_IDX_AUTOSTART = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long MENU_AUTO_HIDE_MS = 15000;
    private final Runnable menuAutoHideRunnable = new Runnable() {
        @Override
        public void run() {
            if (menuPanel.getVisibility() == View.VISIBLE) {
                Log.i(TAG, "menu_auto_hide");
                hideMenu();
            }
        }
    };
    private final List<Channel> channels = new ArrayList<Channel>();
    private final Set<String> badUrls = new HashSet<>();
    private static final Map<String, String> CHANNEL_NAME_MAP = new HashMap<>();
    static {
        CHANNEL_NAME_MAP.put("CCTV1", "CCTV-1 综合");
        CHANNEL_NAME_MAP.put("CCTV-1", "CCTV-1 综合");
        CHANNEL_NAME_MAP.put("CCTV2", "CCTV-2 财经");
        CHANNEL_NAME_MAP.put("CCTV-2", "CCTV-2 财经");
        CHANNEL_NAME_MAP.put("CCTV3", "CCTV-3 综艺");
        CHANNEL_NAME_MAP.put("CCTV-3", "CCTV-3 综艺");
        CHANNEL_NAME_MAP.put("CCTV4", "CCTV-4 中文国际");
        CHANNEL_NAME_MAP.put("CCTV-4", "CCTV-4 中文国际");
        CHANNEL_NAME_MAP.put("CCTV5", "CCTV-5 体育");
        CHANNEL_NAME_MAP.put("CCTV-5", "CCTV-5 体育");
        CHANNEL_NAME_MAP.put("CCTV5+", "CCTV-5+ 体育赛事");
        CHANNEL_NAME_MAP.put("CCTV-5+", "CCTV-5+ 体育赛事");
        CHANNEL_NAME_MAP.put("CCTV6", "CCTV-6 电影");
        CHANNEL_NAME_MAP.put("CCTV-6", "CCTV-6 电影");
        CHANNEL_NAME_MAP.put("CCTV7", "CCTV-7 国防军事");
        CHANNEL_NAME_MAP.put("CCTV-7", "CCTV-7 国防军事");
        CHANNEL_NAME_MAP.put("CCTV8", "CCTV-8 电视剧");
        CHANNEL_NAME_MAP.put("CCTV-8", "CCTV-8 电视剧");
        CHANNEL_NAME_MAP.put("CCTV9", "CCTV-9 纪录");
        CHANNEL_NAME_MAP.put("CCTV-9", "CCTV-9 纪录");
        CHANNEL_NAME_MAP.put("CCTV10", "CCTV-10 科教");
        CHANNEL_NAME_MAP.put("CCTV-10", "CCTV-10 科教");
        CHANNEL_NAME_MAP.put("CCTV11", "CCTV-11 戏曲");
        CHANNEL_NAME_MAP.put("CCTV-11", "CCTV-11 戏曲");
        CHANNEL_NAME_MAP.put("CCTV12", "CCTV-12 社会与法");
        CHANNEL_NAME_MAP.put("CCTV-12", "CCTV-12 社会与法");
        CHANNEL_NAME_MAP.put("CCTV13", "CCTV-13 新闻");
        CHANNEL_NAME_MAP.put("CCTV-13", "CCTV-13 新闻");
        CHANNEL_NAME_MAP.put("CCTV14", "CCTV-14 少儿");
        CHANNEL_NAME_MAP.put("CCTV-14", "CCTV-14 少儿");
        CHANNEL_NAME_MAP.put("CCTV15", "CCTV-15 音乐");
        CHANNEL_NAME_MAP.put("CCTV-15", "CCTV-15 音乐");
        CHANNEL_NAME_MAP.put("CCTV16", "CCTV-16 奥林匹克");
        CHANNEL_NAME_MAP.put("CCTV-16", "CCTV-16 奥林匹克");
        CHANNEL_NAME_MAP.put("CCTV17", "CCTV-17 农业农村");
        CHANNEL_NAME_MAP.put("CCTV-17", "CCTV-17 农业农村");
    }


    private FrameLayout root;
    private WebView bridgeWebView;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private FrameLayout loadingOverlay;
    private GestureTraceView gestureTraceView;
    private TextView overlayText;
    private TextView statusText;
    private LinearLayout menuPanel;
    private TextView menuHeader;
    private ListView channelListView;
    private ChannelAdapter channelAdapter;
    private List<String> menuGroups = new ArrayList<>();
    private Map<String, List<Channel>> groupChannelMap = new LinkedHashMap<>();
    private int selectedGroupIndex = 0;
    private SharedPreferences preferences;

    private String preferredQuality = "fhd";
    private String activeRequestId = "";
    private int requestCounter = 0;
    private int currentIndex = 0;
    private int menuSelection = 0;
    private int settingsSelection = 0;
    private int menuPage = MENU_PAGE_CHANNELS;
    private boolean autoStartOnBoot = true;
    private int bridgeAttempts = 0;
    private boolean channelsLoaded = false;
    private String playbackMode = PLAYBACK_MODE_HW;
    private int touchSlop;
    private final StringBuilder numberBuffer = new StringBuilder();
    private RuyiApi ruyiApi;
    private CenterModule centerModule;
    private TextView ruyiStatusText;
    private String ruyiStatusStr = "Ruyi: connecting...";
    private boolean ruyiBanned = false;
    private String ruyiBlockedReason = "";

    private void applyBlockedState(boolean blocked, String reason) {
        if (blocked == ruyiBanned) return;
        ruyiBanned = blocked;
        ruyiBlockedReason = reason == null ? "" : reason;
        Log.i(TAG, "ruyi_blocked_state changed blocked=" + blocked + " reason=" + reason);
        if (blocked) {
            if (bridgeWebView != null) {
                bridgeWebView.stopLoading();
                bridgeWebView.loadUrl("about:blank");
            }
            if (exoPlayer != null) {
                exoPlayer.stop();
            }
            if (overlayText != null) {
                overlayText.setBackgroundColor(Color.parseColor("#CC000000"));
                overlayText.setTextColor(Color.RED);
                overlayText.setTextSize(28);
                overlayText.setGravity(Gravity.CENTER);
                String msg;
                if ("ban".equals(reason)) {
                    msg = "\n\n\n\n\n\n\n\n\n\n设备已被封禁，无法使用\n\n";
                } else if ("app_closed".equals(reason)) {
                    msg = "\n\n\n\n\n\n\n\n\n\n应用已关闭，请稍后再试\n\n";
                } else {
                    msg = "\n\n\n\n\n\n\n\n\n\n服务暂时不可用\n\n";
                }
                overlayText.setText(msg);
            }
        } else {
            if (bridgeWebView != null) {
                bridgeWebView.loadUrl(YSP_HOME_URL);
            }
            if (channelsLoaded && exoPlayer != null) {
                playCurrentWithExo();
            }
            if (overlayText != null) {
                overlayText.setText("");
                overlayText.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private final Runnable ruyiHeartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (ruyiApi != null) {
                ruyiApi.heartbeat(new RuyiApi.RuyiCallback() {
                    @Override
                    public void onResult(final RuyiApi.RuyiResult result) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (result.isSuccess()) {
                                    Log.i(TAG, "ruyi_heartbeat_ok vip=" + ruyiApi.getVipTime());
                                    if (ruyiBanned) {
                                        Log.i(TAG, "ruyi_heartbeat_recovered from blocked state");
                                        applyBlockedState(false, null);
                                    }
                                } else {
                                    Log.w(TAG, "ruyi_heartbeat_fail code=" + result.code);
                                    if (result.code == 114) {
                                        applyBlockedState(true, "ban");
                                    } else if (result.code == 102) {
                                        applyBlockedState(true, "app_closed");
                                    }
                                }
                                updateRuyiStatus();
                            }
                        });
                    }
                });
            }
            handler.postDelayed(this, 60000);
        }
    };

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
        autoStartOnBoot = preferences.getBoolean(PREF_AUTO_START, true);

        buildUi();
        // setupProtocolBridge(); // DISABLED: using M3U + ExoPlayer instead
        initExoPlayer();
        loadM3uList();
        initRuyi();
        centerModule = new CenterModule(this, ruyiApi, root);
        showOverlay("加载中...", false);
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

        ruyiStatusText = new TextView(this);
        ruyiStatusText.setVisibility(View.GONE);
        ruyiStatusText.setTextColor(0xFF00FF88);
        ruyiStatusText.setTextSize(14);
        ruyiStatusText.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        ruyiStatusText.setBackgroundColor(0xCC000000);
        ruyiStatusText.setPadding(dp(12), dp(4), dp(12), dp(4));
        ruyiStatusText.setText(ruyiStatusStr);
        FrameLayout.LayoutParams ruyiParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.RIGHT);
        ruyiParams.topMargin = dp(8);
        ruyiParams.rightMargin = dp(8);
        root.addView(ruyiStatusText, ruyiParams);

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
        menuHeader.setText("频道列表");
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
        channelListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (menuPanel.getVisibility() == View.VISIBLE && event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                    resetMenuAutoHide();
                }
                return false;
            }
        });
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

    private void initExoPlayer() {
        playerView = new PlayerView(this);
        playerView.setBackgroundColor(Color.BLACK);
        playerView.setUseController(false);
        playerView.setFocusable(false);
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        root.addView(playerView, 0, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.i(TAG, "exo_state_changed state=" + playbackState);
                if (playbackState == Player.STATE_READY) {
                    if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
                        loadingOverlay.setVisibility(View.GONE);
                    }
                    overlayText.setVisibility(View.GONE);
                    handler.removeCallbacks(hideOverlayRunnable);
                    updateStatus();
                } else if (playbackState == Player.STATE_BUFFERING) {
                    showPlaybackOverlay();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "exo_player_error", error);
                showCenterMessage("线路暂时维护中...", 4000);
            }
        });
    }

    private void loadM3uList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(M3U_URL).openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    int code = conn.getResponseCode();
                    Log.i(TAG, "m3u_http_code=" + code);
                    if (code != 200) {
                        throw new IOException("HTTP " + code);
                    }
                    InputStream is = conn.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = is.read(buf)) != -1) {
                        baos.write(buf, 0, r);
                    }
                    is.close();
                    final String content = new String(baos.toByteArray(), Charset.forName("UTF-8"));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            parseM3u8(content);
                        }
                    });
                } catch (final Exception e) {
                    Log.e(TAG, "m3u_load_failed", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showOverlay("频道列表加载失败: " + e.getMessage(), false);
                        }
                    });
                }
            }
        }).start();
    }

    private void parseM3u8(String content) {
        List<Channel> parsed = new ArrayList<>();
        String currentGroup = "默认";
        String currentName = null;
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                if (trimmed.startsWith("#EXTINF")) {
                    int gtIdx = trimmed.indexOf("group-title=\"");
                    if (gtIdx >= 0) {
                        int start = gtIdx + "group-title=\"".length();
                        int end = trimmed.indexOf("\"", start);
                        if (end > start) {
                            currentGroup = trimmed.substring(start, end);
                        }
                    }
                    int commaIdx = trimmed.lastIndexOf(',');
                    if (commaIdx >= 0) {
                        currentName = trimmed.substring(commaIdx + 1).trim();
                    }
                }
                continue;
            }
            if (trimmed.startsWith("http") && currentName != null) {
                parsed.add(new Channel(currentName, trimmed, currentGroup));
                currentName = null;
            }
        }
        Log.i(TAG, "m3u_parsed count=" + parsed.size());
        if (parsed.isEmpty()) {
            showOverlay("M3U 解析无频道", false);
            return;
        }
        channels.clear();
        channels.addAll(parsed);
        channelsLoaded = true;

        groupChannelMap.clear();
        menuGroups.clear();
        for (Channel ch : parsed) {
            String g = (ch.group != null && ch.group.length() > 0) ? ch.group : "默认";
            if (!groupChannelMap.containsKey(g)) {
                groupChannelMap.put(g, new ArrayList<Channel>());
                menuGroups.add(g);
            }
            groupChannelMap.get(g).add(ch);
        }
        Log.i(TAG, "m3u_groups count=" + menuGroups.size());

        String savedPid = preferences.getString(PREF_CHANNEL_PID, "");
        currentIndex = findChannelIndexByPid(savedPid);
        if (currentIndex < 0 || currentIndex >= channels.size()) {
            currentIndex = 0;
        }
        menuSelection = currentIndex;

        Channel curCh = channels.get(currentIndex);
        String curGroup = (curCh.group != null && curCh.group.length() > 0) ? curCh.group : "默认";
        selectedGroupIndex = menuGroups.indexOf(curGroup);
        if (selectedGroupIndex < 0) selectedGroupIndex = 0;

        channelAdapter.notifyDataSetChanged();
        updateMenuHeader();
        playCurrentWithExo();
        detectBadChannels();
    }

    private void playCurrentWithExo() {
        if (channels.isEmpty() || exoPlayer == null) return;
        Channel ch = channels.get(currentIndex);
        preferences.edit().putString(PREF_CHANNEL_PID, String.valueOf(currentIndex)).apply();
        showPlaybackOverlay();
        updateStatus();
        Log.i(TAG, "exo_play index=" + currentIndex + " name=" + ch.name + " url=" + ch.streamUrl);
        String url = ch.streamUrl;
        if (url == null || url.isEmpty()) return;
        androidx.media3.datasource.DefaultHttpDataSource.Factory httpFactory =
                new androidx.media3.datasource.DefaultHttpDataSource.Factory()
                        .setConnectTimeoutMs(15000)
                        .setReadTimeoutMs(15000)
                        .setAllowCrossProtocolRedirects(true)
                        .setUserAgent("Mozilla/5.0 (Linux; Android 14; TV) AppleWebKit/537.36 Chrome/120.0 Mobile");
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        androidx.media3.exoplayer.hls.HlsMediaSource.Factory hlsFactory =
                new androidx.media3.exoplayer.hls.HlsMediaSource.Factory(httpFactory);
        androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory progFactory =
                new androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(httpFactory);
        androidx.media3.exoplayer.source.MediaSource source;
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mkv") || lowerUrl.endsWith(".avi")
                || lowerUrl.endsWith(".flv") || lowerUrl.endsWith(".webm") || lowerUrl.endsWith(".mov")) {
            source = progFactory.createMediaSource(mediaItem);
            Log.i(TAG, "exo_source=progressive url=" + url);
        } else {
            source = hlsFactory.createMediaSource(mediaItem);
            Log.i(TAG, "exo_source=hls url=" + url);
        }
        exoPlayer.setMediaSource(source);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    private void detectBadChannels() {
        final int total = channels.size();
        Log.i(TAG, "channel_detect start total=" + total);
        new Thread(new Runnable() {
            @Override
            public void run() {
                java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(12);
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(total);
                int checkedCount = 0;
                for (int i = 0; i < total; i++) {
                    final int idx = i;
                    final Channel ch = channels.get(idx);
                    if (ch.checked) { latch.countDown(); continue; }
                    if (i == currentIndex) {
                        ch.checked = true;
                        latch.countDown();
                        continue;
                    }
                    checkedCount++;
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HttpURLConnection conn = (HttpURLConnection) new URL(ch.streamUrl).openConnection();
                                conn.setConnectTimeout(5000);
                                conn.setReadTimeout(5000);
                                conn.setRequestMethod("HEAD");
                                conn.setInstanceFollowRedirects(true);
                                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                                int code = conn.getResponseCode();
                                conn.disconnect();
                                if (code == 405) {
                                    HttpURLConnection conn2 = (HttpURLConnection) new URL(ch.streamUrl).openConnection();
                                    conn2.setConnectTimeout(5000);
                                    conn2.setReadTimeout(5000);
                                    conn2.setRequestMethod("GET");
                                    conn2.setInstanceFollowRedirects(true);
                                    conn2.setRequestProperty("User-Agent", "Mozilla/5.0");
                                    conn2.setRequestProperty("Range", "bytes=0-0");
                                    code = conn2.getResponseCode();
                                    conn2.disconnect();
                                }
                                if (code >= 400) {
                                    ch.hidden = true;
                                    badUrls.add(ch.streamUrl);
                                    Log.w(TAG, "channel_bad_http idx=" + idx + " code=" + code + " name=" + ch.name);
                                }
                            } catch (Exception e) {
                                ch.hidden = true;
                                badUrls.add(ch.streamUrl);
                                Log.w(TAG, "channel_bad_net idx=" + idx + " err=" + e.getClass().getSimpleName() + " name=" + ch.name);
                            } finally {
                                ch.checked = true;
                                latch.countDown();
                            }
                        }
                    });
                }
                try { latch.await(90, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
                pool.shutdown();
                Log.i(TAG, "channel_detect done checked=" + checkedCount + " bad=" + badUrls.size());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        rebuildVisibleLists();
                    }
                });
            }
        }, "ChannelDetect").start();
    }

    private synchronized void rebuildVisibleLists() {
        List<Channel> visible = new ArrayList<>();
        int hiddenCount = 0;
        for (Channel ch : channels) {
            if (ch.hidden) hiddenCount++;
            else visible.add(ch);
        }
        if (hiddenCount == 0) {
            channelAdapter.notifyDataSetChanged();
            return;
        }
        int wasCurrentUrlIndex = -1;
        String curUrl = "";
        if (currentIndex >= 0 && currentIndex < channels.size()) {
            curUrl = channels.get(currentIndex).streamUrl;
        }
        channels.clear();
        channels.addAll(visible);
        groupChannelMap.clear();
        menuGroups.clear();
        for (Channel ch : visible) {
            String g = (ch.group != null && ch.group.length() > 0) ? ch.group : "默认";
            if (!groupChannelMap.containsKey(g)) {
                groupChannelMap.put(g, new ArrayList<Channel>());
                menuGroups.add(g);
            }
            groupChannelMap.get(g).add(ch);
        }
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).streamUrl.equals(curUrl)) { wasCurrentUrlIndex = i; break; }
        }
        if (wasCurrentUrlIndex < 0) wasCurrentUrlIndex = 0;
        currentIndex = wasCurrentUrlIndex;
        menuSelection = currentIndex;
        if (!channels.isEmpty()) {
            String curGroup = (channels.get(currentIndex).group != null && channels.get(currentIndex).group.length() > 0)
                    ? channels.get(currentIndex).group : "默认";
            selectedGroupIndex = menuGroups.indexOf(curGroup);
            if (selectedGroupIndex < 0) selectedGroupIndex = 0;
        }
        Log.i(TAG, "channel_rebuild hidden=" + hiddenCount + " remain=" + channels.size());
        channelAdapter.notifyDataSetChanged();
        updateMenuHeader();
        if (channels.isEmpty()) {
            showCenterMessage("所有频道均无法播放", 0);
        } else if (currentIndex >= channels.size()) {
            currentIndex = 0;
        }
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
        bridgeWebView.setVisibility(View.INVISIBLE);
        FrameLayout.LayoutParams bridgeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(bridgeWebView, 0, bridgeParams);

        loadingOverlay = new FrameLayout(this);
        loadingOverlay.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(loadingOverlay, 1, loadingParams);

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
                String rawName = object.optString("name");
                String displayName = CHANNEL_NAME_MAP.get(rawName) != null
                        ? CHANNEL_NAME_MAP.get(rawName) : rawName;
                channels.add(new Channel(
                        displayName,
                        object.optString("pid"),
                        object.optString("streamId"),
                        object.optString("type"),
                        object.optBoolean("is4K", false)));
            }
            if (channels.isEmpty()) {
                Log.w(TAG, "channels_loaded count=0");
                showOverlay("暂无可用频道", false);
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
        try {
            int idx = Integer.parseInt(pid.trim());
            if (idx >= 0 && idx < channels.size()) return idx;
        } catch (NumberFormatException ignored) {}
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
        if (channels.isEmpty()) return;
        Channel channel = channels.get(currentIndex);
        if (channel.streamUrl != null && channel.streamUrl.length() > 0) {
            playCurrentWithExo();
            return;
        }
        if (bridgeWebView == null) return;
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
            if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
                loadingOverlay.setVisibility(View.GONE);
                if (bridgeWebView != null) {
                    bridgeWebView.setVisibility(View.VISIBLE);
                }
                Log.i(TAG, "loading_hidden_webview_shown");
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
        if (centerModule != null) centerModule.onChannelChanged();
        int next = currentIndex;
        for (int step = 0; step < channels.size(); step++) {
            next = (next + delta + channels.size()) % channels.size();
            if (!channels.get(next).hidden) break;
        }
        currentIndex = next;
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
        return PLAYBACK_MODE_SW.equals(playbackMode) ? "软解" : "硬解";
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

    private void showCenterMessage(final String msg, long durationMs) {
        handler.removeCallbacks(hideOverlayRunnable);
        overlayText.setTextColor(Color.WHITE);
        overlayText.setTextSize(32);
        overlayText.setText("\n\n" + msg + "\n\n");
        overlayText.setBackgroundColor(0xCC000000);
        overlayText.setGravity(Gravity.CENTER);
        overlayText.setVisibility(View.VISIBLE);
        if (durationMs > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    overlayText.setVisibility(View.GONE);
                    overlayText.setBackgroundColor(Color.TRANSPARENT);
                    overlayText.setTextSize(44);
                    overlayText.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                }
            }, durationMs);
        }
    }

    private void toggleMenu() {
        if (menuPanel.getVisibility() == View.VISIBLE) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    private void showMenu() {
        if (channels.isEmpty()) {
            showOverlay("频道列表加载中...", true);
            return;
        }
        showGroupsMenu();
    }

    private void showGroupsMenu() {
        menuPage = MENU_PAGE_GROUPS;
        menuSelection = selectedGroupIndex;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        channelAdapter.notifyDataSetChanged();
        channelListView.setSelection(menuSelection);
        resetMenuAutoHide();
    }

    private void showGroupChannelsMenu() {
        menuPage = MENU_PAGE_GROUP_CHANNELS;
        String groupName = menuGroups.get(selectedGroupIndex);
        List<Channel> groupChans = groupChannelMap.get(groupName);
        if (groupChans == null || groupChans.isEmpty()) return;
        int groupStartIdx = channels.indexOf(groupChans.get(0));
        menuSelection = groupStartIdx;
        updateMenuHeader();
        channelAdapter.notifyDataSetChanged();
        channelListView.setSelection(0);
        resetMenuAutoHide();
    }

    private void showChannelsMenu() {
        menuPage = MENU_PAGE_CHANNELS;
        menuSelection = currentIndex + 1;
        if (menuSelection > channels.size()) menuSelection = 0;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        channelAdapter.notifyDataSetChanged();
        channelListView.setSelection(menuSelection);
        resetMenuAutoHide();
    }

    private void showSettingsMenu() {
        menuPage = MENU_PAGE_SETTINGS;
        settingsSelection = 0;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        channelAdapter.notifyDataSetChanged();
        channelListView.setSelection(settingsSelection);
        resetMenuAutoHide();
    }

    private void hideMenu() {
        handler.removeCallbacks(menuAutoHideRunnable);
        menuPanel.setVisibility(View.GONE);
        hideSystemUi();
    }

    private void resetMenuAutoHide() {
        handler.removeCallbacks(menuAutoHideRunnable);
        handler.postDelayed(menuAutoHideRunnable, MENU_AUTO_HIDE_MS);
    }

    private void updateMenuHeader() {
        if (menuHeader == null) return;
        if (menuPage == MENU_PAGE_SETTINGS) {
            menuHeader.setText("设置");
        } else if (menuPage == MENU_PAGE_GROUPS) {
            menuHeader.setText("频道分类  共 " + menuGroups.size() + " 类  " + channels.size() + " 频道");
        } else if (menuPage == MENU_PAGE_GROUP_CHANNELS) {
            String groupName = menuGroups.get(selectedGroupIndex);
            int sz = groupChannelMap.containsKey(groupName) ? groupChannelMap.get(groupName).size() : 0;
            menuHeader.setText(groupName + "  (" + sz + ")\u2003\u2003按←返回");
        } else {
            menuHeader.setText("频道  " + channels.size());
        }
    }

    private void moveMenuSelection(int delta) {
        if (menuPage == MENU_PAGE_SETTINGS) {
            settingsSelection = (settingsSelection + delta + SETTINGS_ITEM_COUNT) % SETTINGS_ITEM_COUNT;
            channelListView.setSelection(settingsSelection);
            channelAdapter.notifyDataSetChanged();
            resetMenuAutoHide();
            return;
        }
        if (menuPage == MENU_PAGE_GROUPS) {
            int total = menuGroups.size() + 2;
            if (total <= 0) return;
            menuSelection = (menuSelection + delta + total) % total;
            selectedGroupIndex = menuSelection;
            if (selectedGroupIndex >= menuGroups.size()) selectedGroupIndex = 0;
            channelListView.setSelection(menuSelection);
            channelAdapter.notifyDataSetChanged();
            resetMenuAutoHide();
            return;
        }
        if (menuPage == MENU_PAGE_GROUP_CHANNELS) {
            String groupName = menuGroups.get(selectedGroupIndex);
            List<Channel> groupChans = groupChannelMap.get(groupName);
            int total = groupChans.size();
            if (total <= 0) return;
            int groupStartIdx = channels.indexOf(groupChans.get(0));
            int localPos = menuSelection - groupStartIdx;
            localPos = (localPos + delta + total) % total;
            menuSelection = groupStartIdx + localPos;
            channelListView.setSelection(localPos);
            channelAdapter.notifyDataSetChanged();
            resetMenuAutoHide();
            return;
        }
        int total = channels.size() + 1;
        if (total <= 0) return;
        menuSelection = (menuSelection + delta + total) % total;
        channelListView.setSelection(menuSelection);
        channelAdapter.notifyDataSetChanged();
        resetMenuAutoHide();
    }

    private void togglePlaybackMode() {
        playbackMode = PLAYBACK_MODE_SW.equals(playbackMode) ? PLAYBACK_MODE_HW : PLAYBACK_MODE_SW;
        preferences.edit().putString(PREF_PLAYBACK_MODE, playbackMode).apply();
        applyPlaybackModeToWebView();
        channelAdapter.notifyDataSetChanged();
    }

    private void toggleAutoStart() {
        autoStartOnBoot = !autoStartOnBoot;
        preferences.edit().putBoolean(PREF_AUTO_START, autoStartOnBoot).apply();
        channelAdapter.notifyDataSetChanged();
    }

    private void selectMenuChannel() {
        if (channels.isEmpty()) return;
        int realIndex = menuSelection;
        if (menuPage == MENU_PAGE_CHANNELS) realIndex = menuSelection - 1;
        if (realIndex < 0 || realIndex >= channels.size()) return;
        currentIndex = realIndex;
        Channel playedCh = channels.get(currentIndex);
        Log.i(TAG, "select_play absIdx=" + currentIndex + " name=" + playedCh.name + " url=" + playedCh.streamUrl);
        String playedGroup = (playedCh.group != null && playedCh.group.length() > 0) ? playedCh.group : "默认";
        selectedGroupIndex = menuGroups.indexOf(playedGroup);
        if (selectedGroupIndex < 0) selectedGroupIndex = 0;
        requestCurrentStream();
        hideMenu();
    }

    private void selectMenuItemAt(int position) {
        if (menuPage == MENU_PAGE_SETTINGS) {
            if (position == SETTINGS_IDX_DECODER) togglePlaybackMode();
            else if (position == SETTINGS_IDX_AUTOSTART) toggleAutoStart();
            return;
        }
        if (menuPage == MENU_PAGE_GROUPS) {
            if (position < menuGroups.size()) {
                selectedGroupIndex = position;
                showGroupChannelsMenu();
            } else if (position == menuGroups.size()) {
                showSettingsMenu();
            } else if (position == menuGroups.size() + 1) {
                hideMenu();
            }
            return;
        }
        if (menuPage == MENU_PAGE_GROUP_CHANNELS) {
            String gn = menuGroups.get(selectedGroupIndex);
            List<Channel> gcs = groupChannelMap.get(gn);
            if (gcs == null || gcs.isEmpty()) return;
            int localPos;
            if (position >= 0 && position < gcs.size()) {
                localPos = position;
            } else {
                localPos = position - channels.indexOf(gcs.get(0));
                if (localPos < 0 || localPos >= gcs.size()) return;
            }
            Channel picked = gcs.get(localPos);
            int absIdx = channels.indexOf(picked);
            menuSelection = absIdx;
            Log.i(TAG, "select_pick localPos=" + localPos + " absIdx=" + absIdx + " name=" + picked.name);
            selectMenuChannel();
            return;
        }
        if (menuPage == MENU_PAGE_CHANNELS) {
            if (position == 0) { showSettingsMenu(); }
            else { menuSelection = position; selectMenuChannel(); }
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
        if (numberBuffer.length() == 0 || channels.isEmpty()) return;
        try {
            int oneBased = Integer.parseInt(numberBuffer.toString());
            numberBuffer.setLength(0);
            if (oneBased >= 1 && oneBased <= channels.size()) {
                currentIndex = oneBased - 1;
                menuSelection = currentIndex + 1;
                requestCurrentStream();
            } else {
                showOverlay("Channel out of range", true);
            }
        } catch (NumberFormatException ignored) {
            numberBuffer.setLength(0);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return true;
        int keyCode = event.getKeyCode();
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            appendNumber(keyCode - KeyEvent.KEYCODE_0);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (centerModule != null && centerModule.isVisible()) {
                centerModule.handleKey(keyCode);
                return true;
            }
            if (menuPanel.getVisibility() != View.VISIBLE) {
                if (centerModule != null) {
                    centerModule.toggle();
                    return true;
                }
            }
        }

        if (centerModule != null && centerModule.isVisible()) {
            if (centerModule.handleKey(keyCode)) return true;
        }

        if (menuPanel.getVisibility() == View.VISIBLE) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { moveMenuSelection(-1); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { moveMenuSelection(1); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (menuPage == MENU_PAGE_GROUP_CHANNELS) { showGroupsMenu(); }
                else if (menuPage == MENU_PAGE_SETTINGS) { showGroupsMenu(); }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (menuPage == MENU_PAGE_GROUPS && menuSelection < menuGroups.size()) {
                    selectedGroupIndex = menuSelection;
                    showGroupChannelsMenu();
                } else if (menuPage == MENU_PAGE_SETTINGS) {
                    selectMenuItemAt(settingsSelection);
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                selectMenuItemAt(menuPage == MENU_PAGE_SETTINGS ? settingsSelection : menuSelection);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (menuPage == MENU_PAGE_GROUP_CHANNELS) showGroupsMenu();
                else if (menuPage == MENU_PAGE_SETTINGS) showGroupsMenu();
                else hideMenu();
                return true;
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { changeChannel(-1); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { changeChannel(1); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { if (centerModule != null) centerModule.toggle(); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) { changeQuality(1); return true; }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_MENU) {
            toggleMenu(); return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) { finish(); return true; }
        return super.dispatchKeyEvent(event);
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
        if (centerModule != null && centerModule.isVisible()) {
            if (Math.abs(dx) > Math.abs(dy) && dx < 0) {
                centerModule.hide();
            }
            return;
        }
        if (menuPanel.getVisibility() == View.VISIBLE && startedInMenu) {
            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx < 0) {
                    if (menuPage == MENU_PAGE_GROUP_CHANNELS || menuPage == MENU_PAGE_SETTINGS) showGroupsMenu();
                } else if (dx > 0) {
                    if (menuPage == MENU_PAGE_GROUPS && menuSelection < menuGroups.size()) {
                        selectedGroupIndex = menuSelection;
                        showGroupChannelsMenu();
                    } else if (menuPage == MENU_PAGE_SETTINGS) {
                        selectMenuItemAt(settingsSelection);
                    }
                }
            }
            return;
        }
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx < 0) {
                if (centerModule != null) centerModule.toggle();
            } else {
                changeQuality(1);
            }
        } else {
            changeChannel(dy > 0 ? 1 : -1);
        }
    }

    private boolean isPointInsideMenu(float x, float y) {
        return menuPanel.getVisibility() == View.VISIBLE
                && x >= menuPanel.getLeft() && x <= menuPanel.getRight()
                && y >= menuPanel.getTop() && y <= menuPanel.getBottom();
    }

    private int pointToMenuPosition(float x, float y) {
        Rect rect = getListRectInRoot();
        if (rect == null || !rect.contains((int) x, (int) y)) return -1;
        return channelListView.pointToPosition((int) (x - rect.left), (int) (y - rect.top));
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

    private String getDeviceId() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId == null || androidId.length() == 0) {
            androidId = "dev_" + Build.MODEL.replaceAll("[^a-zA-Z0-9]", "_");
        }
        return "tv_" + androidId.substring(0, Math.min(16, androidId.length()));
    }

    private void initRuyi() {
        String deviceId = getDeviceId();
        Log.i(TAG, "ruyi_init deviceId=" + deviceId);
        ruyiApi = new RuyiApi(this, deviceId);
        ruyiApi.autoRegisterOrLogin(new RuyiApi.RuyiCallback() {
            @Override
            public void onResult(final RuyiApi.RuyiResult result) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (result.isSuccess()) {
                            Log.i(TAG, "ruyi_login_ok vip=" + ruyiApi.getVipTime() + " user=" + ruyiApi.getUsername());
                            ruyiStatusStr = "Ruyi: OK | User: " + ruyiApi.getUsername()
                                    + " | VIP: " + (ruyiApi.isVip() ? "Yes" : "No")
                                    + " | Dev: " + ruyiApi.getDeviceId();
                            applyBlockedState(false, null);
                            checkAppVersion();
                        } else {
                            Log.w(TAG, "ruyi_login_fail code=" + result.code + " msg=" + result.message);
                            ruyiStatusStr = "Ruyi: code=" + result.code + " msg=" + result.message;
                            if (result.code == 114) {
                                applyBlockedState(true, "ban");
                            } else if (result.code == 102) {
                                applyBlockedState(true, "app_closed");
                            }
                        }
                        updateRuyiStatus();
                    }
                });
            }
        });
        handler.postDelayed(ruyiHeartbeatRunnable, 60000);
    }

    private void checkAppVersion() {
        try {
            String currentVer = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            ruyiApi.checkVersion(currentVer, new RuyiApi.VersionCallback() {
                @Override
                public void onVersionResult(final RuyiApi.VersionInfo info) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (info.hasUpdate) {
                                showVersionDialog(info);
                            } else {
                                Log.i(TAG, "version_check_no_update current=" + currentVer + " remote=" + info.remoteVersion);
                            }
                        }
                    });
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "checkAppVersion error: " + e.getMessage());
        }
    }

    private void showVersionDialog(final RuyiApi.VersionInfo info) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "showVersionDialog hasUpdate=true url=" + info.updateUrl + " compel=" + info.compel);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("发现新版本 v" + info.remoteVersion);
                    StringBuilder msg = new StringBuilder();
                    msg.append("当前版本: ").append(getVersionNameShort()).append("\n");
                    msg.append("最新版本: ").append(info.remoteVersion).append("\n\n");
                    if (info.updateNotes != null && info.updateNotes.length() > 0) {
                        msg.append("更新内容:\n").append(info.updateNotes).append("\n\n");
                    }
                    msg.append("是否立即下载并更新?");
                    builder.setMessage(msg.toString());
                    builder.setPositiveButton("立即更新", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            dialog.dismiss();
                            downloadAndInstall(info);
                        }
                    });
                    if (!info.compel) {
                        builder.setNegativeButton("稍后再说", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                    builder.setCancelable(!info.compel);
                    builder.show();
                } catch (Exception e) {
                    Log.w(TAG, "showVersionDialog error: " + e.getMessage());
                }
            }
        });
    }

    private String getVersionNameShort() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "?";
        }
    }

    private void downloadAndInstall(final RuyiApi.VersionInfo info) {
        if (info.updateUrl == null || info.updateUrl.length() == 0) {
            Log.w(TAG, "updateUrl is empty, cannot download");
            return;
        }
        if (overlayText != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (overlayText != null) {
                        overlayText.setText("\n\n\n\n\n\n\n\n\n\n正在下载新版本，请稍候...\n");
                        overlayText.setBackgroundColor(Color.parseColor("#CC000000"));
                        overlayText.setTextColor(Color.WHITE);
                        overlayText.setGravity(Gravity.CENTER);
                    }
                }
            });
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                File outputFile = null;
                try {
                    URL url = new URL(info.updateUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        throw new IOException("HTTP " + responseCode);
                    }
                    int contentLength = conn.getContentLength();
                    File downloadDir;
                    if (Build.VERSION.SDK_INT >= 29) {
                        downloadDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "apk_updates");
                    } else {
                        downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "apk_updates");
                    }
                    if (!downloadDir.exists()) downloadDir.mkdirs();
                    outputFile = new File(downloadDir, "update_" + System.currentTimeMillis() + ".apk");
                    bis = new BufferedInputStream(conn.getInputStream());
                    fos = new FileOutputStream(outputFile);
                    byte[] buf = new byte[8192];
                    long total = 0;
                    long startTime = System.currentTimeMillis();
                    while (true) {
                        int read = bis.read(buf);
                        if (read == -1) break;
                        fos.write(buf, 0, read);
                        total += read;
                        long elapsed = System.currentTimeMillis() - startTime;
                        if (elapsed > 500) {
                            final int pct = contentLength > 0 ? (int)(total * 100 / contentLength) : -1;
                            final long kbDone = total / 1024;
                            final long kbTotal = contentLength / 1024;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (overlayText != null) {
                                        String text = "\n\n\n\n\n\n\n\n\n\n下载中... " + kbDone + "KB";
                                        if (pct >= 0) text += " (" + pct + "%)";
                                        overlayText.setText(text + "\n");
                                    }
                                }
                            });
                            startTime = System.currentTimeMillis();
                        }
                    }
                    fos.flush();
                    Log.i(TAG, "APK downloaded size=" + total + " file=" + outputFile.getAbsolutePath());
                    final File apkFile = outputFile;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (overlayText != null) {
                                overlayText.setText("");
                                overlayText.setBackgroundColor(Color.TRANSPARENT);
                            }
                            showInstallConfirmDialog(apkFile);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "download failed: " + e.getMessage());
                    final String errMsg = e.getMessage();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (overlayText != null) {
                                overlayText.setText("");
                                overlayText.setBackgroundColor(Color.TRANSPARENT);
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("下载失败");
                            builder.setMessage("无法下载更新包: " + errMsg + "\n\n是否改用浏览器下载?");
                            builder.setPositiveButton("浏览器下载", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(info.updateUrl)));
                                    } catch (Exception ex) {
                                        Log.w(TAG, "open browser failed: " + ex.getMessage());
                                    }
                                }
                            });
                            builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    });
                } finally {
                    try { if (fos != null) fos.close(); } catch (Exception ignored) {}
                    try { if (bis != null) bis.close(); } catch (Exception ignored) {}
                    try { if (conn != null) conn.disconnect(); } catch (Exception ignored) {}
                }
            }
        }).start();
    }

    private void showInstallConfirmDialog(final File apkFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("下载完成");
        builder.setMessage("新版本已下载就绪，是否立即安装?");
        builder.setPositiveButton("立即安装", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.dismiss();
                installApk(apkFile);
            }
        });
        builder.setNegativeButton("稍后安装", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    private void installApk(File apkFile) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            startActivity(intent);
            Log.i(TAG, "installApk launched uri=" + apkUri);
        } catch (Exception e) {
            Log.e(TAG, "installApk error: " + e.getMessage());
        }
    }

    private void updateRuyiStatus() {
        if (ruyiStatusText != null) {
            if (ruyiApi != null && ruyiApi.isRegistered()) {
                ruyiStatusStr = "Ruyi: OK | " + ruyiApi.getUsername()
                        + " | VIP:" + (ruyiApi.isVip() ? "Yes" : "No");
            }
            ruyiStatusText.setText(ruyiStatusStr);
            ruyiStatusText.setVisibility(View.VISIBLE);
            handler.removeCallbacks(hideRuyiStatusRunnable);
            handler.postDelayed(hideRuyiStatusRunnable, 7000);
        }
    }

    private final Runnable hideRuyiStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (ruyiStatusText != null) {
                ruyiStatusText.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (centerModule != null) {
            centerModule.destroy();
            centerModule = null;
        }
        handler.removeCallbacksAndMessages(null);
        if (bridgeWebView != null) {
            bridgeWebView.stopLoading();
            bridgeWebView.loadUrl("about:blank");
            bridgeWebView.removeJavascriptInterface("YspAndroid");
            bridgeWebView.destroy();
            bridgeWebView = null;
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
            playerView = null;
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
        final String streamUrl;
        final String group;
        final String pid;
        final String streamId;
        volatile boolean hidden = false;
        volatile boolean checked = false;

        Channel(String name, String streamUrl, String group) {
            this.name = name;
            this.streamUrl = streamUrl;
            this.group = group;
            this.pid = "";
            this.streamId = "";
        }

        Channel(String name, String pid, String streamId, String type, boolean is4K) {
            this.name = name;
            this.streamUrl = "";
            this.group = "";
            this.pid = pid;
            this.streamId = streamId;
        }
    }

    private final class ChannelAdapter extends BaseAdapter {
        private final Context context;

        ChannelAdapter(Context context) { this.context = context; }

        @Override
        public int getCount() {
            if (menuPage == MENU_PAGE_SETTINGS) return SETTINGS_ITEM_COUNT;
            if (menuPage == MENU_PAGE_GROUPS) return menuGroups.size() + 2;
            if (menuPage == MENU_PAGE_GROUP_CHANNELS) {
                String g = menuGroups.get(selectedGroupIndex);
                List<Channel> chans = groupChannelMap.get(g);
                return chans != null ? chans.size() : 0;
            }
            return channels.size() + 1;
        }

        @Override
        public Object getItem(int position) { return position; }

        @Override
        public long getItemId(int position) { return position; }

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
            int curIdxForBg = -1;

            if (menuPage == MENU_PAGE_SETTINGS) {
                if (position == SETTINGS_IDX_DECODER) {
                    textView.setText("解码器模式    " + playbackModeLabel());
                } else {
                    textView.setText("开机自启       " + (autoStartOnBoot ? "开" : "关"));
                }
                selected = position == settingsSelection;
            } else if (menuPage == MENU_PAGE_GROUPS) {
                if (position < menuGroups.size()) {
                    String g = menuGroups.get(position);
                    int sz = groupChannelMap.containsKey(g) ? groupChannelMap.get(g).size() : 0;
                    String arrow = (position == selectedGroupIndex) ? "▶ " : "    ";
                    textView.setText(arrow + g + "  (" + sz + ")");
                    textView.setTextColor(position == selectedGroupIndex ? Color.WHITE : 0xFFBBBBBB);
                } else if (position == menuGroups.size()) {
                    textView.setText("      ⚙ 设置");
                } else {
                    textView.setText("      关闭菜单");
                }
                selected = position == menuSelection;
            } else if (menuPage == MENU_PAGE_GROUP_CHANNELS) {
                String g = menuGroups.get(selectedGroupIndex);
                List<Channel> groupChans = groupChannelMap.get(g);
                Channel ch = groupChans.get(position);
                textView.setText("  " + ch.name);
                int absIdx = channels.indexOf(ch);
                curIdxForBg = absIdx;
                selected = (menuSelection == absIdx);
                if (selected) textView.setTextColor(Color.WHITE);
                else textView.setTextColor(0xFFBBBBBB);
            } else {
                if (position == 0) {
                    textView.setText("设置");
                } else {
                    Channel channel = channels.get(position - 1);
                    String groupLabel = (channel.group != null && channel.group.length() > 0) ? channel.group : "";
                    textView.setText(position + ". " + channel.name + (groupLabel.length() > 0 ? "  " + groupLabel : ""));
                }
                selected = position == menuSelection;
            }

            if (selected) {
                textView.setBackgroundColor(0xFF1D6FFF);
            } else if (menuPage == MENU_PAGE_GROUP_CHANNELS && curIdxForBg == currentIndex) {
                textView.setBackgroundColor(0x66333333);
            } else if (menuPage == MENU_PAGE_CHANNELS && position == currentIndex + 1) {
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