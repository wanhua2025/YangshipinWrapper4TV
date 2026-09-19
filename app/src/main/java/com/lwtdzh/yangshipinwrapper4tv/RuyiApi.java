package com.lwtdzh.yangshipinwrapper4tv;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class RuyiApi {
    private static final String TAG = "RUYI";
    private static final String BASE_URL = "http://ry.400239.com";
    private static final String API_PATH = "/api.php";
    private static final int APPID = 10000;
    private static final String APPKEY = "4d86cdb33aa6f9dd27c4e6adee49995e";
    private static final String RC4_KEY = "GN8ZGa4DmaHQrHhSTyQ3FwnhCQt68EXQ";
    private static final String PREFS_FILE = "ruyi_prefs";
    private static final String PREF_TOKEN = "token";
    private static final String PREF_USERNAME = "username";

    private static final Charset GBK = Charset.forName("GBK");
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final android.content.Context context;
    private final String deviceId;
    private String token;
    private long vipTime;
    private String username;
    private long lastHeartbeat;
    private boolean registered;

    public interface RuyiCallback {
        void onResult(RuyiResult result);
    }

    public static class RuyiResult {
        public final int code;
        public final String message;
        public final String token;
        public final long vipTime;
        public final long tryTime;

        public RuyiResult(int code, String message, String token, long vipTime, long tryTime) {
            this.code = code;
            this.message = message;
            this.token = token;
            this.vipTime = vipTime;
            this.tryTime = tryTime;
        }

        public boolean isSuccess() {
            return code == 200;
        }
    }

    public RuyiApi(android.content.Context ctx, String deviceId) {
        this.context = ctx.getApplicationContext();
        this.deviceId = deviceId;
        loadPrefs();
    }

    private void loadPrefs() {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, android.content.Context.MODE_PRIVATE);
        this.token = prefs.getString(PREF_TOKEN, null);
        this.username = prefs.getString(PREF_USERNAME, null);
        this.registered = (token != null);
    }

    private void savePrefs() {
        context.getSharedPreferences(PREFS_FILE, android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_TOKEN, token)
                .putString(PREF_USERNAME, username)
                .apply();
    }

    public String getDeviceId() { return deviceId; }
    public String getToken() { return token; }
    public long getVipTime() { return vipTime; }
    public String getUsername() { return username; }
    public boolean isRegistered() { return registered && token != null; }
    public boolean isVip() { return vipTime == 999999999L || vipTime > System.currentTimeMillis() / 1000L; }

    public void register(final String user, final String password, final RuyiCallback cb) {
        android.os.AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long t = System.currentTimeMillis() / 1000L;
                    List<Param> params = new ArrayList<Param>();
                    params.add(new Param("user", user));
                    params.add(new Param("password", password));
                    params.add(new Param("markcode", deviceId));
                    params.add(new Param("t", String.valueOf(t)));

                    ApiResp resp = callApi("user_reg", params);
                    RuyiResult result;
                    if (resp.code == 200) {
                        username = user;
                        registered = true;
                        savePrefs();
                        result = new RuyiResult(200, resp.msg, null, 0, 0);
                    } else {
                        result = new RuyiResult(resp.code, resp.msg, null, 0, 0);
                    }
                    cb.onResult(result);
                } catch (Exception e) {
                    Log.e(TAG, "register error", e);
                    cb.onResult(new RuyiResult(-1, "Network error: " + e.getMessage(), null, 0, 0));
                }
            }
        });
    }

    public void login(final String account, final String password, final RuyiCallback cb) {
        android.os.AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long t = System.currentTimeMillis() / 1000L;
                    List<Param> params = new ArrayList<Param>();
                    params.add(new Param("account", account));
                    params.add(new Param("password", password));
                    params.add(new Param("markcode", deviceId));
                    params.add(new Param("t", String.valueOf(t)));

                    ApiResp resp = callApi("user_logon", params);
                    if (resp.code != 200) {
                        cb.onResult(new RuyiResult(resp.code, resp.msg, null, 0, 0));
                        return;
                    }
                    String decodedMsg;
                    try {
                        decodedMsg = miRc4Decrypt(resp.msg);
                    } catch (Exception e) {
                        decodedMsg = resp.msg;
                    }
                    JSONObject json = new JSONObject(decodedMsg);
                    String newToken = json.optString("token", null);
                    long vip = 0;
                    JSONObject info = json.optJSONObject("info");
                    if (info != null) {
                        String vipStr = info.optString("vip", "0");
                        try { vip = Long.parseLong(vipStr); } catch (NumberFormatException ignored) {}
                        username = info.optString("user", null);
                    }
                    if (newToken == null || newToken.length() == 0) {
                        newToken = json.optString("token", null);
                    }
                    token = newToken;
                    vipTime = vip;
                    registered = true;
                    savePrefs();
                    cb.onResult(new RuyiResult(200, "Login OK", token, vip, 0));
                } catch (Exception e) {
                    Log.e(TAG, "login error", e);
                    cb.onResult(new RuyiResult(-1, "Network error: " + e.getMessage(), null, 0, 0));
                }
            }
        });
    }

    public void autoRegisterOrLogin(final RuyiCallback cb) {
        if (isRegistered() && token != null) {
            login(username, "tvpass01", new RuyiCallback() {
                @Override
                public void onResult(RuyiResult result) {
                    if (result.code == 200) {
                        cb.onResult(result);
                    } else if (result.code == 114 || result.code == 102) {
                        Log.w(TAG, "login blocked code=" + result.code + " keeping token for heartbeat monitoring");
                        cb.onResult(result);
                    } else {
                        Log.w(TAG, "login failed code=" + result.code + " re-registering");
                        registered = false;
                        token = null;
                        username = null;
                        savePrefs();
                        doRegister(cb);
                    }
                }
            });
        } else {
            doRegister(cb);
        }
    }

    private void doRegister(final RuyiCallback cb) {
        String autoUser = "auto_" + deviceId.replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(8, deviceId.length()));
        final String user = autoUser;
        final String pass = "tvpass01";
        register(user, pass, new RuyiCallback() {
            @Override
            public void onResult(RuyiResult regResult) {
                if (regResult.code == 200 || regResult.code == 115) {
                    login(user, pass, cb);
                } else {
                    cb.onResult(regResult);
                }
            }
        });
    }

    public void heartbeat(final RuyiCallback cb) {
        android.os.AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (token == null) {
                        cb.onResult(new RuyiResult(-1, "Not logged in", null, 0, 0));
                        return;
                    }
                    long t = System.currentTimeMillis() / 1000L;
                    List<Param> params = new ArrayList<Param>();
                    params.add(new Param("token", token));
                    params.add(new Param("t", String.valueOf(t)));

                    ApiResp resp = callApi("motion", params);
                    lastHeartbeat = System.currentTimeMillis() / 1000L;
                    Log.d(TAG, "heartbeat resp code=" + resp.code + " msg_len=" + (resp.msg == null ? 0 : resp.msg.length()));
                    if (resp.code != 200) {
                        if (resp.code == 125 || resp.code == 127) {
                            token = null;
                            registered = false;
                            savePrefs();
                        }
                        cb.onResult(new RuyiResult(resp.code, resp.msg, null, 0, 0));
                        return;
                    }
                    String decodedMsg;
                    try {
                        decodedMsg = miRc4Decrypt(resp.msg);
                        Log.d(TAG, "heartbeat decoded=" + decodedMsg);
                    } catch (Exception e) {
                        Log.w(TAG, "heartbeat decrypt fail: " + e.getMessage() + " raw=" + resp.msg);
                        decodedMsg = resp.msg;
                    }
                    long tryVal = 0;
                    try {
                        JSONObject json = new JSONObject(decodedMsg);
                        tryVal = json.optLong("Try", 0);
                        String vipStr = json.optString("vip", "0");
                        try {
                            long newVip = Long.parseLong(vipStr);
                            if (newVip > 0) { vipTime = newVip; }
                        } catch (NumberFormatException ignored) {}
                        Log.d(TAG, "heartbeat parsed vip=" + vipTime + " try=" + tryVal);
                    } catch (Exception e) {
                        Log.w(TAG, "heartbeat parse msg=" + decodedMsg);
                    }
                    cb.onResult(new RuyiResult(200, "OK", token, vipTime, tryVal));
                } catch (Exception e) {
                    Log.e(TAG, "heartbeat error", e);
                    cb.onResult(new RuyiResult(-1, "Network error: " + e.getMessage(), null, 0, 0));
                }
            }
        });
    }

    public interface VersionCallback {
        void onVersionResult(VersionInfo info);
    }

    public static class VersionInfo {
        public boolean hasUpdate;
        public String remoteVersion;
        public String updateUrl;
        public String updateNotes;
        public boolean compel;

        public VersionInfo(boolean hasUpdate, String remoteVersion, String updateUrl, String updateNotes, boolean compel) {
            this.hasUpdate = hasUpdate;
            this.remoteVersion = remoteVersion;
            this.updateUrl = updateUrl;
            this.updateNotes = updateNotes;
            this.compel = compel;
        }
    }

    public void checkVersion(final String currentVersion, final VersionCallback cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long t = System.currentTimeMillis() / 1000L;
                    List<Param> params = new ArrayList<Param>();
                    params.add(new Param("t", String.valueOf(t)));
                    ApiResp resp = callApi("ini", params);
                    Log.d(TAG, "checkVersion code=" + resp.code + " msg=" + resp.msg);
                    if (resp.code != 200) {
                        cb.onVersionResult(new VersionInfo(false, currentVersion, "", "", false));
                        return;
                    }
                    JSONObject iniJson = new JSONObject(resp.msg);
                    String remoteBb = iniJson.optString("app_bb", currentVersion);
                    String remoteNshow = iniJson.optString("app_nshow", "");
                    String remoteNurl = iniJson.optString("app_nurl", "");
                    String compelStr = iniJson.optString("compel", "0");
                    boolean compel = "1".equals(compelStr);
                    boolean hasUpdate = compareVersion(remoteBb, currentVersion) > 0;
                    Log.i(TAG, "checkVersion current=" + currentVersion + " remote=" + remoteBb + " hasUpdate=" + hasUpdate + " compel=" + compel);
                    cb.onVersionResult(new VersionInfo(hasUpdate, remoteBb, remoteNurl, remoteNshow, compel));
                } catch (Exception e) {
                    Log.w(TAG, "checkVersion error: " + e.getMessage());
                    cb.onVersionResult(new VersionInfo(false, currentVersion, "", "", false));
                }
            }
        }).start();
    }

    public static int compareVersion(String v1, String v2) {
        String[] p1 = v1.split("\\.");
        String[] p2 = v2.split("\\.");
        int len = Math.max(p1.length, p2.length);
        for (int i = 0; i < len; i++) {
            int n1 = i < p1.length ? Integer.parseInt(p1[i].trim()) : 0;
            int n2 = i < p2.length ? Integer.parseInt(p2[i].trim()) : 0;
            if (n1 != n2) return n1 - n2;
        }
        return 0;
    }

    private ApiResp callApi(String act, List<Param> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append('&');
            Param p = params.get(i);
            sb.append(p.key).append('=').append(p.value);
        }
        String plain = sb.toString();
        String rc4Hex = miRc4Encrypt(plain);
        String sign = arrSign(sb.toString() + "&");

        String urlStr = BASE_URL + API_PATH + "?app=" + APPID + "&act=" + act + "&data=" + rc4Hex + "&sign=" + sign;
        Log.d(TAG, "callApi url=" + urlStr);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "YangshipinTV/1.0");

        int httpCode = conn.getResponseCode();
        BufferedReader reader;
        if (httpCode >= 200 && httpCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF8));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), UTF8));
        }
        StringBuilder respSb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) respSb.append(line);
        reader.close();
        conn.disconnect();

        String raw = respSb.toString();
        Log.d(TAG, "callApi raw=" + raw);

        String jsonStr = extractJson(raw);
        if (jsonStr == null) {
            throw new Exception("No JSON in response: " + raw.substring(0, Math.min(200, raw.length())));
        }
        JSONObject json = new JSONObject(jsonStr);
        int code = json.optInt("code", -1);
        String msg;
        try {
            Object msgObj = json.get("msg");
            msg = msgObj instanceof JSONObject ? ((JSONObject) msgObj).toString() : String.valueOf(msgObj);
        } catch (Exception e) {
            msg = "";
        }
        return new ApiResp(code, msg);
    }

    private static class ApiResp {
        final int code;
        final String msg;
        ApiResp(int code, String msg) { this.code = code; this.msg = msg; }
    }

    private static String extractJson(String raw) {
        if (raw == null || raw.length() == 0) return null;
        int start = raw.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        for (int i = start; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return raw.substring(start, i + 1);
            }
        }
        return null;
    }

    private static class Param {
        final String key;
        final String value;
        Param(String key, String value) { this.key = key; this.value = value; }
    }

    private static String arrSign(String keyvalStr) {
        String src = keyvalStr.endsWith("&") ? keyvalStr.substring(0, keyvalStr.length() - 1) : keyvalStr;
        src = src + "&" + APPKEY;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(src.getBytes(UTF8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b & 0xFF));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String miRc4Encrypt(String plainUtf8) {
        byte[] data = plainUtf8.getBytes(GBK);
        byte[] keyBytes = RC4_KEY.getBytes(GBK);
        byte[] out = rc4Crypt(keyBytes, data);
        StringBuilder hex = new StringBuilder();
        for (byte b : out) hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }

    private static String miRc4Decrypt(String hexCipherUtf8) {
        String hexClean = hexCipherUtf8.replaceAll("\\s", "");
        byte[] data = new byte[hexClean.length() / 2];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) Integer.parseInt(hexClean.substring(i * 2, i * 2 + 2), 16);
        }
        byte[] keyBytes = RC4_KEY.getBytes(GBK);
        byte[] raw = rc4Crypt(keyBytes, data);
        return new String(raw, GBK);
    }

    private static byte[] rc4Crypt(byte[] key, byte[] data) {
        int[] S = new int[256];
        for (int i = 0; i < 256; i++) S[i] = i;
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + (key[i % key.length] & 0xFF)) & 0xFF;
            int tmp = S[i]; S[i] = S[j]; S[j] = tmp;
        }
        int i = 0; j = 0;
        byte[] out = new byte[data.length];
        for (int k = 0; k < data.length; k++) {
            i = (i + 1) & 0xFF;
            j = (j + S[i]) & 0xFF;
            int tmp = S[i]; S[i] = S[j]; S[j] = tmp;
            out[k] = (byte) ((data[k] ^ S[(S[i] + S[j]) & 0xFF]) & 0xFF);
        }
        return out;
    }
}