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
import android.graphics.drawable.GradientDrawable;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;

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
    private RecyclerView groupRecyclerView;
    private RecyclerView channelRecyclerView;
    private GroupAdapter groupAdapter;
    private ChannelItemAdapter channelItemAdapter;
    private final List<String> menuGroups = new ArrayList<>();
    private final Map<String, List<Channel>> groupChannelMap = new LinkedHashMap<>();
    private int selectedGroupIndex = 0;
    private int selectedChannelInGroup = 0;
    private boolean focusOnGroupSide = true;
    private SharedPreferences preferences;

    private String preferredQuality = "fhd";
    private String activeRequestId = "";
    private int requestCounter = 0;
    private int currentIndex = 0;
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
        installTlsCompatIfNeeded();
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

        gestureTraceView = new GestureTraceView(this);
        root.addView(gestureTraceView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        buildMenu();
        setContentView(root);
        hideSystemUi();
    }

    private void buildMenu() {
        menuPanel = new LinearLayout(this);
        menuPanel.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setShape(GradientDrawable.RECTANGLE);
        panelBg.setColor(0xE6101010);
        panelBg.setCornerRadius(dp(12));
        panelBg.setStroke(0, 0);
        menuPanel.setBackground(panelBg);
        menuPanel.setPadding(dp(12), dp(12), dp(12), dp(12));
        menuPanel.setVisibility(View.GONE);

        menuHeader = new TextView(this);
        menuHeader.setTextColor(Color.WHITE);
        menuHeader.setTextSize(20);
        menuHeader.setText("频道列表");
        menuHeader.setGravity(Gravity.CENTER_VERTICAL);
        menuHeader.setPadding(dp(8), dp(8), dp(8), dp(8));
        menuPanel.addView(menuHeader, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(44)));

        LinearLayout bodyLayout = new LinearLayout(this);
        bodyLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1);
        menuPanel.addView(bodyLayout, bodyParams);

        groupRecyclerView = new RecyclerView(this);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        groupAdapter = new GroupAdapter();
        groupRecyclerView.setAdapter(groupAdapter);
        groupRecyclerView.setFocusable(true);
        groupRecyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        groupRecyclerView.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(
                dp(130),
                ViewGroup.LayoutParams.MATCH_PARENT);
        groupParams.rightMargin = dp(8);
        bodyLayout.addView(groupRecyclerView, groupParams);

        View divider = new View(this);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                dp(1),
                ViewGroup.LayoutParams.MATCH_PARENT);
        divider.setBackgroundColor(0x33FFFFFF);
        bodyLayout.addView(divider, divParams);

        channelRecyclerView = new RecyclerView(this);
        channelRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        channelItemAdapter = new ChannelItemAdapter();
        channelRecyclerView.setAdapter(channelItemAdapter);
        channelRecyclerView.setFocusable(true);
        channelRecyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        channelRecyclerView.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams channelParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1);
        channelParams.leftMargin = dp(8);
        bodyLayout.addView(channelRecyclerView, channelParams);

        groupRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (menuPanel.getVisibility() == View.VISIBLE && event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                    resetMenuAutoHide();
                }
                return false;
            }
        });
        channelRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (menuPanel.getVisibility() == View.VISIBLE && event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                    resetMenuAutoHide();
                }
                return false;
            }
        });

        int menuWidth = Math.min(dp(640), (int) (getResources().getDisplayMetrics().widthPixels * 0.88f));
        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
                menuWidth,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.LEFT);
        menuParams.leftMargin = dp(12);
        menuParams.topMargin = dp(12);
        menuParams.bottomMargin = dp(12);
        root.addView(menuPanel, menuParams);
    }

    private void installTlsCompatIfNeeded() {
        if (Build.VERSION.SDK_INT >= 22) return;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new SecureRandom());
            SSLSocketFactory factory = new TlsCompatSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(factory);
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            if (Build.VERSION.SDK_INT >= 16) {
                javax.net.ssl.SSLContext.getInstance("TLSv1.2");
            }
            Log.i(TAG, "tls_compat_installed sdk=" + Build.VERSION.SDK_INT);
        } catch (Exception e) {
            Log.w(TAG, "tls_compat_fail: " + e.getMessage());
        }
    }

    private static class TlsCompatSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;
        TlsCompatSocketFactory(SSLSocketFactory d) { this.delegate = d; }
        private SSLSocket patch(Socket s) {
            if (s instanceof SSLSocket) {
                try { ((SSLSocket) s).setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.1", "TLSv1"}); }
                catch (Exception ignored) {}
            }
            return (SSLSocket) s;
        }
        @Override public String[] getDefaultCipherSuites() { return delegate.getDefaultCipherSuites(); }
        @Override public String[] getSupportedCipherSuites() { return delegate.getSupportedCipherSuites(); }
        @Override public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException { return patch(delegate.createSocket(s, host, port, autoClose)); }
        @Override public Socket createSocket(String host, int port) throws IOException { return patch(delegate.createSocket(host, port)); }
        @Override public Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws IOException { return patch(delegate.createSocket(host, port, localHost, localPort)); }
        @Override public Socket createSocket(java.net.InetAddress host, int port) throws IOException { return patch(delegate.createSocket(host, port)); }
        @Override public Socket createSocket(java.net.InetAddress address, int port, java.net.InetAddress localAddress, int localPort) throws IOException { return patch(delegate.createSocket(address, port, localAddress, localPort)); }
    }

    private void initExoPlayer() {
        playerView = new PlayerView(this);
        playerView.setBackgroundColor(Color.BLACK);
        playerView.setUseController(false);
        playerView.setFocusable(false);

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        renderersFactory.setEnableDecoderFallback(true);
        if (Build.VERSION.SDK_INT < 21) {
            renderersFactory.setMediaCodecSelector(MediaCodecSelector.DEFAULT);
            Log.i(TAG, "exo_use_default_codec_selector sdk=" + Build.VERSION.SDK_INT);
        }

        exoPlayer = new ExoPlayer.Builder(this)
                .setRenderersFactory(renderersFactory)
                .build();
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

        Channel curCh = channels.get(currentIndex);
        String curGroup = (curCh.group != null && curCh.group.length() > 0) ? curCh.group : "默认";
        selectedGroupIndex = menuGroups.indexOf(curGroup);
        if (selectedGroupIndex < 0) selectedGroupIndex = 0;

        selectedChannelInGroup = findChannelIndexInGroup(selectedGroupIndex, curCh);

        groupAdapter.notifyDataSetChanged();
        channelItemAdapter.notifyDataSetChanged();
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
            groupAdapter.notifyDataSetChanged();
            channelItemAdapter.notifyDataSetChanged();
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
        if (!channels.isEmpty()) {
            String curGroup = (channels.get(currentIndex).group != null && channels.get(currentIndex).group.length() > 0)
                    ? channels.get(currentIndex).group : "默认";
            selectedGroupIndex = menuGroups.indexOf(curGroup);
            if (selectedGroupIndex < 0) selectedGroupIndex = 0;
            selectedChannelInGroup = findChannelIndexInGroup(selectedGroupIndex, channels.get(currentIndex));
        }
        Log.i(TAG, "channel_rebuild hidden=" + hiddenCount + " remain=" + channels.size());
        groupAdapter.notifyDataSetChanged();
        channelItemAdapter.notifyDataSetChanged();
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
            Channel ch0 = channels.get(currentIndex);
            String g0 = (ch0.group != null && ch0.group.length() > 0) ? ch0.group : "默认";
            selectedGroupIndex = menuGroups.indexOf(g0);
            if (selectedGroupIndex < 0) selectedGroupIndex = 0;
            selectedChannelInGroup = findChannelIndexInGroup(selectedGroupIndex, ch0);
            groupAdapter.notifyDataSetChanged();
            channelItemAdapter.notifyDataSetChanged();
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
        Channel curCh = channels.get(currentIndex);
        String curGroup = (curCh.group != null && curCh.group.length() > 0) ? curCh.group : "默认";
        selectedGroupIndex = menuGroups.indexOf(curGroup);
        if (selectedGroupIndex < 0) selectedGroupIndex = 0;
        selectedChannelInGroup = findChannelIndexInGroup(selectedGroupIndex, curCh);
        groupAdapter.notifyDataSetChanged();
        channelItemAdapter.notifyDataSetChanged();
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
        Log.d(TAG, "toggleMenu called vis=" + (menuPanel.getVisibility() == View.VISIBLE));
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
        Log.d(TAG, "showGroupsMenu grpSize=" + menuGroups.size() + " selGrp=" + selectedGroupIndex);
        if (menuGroups.size() > 0 && selectedGroupIndex >= menuGroups.size()) {
            selectedGroupIndex = 0;
        }
        focusOnGroupSide = true;
        menuPage = MENU_PAGE_GROUPS;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        groupAdapter.notifyDataSetChanged();
        channelItemAdapter.notifyDataSetChanged();
        scrollGroupToPosition(selectedGroupIndex);
        syncRightPanel();
        groupRecyclerView.requestFocus();
        resetMenuAutoHide();
    }

    private void showGroupChannelsMenu() {
        if (menuGroups.size() > 0 && selectedGroupIndex >= menuGroups.size()) {
            selectedGroupIndex = 0;
        }
        menuPage = MENU_PAGE_GROUP_CHANNELS;
        focusOnGroupSide = false;
        updateMenuHeader();
        menuPanel.setVisibility(View.VISIBLE);
        groupAdapter.notifyDataSetChanged();
        channelItemAdapter.notifyDataSetChanged();
        scrollGroupToPosition(selectedGroupIndex);
        scrollChannelToPosition(selectedChannelInGroup);
        channelRecyclerView.requestFocus();
        resetMenuAutoHide();
    }

    private void showSettingsMenu() {
        menuPage = MENU_PAGE_SETTINGS;
        updateMenuHeader();
        handler.removeCallbacks(menuAutoHideRunnable);

        final String[] items = new String[]{
                "解码器模式  " + playbackModeLabel(),
                "开机自启    " + (autoStartOnBoot ? "开" : "关"),
                "关闭"
        };

        ListView listView = new ListView(this);
        final android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, items) {
            @Override
            public android.view.View getView(int position, android.view.View convertView,
                                             android.view.ViewGroup parent) {
                android.widget.TextView tv = (android.widget.TextView) super.getView(position, convertView, parent);
                tv.setTextSize(16);
                tv.setPadding(dp(16), dp(12), dp(16), dp(12));
                return tv;
            }
        };
        listView.setAdapter(adapter);

        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("设置")
                .setView(listView)
                .setCancelable(true)
                .setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(android.content.DialogInterface di) {
                        menuPage = MENU_PAGE_GROUPS;
                        groupRecyclerView.requestFocus();
                        resetMenuAutoHide();
                    }
                })
                .create();

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view,
                                    int position, long id) {
                if (position == 0) {
                    togglePlaybackMode();
                    items[0] = "解码器模式  " + playbackModeLabel();
                    adapter.notifyDataSetChanged();
                } else if (position == 1) {
                    toggleAutoStart();
                    items[1] = "开机自启    " + (autoStartOnBoot ? "开" : "关");
                    adapter.notifyDataSetChanged();
                } else if (position == 2) {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void hideMenu() {
        Log.d(TAG, "hideMenu");
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
        } else {
            String g = (selectedGroupIndex >= 0 && selectedGroupIndex < menuGroups.size())
                    ? menuGroups.get(selectedGroupIndex) : "全部";
            int sz = groupChannelMap.containsKey(g) ? groupChannelMap.get(g).size() : 0;
            menuHeader.setText(g + " (" + sz + ")  |  共 " + channels.size() + " 频道");
        }
    }

    private void syncRightPanel() {
        channelItemAdapter.notifyDataSetChanged();
        scrollChannelToPosition(selectedChannelInGroup);
    }

    private void onGroupSelected(int groupIdx) {
        selectedGroupIndex = groupIdx;
        selectedChannelInGroup = 0;
        groupAdapter.notifyDataSetChanged();
        syncRightPanel();
        updateMenuHeader();
        resetMenuAutoHide();
    }

    private void onGroupItemClicked(int position) {
        Log.d(TAG, "onGroupItemClicked pos=" + position + " totalGroups=" + menuGroups.size());
        if (position < menuGroups.size()) {
            onGroupSelected(position);
            focusOnGroupSide = false;
            channelRecyclerView.requestFocus();
        } else if (position == menuGroups.size()) {
            showSettingsMenu();
        } else if (position == menuGroups.size() + 1) {
            hideMenu();
        }
    }

    private void onChannelClicked(int position) {
        Channel picked = getCurrentGroupChannels().get(position);
        int absIdx = channels.indexOf(picked);
        if (absIdx < 0) {
            absIdx = 0;
        }
        currentIndex = absIdx;
        selectedChannelInGroup = position;
        Log.i(TAG, "select_play channel=" + picked.name + " absIdx=" + absIdx);
        Channel playedCh = channels.get(currentIndex);
        String playedGroup = (playedCh.group != null && playedCh.group.length() > 0) ? playedCh.group : "默认";
        selectedGroupIndex = menuGroups.indexOf(playedGroup);
        if (selectedGroupIndex < 0) selectedGroupIndex = 0;
        requestCurrentStream();
        groupAdapter.notifyDataSetChanged();
        channelItemAdapter.notifyDataSetChanged();
        hideMenu();
    }

    private void togglePlaybackMode() {
        playbackMode = PLAYBACK_MODE_SW.equals(playbackMode) ? PLAYBACK_MODE_HW : PLAYBACK_MODE_SW;
        preferences.edit().putString(PREF_PLAYBACK_MODE, playbackMode).apply();
        applyPlaybackModeToWebView();
    }

    private void toggleAutoStart() {
        autoStartOnBoot = !autoStartOnBoot;
        preferences.edit().putBoolean(PREF_AUTO_START, autoStartOnBoot).apply();
    }

    private List<Channel> getCurrentGroupChannels() {
        if (selectedGroupIndex < 0 || selectedGroupIndex >= menuGroups.size()) {
            return new ArrayList<>();
        }
        String g = menuGroups.get(selectedGroupIndex);
        List<Channel> chans = groupChannelMap.get(g);
        return chans != null ? chans : new ArrayList<Channel>();
    }

    private int findChannelIndexInGroup(int groupIndex, Channel target) {
        if (groupIndex < 0 || groupIndex >= menuGroups.size() || target == null) return 0;
        List<Channel> chans = groupChannelMap.get(menuGroups.get(groupIndex));
        if (chans == null) return 0;
        for (int i = 0; i < chans.size(); i++) {
            if (chans.get(i) == target || target.name.equals(chans.get(i).name)) {
                return i;
            }
        }
        return 0;
    }

    private void scrollGroupToPosition(int position) {
        if (groupRecyclerView == null) return;
        if (position < 0) position = 0;
        int total = menuGroups.size() + 2;
        if (position >= total) position = total - 1;
        if (groupRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            ((LinearLayoutManager) groupRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, dp(4));
        } else {
            groupRecyclerView.scrollToPosition(position);
        }
    }

    private void scrollChannelToPosition(int position) {
        if (channelRecyclerView == null) return;
        List<Channel> chans = getCurrentGroupChannels();
        if (position < 0) position = 0;
        if (position >= chans.size()) position = Math.max(0, chans.size() - 1);
        if (channelRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            ((LinearLayoutManager) channelRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, dp(4));
        } else {
            channelRecyclerView.scrollToPosition(position);
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
                Channel ch = channels.get(currentIndex);
                String g = (ch.group != null && ch.group.length() > 0) ? ch.group : "默认";
                selectedGroupIndex = menuGroups.indexOf(g);
                if (selectedGroupIndex < 0) selectedGroupIndex = 0;
                selectedChannelInGroup = findChannelIndexInGroup(selectedGroupIndex, ch);
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
        Log.d(TAG, "dispatchKey key=" + keyCode + "(" + KeyEvent.keyCodeToString(keyCode)
                + ") menuVis=" + (menuPanel.getVisibility() == View.VISIBLE)
                + " focusOnGroup=" + focusOnGroupSide
                + " selGrp=" + selectedGroupIndex
                + " selChInGrp=" + selectedChannelInGroup);
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
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (focusOnGroupSide) moveGroupSelection(-1);
                else moveChannelSelection(-1);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (focusOnGroupSide) moveGroupSelection(1);
                else moveChannelSelection(1);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (!focusOnGroupSide) {
                    focusOnGroupSide = true;
                    groupRecyclerView.requestFocus();
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (focusOnGroupSide) {
                    if (selectedGroupIndex < menuGroups.size()) {
                        focusOnGroupSide = false;
                        channelRecyclerView.requestFocus();
                    } else {
                        onGroupItemClicked(selectedGroupIndex);
                    }
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (focusOnGroupSide) {
                    onGroupItemClicked(selectedGroupIndex);
                } else {
                    List<Channel> chans = getCurrentGroupChannels();
                    if (selectedChannelInGroup >= 0 && selectedChannelInGroup < chans.size()) {
                        onChannelClicked(selectedChannelInGroup);
                    }
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hideMenu();
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

    private void moveGroupSelection(int delta) {
        int adapterTotal = menuGroups.size() + 2;
        if (adapterTotal <= 0) return;
        int oldIdx = selectedGroupIndex;
        selectedGroupIndex = (selectedGroupIndex + delta + adapterTotal) % adapterTotal;

        boolean isGroup = selectedGroupIndex < menuGroups.size();
        if (isGroup && (oldIdx >= menuGroups.size() || !menuGroups.get(selectedGroupIndex).equals(
                (oldIdx >= 0 && oldIdx < menuGroups.size()) ? menuGroups.get(oldIdx) : null))) {
            selectedChannelInGroup = 0;
            syncRightPanel();
            updateMenuHeader();
        } else if (!isGroup) {
            channelItemAdapter.notifyDataSetChanged();
        }
        groupAdapter.notifyDataSetChanged();
        scrollGroupToPosition(selectedGroupIndex);
        resetMenuAutoHide();
    }

    private void moveChannelSelection(int delta) {
        List<Channel> chans = getCurrentGroupChannels();
        int total = chans.size();
        if (total <= 0) return;
        selectedChannelInGroup = (selectedChannelInGroup + delta + total) % total;
        scrollChannelToPosition(selectedChannelInGroup);
        channelItemAdapter.notifyDataSetChanged();
        resetMenuAutoHide();
    }

    private void handleTouchTap(float x, float y) {
        Log.d(TAG, "handleTouchTap x=" + (int)x + " y=" + (int)y
                + " menuVis=" + (menuPanel.getVisibility() == View.VISIBLE)
                + " inMenu=" + isPointInsideMenu(x, y));
        if (menuPanel.getVisibility() == View.VISIBLE) {
            if (!isPointInsideMenu(x, y)) {
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
                    focusOnGroupSide = false;
                    channelRecyclerView.requestFocus();
                } else {
                    focusOnGroupSide = true;
                    groupRecyclerView.requestFocus();
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

    private final class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.VH> {

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            TextView tv = new TextView(MainActivity.this);
            tv.setTextSize(19);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setSingleLine(true);
            tv.setFocusable(true);
            tv.setClickable(true);
            int h = dp(44);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, h);
            int innerPad = dp(10);
            tv.setPadding(innerPad, dp(4), innerPad, dp(4));
            tv.setTextColor(Color.WHITE);
            tv.setLayoutParams(lp);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dp(6));
            tv.setBackground(bg);
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(VH holder, final int position) {
            TextView tv = holder.textView;
            int total = menuGroups.size() + 2;
            if (position == total - 2) {
                tv.setText("⚙ 设置");
                tv.setTextColor(0xFFCCCCCC);
            } else if (position == total - 1) {
                tv.setText("✕ 关闭");
                tv.setTextColor(0xFFCCCCCC);
            } else if (position < menuGroups.size()) {
                String g = menuGroups.get(position);
                int sz = groupChannelMap.containsKey(g) ? groupChannelMap.get(g).size() : 0;
                tv.setText(g + " (" + sz + ")");
                tv.setTextColor(0xFFE0E0E0);
            } else {
                tv.setText("—");
                tv.setTextColor(0xFF888888);
            }

            GradientDrawable bg = (GradientDrawable) tv.getBackground();
            if (position == selectedGroupIndex) {
                bg.setColor(0xFF1D6FFF);
                bg.setStroke(0, 0);
                tv.setTextColor(Color.WHITE);
            } else {
                bg.setColor(Color.TRANSPARENT);
                bg.setStroke(0, 0);
            }

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGroupItemClicked(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return menuGroups.size() + 2;
        }

        class VH extends RecyclerView.ViewHolder {
            TextView textView;
            VH(TextView v) {
                super(v);
                textView = v;
            }
        }
    }

    private final class ChannelItemAdapter extends RecyclerView.Adapter<ChannelItemAdapter.VH> {

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            LinearLayout container = new LinearLayout(MainActivity.this);
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setGravity(Gravity.CENTER_VERTICAL);
            container.setFocusable(true);
            container.setClickable(true);
            int h = dp(46);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, h);
            clp.setMargins(dp(4), dp(2), dp(4), dp(2));
            container.setLayoutParams(clp);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dp(6));
            container.setBackground(bg);

            TextView numView = new TextView(MainActivity.this);
            numView.setTextSize(13);
            numView.setTextColor(0xFF999999);
            int numW = dp(36);
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(numW, ViewGroup.LayoutParams.WRAP_CONTENT);
            numView.setLayoutParams(nlp);
            numView.setGravity(Gravity.CENTER);
            numView.setSingleLine(true);

            TextView nameView = new TextView(MainActivity.this);
            nameView.setTextSize(19);
            nameView.setTextColor(Color.WHITE);
            nameView.setSingleLine(true);
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            nameView.setLayoutParams(mlp);

            container.addView(numView);
            container.addView(nameView);
            return new VH(container, numView, nameView);
        }

        @Override
        public void onBindViewHolder(VH holder, final int position) {
            List<Channel> chans = getCurrentGroupChannels();
            if (position < 0 || position >= chans.size()) return;
            Channel ch = chans.get(position);
            int absIdx = channels.indexOf(ch);
            if (absIdx < 0) absIdx = 0;

            holder.numView.setText(String.format("%03d", position + 1));
            holder.nameView.setText(ch.name);

            GradientDrawable bg = (GradientDrawable) holder.container.getBackground();
            if (position == selectedChannelInGroup && !focusOnGroupSide) {
                bg.setColor(0xFF1D6FFF);
                bg.setStroke(0, 0);
                holder.nameView.setTextColor(Color.WHITE);
                holder.numView.setTextColor(0xFFFFFFFF);
            } else if (absIdx == currentIndex) {
                bg.setColor(0x44222222);
                bg.setStroke(0, 0);
                holder.numView.setTextColor(0xFF999999);
                holder.nameView.setTextColor(0xFFE0E0E0);
            } else {
                bg.setColor(Color.TRANSPARENT);
                bg.setStroke(0, 0);
                holder.numView.setTextColor(0xFF999999);
                holder.nameView.setTextColor(Color.WHITE);
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChannelClicked(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return getCurrentGroupChannels().size();
        }

        class VH extends RecyclerView.ViewHolder {
            LinearLayout container;
            TextView numView;
            TextView nameView;
            VH(LinearLayout c, TextView n, TextView na) {
                super(c);
                container = c;
                numView = n;
                nameView = na;
            }
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
                    Log.d(TAG, "touchDOWN x=" + (int)x + " y=" + (int)y + " inMenu=" + isPointInsideMenu(x, y));
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
                            if (focusOnGroupSide && groupRecyclerView != null) {
                                groupRecyclerView.scrollBy(0, (int) -stepDy);
                            } else if (channelRecyclerView != null) {
                                channelRecyclerView.scrollBy(0, (int) -stepDy);
                            }
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