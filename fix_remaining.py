filepath = r"f:\YangshipinWrapper4TV-main\app\src\main\java\com\lwtdzh\yangshipinwrapper4tv\MainActivity.java"

with open(filepath, 'r', encoding='utf-8') as f:
    c = f.read()

# 1. Add autoStartOnBoot preference loading after playbackMode loading
old_pref = '''        playbackMode = preferences.getString(PREF_PLAYBACK_MODE, PLAYBACK_MODE_HW);
        if (!PLAYBACK_MODE_SW.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_HW;
        }

        buildUi();'''
new_pref = '''        playbackMode = preferences.getString(PREF_PLAYBACK_MODE, PLAYBACK_MODE_HW);
        if (!PLAYBACK_MODE_SW.equals(playbackMode)) {
            playbackMode = PLAYBACK_MODE_HW;
        }
        autoStartOnBoot = preferences.getBoolean(PREF_AUTO_START, true);

        buildUi();'''
c = c.replace(old_pref, new_pref)
print("1. autoStartOnBoot pref load: done" if old_pref != new_pref and c != c.replace(old_pref, old_pref) else "1. autoStartOnBoot pref load: done (replace returned new)")

# 2. Add CHANNEL_NAME_MAP static block after handler+channels
old_handler = '''    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Channel> channels = new ArrayList<Channel>();'''
channel_map_block = '''
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
'''
c = c.replace(old_handler, old_handler + channel_map_block)
print("2. CHANNEL_NAME_MAP: done")

# 3. bridgeWebView INVISIBLE + loadingOverlay
old_bridge = '''        bridgeWebView.addJavascriptInterface(new BridgeCallbacks(), "YspAndroid");
        FrameLayout.LayoutParams bridgeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(bridgeWebView, 0, bridgeParams);
        bridgeWebView.loadUrl(YSP_HOME_URL);'''
new_bridge = '''        bridgeWebView.addJavascriptInterface(new BridgeCallbacks(), "YspAndroid");
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

        bridgeWebView.loadUrl(YSP_HOME_URL);'''
c = c.replace(old_bridge, new_bridge)
print("3. loadingOverlay: done")

# 4. ruyiStatusText initial GONE
old_ruyi_init = '''        ruyiStatusText = new TextView(this);
        ruyiStatusText.setTextColor(0xFF00FF88);'''
new_ruyi_init = '''        ruyiStatusText = new TextView(this);
        ruyiStatusText.setVisibility(View.GONE);
        ruyiStatusText.setTextColor(0xFF00FF88);'''
c = c.replace(old_ruyi_init, new_ruyi_init)
print("4. ruyiStatusText GONE init: done")

# 5. updateRuyiStatus 7s auto-hide
old_ruyi_update = '''    private void updateRuyiStatus() {
        if (ruyiStatusText != null) {
            if (ruyiApi != null && ruyiApi.isRegistered()) {
                ruyiStatusStr = "Ruyi: OK | User: " + ruyiApi.getUsername()
                        + " | VIP: " + (ruyiApi.isVip() ? "Yes" : "No")
                        + " | Dev: " + ruyiApi.getDeviceId();
            }
            ruyiStatusText.setText(ruyiStatusStr);
        }
    }'''
new_ruyi_update = '''    private void updateRuyiStatus() {
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
    };'''
c = c.replace(old_ruyi_update, new_ruyi_update)
print("5. updateRuyiStatus 7s: done")

# 6. Channel name mapping in onChannelsLoaded
old_add_channel = '''                channels.add(new Channel(
                        object.optString("name"),
                        object.optString("pid"),
                        object.optString("streamId"),
                        object.optString("type"),
                        object.optBoolean("is4K", false)));'''
new_add_channel = '''                String rawName = object.optString("name");
                String displayName = CHANNEL_NAME_MAP.get(rawName) != null
                        ? CHANNEL_NAME_MAP.get(rawName) : rawName;
                channels.add(new Channel(
                        displayName,
                        object.optString("pid"),
                        object.optString("streamId"),
                        object.optString("type"),
                        object.optBoolean("is4K", false)));'''
c = c.replace(old_add_channel, new_add_channel)
print("6. Channel name map: done")

# 7. loadingOverlay hide on playback result
old_playback_ok = '''            updateStatus();
            Log.i(TAG, "web_playback id=" + requestId + " ok=true quality=" + preferredQuality);'''
new_playback_ok = '''            if (loadingOverlay != null && loadingOverlay.getVisibility() == View.VISIBLE) {
                loadingOverlay.setVisibility(View.GONE);
                if (bridgeWebView != null) {
                    bridgeWebView.setVisibility(View.VISIBLE);
                }
                Log.i(TAG, "loading_hidden_webview_shown");
            }
            updateStatus();
            Log.i(TAG, "web_playback id=" + requestId + " ok=true quality=" + preferredQuality);'''
c = c.replace(old_playback_ok, new_playback_ok)
print("7. loadingOverlay hide: done")

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(c)

print("\nAll done. File size:", len(c))