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
import android.text.style.ForegroundColorSpan;
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
import java.util.LinkedHashSet;
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
    private static final String M3U_URL = "http://ry.400239.com/ruyi/interface.m3u";
    private static final String PREFS = "yangshipin_tv";
    private static final String PREF_CHANNEL_PID = "channel_pid";
    private static final String PREF_PLAYBACK_MODE = "playback_mode";
    private static final String PREF_AUTO_START = "auto_start_on_boot";
    private static final String PLAYBACK_MODE_HW = "hw";
    private static final String PLAYBACK_MODE_SW = "sw";
    private static final String PLAYBACK_HELP_TEXT = "按上下键换台，按OK键打开频道列表，按右键切换线路";
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
                boolean switched = switchToNextLine(true);
                if (switched) {
                    Log.i(TAG, "auto_switch_line on_player_error");
                } else {
                    showCenterMessage("线路暂时维护中...", 4000);
                }
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

    private String normalizeChannelName(String raw) {
        if (raw == null) return "";
        String n = raw.trim();
        n = n.replaceAll("^\\[[^\\]]*\\]\\s*", "");
        n = n.replaceAll("[\\(（][^\\)）]*[\\)）]", "");
        n = n.replaceAll("\\s*[-_\\s]\\s*(高清|超清|蓝光|流畅|原画|极速|标清|原画|低码|高码)\\b", "");
        n = n.replaceAll("\\b(高清|超清|蓝光|流畅|原画|极速|标清)\\b\\s*[-_]?\\s*", "");
        n = n.replaceAll("\\s*[-_]\\s*(HD|FHD|4K|SD|LD|原画)\\b", "");
        n = n.replaceAll("\\b(HD|FHD|4K|SD|LD)\\b\\s*[-_]?\\s*", "");
        n = n.replaceAll("\\s*[-_]\\s*\\d+[号号频道]?\\s*$", "");
        n = n.replaceAll("\\s+", " ").trim();
        java.util.regex.Matcher cctv = java.util.regex.Pattern.compile("(?i)^(CCTV)\\s*[-_\\s]*(\\d+).*").matcher(n);
        if (cctv.matches()) {
            n = cctv.group(1).toUpperCase() + "-" + cctv.group(2);
        }
        return n;
    }

    private String canonicalDisplayName(String normalized) {
        if (CHANNEL_NAME_MAP.containsKey(normalized)) return CHANNEL_NAME_MAP.get(normalized);
        return normalized;
    }

    private String rewriteGroup(String rawGroup, String channelName) {
        if (rawGroup == null) rawGroup = "默认";
        if (rawGroup.equals("更新时间")) return null;

        if (channelName != null && channelName.contains("卫视")) return "卫视";

        if (rawGroup.equals("央视频道")) return "央视频";
        if (rawGroup.equals("卫视频道")) return "卫视";

        if (rawGroup.equals("地方频道")) {
            String prov = findProvinceByChannelName(channelName);
            if (prov != null) return prov;
            return "地方频道";
        }

        if (rawGroup.contains("景观") || rawGroup.contains("风景")) {
            String prov = findProvinceByGroup(rawGroup);
            if (prov != null) return prov;
        }

        String prov = findProvinceByGroup(rawGroup);
        if (prov != null) return prov;

        return rawGroup;
    }

    private static final String[][] CITY_PROVINCE = {
            {"北京", "北京"}, {"天津", "天津"}, {"河北", "河北"}, {"山西", "山西"},
            {"内蒙古", "内蒙古"}, {"辽宁", "辽宁"}, {"吉林", "吉林"}, {"黑龙江", "黑龙江"},
            {"上海", "上海"}, {"江苏", "江苏"}, {"浙江", "浙江"}, {"安徽", "安徽"},
            {"福建", "福建"}, {"江西", "江西"}, {"山东", "山东"}, {"河南", "河南"},
            {"湖北", "湖北"}, {"湖南", "湖南"}, {"广东", "广东"}, {"广西", "广西"},
            {"海南", "海南"}, {"重庆", "重庆"}, {"四川", "四川"}, {"贵州", "贵州"},
            {"云南", "云南"}, {"西藏", "西藏"}, {"陕西", "陕西"}, {"甘肃", "甘肃"},
            {"青海", "青海"}, {"宁夏", "宁夏"}, {"新疆", "新疆"},
            {"香港", "香港"}, {"澳门", "澳门"}, {"台湾", "台湾"},
            {"兵团", "新疆"}, {"伊犁", "新疆"}, {"可克达拉", "新疆"}, {"奎屯", "新疆"},
            {"石河子", "新疆"}, {"双河", "新疆"}, {"玛纳斯", "新疆"},
            {"青岛", "山东"}, {"烟台", "山东"}, {"济南", "山东"}, {"临沂", "山东"},
            {"泰安", "山东"}, {"威海", "山东"}, {"潍坊", "山东"},
            {"南京", "江苏"}, {"苏州", "江苏"}, {"无锡", "江苏"}, {"常州", "江苏"},
            {"镇江", "江苏"}, {"扬州", "江苏"}, {"泰州", "江苏"}, {"淮安", "江苏"},
            {"盐城", "江苏"}, {"宿迁", "江苏"}, {"徐州", "江苏"}, {"南通", "江苏"},
            {"连云港", "江苏"},
            {"杭州", "浙江"}, {"宁波", "浙江"}, {"温州", "浙江"}, {"嘉兴", "浙江"},
            {"湖州", "浙江"}, {"绍兴", "浙江"}, {"金华", "浙江"}, {"衢州", "浙江"},
            {"舟山", "浙江"}, {"台州", "浙江"}, {"丽水", "浙江"},
            {"海宁", "浙江"}, {"平湖", "浙江"}, {"余姚", "浙江"}, {"慈溪", "浙江"},
            {"上虞", "浙江"}, {"嵊州", "浙江"}, {"新昌", "浙江"}, {"诸暨", "浙江"},
            {"萧山", "浙江"}, {"余杭", "浙江"}, {"东阳", "浙江"}, {"义乌", "浙江"},
            {"兰溪", "浙江"}, {"永康", "浙江"}, {"武义", "浙江"}, {"缙云", "浙江"},
            {"云和", "浙江"}, {"松阳", "浙江"}, {"遂昌", "浙江"}, {"龙泉", "浙江"},
            {"庆元", "浙江"}, {"青田", "浙江"}, {"乐清", "浙江"}, {"永嘉", "浙江"},
            {"苍南", "浙江"}, {"洞头", "浙江"}, {"文成", "浙江"}, {"泰顺", "浙江"},
            {"开化", "浙江"}, {"龙游", "浙江"}, {"衢江", "浙江"}, {"普陀", "浙江"},
            {"嵊泗", "浙江"}, {"象山", "浙江"},
            {"合肥", "安徽"}, {"芜湖", "安徽"}, {"蚌埠", "安徽"}, {"淮南", "安徽"},
            {"马鞍山", "安徽"}, {"淮北", "安徽"}, {"铜陵", "安徽"}, {"安庆", "安徽"},
            {"黄山", "安徽"}, {"滁州", "安徽"}, {"六安", "安徽"}, {"池州", "安徽"},
            {"亳州", "安徽"}, {"宣城", "安徽"}, {"阜阳", "安徽"},
            {"祁门", "安徽"}, {"广德", "安徽"}, {"固镇", "安徽"},
            {"福州", "福建"}, {"厦门", "福建"}, {"泉州", "福建"}, {"漳州", "福建"},
            {"莆田", "福建"}, {"三明", "福建"}, {"南平", "福建"}, {"龙岩", "福建"},
            {"宁德", "福建"}, {"云霄", "福建"},
            {"南昌", "江西"}, {"九江", "江西"}, {"赣州", "江西"}, {"吉安", "江西"},
            {"上饶", "江西"}, {"宜春", "江西"}, {"抚州", "江西"},
            {"郑州", "河南"}, {"开封", "河南"}, {"洛阳", "河南"}, {"平顶山", "河南"},
            {"安阳", "河南"}, {"鹤壁", "河南"}, {"新乡", "河南"}, {"焦作", "河南"},
            {"濮阳", "河南"}, {"许昌", "河南"}, {"漯河", "河南"}, {"三门峡", "河南"},
            {"南阳", "河南"}, {"商丘", "河南"}, {"信阳", "河南"}, {"周口", "河南"},
            {"驻马店", "河南"}, {"荥阳", "河南"}, {"永城", "河南"}, {"新野", "河南"},
            {"武汉", "湖北"}, {"黄石", "湖北"}, {"十堰", "湖北"}, {"宜昌", "湖北"},
            {"襄阳", "湖北"}, {"鄂州", "湖北"}, {"荆门", "湖北"}, {"孝感", "湖北"},
            {"荆州", "湖北"}, {"黄冈", "湖北"}, {"咸宁", "湖北"}, {"随州", "湖北"},
            {"恩施", "湖北"}, {"江夏", "湖北"},
            {"长沙", "湖南"}, {"株洲", "湖南"}, {"湘潭", "湖南"}, {"衡阳", "湖南"},
            {"邵阳", "湖南"}, {"岳阳", "湖南"}, {"常德", "湖南"}, {"张家界", "湖南"},
            {"益阳", "湖南"}, {"郴州", "湖南"}, {"永州", "湖南"}, {"怀化", "湖南"},
            {"娄底", "湖南"},
            {"广州", "广东"}, {"深圳", "广东"}, {"珠海", "广东"}, {"汕头", "广东"},
            {"佛山", "广东"}, {"韶关", "广东"}, {"湛江", "广东"}, {"肇庆", "广东"},
            {"江门", "广东"}, {"茂名", "广东"}, {"惠州", "广东"}, {"梅州", "广东"},
            {"汕尾", "广东"}, {"河源", "广东"}, {"阳江", "广东"}, {"清远", "广东"},
            {"东莞", "广东"}, {"中山", "广东"}, {"潮州", "广东"}, {"揭阳", "广东"},
            {"云浮", "广东"}, {"番禺", "广东"},
            {"南宁", "广西"}, {"柳州", "广西"}, {"桂林", "广西"}, {"梧州", "广西"},
            {"北海", "广西"}, {"防城港", "广西"}, {"钦州", "广西"}, {"贵港", "广西"},
            {"玉林", "广西"}, {"百色", "广西"}, {"贺州", "广西"}, {"河池", "广西"},
            {"来宾", "广西"}, {"崇左", "广西"}, {"灌阳", "广西"},
            {"海口", "海南"}, {"三亚", "海南"},
            {"万州", "重庆"}, {"铜梁", "重庆"}, {"璧山", "重庆"},
            {"成都", "四川"}, {"自贡", "四川"}, {"攀枝花", "四川"}, {"泸州", "四川"},
            {"德阳", "四川"}, {"绵阳", "四川"}, {"广元", "四川"}, {"遂宁", "四川"},
            {"内江", "四川"}, {"乐山", "四川"}, {"南充", "四川"}, {"眉山", "四川"},
            {"宜宾", "四川"}, {"广安", "四川"}, {"达州", "四川"}, {"雅安", "四川"},
            {"巴中", "四川"}, {"资阳", "四川"}, {"阿坝", "四川"}, {"甘孜", "四川"},
            {"凉山", "四川"},
            {"剑阁", "四川"}, {"青川", "四川"}, {"朝天", "四川"}, {"旺苍", "四川"},
            {"夹江", "四川"}, {"井研", "四川"}, {"沐川", "四川"},
            {"仁寿", "四川"}, {"乐至", "四川"}, {"荥经", "四川"}, {"名山", "四川"},
            {"松潘", "四川"}, {"汶川", "四川"}, {"泸县", "四川"}, {"营山", "四川"},
            {"贵阳", "贵州"}, {"六盘水", "贵州"}, {"遵义", "贵州"}, {"安顺", "贵州"},
            {"昆明", "云南"}, {"曲靖", "云南"}, {"玉溪", "云南"}, {"保山", "云南"},
            {"昭通", "云南"}, {"丽江", "云南"}, {"普洱", "云南"}, {"临沧", "云南"},
            {"楚雄", "云南"}, {"红河", "云南"}, {"文山", "云南"}, {"西双版纳", "云南"},
            {"大理", "云南"}, {"德宏", "云南"}, {"怒江", "云南"}, {"迪庆", "云南"},
            {"通海", "云南"}, {"易门", "云南"},
            {"西安", "陕西"}, {"铜川", "陕西"}, {"宝鸡", "陕西"}, {"咸阳", "陕西"},
            {"渭南", "陕西"}, {"延安", "陕西"}, {"汉中", "陕西"}, {"榆林", "陕西"},
            {"安康", "陕西"}, {"商洛", "陕西"},
            {"兰州", "甘肃"}, {"嘉峪关", "甘肃"}, {"金昌", "甘肃"}, {"白银", "甘肃"},
            {"天水", "甘肃"}, {"武威", "甘肃"}, {"张掖", "甘肃"}, {"平凉", "甘肃"},
            {"酒泉", "甘肃"}, {"庆阳", "甘肃"}, {"定西", "甘肃"}, {"陇南", "甘肃"},
            {"临夏", "甘肃"}, {"甘南", "甘肃"},
            {"西峰", "甘肃"}, {"永昌", "甘肃"}, {"天祝", "甘肃"}, {"渭源", "甘肃"},
            {"西宁", "青海"}, {"海东", "青海"},
            {"银川", "宁夏"}, {"石嘴山", "宁夏"}, {"吴忠", "宁夏"}, {"固原", "宁夏"},
            {"中卫", "宁夏"},
            {"呼和浩特", "内蒙古"}, {"包头", "内蒙古"}, {"乌海", "内蒙古"},
            {"赤峰", "内蒙古"}, {"通辽", "内蒙古"}, {"鄂尔多斯", "内蒙古"},
            {"呼伦贝尔", "内蒙古"}, {"巴彦淖尔", "内蒙古"}, {"乌兰察布", "内蒙古"},
            {"兴安盟", "内蒙古"}, {"锡林郭勒", "内蒙古"}, {"阿拉善", "内蒙古"},
            {"长春", "吉林"}, {"吉林", "吉林"}, {"四平", "吉林"}, {"辽源", "吉林"},
            {"通化", "吉林"}, {"白山", "吉林"}, {"松原", "吉林"}, {"白城", "吉林"},
            {"延边", "吉林"},
            {"梅河口", "吉林"}, {"桦甸", "吉林"}, {"舒兰", "吉林"}, {"磐石", "吉林"},
            {"蛟河", "吉林"}, {"德惠", "吉林"}, {"九台", "吉林"}, {"榆树", "吉林"},
            {"农安", "吉林"}, {"东丰", "吉林"}, {"辉南", "吉林"}, {"柳河", "吉林"},
            {"集安", "吉林"}, {"靖宇", "吉林"}, {"长白", "吉林"}, {"抚松", "吉林"},
            {"临江", "吉林"}, {"和龙", "吉林"}, {"敦化", "吉林"}, {"龙井", "吉林"},
            {"图们", "吉林"}, {"汪清", "吉林"},
            {"哈尔滨", "黑龙江"}, {"齐齐哈尔", "黑龙江"}, {"鸡西", "黑龙江"},
            {"鹤岗", "黑龙江"}, {"双鸭山", "黑龙江"}, {"大庆", "黑龙江"},
            {"伊春", "黑龙江"}, {"佳木斯", "黑龙江"}, {"七台河", "黑龙江"},
            {"牡丹江", "黑龙江"}, {"黑河", "黑龙江"}, {"绥化", "黑龙江"},
            {"大兴安岭", "黑龙江"},
            {"甘南", "黑龙江"},
            {"石家庄", "河北"}, {"唐山", "河北"}, {"秦皇岛", "河北"},
            {"邯郸", "河北"}, {"邢台", "河北"}, {"保定", "河北"}, {"张家口", "河北"},
            {"承德", "河北"}, {"沧州", "河北"}, {"廊坊", "河北"}, {"衡水", "河北"},
            {"昌黎", "河北"}, {"滦平", "河北"}, {"平泉", "河北"}, {"兴隆", "河北"},
            {"任丘", "河北"}, {"清河", "河北"},
            {"太原", "山西"}, {"大同", "山西"}, {"阳泉", "山西"}, {"长治", "山西"},
            {"晋城", "山西"}, {"朔州", "山西"}, {"晋中", "山西"}, {"运城", "山西"},
            {"忻州", "山西"}, {"临汾", "山西"}, {"吕梁", "山西"},
            {"平遥", "山西"}, {"太谷", "山西"}, {"定襄", "山西"}, {"汾西", "山西"},
            {"古县", "山西"}, {"长子", "山西"}, {"万荣", "山西"}, {"怀仁", "山西"},
            {"大宁", "山西"},
            {"沈阳", "辽宁"}, {"大连", "辽宁"}, {"鞍山", "辽宁"}, {"抚顺", "辽宁"},
            {"本溪", "辽宁"}, {"丹东", "辽宁"}, {"锦州", "辽宁"}, {"营口", "辽宁"},
            {"阜新", "辽宁"}, {"辽阳", "辽宁"}, {"盘锦", "辽宁"}, {"铁岭", "辽宁"},
            {"朝阳", "辽宁"}, {"葫芦岛", "辽宁"},
            {"宜兴", "江苏"}, {"新沂", "江苏"}, {"涟水", "江苏"}, {"泗洪", "江苏"},
            {"句容", "江苏"}, {"靖江", "江苏"}, {"常熟", "江苏"}, {"武进", "江苏"},
            {"金湖", "江苏"},
            {"滨海", "天津"}, {"津南", "天津"},
            {"双辽", "吉林"}, {"长影", "吉林"},
            {"中国蓝", "浙江"},
            {"香港", "香港"},
    };

    private String findProvinceByChannelName(String channelName) {
        if (channelName == null) return null;
        for (String[] pair : CITY_PROVINCE) {
            if (channelName.contains(pair[0])) return pair[1];
        }
        return null;
    }

    private String findProvinceByGroup(String groupName) {
        if (groupName == null) return null;
        for (String[] pair : CITY_PROVINCE) {
            if (groupName.contains(pair[0])) return pair[1];
        }
        return null;
    }

    private static final String[] PROVINCE_KEYWORDS = {
            "北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江",
            "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南",
            "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州",
            "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆",
            "香港", "澳门", "台湾", "青岛", "南京"
    };

    private static final String[] HOT_FRONT = {
            "央视频", "卫视", "电影频道", "直播中国",
            "央视景观", "体育", "纪录频道", "iPanda", "少儿"
    };

    private static final String[] HOT_BACK = {
            "春晚频道", "B站", "斗鱼", "虎牙"
    };

    private boolean isProvinceGroup(String g) {
        if (g == null) return false;
        for (String k : PROVINCE_KEYWORDS) {
            if (g.contains(k)) return true;
        }
        return false;
    }

    private int frontIndex(String g) {
        for (int i = 0; i < HOT_FRONT.length; i++) {
            if (HOT_FRONT[i].equals(g)) return i;
        }
        return -1;
    }

    private int backIndex(String g) {
        for (int i = 0; i < HOT_BACK.length; i++) {
            if (HOT_BACK[i].equals(g)) return i;
        }
        return -1;
    }

    private void sortMenuGroups(List<String> groups) {
        java.util.Collections.sort(groups, (a, b) -> {
            int fa = frontIndex(a);
            int fb = frontIndex(b);
            if (fa >= 0 && fb >= 0) return Integer.compare(fa, fb);
            if (fa >= 0) return -1;
            if (fb >= 0) return 1;

            boolean pa = isProvinceGroup(a);
            boolean pb = isProvinceGroup(b);
            if (pa && !pb) return -1;
            if (!pa && pb) return 1;

            int ba = backIndex(a);
            int bb = backIndex(b);
            if (ba >= 0 && bb >= 0) return Integer.compare(ba, bb);
            if (ba >= 0) return 1;
            if (bb >= 0) return -1;

            return a.compareTo(b);
        });
    }

    private void parseM3u8(String content) {
        List<Channel> parsed = new ArrayList<>();
        String currentGroup = "默认";
        String currentName = null;
        String[] lines = content.split("\\r?\\n");

        Map<String, Channel> merged = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> groupVotes = new LinkedHashMap<>();
        Map<String, Set<String>> seenUrls = new LinkedHashMap<>();

        int rawEntryCount = 0;
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
                rawEntryCount++;
                String normalized = normalizeChannelName(currentName);
                if (normalized.length() == 0) normalized = currentName;
                String displayName = canonicalDisplayName(normalized);
                String key = displayName;

                String effectiveGroup = rewriteGroup(currentGroup, displayName);
                if (effectiveGroup == null) {
                    currentName = null;
                    continue;
                }

                if (!merged.containsKey(key)) {
                    merged.put(key, new Channel(displayName, effectiveGroup));
                    groupVotes.put(key, new LinkedHashMap<String, Integer>());
                    seenUrls.put(key, new LinkedHashSet<String>());
                }
                Channel ch = merged.get(key);

                if (!groupVotes.get(key).containsKey(effectiveGroup)) {
                    groupVotes.get(key).put(effectiveGroup, 0);
                }
                groupVotes.get(key).put(effectiveGroup, groupVotes.get(key).get(effectiveGroup) + 1);

                if (!seenUrls.get(key).contains(trimmed)) {
                    seenUrls.get(key).add(trimmed);
                    StreamLine sl = new StreamLine(trimmed, effectiveGroup);
                    ch.lines.add(sl);
                }
                currentName = null;
            }
        }

        for (Map.Entry<String, Map<String, Integer>> e : groupVotes.entrySet()) {
            String key = e.getKey();
            Map<String, Integer> votes = e.getValue();
            String bestGroup = "";
            int bestCount = -1;
            for (Map.Entry<String, Integer> ge : votes.entrySet()) {
                if (ge.getValue() > bestCount) {
                    bestCount = ge.getValue();
                    bestGroup = ge.getKey();
                }
            }
            Channel ch = merged.get(key);
            if (ch != null && bestGroup.length() > 0) {
                if (!bestGroup.equals(ch.group)) {
                    merged.remove(key);
                    Channel moved = new Channel(ch.name, bestGroup);
                    moved.lines.addAll(ch.lines);
                    merged.put(key, moved);
                }
            }
        }

        parsed.addAll(merged.values());

        int multiLineCount = 0;
        int totalLines = 0;
        int maxLines = 0;
        for (Channel c : parsed) {
            totalLines += c.lines.size();
            if (c.lines.size() > 1) multiLineCount++;
            if (c.lines.size() > maxLines) maxLines = c.lines.size();
        }
        Log.i(TAG, "m3u_raw_entries=" + rawEntryCount + " unique_channels=" + parsed.size()
                + " merged_multi_line=" + multiLineCount + " total_lines=" + totalLines
                + " max_lines_per_channel=" + maxLines);

        parsed.sort((a, b) -> {
            if (a.lines.size() != b.lines.size()) return b.lines.size() - a.lines.size();
            return a.name.compareTo(b.name);
        });

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
        sortMenuGroups(menuGroups);
        Log.i(TAG, "m3u_groups count=" + menuGroups.size() + " order=" + menuGroups);

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

        if (ch.lines.isEmpty()) {
            Log.w(TAG, "channel_no_lines name=" + ch.name);
            return;
        }

        sortLinesByLatency(ch);

        int lineIdx = Math.min(ch.currentLineIndex, ch.lines.size() - 1);
        if (lineIdx < 0) lineIdx = 0;
        StreamLine line = ch.lines.get(lineIdx);
        String url = line.url;

        String latStr = !line.checked ? "?" : (!line.good ? "TIMEOUT" : line.latencyMs + "ms");
        Log.i(TAG, "exo_play index=" + currentIndex + " name=" + ch.name
                + " line=" + (lineIdx + 1) + "/" + ch.lines.size()
                + " lat=" + latStr
                + " url=" + url);

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
        Log.i(TAG, "channel_detect start async");

        final List<StreamLine> allLines = new ArrayList<>();
        for (Channel ch : channels) {
            for (StreamLine sl : ch.lines) {
                allLines.add(sl);
            }
        }
        if (allLines.isEmpty()) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(16);
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(allLines.size());

                for (final StreamLine sl : allLines) {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                long start = System.currentTimeMillis();
                                HttpURLConnection conn = (HttpURLConnection) new URL(sl.url).openConnection();
                                conn.setConnectTimeout(5000);
                                conn.setReadTimeout(5000);
                                conn.setRequestMethod("HEAD");
                                conn.setInstanceFollowRedirects(true);
                                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                                int code = conn.getResponseCode();
                                conn.disconnect();
                                if (code == 405) {
                                    HttpURLConnection conn2 = (HttpURLConnection) new URL(sl.url).openConnection();
                                    conn2.setConnectTimeout(5000);
                                    conn2.setReadTimeout(5000);
                                    conn2.setRequestMethod("GET");
                                    conn2.setInstanceFollowRedirects(true);
                                    conn2.setRequestProperty("User-Agent", "Mozilla/5.0");
                                    conn2.setRequestProperty("Range", "bytes=0-0");
                                    code = conn2.getResponseCode();
                                    conn2.disconnect();
                                }
                                long latency = System.currentTimeMillis() - start;
                                if (code >= 400) {
                                    sl.good = false;
                                    sl.latencyMs = 99999;
                                    Log.w(TAG, "line_bad_http code=" + code + " url=" + sl.url);
                                } else {
                                    sl.good = true;
                                    sl.latencyMs = latency;
                                    Log.d(TAG, "line_ok lat=" + latency + "ms url=" + sl.url.substring(0, Math.min(60, sl.url.length())));
                                }
                            } catch (Exception e) {
                                sl.good = false;
                                sl.latencyMs = 99999;
                                Log.w(TAG, "line_bad_net err=" + e.getClass().getSimpleName() + " url=" + sl.url.substring(0, Math.min(60, sl.url.length())));
                            } finally {
                                sl.checked = true;
                                latch.countDown();
                            }
                        }
                    });
                }
                try { latch.await(90, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
                pool.shutdown();

                final int[] badLineCount = {0};
                for (Channel ch : channels) {
                    boolean hasGood = false;
                    for (StreamLine sl : ch.lines) {
                        if (sl.good) { hasGood = true; break; }
                    }
                    if (!hasGood) {
                        badLineCount[0]++;
                    }
                }
                Log.i(TAG, "channel_detect done bad_channels=" + badLineCount[0] + " total_channels=" + channels.size());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        channelItemAdapter.notifyDataSetChanged();
                        groupAdapter.notifyDataSetChanged();
                        updateStatus();
                    }
                });
            }
        }, "ChannelDetectAsync").start();
    }

    private synchronized void rebuildVisibleLists() {
        List<Channel> visible = new ArrayList<>();
        int hiddenCount = 0;
        for (Channel ch : channels) {
            boolean allBad = !ch.lines.isEmpty();
            for (StreamLine sl : ch.lines) {
                if (!sl.checked || sl.good) { allBad = false; break; }
            }
            if (allBad) {
                hiddenCount++;
            } else {
                visible.add(ch);
            }
        }
        if (hiddenCount == 0) {
            groupAdapter.notifyDataSetChanged();
            channelItemAdapter.notifyDataSetChanged();
            return;
        }
        String curName = "";
        if (currentIndex >= 0 && currentIndex < channels.size()) {
            curName = channels.get(currentIndex).name;
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
        sortMenuGroups(menuGroups);
        int wasCurrentIndex = 0;
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).name.equals(curName)) { wasCurrentIndex = i; break; }
        }
        currentIndex = wasCurrentIndex;
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
        Log.d(TAG, "onChannelsLoaded_ignored len=" + (json == null ? 0 : json.length()));
    }

    private int findChannelIndexByPid(String pid) {
        if (pid == null || pid.length() == 0) {
            return -1;
        }
        try {
            int idx = Integer.parseInt(pid.trim());
            if (idx >= 0 && idx < channels.size()) return idx;
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    private void requestCurrentStream() {
        requestCurrentStream("channel");
    }

    private void requestCurrentStream(String reason) {
        if (channels.isEmpty()) return;
        playCurrentWithExo();
    }

    private void onPlaybackResult(String requestId, String json) {
        Log.d(TAG, "onPlaybackResult_ignored id=" + requestId);
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
            Channel nc = channels.get(next);
            boolean allBad = !nc.lines.isEmpty();
            for (StreamLine sl : nc.lines) {
                if (!sl.checked || sl.good) { allBad = false; break; }
            }
            if (!allBad) break;
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

    private void switchLine(int delta) {
        if (channels.isEmpty()) return;
        Channel ch = channels.get(currentIndex);
        if (ch.lines.size() <= 1) {
            showOverlay("当前频道只有1条线路", true);
            return;
        }
        sortLinesByLatency(ch);
        ch.currentLineIndex = (ch.currentLineIndex + delta + ch.lines.size()) % ch.lines.size();
        StreamLine line = ch.lines.get(ch.currentLineIndex);
        String prefix = "切换线路: " + (ch.currentLineIndex + 1) + "/" + ch.lines.size() + "  ";
        showOverlay(buildColoredLatencySpan(prefix, line), true);
        Log.i(TAG, "switch_line channel=" + ch.name + " line=" + (ch.currentLineIndex + 1) + "/" + ch.lines.size());
        playCurrentWithExo();
    }

    private boolean switchToNextLine(boolean fromError) {
        if (channels.isEmpty()) return false;
        Channel ch = channels.get(currentIndex);
        if (ch.lines.size() <= 1) return false;

        int startIdx = ch.currentLineIndex;
        sortLinesByLatency(ch);

        for (int i = 1; i <= ch.lines.size(); i++) {
            int candidate = (startIdx + i) % ch.lines.size();
            StreamLine line = ch.lines.get(candidate);
            if (!line.checked || line.good) {
                ch.currentLineIndex = candidate;
                Log.i(TAG, "auto_switch_line fromError=" + fromError + " newIdx=" + candidate
                        + " url=" + line.url.substring(0, Math.min(60, line.url.length())));
                playCurrentWithExo();
                return true;
            }
        }
        return false;
    }

    private void sortLinesByLatency(Channel ch) {
        if (ch.lines.size() < 2) return;
        try {
            java.util.Collections.sort(ch.lines, new java.util.Comparator<StreamLine>() {
                @Override
                public int compare(StreamLine a, StreamLine b) {
                    boolean aBad = a.checked && !a.good;
                    boolean bBad = b.checked && !b.good;
                    if (aBad != bBad) return aBad ? 1 : -1;
                    if (a.latencyMs < 0 && b.latencyMs < 0) return 0;
                    if (a.latencyMs < 0) return 1;
                    if (b.latencyMs < 0) return -1;
                    return Long.compare(a.latencyMs, b.latencyMs);
                }
            });
            int newIdx = 0;
            for (int i = 0; i < ch.lines.size(); i++) {
                if (ch.lines.get(i).url.equals(ch.lines.get(ch.currentLineIndex).url)) {
                    newIdx = i;
                    break;
                }
            }
            ch.currentLineIndex = newIdx;
        } catch (Exception ignored) {}
    }

    private String playbackModeLabel() {
        return PLAYBACK_MODE_SW.equals(playbackMode) ? "软解" : "硬解";
    }

    private int latencyColor(long ms, boolean good) {
        if (!good) return Color.parseColor("#B71C1C");
        if (ms <= 50) return Color.parseColor("#00E676");
        if (ms <= 120) return Color.parseColor("#4CAF50");
        if (ms <= 250) return Color.parseColor("#FFEB3B");
        if (ms <= 500) return Color.parseColor("#FF9800");
        if (ms <= 1000) return Color.parseColor("#FF5252");
        return Color.parseColor("#D32F2F");
    }

    private String latencyText(StreamLine sl) {
        if (sl == null) return "—";
        if (!sl.checked) return "检测中...";
        if (!sl.good) return "超时维护中";
        return sl.latencyMs + "ms";
    }

    private CharSequence buildColoredLatencySpan(String prefix, StreamLine sl) {
        String text = prefix + latencyText(sl);
        SpannableString ss = new SpannableString(text);
        if (sl != null && sl.checked) {
            int start = prefix.length();
            int color = latencyColor(sl.latencyMs, sl.good);
            ss.setSpan(new ForegroundColorSpan(color), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (sl != null && !sl.checked) {
            int start = prefix.length();
            ss.setSpan(new ForegroundColorSpan(0xFF888888), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    private CharSequence buildColoredLineInfo(Channel ch, boolean includeChannelName) {
        StringBuilder sb = new StringBuilder();
        if (includeChannelName) {
            sb.append(ch.name);
            sb.append("  ");
        }
        if (!ch.lines.isEmpty()) {
            StreamLine sl = ch.lines.get(Math.min(ch.currentLineIndex, ch.lines.size() - 1));
            sb.append("线路").append(ch.currentLineIndex + 1).append("/").append(ch.lines.size()).append("  ");
            String prefix = sb.toString();
            return buildColoredLatencySpan(prefix, sl);
        }
        return sb.toString();
    }

    private void updateStatus() {
        if (channels.isEmpty()) {
            statusText.setText("");
            return;
        }
        statusText.setText(buildColoredLineInfo(channels.get(currentIndex), true));
    }

    private void showPlaybackOverlay() {
        if (channels.isEmpty()) {
            return;
        }
        Channel ch = channels.get(currentIndex);
        CharSequence title = buildColoredLineInfo(ch, true);
        String text = title.toString() + "\n" + PLAYBACK_HELP_TEXT;
        SpannableString overlay = new SpannableString(text);
        overlay.setSpan(new RelativeSizeSpan(0.45f), title.length() + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (title instanceof SpannableString) {
            SpannableString titleSS = (SpannableString) title;
            ForegroundColorSpan[] fcs = titleSS.getSpans(0, titleSS.length(), ForegroundColorSpan.class);
            for (ForegroundColorSpan span : fcs) {
                int s = titleSS.getSpanStart(span);
                int e = titleSS.getSpanEnd(span);
                overlay.setSpan(new ForegroundColorSpan(span.getForegroundColor()), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
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
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) { switchLine(1); return true; }
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
                switchLine(1);
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

    private static final class StreamLine {
        final String url;
        final String source;
        volatile boolean good = false;
        volatile boolean checked = false;
        volatile long latencyMs = -1;

        StreamLine(String url, String source) {
            this.url = url;
            this.source = source;
        }
    }

    private static final class Channel {
        final String name;
        final String group;
        final List<StreamLine> lines = new ArrayList<>();
        int currentLineIndex = 0;

        Channel(String name, String group) {
            this.name = name;
            this.group = group;
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

            TextView latencyView = new TextView(MainActivity.this);
            latencyView.setTextSize(12);
            latencyView.setTextColor(Color.GRAY);
            latencyView.setGravity(Gravity.CENTER);
            latencyView.setSingleLine(true);
            int latW = dp(70);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(latW, ViewGroup.LayoutParams.WRAP_CONTENT);
            latencyView.setLayoutParams(llp);

            container.addView(numView);
            container.addView(nameView);
            container.addView(latencyView);
            return new VH(container, numView, nameView, latencyView);
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

            if (!ch.lines.isEmpty()) {
                StreamLine best = ch.lines.get(0);
                if (!best.checked) {
                    holder.latencyView.setText("...");
                    holder.latencyView.setTextColor(0xFF888888);
                } else if (!best.good) {
                    holder.latencyView.setText("超时维护中");
                    holder.latencyView.setTextColor(Color.parseColor("#B71C1C"));
                } else {
                    holder.latencyView.setText(best.latencyMs + "ms");
                    holder.latencyView.setTextColor(latencyColor(best.latencyMs, true));
                }
            } else {
                holder.latencyView.setText("—");
                holder.latencyView.setTextColor(0xFF555555);
            }

            GradientDrawable bg = (GradientDrawable) holder.container.getBackground();
            if (position == selectedChannelInGroup && !focusOnGroupSide) {
                bg.setColor(0xFF1D6FFF);
                bg.setStroke(0, 0);
                holder.nameView.setTextColor(Color.WHITE);
                holder.numView.setTextColor(0xFFFFFFFF);
                holder.latencyView.setTextColor(Color.WHITE);
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
            TextView latencyView;
            VH(LinearLayout c, TextView n, TextView na, TextView la) {
                super(c);
                container = c;
                numView = n;
                nameView = na;
                latencyView = la;
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