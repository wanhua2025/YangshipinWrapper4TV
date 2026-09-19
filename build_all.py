# -*- coding: utf-8 -*-
import os
import shutil

src = r"F:\YangshipinWrapper4TV-main_第三版\YangshipinWrapper4TV-main\app\src\main\java\com\lwtdzh\yangshipinwrapper4tv\MainActivity.java"
dst = r"f:\YangshipinWrapper4TV-main\app\src\main\java\com\lwtdzh\yangshipinwrapper4tv\MainActivity.java"

# Copy fresh
shutil.copy2(src, dst)

with open(dst, 'r', encoding='utf-8') as f:
    c = f.read()

print(f"Original size: {len(c)}")

# ===== STEP 1: Import =====
c = c.replace(
    "import java.util.ArrayList;\nimport java.util.List;",
    "import java.util.ArrayList;\nimport java.util.HashMap;\nimport java.util.List;\nimport java.util.Map;"
)
print("1. imports done")

# ===== STEP 2: Constants =====
c = c.replace(
    'private static final String PREF_PLAYBACK_MODE = "playback_mode";',
    'private static final String PREF_PLAYBACK_MODE = "playback_mode";\n    private static final String PREF_AUTO_START = "auto_start_on_boot";'
)

old_menu_consts = """    private static final int MENU_PAGE_MAIN = 0;
    private static final int MENU_PAGE_CHANNELS = 1;
    private static final int MENU_PAGE_SETTINGS = 2;
    private static final int MAIN_MENU_SETTINGS = 0;
    private static final int MAIN_MENU_CHANNELS = 1;
    private static final String[] MAIN_MENU_ITEMS = new String[]{"Settings", "Channels"};"""

new_menu_consts = """    private static final int MENU_PAGE_CHANNELS = 0;
    private static final int MENU_PAGE_SETTINGS = 1;
    private static final int SETTINGS_ITEM_COUNT = 2;
    private static final int SETTINGS_IDX_DECODER = 0;
    private static final int SETTINGS_IDX_AUTOSTART = 1;"""

c = c.replace(old_menu_consts, new_menu_consts)
print("2. constants done")

# ===== STEP 3: Field rename =====
c = c.replace("private WebView bridgeWebView;\n    private GestureTraceView gestureTraceView;",
              "private WebView bridgeWebView;\n    private FrameLayout loadingOverlay;\n    private GestureTraceView gestureTraceView;")
c = c.replace("private int mainMenuSelection = MAIN_MENU_CHANNELS;\n    private int settingsSelection = 0;\n    private int menuPage = MENU_PAGE_CHANNELS;",
              "private int settingsSelection = 0;\n    private int menuPage = MENU_PAGE_CHANNELS;\n    private boolean autoStartOnBoot = true;")
print("3. fields done")

# ===== STEP 4: Safe symbol renames (Python handles UTF-8 perfectly) =====
c = c.replace("MENU_PAGE_MAIN", "MENU_PAGE_CHANNELS")
c = c.replace("MAIN_MENU_SETTINGS", "SETTINGS_IDX_DECODER")
c = c.replace("MAIN_MENU_CHANNELS", "currentIndex")
c = c.replace("MAIN_MENU_ITEMS.length", "SETTINGS_ITEM_COUNT")
c = c.replace('MAIN_MENU_ITEMS[position]', '"Settings"')
c = c.replace("mainMenuSelection", "settingsSelection")
print("4. symbol renames done")

# ===== STEP 5: autoStartOnBoot pref loading =====
c = c.replace(
    """        playbackMode = preferences.getString(PREF_PLAYBACK_MODE, PLAYBACK_MODE_HW);
        if (!PLAYBACK_MODE_SW.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_HW;
        }

        buildUi();""",
    """        playbackMode = preferences.getString(PREF_PLAYBACK_MODE, PLAYBACK_MODE_HW);
        if (!PLAYBACK_MODE_SW.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_HW;
        }
        autoStartOnBoot = preferences.getBoolean(PREF_AUTO_START, true);

        buildUi();"""
)
print("5. autoStartOnBoot pref done")

# ===== STEP 6: CHANNEL_NAME_MAP =====
channel_map_block = """
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
"""
old_handler = """    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Channel> channels = new ArrayList<Channel>();"""
c = c.replace(old_handler, old_handler + channel_map_block)
print("6. CHANNEL_NAME_MAP done")

# ===== STEP 7: loadingOverlay + bridgeWebView INVISIBLE =====
old_bridge = """        bridgeWebView.addJavascriptInterface(new BridgeCallbacks(), "YspAndroid");
        FrameLayout.LayoutParams bridgeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(bridgeWebView, 0, bridgeParams);
        bridgeWebView.loadUrl(YSP_HOME_URL);"""

new_bridge = """        bridgeWebView.addJavascriptInterface(new BridgeCallbacks(), "YspAndroid");
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

        bridgeWebView.loadUrl(YSP_HOME_URL);"""

c = c.replace(old_bridge, new_bridge)
print("7. loadingOverlay done")

# ===== STEP 8: ruyiStatusText init GONE =====
c = c.replace(
    """        ruyiStatusText = new TextView(this);
        ruyiStatusText.setTextColor(0xFF00FF88);""",
    """        ruyiStatusText = new TextView(this);
        ruyiStatusText.setVisibility(View.GONE);
        ruyiStatusText.setTextColor(0xFF00FF88);"""
)
print("8. ruyiStatusText init done")

# ===== STEP 9: updateRuyiStatus 7s auto-hide =====
old_ruyi_update = """    private void updateRuyiStatus() {
        if (ruyiStatusText != null) {
            if (ruyiApi != null && ruyiApi.isRegistered()) {
                ruyiStatusStr = "Ruyi: OK | User: " + ruyiApi.getUsername()
                        + " | VIP: " + (ruyiApi.isVip() ? "Yes" : "No")
                        + " | Dev: " + ruyiApi.getDeviceId();
            }
            ruyiStatusText.setText(ruyiStatusStr);
        }
    }"""

new_ruyi_update = """    private void updateRuyiStatus() {
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
    };"""

c = c.replace(old_ruyi_update, new_ruyi_update)
print("9. updateRuyiStatus 7s done")

# ===== STEP 10: Channel name mapping in onChannelsLoaded =====
old_add_channel = """                channels.add(new Channel(
                        object.optString("name"),
                        object.optString("pid"),
                        object.optString("streamId"),
                        object.optString("type"),
                        object.optBoolean("is4K", false)));"""

new_add_channel = """                String rawName = object.optString("name");
                String displayName = CHANNEL_NAME_MAP.get(rawName) != null
                        ? CHANNEL_NAME_MAP.get(rawName) : rawName;
                channels.add(new Channel(
                        displayName,
                        object.optString("pid"),
                        object.optString("streamId"),
                        object.optString("type"),
                        object.optBoolean("is4K", false)));"""

c = c.replace(old_add_channel, new_add_channel)
print("10. channel name map done")

# ===== STEP 11: loadingOverlay hide on playback result =====
old_playback_ok = """            updateStatus();
            Log.i(TAG, "web_playback id=" + requestId + " ok=true quality=" + preferredQuality);"""

new_playback_ok = """            if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
                loadingOverlay.setVisibility(View.GONE);
                if (bridgeWebView != null) {
                    bridgeWebView.setVisibility(View.VISIBLE);
                }
                Log.i(TAG, "loading_hidden_webview_shown");
            }
            updateStatus();
            Log.i(TAG, "web_playback id=" + requestId + " ok=true quality=" + preferredQuality);"""

c = c.replace(old_playback_ok, new_playback_ok)
print("11. loadingOverlay hide done")

with open(dst, 'w', encoding='utf-8') as f:
    f.write(c)

print(f"\nAll 11 steps done. Final file size: {len(c)}")
print("Now run: gradlew assembleDebug")