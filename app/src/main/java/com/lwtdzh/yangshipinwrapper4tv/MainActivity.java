package com.lwtdzh.yangshipinwrapper4tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
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
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
import android.view.TextureView;
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
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.FileProvider;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import xyz.doikki.videoplayer.exo.OkHttpDataSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;

import okhttp3.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

public class MainActivity extends Activity {
    static {
        try {
            javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
            ctx.init(null, new javax.net.ssl.TrustManager[]{new javax.net.ssl.X509TrustManager() {
                @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
            }}, new java.security.SecureRandom());
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
                @Override public boolean verify(String hostname, javax.net.ssl.SSLSession session) { return true; }
            });
        } catch (Throwable t) {
            Log.w("YSPTV", "ssl_trust_all_init_failed: " + t.getMessage());
        }
    }
    private static final String TAG = "YSPTV";
    private static final String YSP_HOME_URL = "https://www.yangshipin.cn/tv/home";
    private static final String LIVE_TXT_URL = "http://ry.400239.com/live.txt";
    private static final String M3U_FALLBACK_URL = "http://ry.400239.com/ruyi/interface.m3u";
    private static final String PREFS = "yangshipin_tv";
    private static final String PREF_CHANNEL_PID = "channel_pid";
    private static final String PREF_PLAYBACK_MODE = "playback_mode";
    private static final String PREF_AUTO_START = "auto_start_on_boot";
    private static final String PREF_LAST_REFRESH_DATE = "last_refresh_date";
    private static final String PREF_CACHED_M3U = "cached_m3u";
    private static final String PREF_FIRST_LAUNCH_DONE = "first_launch_done";
    private static final long CHANNEL_REFRESH_INTERVAL_MS = 24L * 60L * 60L * 1000L;
    private static final int SOURCE_DOWNLOAD_TIMEOUT_MS = 15000;
    private static final int LIVE_TXT_DOWNLOAD_TIMEOUT_MS = 10000;
    private static final int MAX_SOURCE_PARALLEL = 3;
    private static final int FIRST_SOURCE_TIMEOUT_MS = 12000;
    private static final String PLAYBACK_MODE_AUTO = "auto";
    private static final String PLAYBACK_MODE_HW = "hw";
    private static final String PLAYBACK_MODE_SW = "sw";
    private static final String PLAYBACK_HELP_TEXT = "按上下键换台，按OK键打开频道列表，按右键切换线路";
    private static final int MENU_PAGE_GROUPS = 2;
    private static final int MENU_PAGE_GROUP_CHANNELS = 3;
    private static final int MENU_PAGE_CHANNELS = 0;
    private static final int MENU_PAGE_SETTINGS = 1;
    private static final int SETTINGS_ITEM_COUNT = 3;
    private static final int SETTINGS_IDX_DECODER = 0;
    private static final int SETTINGS_IDX_REFRESH = 1;
    private static final int SETTINGS_IDX_AUTOSTART = 2;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static boolean sVideoDecodeFailed = false;
    private static boolean sIjkCrashed = false;
    private static final long MENU_AUTO_HIDE_MS = 15000;
    private static final long BLACK_SCREEN_CHECK_DELAY = 4000;
    private boolean mVideoRendered = false;
    private boolean mAudioRendered = false;
    private boolean mBlackscreenDetectInProgress = false;
    private int mIjkPlayerRetryCount = 0;
    private static final int MAX_IJK_RETRY = 2;
    private final Runnable blackscreenCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (mUseIjkPlayer) return;
            if (mVideoRendered) {
                Log.i(TAG, "blackscreen_check_passed video_rendered_ok audio_optional");
                mBlackscreenDetectInProgress = false;
                return;
            }
            if (!mAudioRendered) {
                Log.i(TAG, "blackscreen_abort_no_audio_yet wait_more");
                handler.postDelayed(this, 2000);
                return;
            }
            if (!mVideoRendered) {
                Log.w(TAG, "blackscreen_detected! audio_ok_but_no_video_switch_to_ijk");
                mBlackscreenDetectInProgress = false;
                switchPlayerToIjk();
            } else {
                Log.i(TAG, "blackscreen_check_passed video_rendered_ok");
                mBlackscreenDetectInProgress = false;
            }
        }
    };
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
    private static OkHttpClient sSharedOkClient = null;
    private static OkHttpDataSource.Factory sSharedOkFactory = null;
    private static HlsMediaSource.Factory sSharedHlsFactory = null;
    private static ProgressiveMediaSource.Factory sSharedProgFactory = null;
    private boolean mUseIjkPlayer = false;
    private IjkPlayerHelper mIjkPlayer;
    private android.view.SurfaceView mIjkSurfaceView;
    private FrameLayout loadingOverlay;
    private GestureTraceView gestureTraceView;
    private TextView overlayText;
    private LinearLayout updatePanel;
    private ProgressBar updateProgressBar;
    private TextView updateProgressText;
    private TextView updateCancelBtn;
    private volatile boolean downloadCancelled = false;
    private volatile boolean versionDialogShown = false;
    private volatile boolean downloadingNow = false;
    private TextView statusText;
    private LinearLayout menuPanel;
    private TextView menuHeader;
    private RecyclerView groupRecyclerView;
    private RecyclerView channelRecyclerView;
    // API 19 替代方案：使用 ScrollView + LinearLayout 替代 RecyclerView
    private ViewGroup channelListContainer;
    private LinearLayout channelListInnerLayout;
    private GroupAdapter groupAdapter;
    private ChannelItemAdapter channelItemAdapter;
    private LinearLayout loadingPanel;
    private ProgressBar loadingProgressBar;
    private TextView loadingProgressText;
    private TextView loadingPlayBtn;
    private volatile boolean isIncrementalLoading = false;
    private volatile int loadedSourceCount = 0;
    private volatile int totalSourceCount = 0;
    private final List<String> downloadedM3uBuffer = new ArrayList<String>();
    private final List<String> menuGroups = new ArrayList<>();
    private final Map<String, List<Channel>> groupChannelMap = new LinkedHashMap<>();
    private int selectedGroupIndex = 0;
    private int selectedChannelInGroup = 0;
    private boolean focusOnGroupSide = true;
    private SharedPreferences preferences;

    private String activeRequestId = "";
    private int requestCounter = 0;
    private int currentIndex = 0;
    private int mAutoSwitchFailCount = 0;
    private static final int MAX_AUTO_SWITCH_FAIL = 3;
    private int mPlaybackRecoverCount = 0;
    private static final int MAX_PLAYBACK_RECOVER = 3;
    private int mPlaylistStuckRetryCount = 0;
    private static final int MAX_PLAYLIST_STUCK_RETRY = 10;
    private int mSingleLineRetryCount = 0;
    private static final int MAX_SINGLE_LINE_RETRY = 2;
    private int menuPage = MENU_PAGE_CHANNELS;
    private boolean autoStartOnBoot = false;
    private int bridgeAttempts = 0;
    private boolean channelsLoaded = false;
    private String playbackMode = PLAYBACK_MODE_AUTO;
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
                stopPlayer();
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
            if (channelsLoaded) {
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
                                    if (centerModule != null) centerModule.refreshAccountInfo();
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
        playbackMode = preferences.getString(PREF_PLAYBACK_MODE, PLAYBACK_MODE_AUTO);
        if (!PLAYBACK_MODE_SW.equals(playbackMode) && !PLAYBACK_MODE_AUTO.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_HW;
        }
        applyPlaybackModeToPlayerEngine();
        autoStartOnBoot = preferences.getBoolean(PREF_AUTO_START, false);
        applyBootAutoStartState();

        buildUi();
        // setupProtocolBridge(); // DISABLED: using M3U + ExoPlayer instead
        initExoPlayer();
        loadM3uList();
        initRuyi();
        centerModule = new CenterModule(this, ruyiApi, root);
        boolean firstRun = !preferences.contains(PREF_CACHED_M3U);
        if (!firstRun) {
            showOverlay("加载中...", true);
        }
        updateStatus();
        applyCutoutMode();
    }

    private void applyCutoutMode() {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
                java.lang.reflect.Field f = android.view.WindowManager.LayoutParams.class
                        .getDeclaredField("layoutInDisplayCutoutMode");
                f.setInt(lp, 1);
                getWindow().setAttributes(lp);
                Log.i(TAG, "cutout_mode_short_edges_set");
            }
        } catch (Throwable t) {
            Log.d(TAG, "cutout_mode_not_supported");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            try {
                if (exoPlayer.getPlayWhenReady()) {
                    exoPlayer.setPlayWhenReady(false);
                }
            } catch (Throwable ignored) {}
        }
        if (mIjkPlayer != null) {
            try { mIjkPlayer.pause(); } catch (Throwable ignored) {}
        }
        abandonAudioFocusInternal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        requestAudioFocusInternal();
        if (exoPlayer != null) {
            try {
                if (exoPlayer.getPlaybackState() == Player.STATE_READY) {
                    exoPlayer.setPlayWhenReady(true);
                }
            } catch (Throwable ignored) {}
        }
        if (mIjkPlayer != null) {
            try { mIjkPlayer.start(); } catch (Throwable ignored) {}
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestAudioFocusInternal();
    }

    @Override
    protected void onStop() {
        super.onStop();
        abandonAudioFocusInternal();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.i(TAG, "onTrimMemory level=" + level);
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            if (bridgeWebView != null) {
                try {
                    bridgeWebView.loadUrl("about:blank");
                } catch (Throwable ignored) {}
            }
        }
        if (level >= TRIM_MEMORY_MODERATE && exoPlayer != null) {
            try {
                exoPlayer.setPlayWhenReady(false);
                Log.i(TAG, "onTrimMemory MODERATE paused player");
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "onLowMemory triggered!");
        if (exoPlayer != null) {
            try {
                exoPlayer.stop();
                Log.i(TAG, "onLowMemory stopped exoPlayer");
            } catch (Throwable ignored) {}
        }
        if (bridgeWebView != null) {
            try {
                bridgeWebView.loadUrl("about:blank");
            } catch (Throwable ignored) {}
        }
    }

    private void requestAudioFocusInternal() {
        try {
            if (android.content.Context.AUDIO_SERVICE != null) {
                android.media.AudioManager am = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
                if (am != null) {
                    int result = am.requestAudioFocus(
                            null,
                            android.media.AudioManager.STREAM_MUSIC,
                            android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    Log.i(TAG, "audio_focus_requested result=" + result);
                }
            }
        } catch (Throwable t) {
            Log.d(TAG, "audio_focus_request_failed: " + t.getMessage());
        }
    }

    private void abandonAudioFocusInternal() {
        try {
            android.media.AudioManager am = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
            if (am != null) {
                am.abandonAudioFocus(null);
            }
        } catch (Throwable t) {
            Log.d(TAG, "audio_focus_abandon_failed: " + t.getMessage());
        }
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

        buildUpdatePanel();
        buildLoadingPanel();

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

    private void buildUpdatePanel() {
        updatePanel = new LinearLayout(this);
        updatePanel.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setShape(GradientDrawable.RECTANGLE);
        panelBg.setColor(0xE6000000);
        panelBg.setCornerRadius(dp(12));
        updatePanel.setBackground(panelBg);
        updatePanel.setPadding(dp(18), dp(12), dp(18), dp(14));
        updatePanel.setVisibility(View.GONE);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        updateProgressText = new TextView(this);
        updateProgressText.setTextColor(Color.WHITE);
        updateProgressText.setTextSize(13);
        updateProgressText.setText("正在准备更新...");
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        topRow.addView(updateProgressText, textLp);

        updateCancelBtn = new TextView(this);
        updateCancelBtn.setText("取消");
        updateCancelBtn.setTextColor(0xFFAAAAAA);
        updateCancelBtn.setTextSize(13);
        updateCancelBtn.setPadding(dp(10), dp(2), dp(10), dp(2));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setShape(GradientDrawable.RECTANGLE);
        btnBg.setColor(0x33FFFFFF);
        btnBg.setCornerRadius(dp(6));
        updateCancelBtn.setBackground(btnBg);
        updateCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadCancelled = true;
                hideUpdatePanel();
                Log.i(TAG, "update_download_cancelled_by_user");
            }
        });
        topRow.addView(updateCancelBtn);

        updatePanel.addView(topRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        updateProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        updateProgressBar.setMax(100);
        updateProgressBar.setProgress(0);
        updateProgressBar.setIndeterminate(false);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barLp.topMargin = dp(8);
        barLp.height = dp(6);
        updatePanel.addView(updateProgressBar, barLp);

        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        panelLp.gravity = Gravity.BOTTOM;
        panelLp.leftMargin = dp(16);
        panelLp.rightMargin = dp(16);
        panelLp.bottomMargin = dp(24);
        root.addView(updatePanel, panelLp);
    }

    private void buildLoadingPanel() {
        loadingPanel = new LinearLayout(this);
        loadingPanel.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setShape(GradientDrawable.RECTANGLE);
        panelBg.setColor(0xF2000000);
        panelBg.setCornerRadius(dp(14));
        loadingPanel.setBackground(panelBg);
        loadingPanel.setPadding(dp(28), dp(24), dp(28), dp(24));
        loadingPanel.setVisibility(View.GONE);

        LinearLayout.LayoutParams wrapW = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams matchW = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView title = new TextView(this);
        title.setText("📡 订阅节目加载中");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        loadingPanel.addView(title, matchW);

        TextView subtitle = new TextView(this);
        subtitle.setId(View.generateViewId());
        subtitle.setTextColor(0xFFAAAAAA);
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(6), 0, dp(14));
        subtitle.setText("正在从远程源获取最新频道...");
        loadingPanel.addView(subtitle, matchW);

        loadingProgressBar = null;
        try {
            loadingProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            loadingProgressBar.setMax(100);
            loadingProgressBar.setProgress(0);
            loadingProgressBar.setIndeterminate(false);
            GradientDrawable barBg = new GradientDrawable();
            barBg.setShape(GradientDrawable.RECTANGLE);
            barBg.setColor(0xFF333333);
            barBg.setCornerRadius(dp(3));
            loadingProgressBar.setBackgroundDrawable(barBg);
            GradientDrawable pgFill = new GradientDrawable();
            pgFill.setShape(GradientDrawable.RECTANGLE);
            pgFill.setColor(0xFF1D6FFF);
            pgFill.setCornerRadius(dp(3));
            loadingProgressBar.setProgressDrawable(pgFill);
        } catch (Throwable ignore) {
            loadingProgressBar = null;
        }
        if (loadingProgressBar != null) {
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(6));
            barLp.topMargin = dp(2);
            loadingPanel.addView(loadingProgressBar, barLp);
        }

        loadingProgressText = new TextView(this);
        loadingProgressText.setText("0 / 0 源  ·  首源就绪 0%");
        loadingProgressText.setTextColor(0xFFBBBBBB);
        loadingProgressText.setTextSize(13);
        loadingProgressText.setGravity(Gravity.CENTER);
        loadingProgressText.setPadding(0, dp(10), 0, dp(6));
        loadingPanel.addView(loadingProgressText, matchW);

        loadingPlayBtn = new TextView(this);
        loadingPlayBtn.setText("▶  立即播放首个频道");
        loadingPlayBtn.setTextColor(0xFFFFFFFF);
        loadingPlayBtn.setTextSize(15);
        loadingPlayBtn.setGravity(Gravity.CENTER);
        loadingPlayBtn.setPadding(dp(24), dp(10), dp(24), dp(10));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setShape(GradientDrawable.RECTANGLE);
        btnBg.setColor(0xFF1D6FFF);
        btnBg.setCornerRadius(dp(8));
        loadingPlayBtn.setBackground(btnBg);
        loadingPlayBtn.setVisibility(View.GONE);
        loadingPlayBtn.setClickable(true);
        loadingPlayBtn.setFocusable(true);
        loadingPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (channelsLoaded && !channels.isEmpty()) {
                    Channel firstPlayable = findFirstPlayableChannel();
                    if (firstPlayable != null) {
                        playChannel(firstPlayable);
                    } else {
                        showOverlay("首个频道正在准备中，请稍候...", true);
                    }
                } else {
                    showOverlay("频道列表尚未就绪", true);
                }
            }
        });
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.gravity = Gravity.CENTER;
        btnLp.topMargin = dp(10);
        loadingPanel.addView(loadingPlayBtn, btnLp);

        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        panelLp.width = dp(340);
        root.addView(loadingPanel, panelLp);
    }

    private void showLoadingPanel(String firstChannel) {
        if (loadingProgressBar != null) loadingProgressBar.setIndeterminate(true);
        if (loadingProgressText != null) loadingProgressText.setText("连接远程订阅源...");
        if (loadingPlayBtn != null) loadingPlayBtn.setVisibility(View.GONE);
        if (loadingPanel != null) loadingPanel.setVisibility(View.VISIBLE);
    }

    private void hideLoadingPanel() {
        if (loadingPanel != null) loadingPanel.setVisibility(View.GONE);
        isIncrementalLoading = false;
    }

    private void updateLoadingProgress(int loaded, int total, boolean firstReady) {
        if (loadingProgressBar == null || loadingProgressText == null) return;
        totalSourceCount = total;
        loadedSourceCount = loaded;
        int pct = total > 0 ? (int) (loaded * 100f / total) : 0;
        loadingProgressBar.setIndeterminate(false);
        loadingProgressBar.setProgress(pct);
        String firstLabel = firstReady ? "✓ 首源就绪" : "首源加载中";
        loadingProgressText.setText(loaded + " / " + total + " 源  ·  " + firstLabel + "  " + pct + "%");
        if (firstReady && loadingPlayBtn != null && channelsLoaded && !channels.isEmpty()) {
            loadingPlayBtn.setVisibility(View.VISIBLE);
        }
        if (loaded >= total && total > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideLoadingPanel();
                }
            }, 1500);
        }
    }

    private Channel findFirstPlayableChannel() {
        if (channels == null || channels.isEmpty()) return null;
        for (Channel c : channels) {
            if (c.lines != null && !c.lines.isEmpty()) return c;
        }
        return channels.get(0);
    }

    private void playChannel(Channel ch) {
        if (ch == null || ch.lines == null || ch.lines.isEmpty()) return;
        currentIndex = ch.absIndex;
        if (currentIndex < 0 || currentIndex >= channels.size()) return;
        Channel playedCh = channels.get(currentIndex);
        String playedGroup = (playedCh.group != null && playedCh.group.length() > 0) ? playedCh.group : "默认";
        selectedGroupIndex = menuGroups.indexOf(playedGroup);
        if (selectedGroupIndex < 0) selectedGroupIndex = 0;
        selectedChannelInGroup = findChannelIndexInGroup(selectedGroupIndex, playedCh);
        requestCurrentStream();
    }

    private void showUpdatePanel(String msg) {
        if (updateProgressText != null) updateProgressText.setText(msg);
        if (updateProgressBar != null) {
            updateProgressBar.setProgress(0);
            updateProgressBar.setIndeterminate(true);
        }
        if (updatePanel != null) updatePanel.setVisibility(View.VISIBLE);
        downloadCancelled = false;
    }

    private void updateProgressPanel(int pct, String text) {
        if (updateProgressBar != null) {
            updateProgressBar.setIndeterminate(pct < 0);
            if (pct >= 0) updateProgressBar.setProgress(pct);
        }
        if (updateProgressText != null && text != null) updateProgressText.setText(text);
    }

    private void hideUpdatePanel() {
        if (updatePanel != null) updatePanel.setVisibility(View.GONE);
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
        groupRecyclerView.setItemAnimator(null);
        groupRecyclerView.setHasFixedSize(true);
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

        if (Build.VERSION.SDK_INT < 20) {
            // API 19: 使用 ScrollView + LinearLayout 替代 RecyclerView
            ScrollView scrollView = new ScrollView(this);
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            scrollView.addView(ll, new ScrollView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            // 记录 channelListView 为 scrollView，channelInnerLayout 为 ll
            // 注意：ViewGroup 类型的 channelListContainer 用于引用真正的容器
            channelListContainer = scrollView;
            channelListInnerLayout = ll;
            channelItemAdapter = new ChannelItemAdapter();
            LinearLayout.LayoutParams channelParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1);
            channelParams.leftMargin = dp(8);
            bodyLayout.addView(scrollView, channelParams);
        } else {
            channelRecyclerView = new RecyclerView(this);
            channelRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            channelRecyclerView.setItemAnimator(null);
            ((LinearLayoutManager) channelRecyclerView.getLayoutManager()).setInitialPrefetchItemCount(6);
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
        }

        groupRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (menuPanel.getVisibility() == View.VISIBLE && event.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                    resetMenuAutoHide();
                }
                return false;
            }
        });
        if (Build.VERSION.SDK_INT >= 20 && channelRecyclerView != null) {
            channelRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        forceRebindAllVisibleItems();
                    }
                }
            });
        }
        View channelTouchTarget = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;
        channelTouchTarget.setOnTouchListener(new View.OnTouchListener() {
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
        if (mUseIjkPlayer) {
            if (!IjkPlayerHelper.isAvailable()) {
                Log.w(TAG, "ijk_player_native_not_available_force_exo");
                mUseIjkPlayer = false;
            } else if (root != null) {
                initIjkPlayer();
                return;
            } else {
                Log.i(TAG, "init_ijk_player_deferred_root_not_ready");
                return;
            }
        }
        // 如果已存在播放器，先清理
        if (exoPlayer != null) {
            try {
                exoPlayer.stop();
                exoPlayer.release();
            } catch (Throwable t) {
                Log.w(TAG, "exo_release_old", t);
            }
            exoPlayer = null;
        }
        // 清除旧playerView下的播放器
        if (playerView != null) {
            playerView.setPlayer(null);
        } else {
            playerView = new PlayerView(this);
            playerView.setBackgroundColor(Color.BLACK);
            playerView.setUseController(false);
            playerView.setFocusable(false);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            try {
                java.lang.reflect.Method m = playerView.getClass().getMethod("setSurfaceType", int.class);
                m.invoke(playerView, 1);
                Log.i(TAG, "exo_playerView setSurfaceType SURFACE_VIEW");
            } catch (Throwable ignored) {}
            try {
                playerView.setLayerType(View.LAYER_TYPE_NONE, null);
            } catch (Throwable ignored) {}
        }

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this) {
            @Override
            protected void buildAudioRenderers(android.content.Context context,
                    int extensionRendererMode,
                    com.google.android.exoplayer2.mediacodec.MediaCodecSelector mediaCodecSelector,
                    boolean enableDecoderFallback,
                    com.google.android.exoplayer2.audio.AudioSink audioSink,
                    android.os.Handler eventHandler,
                    com.google.android.exoplayer2.audio.AudioRendererEventListener eventListener,
                    java.util.ArrayList<com.google.android.exoplayer2.Renderer> out) {
                boolean isX86Arch = false;
                try {
                    if (Build.VERSION.SDK_INT >= 21) {
                        String[] abis = Build.SUPPORTED_ABIS;
                        if (abis != null) {
                            for (String a : abis) {
                                if (a.equals("x86") || a.equals("x86_64")) {
                                    isX86Arch = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        String abi1 = Build.CPU_ABI;
                        String abi2 = Build.CPU_ABI2;
                        if (abi1 != null && (abi1.equals("x86") || abi1.equals("x86_64"))) {
                            isX86Arch = true;
                        }
                        if (!isX86Arch && abi2 != null && (abi2.equals("x86") || abi2.equals("x86_64"))) {
                            isX86Arch = true;
                        }
                    }
                } catch (Throwable ignored) {}
                boolean ffmpegOk = false;
                if (!isX86Arch) {
                    ffmpegOk = com.google.android.exoplayer2.ext.ffmpeg.FfmpegLibrary.isAvailable();
                }
                Log.i(TAG, "build_audio_renderers ffmpeg=" + ffmpegOk + " sdk=" + Build.VERSION.SDK_INT
                        + " x86=" + isX86Arch);
                if (ffmpegOk) {
                    try {
                        com.google.android.exoplayer2.Renderer ffmpegAudio =
                                new com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer(
                                        eventHandler, eventListener, new com.google.android.exoplayer2.audio.AudioProcessor[0]);
                        out.add(ffmpegAudio);
                        Log.i(TAG, "ffmpeg_audio_renderer_added");
                    } catch (Throwable t) {
                        Log.w(TAG, "ffmpeg_audio_renderer_init_failed", t);
                    }
                }
                // 同TVBOX方案：EXTENSION_RENDERER_MODE_PREFER + enableDecoderFallback
                // MediaCodecAudioRenderer可能在某些低版本设备崩溃，但fallback机制会处理
                try {
                    // API 19: MediaCodecList.getCodecCapabilities()崩溃，跳过MediaCodecAudioRenderer
                    if (Build.VERSION.SDK_INT >= 21) {
                        super.buildAudioRenderers(context, extensionRendererMode, mediaCodecSelector,
                                enableDecoderFallback, audioSink, eventHandler, eventListener, out);
                    } else {
                        Log.i(TAG, "sdk19_skip_mediacodec_audio_renderer_use_ffmpeg_only");
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "build_audio_renderers_crash", t);
                }
            }

            @Override
            protected void buildVideoRenderers(android.content.Context context,
                    @com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode,
                    com.google.android.exoplayer2.mediacodec.MediaCodecSelector mediaCodecSelector,
                    boolean enableDecoderFallback,
                    android.os.Handler eventHandler,
                    com.google.android.exoplayer2.video.VideoRendererEventListener eventListener,
                    long allowedVideoJoiningTimeMs,
                    java.util.ArrayList<com.google.android.exoplayer2.Renderer> out) {
                boolean skipMediaCodec = Build.VERSION.SDK_INT < 21 && sVideoDecodeFailed;
                if (skipMediaCodec) {
                    Log.i(TAG, "skip_mediacodec_video_renderer_on_api19_decode_failed_use_ffmpeg");
                } else {
                    try {
                        super.buildVideoRenderers(context, extensionRendererMode, mediaCodecSelector,
                                enableDecoderFallback, eventHandler, eventListener, allowedVideoJoiningTimeMs, out);
                        Log.i(TAG, "video_renderers_mediacodec_added count=" + out.size());
                    } catch (Throwable t) {
                        Log.w(TAG, "video_renderers_build_crash_sdk=" + Build.VERSION.SDK_INT, t);
                    }
                }
                try {
                    Class<?> cls = Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegVideoRenderer");
                    java.lang.reflect.Constructor<?> ctor = cls.getConstructor(
                            android.os.Handler.class,
                            com.google.android.exoplayer2.video.VideoRendererEventListener.class,
                            long.class);
                    com.google.android.exoplayer2.Renderer ffmpegVideo =
                            (com.google.android.exoplayer2.Renderer) ctor.newInstance(
                                    eventHandler, eventListener, allowedVideoJoiningTimeMs);
                    out.add(ffmpegVideo);
                    Log.i(TAG, "ffmpeg_video_renderer_added total=" + out.size() + " skip_mediacodec=" + skipMediaCodec);
                } catch (ClassNotFoundException ignored) {
                    Log.i(TAG, "ffmpeg_video_renderer_not_available_jellyfin_audio_only");
                } catch (Throwable t) {
                    Log.w(TAG, "ffmpeg_video_renderer_init_failed", t);
                }
            }
        };
        renderersFactory.setEnableDecoderFallback(true);
        renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
        try {
            java.lang.reflect.Field f = DefaultRenderersFactory.class.getDeclaredField("allowedVideoJoiningTimeMs");
            f.setAccessible(true);
            f.setLong(renderersFactory, 500);
            Log.i(TAG, "exo_joining_time_set=500ms_fast_start");
        } catch (Throwable t) {
            Log.d(TAG, "exo_joining_time_reflect_failed: " + t.getMessage());
        }
        renderersFactory.forceDisableMediaCodecAsynchronousQueueing();
        try {
            renderersFactory.experimentalSetSynchronizeCodecInteractionsWithQueueingEnabled(true);
            Log.i(TAG, "exo_force_sync_codec_queue_all_sdk_fix_hi3798_bug sdk=" + Build.VERSION.SDK_INT);
        } catch (Throwable ignored) {}

        boolean isSystemLowMem = false;
        long availMemMB = -1;
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (am != null) {
                android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                availMemMB = mi.availMem / (1024 * 1024);
                isSystemLowMem = mi.lowMemory || (availMemMB > 0 && availMemMB < 200);
            }
        } catch (Throwable t) {
            Log.d(TAG, "memory_info_check_failed: " + t.getMessage());
        }
        long javaHeapBytes = java.lang.Runtime.getRuntime().maxMemory();
        boolean isAndroid9Plus = Build.VERSION.SDK_INT >= 28;
        Log.i(TAG, "exo_mem javaHeap=" + (javaHeapBytes / (1024 * 1024)) + "MB"
                + " availSystem=" + availMemMB + "MB lowMem=" + isSystemLowMem
                + " sdk=" + Build.VERSION.SDK_INT);

        int minBuf, maxBuf, bufPlayback, bufRebuf;
        long targetBufBytes;
        if (isSystemLowMem) {
            minBuf = 6000;
            maxBuf = 20000;
            bufPlayback = 2500;
            bufRebuf = 4500;
            targetBufBytes = 32L * 1024 * 1024;
        } else {
            minBuf = 10000;
            maxBuf = 30000;
            bufPlayback = 3000;
            bufRebuf = 5000;
            targetBufBytes = 64L * 1024 * 1024;
        }
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(minBuf, maxBuf, bufPlayback, bufRebuf)
                .setTargetBufferBytes((int) targetBufBytes)
                .setPrioritizeTimeOverSizeThresholds(true)
                .build();
        com.google.android.exoplayer2.trackselection.DefaultTrackSelector trackSelector =
                new com.google.android.exoplayer2.trackselection.DefaultTrackSelector(this);
        try {
            com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride videoOverride =
                    new com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride(
                            com.google.android.exoplayer2.C.TRACK_TYPE_VIDEO, -1);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                            .setMaxVideoSize(1920, 1080)
                            .setForceLowestBitrate(false)
                            .setPreferredTextLanguage(null)
                            .setRendererDisabled(com.google.android.exoplayer2.C.TRACK_TYPE_TEXT, true)
                            .build());
            Log.i(TAG, "exo_trackSelector_configured_max1080p");
        } catch (Throwable t) {
            Log.d(TAG, "exo_trackSelector_config_failed: " + t.getMessage());
        }
        exoPlayer = new ExoPlayer.Builder(this)
                .setRenderersFactory(renderersFactory)
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build();
        try { exoPlayer.setForegroundMode(true); } catch (Throwable ignored) {}
        exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        if (isAndroid9Plus) {
            try {
                java.lang.reflect.Method m = ExoPlayer.class.getMethod(
                        "setExperimentalCpuAffinityMode", int.class);
                int perfMode = Class.forName("com.google.android.exoplayer2.util.CpuAffinityProvider")
                        .getField("AFFINITY_MODE_PERFORMANCE").getInt(null);
                m.invoke(exoPlayer, perfMode);
                Log.i(TAG, "exo_android9_cpu_affinity_performance_set");
            } catch (Throwable t) {
                Log.d(TAG, "exo_cpu_affinity_not_available");
            }
        }
        playerView.setPlayer(exoPlayer);
        // 确保playerView已经添加到root
        if (playerView.getParent() == null) {
            root.addView(playerView, 0, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        exoPlayer.addListener(new Player.Listener() {
            private boolean videoDecodeFailedOnApi19 = false;

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
                    if (videoDecodeFailedOnApi19) {
                        showCenterMessage("音频播放中（视频解码器模拟器不可用）", 3000);
                    }
                    startBlackscreenDetection();
                    if (Build.VERSION.SDK_INT >= 28) {
                        trySetMediaCodecLowLatency();
                    }
                } else if (playbackState == Player.STATE_BUFFERING) {
                    showPlaybackOverlay();
                } else if (playbackState == Player.STATE_ENDED) {
                    handler.removeCallbacks(blackscreenCheckRunnable);
                }
            }

            @Override
            public void onVideoSizeChanged(com.google.android.exoplayer2.video.VideoSize videoSize) {
                if (videoSize != null && videoSize.width > 0 && videoSize.height > 0) {
                    Log.i(TAG, "exo_video_size_changed w=" + videoSize.width + " h=" + videoSize.height);
                    mVideoRendered = true;
                    handler.removeCallbacks(blackscreenCheckRunnable);
                    mBlackscreenDetectInProgress = false;
                }
            }

            @Override
            public void onAudioSessionIdChanged(int audioSessionId) {
                Log.i(TAG, "exo_audio_session_id=" + audioSessionId);
                mAudioRendered = true;
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "exo_player_error videoRendered=" + mVideoRendered + " audioRendered=" + mAudioRendered, error);
                handler.removeCallbacks(blackscreenCheckRunnable);
                mBlackscreenDetectInProgress = false;
                boolean isVideoDecodeError = false;
                boolean isBehindLiveWindow = false;
                boolean isPlaylistStuck = false;
                Throwable cause = error;
                int depth = 0;
                while (cause != null) {
                    String cn = cause.getClass().getName();
                    Log.e(TAG, "exo_error_cause[" + depth + "]=" + cn);
                    if (cn.contains("MediaCodecVideoDecoderException") || cn.contains("MediaCodecDecoderException")) {
                        isVideoDecodeError = true;
                        break;
                    }
                    if (cn.contains("BehindLiveWindowException")) {
                        isBehindLiveWindow = true;
                    }
                    if (cn.contains("PlaylistStuckException")) {
                        isPlaylistStuck = true;
                    }
                    cause = cause.getCause();
                    depth++;
                }
                Log.e(TAG, "exo_error_flags videoDecode=" + isVideoDecodeError + " behindLive=" + isBehindLiveWindow + " playlistStuck=" + isPlaylistStuck);
                if (isPlaylistStuck) {
                    mPlaylistStuckRetryCount++;
                    mPlaybackRecoverCount = 0;
                    if (mPlaylistStuckRetryCount <= MAX_PLAYLIST_STUCK_RETRY) {
                        Log.w(TAG, "hls_playlist_stuck_retry=" + mPlaylistStuckRetryCount + "/" + MAX_PLAYLIST_STUCK_RETRY);
                        handler.postDelayed(new Runnable() {
                            @Override public void run() { playCurrentWithExo(); }
                        }, 1500);
                    } else {
                        Log.w(TAG, "playlist_stuck_exhausted switch_line_or_maintenance");
                        mPlaylistStuckRetryCount = 0;
                        boolean switched = switchToNextLine(true);
                        if (!switched) {
                            Channel ch = channels.get(currentIndex);
                            if (ch.lines.size() <= 1) {
                                mSingleLineRetryCount++;
                                if (mSingleLineRetryCount <= MAX_SINGLE_LINE_RETRY) {
                                    handler.postDelayed(new Runnable() {
                                        @Override public void run() { playCurrentWithExo(); }
                                    }, 2000);
                                } else {
                                    showCenterMessage("本线路临时维护中......", 0);
                                }
                            } else {
                                showCenterMessage("本线路临时维护中......", 0);
                            }
                        }
                    }
                    return;
                }
                if (isVideoDecodeError) {
                    Log.w(TAG, "exo_video_decode_error_switch_to_ijk");
                    switchPlayerToIjk();
                    return;
                }
                if (isBehindLiveWindow) {
                    Log.w(TAG, "behind_live_window_auto_retry_same_source");
                    handler.postDelayed(new Runnable() {
                        @Override public void run() { playCurrentWithExo(); }
                    }, 500);
                    return;
                }
                if (Build.VERSION.SDK_INT < 21 && isVideoDecodeError && !videoDecodeFailedOnApi19) {
                    Log.i(TAG, "video_decode_error_on_api19_rebuild_without_video");
                    videoDecodeFailedOnApi19 = true;
                    sVideoDecodeFailed = true;
                    initExoPlayer();
                    playCurrentWithExo();
                    return;
                }
                if (mVideoRendered || mAudioRendered) {
                    mPlaylistStuckRetryCount = 0;
                    mPlaybackRecoverCount++;
                    if (mPlaybackRecoverCount <= MAX_PLAYBACK_RECOVER) {
                        Log.w(TAG, "playback_interrupted_recover_retry=" + mPlaybackRecoverCount + "/" + MAX_PLAYBACK_RECOVER);
                        handler.postDelayed(new Runnable() {
                            @Override public void run() { playCurrentWithExo(); }
                        }, 800);
                    } else {
                        Log.w(TAG, "playback_recover_exhausted try_switch_line");
                        mPlaybackRecoverCount = 0;
                        boolean switched = switchToNextLine(true);
                        if (!switched) {
                            Channel ch = channels.get(currentIndex);
                            if (ch.lines.size() <= 1) {
                                mSingleLineRetryCount++;
                                if (mSingleLineRetryCount <= MAX_SINGLE_LINE_RETRY) {
                                    handler.postDelayed(new Runnable() {
                                        @Override public void run() { playCurrentWithExo(); }
                                    }, 2000);
                                } else {
                                    showCenterMessage("本线路临时维护中......", 0);
                                }
                            } else {
                                showCenterMessage("本线路临时维护中......", 0);
                            }
                        }
                    }
                } else {
                    mPlaylistStuckRetryCount = 0;
                    mPlaybackRecoverCount = 0;
                    boolean switched = switchToNextLine(true);
                    if (switched) {
                        Log.i(TAG, "auto_switch_line on_player_error");
                    } else {
                        Channel ch = channels.get(currentIndex);
                        if (ch.lines.size() <= 1) {
                            mSingleLineRetryCount++;
                            if (mSingleLineRetryCount <= MAX_SINGLE_LINE_RETRY) {
                                Log.w(TAG, "single_line_retry=" + mSingleLineRetryCount + "/" + MAX_SINGLE_LINE_RETRY);
                                handler.postDelayed(new Runnable() {
                                    @Override public void run() { playCurrentWithExo(); }
                                }, 1000);
                            } else {
                                showCenterMessage("本线路临时维护中......", 0);
                            }
                        } else {
                            showCenterMessage("本线路临时维护中......", 0);
                        }
                    }
                }
            }
        });
    }

    private void startBlackscreenDetection() {
        handler.removeCallbacks(blackscreenCheckRunnable);
        if (mBlackscreenDetectInProgress) return;
        mBlackscreenDetectInProgress = true;
        handler.postDelayed(blackscreenCheckRunnable, BLACK_SCREEN_CHECK_DELAY);
        Log.i(TAG, "blackscreen_detection_started delay=" + BLACK_SCREEN_CHECK_DELAY
                + " videoRendered=" + mVideoRendered + " audioRendered=" + mAudioRendered);
    }

    private void switchPlayerToIjk() {
        if (mUseIjkPlayer) return;
        if (sIjkCrashed) {
            Log.w(TAG, "switch_to_ijk_blocked_previous_native_crash_use_exo_ffmpeg");
            if (Build.VERSION.SDK_INT < 21 && !sVideoDecodeFailed) {
                Log.i(TAG, "switch_to_ijk_blocked_api19_rebuild_exo_with_ffmpeg");
                sVideoDecodeFailed = true;
                initExoPlayer();
                playCurrentWithExo();
            }
            return;
        }
        if (mIjkPlayerRetryCount >= MAX_IJK_RETRY) {
            Log.w(TAG, "ijk_player_max_retry_reached abort_switch");
            showCenterMessage("自动播放器切换失败，请手动切换解码模式", 5000);
            mIjkPlayerRetryCount = 0;
            return;
        }
        mIjkPlayerRetryCount++;
        Log.i(TAG, "switch_to_ijk retry=" + mIjkPlayerRetryCount + "/" + MAX_IJK_RETRY);
        showCenterMessage("检测到黑屏，正在切换播放器...", 2500);
        handler.removeCallbacks(blackscreenCheckRunnable);
        mBlackscreenDetectInProgress = false;
        mUseIjkPlayer = true;
        initIjkPlayer();
        playCurrentWithIjk();
    }

    private void switchPlayerBackToExo() {
        if (!mUseIjkPlayer) return;
        Log.i(TAG, "switch_back_to_exo");
        releasePlayer();
        mUseIjkPlayer = false;
        if (playerView != null) {
            playerView.setVisibility(View.VISIBLE);
        }
        if (mIjkSurfaceView != null) {
            mIjkSurfaceView.setVisibility(View.GONE);
        }
        initExoPlayer();
        playCurrentWithExo();
    }

    private void initIjkPlayer() {
        if (sIjkCrashed) {
            Log.w(TAG, "init_ijk_player_blocked_previous_native_crash_fallback_exo");
            mUseIjkPlayer = false;
            initExoPlayer();
            return;
        }
        if (!IjkPlayerHelper.isAvailable()) {
            Log.w(TAG, "init_ijk_player_but_native_not_available_fallback_exo");
            mUseIjkPlayer = false;
            initExoPlayer();
            return;
        }
        Log.i(TAG, "init_ijk_player_surfaceview_mode");
        releasePlayer();
        if (mIjkSurfaceView == null) {
            mIjkSurfaceView = new android.view.SurfaceView(this);
            mIjkSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            if (Build.VERSION.SDK_INT >= 28) {
                try {
                    mIjkSurfaceView.setZOrderOnTop(true);
                    Log.i(TAG, "ijk_surfaceView ZOrderOnTop set (Android9+)");
                } catch (Throwable ignored) {}
            }
        }
        if (mIjkSurfaceView.getParent() == null) {
            root.addView(mIjkSurfaceView, 0);
        }
        if (playerView != null) {
            playerView.setVisibility(View.GONE);
        }
        mIjkSurfaceView.setVisibility(View.VISIBLE);
        if (mIjkPlayer == null) {
            mIjkPlayer = new IjkPlayerHelper(mIjkSurfaceView, new IjkPlayerHelper.IjkPlayerCallback() {
                @Override
                public void onPrepared() {
                    Log.i(TAG, "ijk_on_prepared");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
                                loadingOverlay.setVisibility(View.GONE);
                            }
                            overlayText.setVisibility(View.GONE);
                            handler.removeCallbacks(hideOverlayRunnable);
                            updateStatus();
                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    Log.e(TAG, "ijk_on_error msg=" + msg + " videoRendered=" + mVideoRendered);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mVideoRendered || mAudioRendered) {
                                mPlaybackRecoverCount++;
                                if (mPlaybackRecoverCount <= MAX_PLAYBACK_RECOVER) {
                                    Log.w(TAG, "ijk_playback_interrupted_recover retry=" + mPlaybackRecoverCount);
                                    handler.postDelayed(new Runnable() {
                                        @Override public void run() { playCurrentWithIjk(); }
                                    }, 800);
                                } else {
                                    mPlaybackRecoverCount = 0;
                                    boolean switched = switchToNextLine(true);
                                    if (!switched) {
                                        showCenterMessage("本线路临时维护中......", 0);
                                    }
                                }
                            } else {
                                mPlaybackRecoverCount = 0;
                                boolean switched = switchToNextLine(true);
                                if (switched) {
                                    Log.i(TAG, "ijk_error_switch_line");
                                    return;
                                }
                                Channel ch = channels.get(currentIndex);
                                if (ch.lines.size() <= 1) {
                                    mSingleLineRetryCount++;
                                    if (mSingleLineRetryCount <= MAX_SINGLE_LINE_RETRY) {
                                        Log.w(TAG, "ijk_single_line_retry=" + mSingleLineRetryCount);
                                        handler.postDelayed(new Runnable() {
                                            @Override public void run() { playCurrentWithIjk(); }
                                        }, 1000);
                                    } else {
                                        showCenterMessage("本线路临时维护中......", 0);
                                    }
                                } else {
                                    showCenterMessage("本线路临时维护中......", 0);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onInfo(int what, int extra) {
                }

                @Override
                public void onCompletion() {
                    Log.i(TAG, "ijk_on_completion");
                }

                @Override
                public void onVideoSizeChanged(int width, int height) {
                }
            });
        }
    }

    private void playCurrentWithIjk() {
        if (channels.isEmpty() || mIjkPlayer == null) return;
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
        Log.i(TAG, "ijk_play index=" + currentIndex + " name=" + ch.name
                + " line=" + (lineIdx + 1) + "/" + ch.lines.size()
                + " lat=" + latStr
                + " url=" + url);

        if (url == null || url.isEmpty()) return;
        mIjkPlayer.playUrl(url);
    }

    private void stopPlayer() {
        if (mUseIjkPlayer) {
            if (mIjkPlayer != null) mIjkPlayer.stop();
        } else {
            if (exoPlayer != null) exoPlayer.stop();
        }
    }

    private void releasePlayer() {
        if (mUseIjkPlayer) {
            if (mIjkPlayer != null) {
                mIjkPlayer.release();
                mIjkPlayer = null;
            }
        } else {
            if (exoPlayer != null) {
                try { exoPlayer.stop(); } catch (Throwable ignored) {}
                try { exoPlayer.release(); } catch (Throwable ignored) {}
                exoPlayer = null;
            }
        }
    }

    private void trySetMediaCodecLowLatency() {
        if (Build.VERSION.SDK_INT < 28 || exoPlayer == null) return;
        try {
            java.lang.reflect.Field renderersField = ExoPlayer.class.getDeclaredField("renderers");
            renderersField.setAccessible(true);
            Object[] renderers = (Object[]) renderersField.get(exoPlayer);
            if (renderers == null) return;
            Class<?> codecClass = Class.forName("android.media.MediaCodec");
            final int HIGH_QUALITY = codecClass.getField("MEDIA_PERFORMANCE_MODE_HIGH_QUALITY").getInt(null);
            final int LOW_LATENCY = codecClass.getField("MEDIA_PERFORMANCE_MODE_LOW_LATENCY").getInt(null);
            final int POWER_SAVING = codecClass.getField("MEDIA_PERFORMANCE_MODE_POWER_SAVING").getInt(null);
            java.lang.reflect.Method setPerformanceMode = codecClass.getMethod("setPerformanceMode", int.class);
            for (Object renderer : renderers) {
                if (renderer == null) continue;
                String cn = renderer.getClass().getName();
                if (!cn.contains("MediaCodecVideoRenderer") && !cn.contains("MediaCodecAudioRenderer")) {
                    continue;
                }
                try {
                    java.lang.reflect.Method getCodec = renderer.getClass().getDeclaredMethod("getCodec");
                    getCodec.setAccessible(true);
                    Object codec = getCodec.invoke(renderer);
                    if (codec != null && codecClass.isInstance(codec)) {
                        boolean ok = false;
                        try {
                            setPerformanceMode.invoke(codec, HIGH_QUALITY);
                            ok = true;
                            Log.i(TAG, "exo_android9_mediacodec_HIGH_QUALITY on " + (cn.contains("Video") ? "video" : "audio"));
                        } catch (Throwable ignored) {}
                        if (!ok) {
                            try {
                                setPerformanceMode.invoke(codec, LOW_LATENCY);
                                ok = true;
                                Log.i(TAG, "exo_android9_mediacodec_LOW_LATENCY fallback");
                            } catch (Throwable ignored2) {}
                        }
                        if (!ok) {
                            try {
                                setPerformanceMode.invoke(codec, POWER_SAVING);
                                Log.i(TAG, "exo_android9_mediacodec_POWER_SAVING last");
                            } catch (Throwable ignored3) {}
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            Log.d(TAG, "exo_mediacodec_perfmode_reflect_failed: " + t.getMessage());
        }
    }

    private boolean playerIsPlaying() {
        if (mUseIjkPlayer) {
            return mIjkPlayer != null && mIjkPlayer.isPlaying();
        } else {
            if (exoPlayer != null) {
                int state = exoPlayer.getPlaybackState();
                return state == Player.STATE_READY || state == Player.STATE_BUFFERING;
            }
            return false;
        }
    }

    private static final int LIVE_TXT_MAX_RETRY = 3;
    private static final long LIVE_TXT_RETRY_INTERVAL_MS = 3000L;
    private volatile boolean forceRefreshChannels = false;

    private boolean isNetworkReady() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            @SuppressWarnings("deprecation")
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnected()) return true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (cm.getActiveNetwork() != null) return true;
                Network[] nets = cm.getAllNetworks();
                if (nets != null) {
                    for (Network n : nets) {
                        if (cm.getNetworkCapabilities(n) != null) return true;
                    }
                }
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    private void loadM3uList() {
        forceRefreshChannels = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                doLoadPlaylist(false);
            }
        }, "ChannelLoader").start();
    }

    private void forceRefreshM3uList() {
        forceRefreshChannels = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                doLoadPlaylist(true);
            }
        }, "ChannelLoaderForce").start();
    }

    private interface SourceReadyCallback {
        void onSourceReady(int loaded, int total, String m3uText);
        void onAllDone(int totalCount, int successCount);
        void onFailed(String reason);
    }

    private void doLoadPlaylist(boolean force) {
        try {
            String cachedM3u = preferences.getString(PREF_CACHED_M3U, null);
            long lastRefresh = preferences.getLong(PREF_LAST_REFRESH_DATE, 0);
            long now = System.currentTimeMillis();
            long age = now - lastRefresh;
            boolean cacheFresh = (cachedM3u != null) && (age < CHANNEL_REFRESH_INTERVAL_MS) && !force;
            boolean firstInstall = cachedM3u == null;

            if (cacheFresh && !force) {
                Log.i(TAG, "channel_cache_hit age_hours=" + (age / 3600000L));
                final String finalM3u = cachedM3u;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        parseM3u8(finalM3u);
                    }
                });
                return;
            }

            if (!isNetworkReady()) {
                Log.i(TAG, "channel_network_not_ready");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (cachedM3u != null) {
                            parseM3u8(cachedM3u);
                            showOverlay("网络不可用，使用" + (lastRefresh > 0 ? "已缓存" : "内置") + "频道", false);
                        } else {
                            showOverlay("网络不可用且无频道缓存", true);
                        }
                    }
                });
                return;
            }

            final boolean useIncremental = firstInstall;

            String[] sourceUrls = downloadLiveTxtSources();
            if (sourceUrls == null || sourceUrls.length == 0) {
                Log.w(TAG, "live_txt_empty fallback_single=" + M3U_FALLBACK_URL);
                sourceUrls = new String[]{M3U_FALLBACK_URL};
            }
            final String[] finalUrls = sourceUrls;
            final long finalNow = now;

            if (useIncremental) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showLoadingPanel(null);
                    }
                });
                startIncrementalDownload(finalUrls, finalNow, cachedM3u);
            } else {
                List<String> m3uContents = downloadAllSources(finalUrls);
                if (m3uContents.isEmpty()) {
                    m3uContents.add(downloadSingleM3U(M3U_FALLBACK_URL, SOURCE_DOWNLOAD_TIMEOUT_MS));
                }
                if (m3uContents.isEmpty() || m3uContents.get(0) == null) {
                    final String cache = cachedM3u;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cache != null) {
                                parseM3u8(cache);
                                showOverlay("订阅源全部失败，使用缓存频道", false);
                            } else {
                                showOverlay("频道列表加载失败", true);
                            }
                        }
                    });
                    return;
                }
                String merged = mergeM3uTexts(m3uContents);
                final String finalMerged = merged;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        parseM3u8(finalMerged);
                    }
                });
                preferences.edit()
                        .putString(PREF_CACHED_M3U, finalMerged)
                        .putLong(PREF_LAST_REFRESH_DATE, finalNow)
                        .putBoolean(PREF_FIRST_LAUNCH_DONE, true)
                        .apply();
                Log.i(TAG, "channel_refresh_complete bytes=" + finalMerged.length());
            }

        } catch (final Exception e) {
            Log.e(TAG, "channel_load_exception", e);
            final String cache = preferences.getString(PREF_CACHED_M3U, null);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (loadingPanel != null) loadingPanel.setVisibility(View.GONE);
                    if (cache != null) {
                        parseM3u8(cache);
                        showOverlay("频道加载异常，使用缓存", false);
                    } else {
                        showOverlay("频道列表加载异常: " + e.getMessage(), true);
                    }
                }
            });
        }
    }

    private void startIncrementalDownload(final String[] urls, final long now, final String fallbackCache) {
        isIncrementalLoading = true;
        downloadedM3uBuffer.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String firstUrl = urls[0];
                String firstM3u = downloadSingleM3U(firstUrl, FIRST_SOURCE_TIMEOUT_MS);

                if (firstM3u != null && firstM3u.length() > 50) {
                    synchronized (downloadedM3uBuffer) { downloadedM3uBuffer.add(firstM3u); }

                    final String stageOneMerged = mergeM3uTexts(downloadedM3uBuffer);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            parseM3u8(stageOneMerged);
                            updateLoadingProgress(1, urls.length, true);
                            hideLoadingPanel();
                        }
                    });
                    Log.i(TAG, "channel_stage1_done first_source_ok=" + (firstM3u.length() / 1024) + "KB");
                } else {
                    Log.w(TAG, "channel_stage1_fail first_source_retrieve failed");
                    synchronized (downloadedM3uBuffer) {
                        downloadedM3uBuffer.add(downloadSingleM3U(M3U_FALLBACK_URL, SOURCE_DOWNLOAD_TIMEOUT_MS));
                    }
                    final String stageOneFallback = mergeM3uTexts(downloadedM3uBuffer);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            parseM3u8(stageOneFallback);
                            updateLoadingProgress(1, urls.length, true);
                        }
                    });
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 1; i < urls.length; i++) {
                            String txt = downloadSingleM3U(urls[i], SOURCE_DOWNLOAD_TIMEOUT_MS);
                            boolean sourceOk = (txt != null && txt.length() > 50);
                            int nowLoaded;
                            if (sourceOk) {
                                synchronized (downloadedM3uBuffer) {
                                    downloadedM3uBuffer.add(txt);
                                    nowLoaded = downloadedM3uBuffer.size();
                                }
                            } else {
                                synchronized (downloadedM3uBuffer) {
                                    nowLoaded = downloadedM3uBuffer.size();
                                }
                                Log.w(TAG, "channel_stage2_fail source[" + i + "]: " + safeShorten(urls[i]));
                            }

                            synchronized (downloadedM3uBuffer) {
                                if (!downloadedM3uBuffer.isEmpty()) {
                                    String incrementalMerged = mergeM3uTexts(downloadedM3uBuffer);
                                    final String finalIncMerged = incrementalMerged;
                                    final int finalLoaded = nowLoaded;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            parseM3u8(finalIncMerged);
                                            updateLoadingProgress(finalLoaded, urls.length, true);
                                        }
                                    });
                                }
                            }
                        }

                        String finalCache;
                        synchronized (downloadedM3uBuffer) {
                            finalCache = downloadedM3uBuffer.isEmpty() ? null : mergeM3uTexts(downloadedM3uBuffer);
                        }
                        final String finalCacheF = finalCache;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (finalCacheF != null) {
                                    preferences.edit()
                                            .putString(PREF_CACHED_M3U, finalCacheF)
                                            .putLong(PREF_LAST_REFRESH_DATE, now)
                                            .putBoolean(PREF_FIRST_LAUNCH_DONE, true)
                                            .apply();
                                    Log.i(TAG, "channel_incremental_done total=" + urls.length
                                            + " final_bytes=" + finalCacheF.length());
                                }
                                isIncrementalLoading = false;
                            }
                        });
                    }
                }, "ChannelStage2").start();
            }
        }, "ChannelStage1").start();
    }

    private String[] downloadLiveTxtSources() {
        String lastError = null;
        for (int attempt = 0; attempt <= LIVE_TXT_MAX_RETRY; attempt++) {
            if (attempt > 0) {
                try { Thread.sleep(LIVE_TXT_RETRY_INTERVAL_MS * attempt); } catch (InterruptedException ignored) {}
                Log.i(TAG, "live_txt_retry attempt=" + attempt);
            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(LIVE_TXT_URL).openConnection();
                conn.setConnectTimeout(LIVE_TXT_DOWNLOAD_TIMEOUT_MS);
                conn.setReadTimeout(LIVE_TXT_DOWNLOAD_TIMEOUT_MS);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                int code = conn.getResponseCode();
                if (code != 200) { lastError = "HTTP " + code; continue; }
                InputStream is = conn.getInputStream();
                String raw = readAll(is);
                is.close();
                String[] lines = raw.split("\\r?\\n");
                List<String> urls = new ArrayList<String>();
                for (String l : lines) {
                    String t = l.trim();
                    if (t.length() == 0 || t.startsWith("#")) continue;
                    if (t.startsWith("http://") || t.startsWith("https://")) urls.add(t);
                }
                Log.i(TAG, "live_txt_downloaded sources=" + urls.size());
                return urls.toArray(new String[0]);
            } catch (Exception e) {
                lastError = e.getMessage();
                Log.w(TAG, "live_txt_attempt_fail " + attempt + ": " + lastError);
            } finally {
                if (conn != null) try { conn.disconnect(); } catch (Throwable ignored) {}
            }
        }
        Log.e(TAG, "live_txt_all_retry_failed: " + lastError);
        return null;
    }

    private String downloadSingleM3U(String url, int timeoutMs) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            int code = conn.getResponseCode();
            if (code != 200) { Log.w(TAG, "source_http_error code=" + code + " url=" + safeShorten(url)); return null; }
            InputStream is = conn.getInputStream();
            String text = readAll(is);
            is.close();
            return text;
        } catch (Exception e) {
            Log.w(TAG, "source_download_fail " + safeShorten(url) + ": " + e.getMessage());
            return null;
        } finally {
            if (conn != null) try { conn.disconnect(); } catch (Throwable ignored) {}
        }
    }

    private List<String> downloadAllSources(String[] urls) {
        List<String> ok = new ArrayList<String>();
        List<String> failUrls = new ArrayList<String>();
        java.util.concurrent.atomic.AtomicInteger done = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(urls.length);
        java.util.concurrent.Semaphore gate = new java.util.concurrent.Semaphore(MAX_SOURCE_PARALLEL);

        for (final String u : urls) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        gate.acquire();
                        try {
                            String txt = downloadSingleM3U(u, SOURCE_DOWNLOAD_TIMEOUT_MS);
                            if (txt != null && txt.length() > 50) {
                                synchronized (ok) { ok.add(txt); }
                            } else {
                                synchronized (failUrls) { failUrls.add(u); }
                            }
                        } finally {
                            gate.release();
                        }
                    } catch (Throwable t) {
                        synchronized (failUrls) { failUrls.add(u); }
                    } finally {
                        latch.countDown();
                    }
                }
            }, "SrcLoader-" + (done.incrementAndGet())).start();
        }

        try { latch.await(urls.length * 20L, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        if (!failUrls.isEmpty()) {
            Log.w(TAG, "source_fail_list=" + failUrls.size() + ": " + failUrls);
        }
        return ok;
    }

    private String mergeM3uTexts(List<String> sources) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String src : sources) {
            String trimmed = src.trim();
            if (trimmed.isEmpty()) continue;
            if (!first) {
                if (trimmed.startsWith("#EXTM3U")) {
                    int idx = trimmed.indexOf('\n');
                    if (idx >= 0) {
                        sb.append(trimmed, idx + 1, trimmed.length());
                    } else {
                        sb.append(trimmed.substring("#EXTM3U".length()));
                    }
                } else {
                    sb.append(trimmed);
                }
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') sb.append('\n');
            } else {
                sb.append(trimmed);
                if (!trimmed.endsWith("\n")) sb.append('\n');
                first = false;
            }
        }
        return sb.toString();
    }

    private String readAll(InputStream is) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
        return new String(baos.toByteArray(), Charset.forName("UTF-8"));
    }

    private static String safeShorten(String s) {
        if (s == null) return "";
        return s.length() > 70 ? s.substring(0, 70) + "..." : s;
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

    private static int compareChannelNames(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        Integer na = extractCctvNumber(a);
        Integer nb = extractCctvNumber(b);
        if (na != null && nb != null) {
            return Integer.compare(na, nb);
        }
        if (na != null) return -1;
        if (nb != null) return 1;
        return a.compareTo(b);
    }

    private static Integer extractCctvNumber(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().trim();
        if (!lower.startsWith("cctv")) return null;
        int dash = lower.indexOf('-');
        if (dash < 0) return null;
        String rest = lower.substring(dash + 1);
        StringBuilder numBuf = new StringBuilder();
        for (int i = 0; i < rest.length(); i++) {
            char c = rest.charAt(i);
            if (c >= '0' && c <= '9') numBuf.append(c);
            else break;
        }
        if (numBuf.length() == 0) return null;
        try { return Integer.parseInt(numBuf.toString()); }
        catch (NumberFormatException e) { return null; }
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

        java.util.Collections.sort(parsed, new java.util.Comparator<Channel>() {
            @Override
            public int compare(Channel a, Channel b) {
                if (a.lines.size() != b.lines.size()) return b.lines.size() - a.lines.size();
                return a.name.compareTo(b.name);
            }
        });

        if (parsed.isEmpty()) {
            showOverlay("M3U 解析无频道", false);
            return;
        }

        channels.clear();
        channels.addAll(parsed);
        for (int i = 0; i < channels.size(); i++) channels.get(i).absIndex = i;
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
        for (String g : menuGroups) {
            List<Channel> list = groupChannelMap.get(g);
            if (list != null && list.size() > 1) {
                java.util.Collections.sort(list, new java.util.Comparator<Channel>() {
                    @Override
                    public int compare(Channel a, Channel b) {
                        return compareChannelNames(a.name, b.name);
                    }
                });
            }
        }
        channels.clear();
        for (String g : menuGroups) {
            List<Channel> list = groupChannelMap.get(g);
            if (list != null) channels.addAll(list);
        }
        for (int i = 0; i < channels.size(); i++) channels.get(i).absIndex = i;
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
        safeNotifyChannelAdapter();
        updateMenuHeader();
        playCurrentWithExo();
        detectBadChannels();
    }

    private void playCurrentWithExo() {
        if (mUseIjkPlayer) {
            playCurrentWithIjk();
            return;
        }
        mVideoRendered = false;
        mAudioRendered = false;
        handler.removeCallbacks(blackscreenCheckRunnable);
        mBlackscreenDetectInProgress = false;
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
        String userAgent = "Mozilla/5.0 (Linux; Android " + Build.VERSION.SDK_INT + ") AppleWebKit/537.36 Chrome/120.0 Mobile";
        if (sSharedOkClient == null) {
            java.util.concurrent.TimeUnit unit = java.util.concurrent.TimeUnit.MINUTES;
            okhttp3.ConnectionPool pool = new okhttp3.ConnectionPool(5, 5, unit);
            okhttp3.Dispatcher dispatcher = new okhttp3.Dispatcher();
            dispatcher.setMaxRequests(5);
            dispatcher.setMaxRequestsPerHost(3);
            javax.net.ssl.X509TrustManager trustAll = new javax.net.ssl.X509TrustManager() {
                @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
            };
            javax.net.ssl.SSLSocketFactory sslSockFactory = null;
            try {
                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                sslContext.init(null, new javax.net.ssl.TrustManager[]{trustAll}, new java.security.SecureRandom());
                sslSockFactory = sslContext.getSocketFactory();
            } catch (Throwable t) {
                Log.w(TAG, "ssl_context_init_failed: " + t.getMessage());
            }
            javax.net.ssl.HostnameVerifier allHosts = new javax.net.ssl.HostnameVerifier() {
                @Override public boolean verify(String hostname, javax.net.ssl.SSLSession session) { return true; }
            };
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .hostnameVerifier(allHosts)
                    .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .callTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .connectionPool(pool)
                    .dispatcher(dispatcher)
                    .pingInterval(15, java.util.concurrent.TimeUnit.SECONDS);
            if (sslSockFactory != null) {
                clientBuilder.sslSocketFactory(sslSockFactory, trustAll);
            }
            sSharedOkClient = clientBuilder.build();
            sSharedOkFactory = new OkHttpDataSource.Factory(sSharedOkClient)
                    .setUserAgent(userAgent);
            sSharedHlsFactory = new HlsMediaSource.Factory(sSharedOkFactory);
            try { sSharedHlsFactory.setAllowChunklessPreparation(true); } catch (Throwable t) {}
            try { sSharedHlsFactory.setLoadErrorHandlingPolicy(new com.lwtdzh.yangshipinwrapper4tv.exo.HlsErrorHandlingPolicy()); } catch (Throwable t) {}
            sSharedProgFactory = new ProgressiveMediaSource.Factory(sSharedOkFactory);
            Log.i(TAG, "exo_datasource_shared_init sdk=" + Build.VERSION.SDK_INT);
        }
        com.google.android.exoplayer2.source.MediaSource source;
        String lowerUrl = url.toLowerCase();
        Uri uri = Uri.parse(url);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        if (lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mkv") || lowerUrl.endsWith(".avi")
                || lowerUrl.endsWith(".flv") || lowerUrl.endsWith(".webm") || lowerUrl.endsWith(".mov")) {
            source = sSharedProgFactory.createMediaSource(mediaItem);
            Log.i(TAG, "exo_source=progressive url=" + url);
        } else {
            source = sSharedHlsFactory.createMediaSource(mediaItem);
            Log.i(TAG, "exo_source=hls url=" + url);
        }
        try { exoPlayer.stop(); } catch (Throwable ignored) {}
        exoPlayer.setMediaSource(source, true);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    private void detectBadChannels() {
        boolean isSysLowMem = false;
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (am != null) {
                android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                isSysLowMem = mi.lowMemory || (mi.availMem > 0 && mi.availMem < 150 * 1024 * 1024);
            }
        } catch (Throwable ignored) {}
        if (isSysLowMem) {
            Log.i(TAG, "channel_detect_skip_system_low_mem");
            return;
        }
        Log.i(TAG, "channel_detect start async");

        final List<StreamLine> allLines = new ArrayList<>();
        int channelLimit = Math.min(50, channels.size());
        for (int ci = 0; ci < channelLimit; ci++) {
            Channel ch = channels.get(ci);
            for (StreamLine sl : ch.lines) {
                allLines.add(sl);
            }
        }
        if (allLines.isEmpty()) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(4);
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
                int[] emptyLineCount = {0};
                for (Channel ch : channels) {
                    if (ch.lines.isEmpty()) {
                        emptyLineCount[0]++;
                        Log.w(TAG, "channel_zero_lines name=" + ch.name.substring(0, Math.min(20, ch.name.length())));
                        continue;
                    }
                    boolean hasGood = false;
                    for (StreamLine sl : ch.lines) {
                        if (sl.good) { hasGood = true; break; }
                    }
                    if (!hasGood) {
                        badLineCount[0]++;
                    }
                }
                Log.i(TAG, "channel_detect done bad=" + badLineCount[0] + " empty=" + emptyLineCount[0]
                        + " total=" + channels.size());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int cnt = channelItemAdapter.getItemCount();
                        safeNotifyChannelAdapter();
                        if (cnt > 0 && Build.VERSION.SDK_INT >= 20) {
                            if (cnt > 0 && Build.VERSION.SDK_INT >= 20) { channelItemAdapter.notifyItemRangeChanged(0, cnt); }
                        }
                        groupAdapter.notifyDataSetChanged();
                        updateStatus();
                    }
                });
            }
        }, "ChannelDetectAsync").start();
    }

    private void detectSingleChannelLines(final Channel ch, final boolean refreshOverlay) {
        if (ch == null || ch.lines.isEmpty()) return;
        boolean allChecked = true;
        for (StreamLine sl : ch.lines) {
            if (!sl.checked) { allChecked = false; break; }
        }
        if (allChecked) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (final StreamLine sl : ch.lines) {
                    if (sl.checked) continue;
                    try {
                        long start = System.currentTimeMillis();
                        HttpURLConnection conn = (HttpURLConnection) new URL(sl.url).openConnection();
                        conn.setConnectTimeout(3000);
                        conn.setReadTimeout(3000);
                        conn.setRequestMethod("HEAD");
                        conn.setInstanceFollowRedirects(true);
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        int code = conn.getResponseCode();
                        conn.disconnect();
                        if (code == 405) {
                            HttpURLConnection conn2 = (HttpURLConnection) new URL(sl.url).openConnection();
                            conn2.setConnectTimeout(3000);
                            conn2.setReadTimeout(3000);
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
                        } else {
                            sl.good = true;
                            sl.latencyMs = latency;
                        }
                    } catch (Exception e) {
                        sl.good = false;
                        sl.latencyMs = 99999;
                    } finally {
                        sl.checked = true;
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        safeNotifyChannelAdapter();
                        groupAdapter.notifyDataSetChanged();
                        updateStatus();
                        if (refreshOverlay) {
                            showPlaybackOverlay();
                        }
                    }
                });
            }
        }, "SingleChDetect").start();
    }

    private void safeNotifyChannelAdapter() {
        if (channelItemAdapter == null) return;
        if (Build.VERSION.SDK_INT >= 20 && channelRecyclerView == null) return;
        if (Build.VERSION.SDK_INT < 20) {
            Log.d(TAG, "safeNotify API19 mode: manualRender");
            // API 19: 绕过 RecyclerView 适配器，直接手动构建视图
            channelItemAdapter.setItems(getCurrentGroupChannels());
            manualRenderChannelList();
        } else {
            channelItemAdapter.updateData(getCurrentGroupChannels());
            channelItemAdapter.notifyDataSetChanged();
        }
    }

    @android.annotation.TargetApi(19)
    private void forceRebindAllVisibleItems() {
        if (Build.VERSION.SDK_INT < 20) return;
        if (channelRecyclerView == null || channelItemAdapter == null) return;
        channelRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int childCount = channelRecyclerView.getChildCount();
                if (childCount == 0) {
                    // API 19 RecyclerView 尚未完成首次布局，直接强制整个列表刷新
                    channelRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            channelItemAdapter.notifyDataSetChanged();
                            channelRecyclerView.requestLayout();
                            channelRecyclerView.invalidate();
                        }
                    });
                    return;
                }
                for (int i = 0; i < childCount; i++) {
                    View child = channelRecyclerView.getChildAt(i);
                    int pos = channelRecyclerView.getChildAdapterPosition(child);
                    if (pos >= 0) {
                        channelItemAdapter.notifyItemChanged(pos);
                    }
                }
                // 二次刷新确保所有视图重绘
                channelRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int childCount2 = channelRecyclerView.getChildCount();
                        for (int i = 0; i < childCount2; i++) {
                            View child = channelRecyclerView.getChildAt(i);
                            int pos = channelRecyclerView.getChildAdapterPosition(child);
                            if (pos >= 0) {
                                channelItemAdapter.notifyItemChanged(pos);
                            }
                        }
                        // 强制 RecyclerView 重新布局
                        channelRecyclerView.requestLayout();
                        channelRecyclerView.invalidate();
                    }
                });
            }
        });
    }
    private void manualRenderChannelList() {
        if (channelItemAdapter == null) return;
        if (channelListInnerLayout == null) return;
        // 仅在菜单可见时才重建视图（避免后台重建大量View浪费内存）
        if (menuPanel.getVisibility() != View.VISIBLE) return;
        // 记录列表可见区域顶部位置
        int firstVisiblePos = 0;
        View firstChild = channelListInnerLayout.getChildCount() > 0 ? channelListInnerLayout.getChildAt(0) : null;
        if (firstChild != null) {
            firstVisiblePos = 0;
            if (firstVisiblePos < 0) firstVisiblePos = 0;
        }
        // 移除所有子视图
        channelListInnerLayout.removeAllViews();
        List<Channel> items = channelItemAdapter.getItems();
        if (items == null || items.isEmpty()) return;
        for (int i = 0; i < items.size(); i++) {
            Channel ch = items.get(i);
            LinearLayout container = channelItemAdapter.createItemView(ch, i);
            channelListInnerLayout.addView(container);
        }
        // 尝试保持滚动位置
        final int scrollTarget = firstVisiblePos;
        channelListContainer.post(new Runnable() {
            @Override
            public void run() {
                int childCount = channelListInnerLayout.getChildCount();
                if (childCount > scrollTarget) {
                    View target = channelListInnerLayout.getChildAt(Math.min(scrollTarget, childCount - 1));
                    if (target != null) {
                        target.requestFocus();
                        int top = target.getTop();
                        channelListContainer.scrollTo(0, Math.max(0, top - dp(4)));
                    }
                }
                channelListContainer.requestLayout();
                channelListContainer.invalidate();
            }
        });
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
            safeNotifyChannelAdapter();
            return;
        }
        String curName = "";
        if (currentIndex >= 0 && currentIndex < channels.size()) {
            curName = channels.get(currentIndex).name;
        }
        channels.clear();
        channels.addAll(visible);
        for (int i = 0; i < channels.size(); i++) channels.get(i).absIndex = i;
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
        for (String g : menuGroups) {
            List<Channel> list = groupChannelMap.get(g);
            if (list != null && list.size() > 1) {
                java.util.Collections.sort(list, new java.util.Comparator<Channel>() {
                    @Override
                    public int compare(Channel a, Channel b) {
                        return compareChannelNames(a.name, b.name);
                    }
                });
            }
        }
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
        safeNotifyChannelAdapter();
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

    private void applyPlaybackModeToPlayerEngine() {
        String effective = resolveEffectiveMode();
        boolean useIjk;
        boolean api19 = Build.VERSION.SDK_INT < 21;
        if (sIjkCrashed) {
            useIjk = false;
            Log.w(TAG, "playback_engine force_exo_ijk_previously_crashed sdk="
                    + Build.VERSION.SDK_INT);
        } else if (PLAYBACK_MODE_SW.equals(effective)) {
            useIjk = true;
        } else if (PLAYBACK_MODE_HW.equals(effective)) {
            useIjk = false;
        } else { // auto
            useIjk = api19 && IjkPlayerHelper.isAvailable();
        }
        if (useIjk != mUseIjkPlayer) {
            mUseIjkPlayer = useIjk;
            Log.i(TAG, "playback_engine mode=" + playbackMode + " effective=" + effective
                    + " useIjk=" + useIjk + " sdk=" + Build.VERSION.SDK_INT);
        }
    }

    private String resolveEffectiveMode() {
        if (PLAYBACK_MODE_AUTO.equals(playbackMode)) {
            if (sIjkCrashed) return PLAYBACK_MODE_HW;
            return (Build.VERSION.SDK_INT < 21 && IjkPlayerHelper.isAvailable())
                    ? PLAYBACK_MODE_SW : PLAYBACK_MODE_HW;
        }
        return playbackMode;
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

        Channel curCh = channels.get(currentIndex);
        String curGroupName = (curCh.group != null && curCh.group.length() > 0) ? curCh.group : "默认";
        int startGroupIdx = menuGroups.indexOf(curGroupName);
        if (startGroupIdx < 0) startGroupIdx = 0;

        int totalGroups = menuGroups.size();
        Channel found = null;

        for (int groupOffset = 0; groupOffset < totalGroups && found == null; groupOffset++) {
            int groupIdx = (startGroupIdx + groupOffset * delta + totalGroups) % totalGroups;
            String gName = menuGroups.get(groupIdx);
            List<Channel> groupChans = groupChannelMap.get(gName);
            if (groupChans == null || groupChans.isEmpty()) continue;

            if (groupOffset == 0) {
                int idxInGroup = findChannelIndexInGroup(groupIdx, curCh);
                for (int s = 1; s <= groupChans.size(); s++) {
                    int candidate = idxInGroup + s * delta;
                    if (candidate < 0 || candidate >= groupChans.size()) break;
                    Channel nc = groupChans.get(candidate);
                    if (isChannelPlayable(nc)) { found = nc; break; }
                }
            } else {
                if (delta > 0) {
                    for (int i = 0; i < groupChans.size(); i++) {
                        Channel nc = groupChans.get(i);
                        if (isChannelPlayable(nc)) { found = nc; break; }
                    }
                } else {
                    for (int i = groupChans.size() - 1; i >= 0; i--) {
                        Channel nc = groupChans.get(i);
                        if (isChannelPlayable(nc)) { found = nc; break; }
                    }
                }
            }
        }

        if (found == null) {
            int next = currentIndex;
            for (int step = 0; step < channels.size(); step++) {
                next = (next + delta + channels.size()) % channels.size();
                Channel nc = channels.get(next);
                if (isChannelPlayable(nc)) break;
            }
            found = channels.get(next);
        }

        currentIndex = channels.indexOf(found);
        if (currentIndex < 0) currentIndex = 0;
        mAutoSwitchFailCount = 0;
        mPlaybackRecoverCount = 0;
        mPlaylistStuckRetryCount = 0;
        mSingleLineRetryCount = 0;
        Log.i(TAG, "channel_change index=" + currentIndex + " name=" + channels.get(currentIndex).name);
        Channel ch = channels.get(currentIndex);
        String g = (ch.group != null && ch.group.length() > 0) ? ch.group : "默认";
        selectedGroupIndex = menuGroups.indexOf(g);
        if (selectedGroupIndex < 0) selectedGroupIndex = 0;
        selectedChannelInGroup = findChannelIndexInGroup(selectedGroupIndex, ch);
        groupAdapter.notifyDataSetChanged();
        safeNotifyChannelAdapter();
        requestCurrentStream("channel");
        detectSingleChannelLines(ch, true);
    }

    private boolean isChannelPlayable(Channel nc) {
        if (nc == null) return false;
        boolean allBad = !nc.lines.isEmpty();
        for (StreamLine sl : nc.lines) {
            if (!sl.checked || sl.good) { allBad = false; break; }
        }
        return !allBad;
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
        detectSingleChannelLines(ch, false);
    }

    private boolean switchToNextLine(boolean fromError) {
        if (channels.isEmpty()) return false;
        Channel ch = channels.get(currentIndex);
        if (ch.lines.size() <= 1) return false;

        if (fromError) {
            StreamLine cur = ch.lines.get(ch.currentLineIndex);
            if (cur != null && cur.checked) {
                cur.good = false;
                cur.latencyMs = 99999;
            }
            mAutoSwitchFailCount++;
            if (mAutoSwitchFailCount >= MAX_AUTO_SWITCH_FAIL) {
                Log.w(TAG, "auto_switch_fuse_triggered failCount=" + mAutoSwitchFailCount
                        + " stop auto-switching for channel=" + ch.name);
                return false;
            }
        } else {
            mAutoSwitchFailCount = 0;
            mSingleLineRetryCount = 0;
        }

        int startIdx = ch.currentLineIndex;
        sortLinesByLatency(ch);

        for (int i = 1; i <= ch.lines.size(); i++) {
            int candidate = (startIdx + i) % ch.lines.size();
            StreamLine line = ch.lines.get(candidate);
            if (!line.checked || line.good) {
                ch.currentLineIndex = candidate;
                Log.i(TAG, "auto_switch_line fromError=" + fromError + " failCount=" + mAutoSwitchFailCount
                        + " newIdx=" + candidate
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
        if (PLAYBACK_MODE_AUTO.equals(playbackMode)) return "自动";
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
        if (!sl.good) return "本线路临时维护中......";
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
            if (centerModule != null && centerModule.isVisible()) centerModule.hide();
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
        safeNotifyChannelAdapter();
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
        safeNotifyChannelAdapter();
        scrollGroupToPosition(selectedGroupIndex);
        scrollChannelToPosition(selectedChannelInGroup);
        if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) {
            channelListContainer.requestFocus();
        } else if (channelRecyclerView != null) {
            channelRecyclerView.requestFocus();
        }
        resetMenuAutoHide();
    }

    private void showSettingsMenu() {
        menuPage = MENU_PAGE_SETTINGS;
        updateMenuHeader();
        handler.removeCallbacks(menuAutoHideRunnable);

        final String[] items = new String[]{
                "解码器模式  " + playbackModeLabel(),
                "刷新频道源",
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
                    dialog.dismiss();
                    showOverlay("正在从订阅源刷新频道...", true);
                    forceRefreshM3uList();
                } else if (position == 2) {
                    toggleAutoStart();
                    items[2] = "开机自启    " + (autoStartOnBoot ? "开" : "关");
                    adapter.notifyDataSetChanged();
                } else if (position == 3) {
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
        safeNotifyChannelAdapter();
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
            if (Build.VERSION.SDK_INT < 20 && channelListContainer != null) {
                channelListContainer.requestFocus();
            } else if (channelRecyclerView != null) {
                channelRecyclerView.requestFocus();
            }
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
        safeNotifyChannelAdapter();
        hideMenu();
    }

    private void togglePlaybackMode() {
        if (PLAYBACK_MODE_AUTO.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_HW;
        } else if (PLAYBACK_MODE_HW.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_SW;
        } else {
            playbackMode = PLAYBACK_MODE_AUTO;
        }
        preferences.edit().putString(PREF_PLAYBACK_MODE, playbackMode).apply();
        applyPlaybackModeToPlayerEngine();
        initExoPlayer();
        applyPlaybackModeToWebView();
    }

    private void toggleAutoStart() {
        autoStartOnBoot = !autoStartOnBoot;
        preferences.edit().putBoolean(PREF_AUTO_START, autoStartOnBoot).apply();
        applyBootAutoStartState();
    }

    private void applyBootAutoStartState() {
        if (autoStartOnBoot) {
            scheduleBootAlarm();
            ensureBootReceiverEnabled();
            Log.i(TAG, "boot_auto_start_ENABLED");
        } else {
            cancelBootAlarm();
            disableBootReceiver();
            Log.i(TAG, "boot_auto_start_DISABLED_alarm_canceled");
        }
    }

    private void cancelBootAlarm() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(this, 10001, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null) {
                am.cancel(pi);
                Log.i(TAG, "cancelBootAlarm: alarm canceled");
            }
        } catch (Throwable t) {
            Log.e(TAG, "cancelBootAlarm failed", t);
        }
    }

    private void disableBootReceiver() {
        try {
            ComponentName cn = new ComponentName(this, BootReceiver.class);
            PackageManager pm = getPackageManager();
            int state = pm.getComponentEnabledSetting(cn);
            if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                pm.setComponentEnabledSetting(cn,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                Log.i(TAG, "disableBootReceiver: BootReceiver DISABLED (was=" + state + ")");
            }
        } catch (Throwable t) {
            Log.e(TAG, "disableBootReceiver failed", t);
        }
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
        final int targetPos = position;
        if (groupRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            ((LinearLayoutManager) groupRecyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, dp(4));
        } else {
            groupRecyclerView.scrollToPosition(targetPos);
        }
        groupRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                RecyclerView.ViewHolder vh = groupRecyclerView.findViewHolderForAdapterPosition(targetPos);
                if (vh != null && vh.itemView != null) {
                    vh.itemView.requestFocus();
                }
            }
        });
    }

    private void scrollChannelToPosition(int position) {
        if (Build.VERSION.SDK_INT < 20) {
            if (channelListContainer == null || channelListInnerLayout == null) return;
            if (position < 0) position = 0;
            int total = channelItemAdapter.getItemCount();
            if (position >= total) position = Math.max(0, total - 1);
            final int targetPos = position;
            channelListContainer.post(new Runnable() {
                @Override
                public void run() {
                    int childCount = channelListInnerLayout.getChildCount();
                    if (childCount > 0 && targetPos < childCount) {
                        View target = channelListInnerLayout.getChildAt(targetPos);
                        if (target != null) {
                            int top = target.getTop();
                            channelListContainer.scrollTo(0, Math.max(0, top - dp(4)));
                            target.requestFocus();
                        }
                    }
                }
            });
        } else {
            if (channelRecyclerView == null) return;
            if (position < 0) position = 0;
            int total = channelItemAdapter.getItemCount();
            if (position >= total) position = Math.max(0, total - 1);
            final int targetPos = position;
            if (channelRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                ((LinearLayoutManager) channelRecyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, dp(4));
            } else {
                channelRecyclerView.scrollToPosition(targetPos);
            }
            channelRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    RecyclerView.ViewHolder vh = channelRecyclerView.findViewHolderForAdapterPosition(targetPos);
                    if (vh != null && vh.itemView != null) {
                        vh.itemView.requestFocus();
                    }
                }
            });
        }
    }

    private void refreshMenuAdapters() {
        if (groupAdapter != null) groupAdapter.notifyDataSetChanged();
        safeNotifyChannelAdapter();
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
        boolean centerVisible = centerModule != null && centerModule.isVisible();
        boolean menuVisible = menuPanel.getVisibility() == View.VISIBLE;
        Log.d(TAG, "dispatchKey key=" + keyCode + "(" + KeyEvent.keyCodeToString(keyCode)
                + ") center=" + centerVisible + " menu=" + menuVisible);
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            appendNumber(keyCode - KeyEvent.KEYCODE_0);
            return true;
        }

        if (centerVisible) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_BACK) {
                centerModule.handleKey(keyCode);
                return true;
            }
        }

        if (menuVisible) {
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
                    refreshMenuAdapters();
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (focusOnGroupSide) {
                    if (selectedGroupIndex < menuGroups.size()) {
                        focusOnGroupSide = false;
                        View focusTarget = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;
                        if (focusTarget != null) focusTarget.requestFocus();
                        refreshMenuAdapters();
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
            safeNotifyChannelAdapter();
        }
        if (Build.VERSION.SDK_INT >= 20) {
            groupAdapter.notifyItemChanged(oldIdx);
            groupAdapter.notifyItemChanged(selectedGroupIndex);
        } else {
            groupAdapter.notifyDataSetChanged();
        }
        scrollGroupToPosition(selectedGroupIndex);
        resetMenuAutoHide();
    }

    private void moveChannelSelection(int delta) {
        List<Channel> chans = getCurrentGroupChannels();
        int total = chans.size();
        if (total <= 0) return;
        int oldSel = selectedChannelInGroup;
        selectedChannelInGroup = (selectedChannelInGroup + delta + total) % total;
        if (Build.VERSION.SDK_INT >= 20) {
            channelItemAdapter.notifyItemChanged(oldSel);
            channelItemAdapter.notifyItemChanged(selectedChannelInGroup);
        } else {
            safeNotifyChannelAdapter();
        }
        scrollChannelToPosition(selectedChannelInGroup);
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
                    View focusTarget = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;
                    if (focusTarget != null) focusTarget.requestFocus();
                    refreshMenuAdapters();
                } else {
                    focusOnGroupSide = true;
                    groupRecyclerView.requestFocus();
                    refreshMenuAdapters();
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

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "version_check_first_attempt");
                checkAppVersion();
            }
        }, 3000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "version_check_retry_network_ready");
                checkAppVersion();
            }
        }, 12000);

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
                            if (centerModule != null) centerModule.refreshAccountInfo();
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
            Log.i(TAG, "version_check_start currentVer=" + currentVer + " ruyiApi=" + (ruyiApi != null));
            if (ruyiApi == null) {
                Log.w(TAG, "version_check_abort ruyiApi is null");
                return;
            }
            ruyiApi.checkVersion(currentVer, new RuyiApi.VersionCallback() {
                @Override
                public void onVersionResult(final RuyiApi.VersionInfo info) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "version_check_result hasUpdate=" + info.hasUpdate
                                    + " remoteVer=" + info.remoteVersion
                                    + " url=" + info.updateUrl
                                    + " compel=" + info.compel);
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
        } catch (Exception e) {
            Log.w(TAG, "checkAppVersion crash: " + e.getMessage());
        }
    }

    private void showVersionDialog(final RuyiApi.VersionInfo info) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (versionDialogShown) {
                        Log.i(TAG, "showVersionDialog skip_already_shown");
                        return;
                    }
                    versionDialogShown = true;
                    Log.i(TAG, "showVersionDialog hasUpdate=true url=" + info.updateUrl + " compel=" + info.compel);
                    if (isFinishing()) {
                        Log.w(TAG, "showVersionDialog SKIP_ACTIVITY_FINISHING");
                        return;
                    }
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
                    Log.i(TAG, "showVersionDialog BEFORE_SHOW");
                    builder.show();
                    Log.i(TAG, "showVersionDialog AFTER_SHOW");
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

    private File getUpdateDownloadDir() {
        File dir;
        if (Build.VERSION.SDK_INT >= 29) {
            dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "apk_updates");
        } else {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "apk_updates");
        }
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private String buildTargetApkName(String url) {
        String suffix = "";
        try {
            int q = url.indexOf('?');
            String clean = q > 0 ? url.substring(0, q) : url;
            int sl = clean.lastIndexOf('/');
            if (sl > 0) suffix = clean.substring(sl + 1);
        } catch (Exception ignored) {}
        if (suffix == null || suffix.isEmpty() || !suffix.endsWith(".apk")) {
            try {
                String md5 = Integer.toHexString(url.hashCode());
                suffix = "update_" + md5 + ".apk";
            } catch (Exception e) {
                suffix = "update.apk";
            }
        }
        return suffix;
    }

    private File findExistingApk(String url, long expectedSize) {
        File dir = getUpdateDownloadDir();
        String targetName = buildTargetApkName(url);
        File target = new File(dir, targetName);
        if (target.exists()) {
            if (expectedSize > 0 && target.length() == expectedSize) {
                Log.i(TAG, "update_found_existing_apk size=" + target.length() + " file=" + target.getAbsolutePath());
                return target;
            }
            if (expectedSize <= 0 && target.length() > 1024 * 1024) {
                Log.i(TAG, "update_found_existing_apk (no size check) file=" + target.getAbsolutePath());
                return target;
            }
        }
        File[] olds = dir.listFiles(new java.io.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".apk") && !f.getName().endsWith(".part");
            }
        });
        if (olds != null) {
            for (File f : olds) {
                if (expectedSize > 0 && f.length() == expectedSize) {
                    Log.i(TAG, "update_found_matching_size_apk size=" + f.length() + " file=" + f.getAbsolutePath());
                    return f;
                }
            }
        }
        return null;
    }

    private void downloadAndInstall(final RuyiApi.VersionInfo info) {
        Log.i(TAG, "downloadAndInstall ENTER url=" + info.updateUrl);
        if (info.updateUrl == null || info.updateUrl.length() == 0) {
            Log.w(TAG, "updateUrl is empty, cannot download");
            return;
        }

        if (downloadingNow) {
            Log.i(TAG, "downloadAndInstall SKIP_ALREADY_DOWNLOADING");
            return;
        }
        downloadingNow = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                File targetFile = null;
                File partFile = null;
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                try {
                    File downloadDir = getUpdateDownloadDir();
                    String targetName = buildTargetApkName(info.updateUrl);
                    targetFile = new File(downloadDir, targetName);

                    File existing = findExistingApk(info.updateUrl, -1);
                    if (existing != null && existing.exists()) {
                        final File apkFile = existing;
                        Log.i(TAG, "update_found_existing_skip_network=" + apkFile.getAbsolutePath() + " size=" + apkFile.length());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showInstallConfirmDialog(apkFile);
                            }
                        });
                        return;
                    }

                    Log.i(TAG, "update_no_local_apk_connecting_server...");
                    URL url = new URL(info.updateUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    Log.i(TAG, "update_server_response code=" + responseCode);
                    if (responseCode != 200 && responseCode != 206) {
                        throw new IOException("HTTP " + responseCode);
                    }
                    long contentLength = -1;
                    String clHeader = conn.getHeaderField("Content-Length");
                    if (clHeader != null) {
                        try { contentLength = Long.parseLong(clHeader); } catch (NumberFormatException ignored) {}
                    }
                    if (contentLength < 0) {
                        int clInt = conn.getContentLength();
                        contentLength = clInt > 0 ? clInt : -1;
                    }
                    Log.i(TAG, "update_server_content_length=" + contentLength);
                    conn.disconnect();
                    conn = null;

                    existing = findExistingApk(info.updateUrl, contentLength);
                    if (existing != null && existing.exists() && existing.length() >= contentLength - 1024) {
                        final File apkFile = existing;
                        Log.i(TAG, "update_skip_download_after_check=" + apkFile.getAbsolutePath() + " size=" + apkFile.length());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showInstallConfirmDialog(apkFile);
                            }
                        });
                        return;
                    }

                    partFile = new File(downloadDir, targetName + ".part");
                    long downloaded = partFile.exists() ? partFile.length() : 0;
                    long totalSize = contentLength > 0 ? contentLength : -1;
                    if (downloaded > 0 && totalSize > 0 && downloaded >= totalSize - 1024) {
                        if (!partFile.renameTo(targetFile)) {
                            copyFile(partFile, targetFile);
                        }
                        if (partFile.exists()) {
                            partFile.delete();
                        }
                        final File apkFile = targetFile;
                        Log.i(TAG, "update_part_already_complete renamed=" + apkFile.getAbsolutePath());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showInstallConfirmDialog(apkFile);
                            }
                        });
                        return;
                    }

                    final long fTotalSize = totalSize;
                    final long fResumeFrom = downloaded;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (fResumeFrom > 0) {
                                showUpdatePanel("断点续传中 " + (fResumeFrom / 1024) + "KB ...");
                            } else {
                                showUpdatePanel("正在连接服务器...");
                            }
                        }
                    });

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(20000);
                    conn.setRequestMethod("GET");
                    conn.setInstanceFollowRedirects(true);
                    if (downloaded > 0) {
                        conn.setRequestProperty("Range", "bytes=" + downloaded + "-");
                    }
                    conn.connect();
                    responseCode = conn.getResponseCode();
                    if (downloaded > 0 && responseCode == 206) {
                        Log.i(TAG, "update_resume_supported resumeFrom=" + downloaded);
                        String clHeader2 = conn.getHeaderField("Content-Length");
                        long cl = -1;
                        if (clHeader2 != null) { try { cl = Long.parseLong(clHeader2); } catch (NumberFormatException ignored) {} }
                        if (cl > 0 && totalSize < 0) totalSize = downloaded + cl;
                        fos = new FileOutputStream(partFile, true);
                    } else if (responseCode == 200) {
                        if (downloaded > 0) {
                            Log.i(TAG, "update_resume_not_supported restart_from_zero");
                            downloaded = 0;
                            if (partFile.exists()) partFile.delete();
                        }
                        String clHeader3 = conn.getHeaderField("Content-Length");
                        if (clHeader3 != null) { try { totalSize = Long.parseLong(clHeader3); } catch (NumberFormatException ignored) {} }
                        if (totalSize < 0) {
                            int clInt2 = conn.getContentLength();
                            totalSize = clInt2 > 0 ? clInt2 : -1;
                        }
                        fos = new FileOutputStream(partFile, false);
                    } else {
                        throw new IOException("HTTP " + responseCode);
                    }

                    bis = new BufferedInputStream(conn.getInputStream(), 65536);
                    byte[] buf = new byte[8192];
                    long totalNow = downloaded;
                    long lastUpdate = 0;
                    while (true) {
                        if (downloadCancelled) {
                            Log.i(TAG, "update_cancelled_while_downloading");
                            break;
                        }
                        int read = bis.read(buf);
                        if (read == -1) break;
                        fos.write(buf, 0, read);
                        totalNow += read;
                        long now = System.currentTimeMillis();
                        if (now - lastUpdate > 400) {
                            final long fDownloaded = totalNow;
                            final int pct = totalSize > 0 ? (int)(fDownloaded * 100 / totalSize) : -1;
                            final long kbDone = fDownloaded / 1024;
                            final long kbTotal = totalSize / 1024;
                            final String progressText;
                            if (pct >= 0) {
                                progressText = "下载中 " + pct + "% (" + kbDone + "/" + kbTotal + "KB)";
                            } else {
                                progressText = "下载中 " + kbDone + "KB";
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateProgressPanel(pct, progressText);
                                }
                            });
                            lastUpdate = now;
                        }
                    }
                    fos.flush();

                    if (downloadCancelled) {
                        Log.i(TAG, "update_cancelled_partial saved=" + totalNow);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                hideUpdatePanel();
                            }
                        });
                        return;
                    }

                    if (totalSize > 0 && totalNow < totalSize - 1024) {
                        Log.w(TAG, "update_incomplete_download saved=" + totalNow + " expected=" + totalSize);
                        final long fSaved = totalNow;
                        final long fExpected = totalSize;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                hideUpdatePanel();
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("下载中断");
                                builder.setMessage("检测到下载未完成，下次将自动断点续传。\n\n已保存 " + (fSaved/1024) + "KB / " + (fExpected/1024) + "KB");
                                builder.setPositiveButton("好的", null);
                                builder.show();
                            }
                        });
                        return;
                    }

                    if (!partFile.renameTo(targetFile)) {
                        copyFile(partFile, targetFile);
                    }
                    if (partFile.exists()) {
                        partFile.delete();
                    }
                    cleanOldApks(downloadDir, targetFile);

                    Log.i(TAG, "update_download_complete file=" + targetFile.getAbsolutePath() + " size=" + targetFile.length());
                    final File apkFile = targetFile;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideUpdatePanel();
                            showInstallConfirmDialog(apkFile);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "download failed: " + e.getMessage());
                    final String errMsg = e.getMessage();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideUpdatePanel();
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
                    downloadingNow = false;
                }
            }
        }).start();
    }

    private void copyFile(File src, File dst) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new java.io.FileInputStream(src));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dst));
        byte[] buf = new byte[8192];
        int n;
        while ((n = bis.read(buf)) > 0) bos.write(buf, 0, n);
        bis.close();
        bos.close();
    }

    private void cleanOldApks(File dir, File keep) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.equals(keep)) continue;
            if (f.getName().endsWith(".apk") || f.getName().endsWith(".part")) {
                try {
                    f.delete();
                    Log.d(TAG, "update_clean_old=" + f.getName());
                } catch (Exception ignored) {}
            }
        }
    }

    private void showInstallConfirmDialog(final File apkFile) {
        Log.i(TAG, "showInstallConfirmDialog CALLED file=" + apkFile.getAbsolutePath() + " exists=" + apkFile.exists() + " size=" + apkFile.length());
        if (apkFile == null || !apkFile.exists() || apkFile.length() < 1024 * 1024) {
            Log.w(TAG, "showInstallConfirmDialog invalid apk file");
            return;
        }
        if (isFinishing()) {
            Log.w(TAG, "showInstallConfirmDialog activity finishing, skip dialog, direct install");
            installApk(apkFile);
            return;
        }
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "showInstallConfirmDialog error: " + e.getMessage());
            installApk(apkFile);
        }
    }

    private void installApk(File apkFile) {
        try {
            if (apkFile == null || !apkFile.exists()) {
                Log.e(TAG, "installApk SKIP_FILE_NOT_EXISTS");
                return;
            }
            long sz = apkFile.length();
            if (sz < 1024 * 1024) {
                Log.e(TAG, "installApk SKIP_FILE_TOO_SMALL size=" + sz);
                return;
            }
            Log.i(TAG, "installApk ENTER file=" + apkFile.getAbsolutePath() + " size=" + sz);
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
        cancelBootAlarm();
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
        releasePlayer();
        if (mUseIjkPlayer) {
            if (mIjkSurfaceView != null) {
                mIjkSurfaceView.setVisibility(View.GONE);
                mIjkSurfaceView = null;
            }
        } else {
            if (playerView != null) {
                playerView.setPlayer(null);
                playerView = null;
            }
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
        int absIndex = 0;

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

        private List<Channel> items = new ArrayList<>();

        List<Channel> getItems() {
            return items;
        }

        void updateData(List<Channel> newList) {
            items = new ArrayList<>(newList != null ? newList : new ArrayList<Channel>());
            notifyDataSetChanged();
        }

        void setItems(List<Channel> newList) {
            items = new ArrayList<>(newList != null ? newList : new ArrayList<Channel>());
        }

        LinearLayout createItemView(final Channel ch, final int position) {
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
            numView.setTextColor(0xFFBBBBBB);
            int numW = dp(44);
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(numW, ViewGroup.LayoutParams.WRAP_CONTENT);
            numView.setLayoutParams(nlp);
            numView.setGravity(Gravity.CENTER);
            numView.setSingleLine(true);
            numView.setText(String.format("%03d", position + 1));

            TextView nameView = new TextView(MainActivity.this);
            nameView.setTextSize(19);
            nameView.setTextColor(Color.WHITE);
            nameView.setSingleLine(true);
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            nameView.setLayoutParams(mlp);
            nameView.setText(ch.name);

            TextView latencyView = new TextView(MainActivity.this);
            latencyView.setTextSize(12);
            latencyView.setTextColor(Color.GRAY);
            latencyView.setGravity(Gravity.CENTER);
            latencyView.setSingleLine(true);
            int latW = dp(70);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(latW, ViewGroup.LayoutParams.WRAP_CONTENT);
            latencyView.setLayoutParams(llp);

            StreamLine best = findBestLine(ch);
            if (best != null) {
                if (!best.checked) {
                    latencyView.setText("...");
                    latencyView.setTextColor(0xFF888888);
                } else if (!best.good) {
                    latencyView.setText("维护中");
                    latencyView.setTextColor(Color.parseColor("#B71C1C"));
                } else {
                    latencyView.setText(best.latencyMs + "ms");
                    latencyView.setTextColor(latencyColor(best.latencyMs, true));
                }
            } else {
                latencyView.setText("—");
                latencyView.setTextColor(0xFF555555);
            }

            int absIdx = ch.absIndex;
            if (position == selectedChannelInGroup && !focusOnGroupSide) {
                bg.setColor(0xFF1D6FFF);
                numView.setTextColor(0xFFFFFFFF);
                latencyView.setTextColor(Color.WHITE);
            } else if (absIdx == currentIndex) {
                bg.setColor(0x44222222);
                nameView.setTextColor(0xFFE0E0E0);
            }

            container.addView(numView);
            container.addView(nameView);
            container.addView(latencyView);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChannelClicked(position);
                }
            });
            return container;
        }

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
            numView.setTextColor(0xFFBBBBBB);
            int numW = dp(44);
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(numW, ViewGroup.LayoutParams.WRAP_CONTENT);
            numView.setLayoutParams(nlp);
            numView.setGravity(Gravity.CENTER);
            numView.setSingleLine(true);
            // API 19 必须设置初始文本，否则空文本测量高度=0，setText后也不重绘
            numView.setText("000");

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
            // API 19 必须设置初始文本
            latencyView.setText("---");

            container.addView(numView);
            container.addView(nameView);
            container.addView(latencyView);
            return new VH(container, numView, nameView, latencyView);
        }

        @Override
        public void onBindViewHolder(VH holder, final int position) {
            if (position < 0 || position >= items.size()) return;
            Channel ch = items.get(position);
            int absIdx = ch.absIndex;

            holder.numView.setText(String.format("%03d", position + 1));
            holder.numView.setTextColor(0xFFBBBBBB);
            holder.nameView.setText(ch.name);

            StreamLine best = findBestLine(ch);
            if (best != null) {
                if (!best.checked) {
                    holder.latencyView.setText("...");
                    holder.latencyView.setTextColor(0xFF888888);
                } else if (!best.good) {
                    holder.latencyView.setText("维护中");
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
                holder.numView.setTextColor(0xFFBBBBBB);
                holder.nameView.setTextColor(0xFFE0E0E0);
            } else {
                bg.setColor(Color.TRANSPARENT);
                bg.setStroke(0, 0);
                holder.numView.setTextColor(0xFFBBBBBB);
                holder.nameView.setTextColor(Color.WHITE);
            }

            // API 19 兼容：强制子视图重绘以确保文本显示
            if (Build.VERSION.SDK_INT < 20) {
                holder.numView.invalidate();
                holder.latencyView.invalidate();
                holder.itemView.invalidate();
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChannelClicked(position);
                }
            });
        }

        /** 找到频道中最优线路：优先已检测且正常的，其次已检测的，最后取 currentLineIndex */
        private StreamLine findBestLine(Channel ch) {
            if (ch == null || ch.lines.isEmpty()) return null;
            // 1. 已检测且正常的线路中选延迟最低的
            StreamLine best = null;
            long bestLat = Long.MAX_VALUE;
            for (StreamLine sl : ch.lines) {
                if (sl.checked && sl.good) {
                    if (sl.latencyMs >= 0 && sl.latencyMs < bestLat) {
                        bestLat = sl.latencyMs;
                        best = sl;
                    }
                }
            }
            if (best != null) return best;
            // 2. 取已检测的线路（无论好坏）
            for (StreamLine sl : ch.lines) {
                if (sl.checked) return sl;
            }
            // 3. 取 currentLineIndex 对应的线路
            int idx = Math.min(ch.currentLineIndex, ch.lines.size() - 1);
            if (idx >= 0) return ch.lines.get(idx);
            // 4. 最后保底取第一条
            return ch.lines.get(0);
        }

        @Override
        public int getItemCount() {
            return items.size();
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
                            } else if (channelRecyclerView != null || channelListContainer != null) {
                                View sv = (Build.VERSION.SDK_INT < 20 && channelListContainer != null) ? channelListContainer : channelRecyclerView;
                                if (sv != null) sv.scrollBy(0, (int) -stepDy);
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

    private void scheduleBootAlarm() {
        try {
            cancelBootAlarm();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(this, 10001, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null) {
                long trigger = SystemClock.elapsedRealtime() + 3000;
                am.set(AlarmManager.ELAPSED_REALTIME, trigger, pi);
                Log.i("YSPTV", "scheduleBootAlarm: one-shot alarm set (not repeating)");
            }
        } catch (Throwable t) {
            Log.e("YSPTV", "scheduleBootAlarm failed", t);
        }
    }

    private void ensureBootReceiverEnabled() {
        try {
            ComponentName cn = new ComponentName(this, BootReceiver.class);
            PackageManager pm = getPackageManager();
            int state = pm.getComponentEnabledSetting(cn);
            if (state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(cn,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
                Log.i("YSPTV", "ensureBootReceiverEnabled: re-enabled BootReceiver (was=" + state + ")");
            }
        } catch (Throwable t) {
            Log.e("YSPTV", "ensureBootReceiverEnabled failed", t);
        }
    }
}