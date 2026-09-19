# YSPTV (YangshipinWrapper4TV) — 项目技术文档

> 本文档供 AI 编程助手快速理解项目结构、协议细节、服务器对接规范，以便准确修改/扩展代码。

---

## 1. 项目概览

### 1.1 基本信息
| 项 | 值 |
|---|---|
| 包名 | `com.lwtdzh.yangshipinwrapper4tv` |
| App 名称 | YSPTV |
| 版本 | versionCode=3, versionName=1.91 |
| compileSdk | 33 |
| targetSdk | 33 |
| minSdk | 19 (Android 4.4) |
| 语言 | Java 8 (Android) |
| 构建 | Gradle + AGP 7.3.1 |
| 依赖 | 仅 `androidx.core:core:1.9.0` |
| 运行设备 | 小米电视 / Android TV (LEANBACK) |

### 1.2 项目定位
这是一个**电视端 (Leanback) IPTV 视频播放壳应用**，工作原理：

```
┌─────────────┐     WebView (hidden 初始)      ┌──────────────────┐
│  Android TV │ ──────────────────────────────▶ │ yangshipin.cn    │
│  YSPTV App  │                                 │ 网页版视频播放器  │
│             │ ── ysp_bridge.js 注入 ─────────▶│ (劫持 DOM/事件)   │
│             │ ◀── YspAndroid JSInterface ───── │                  │
└──────┬──────┘                                 └──────────────────┘
       │
       ├── RuyiApi ──▶ ry.400239.com (如意API: 登录/注册/心跳/版本)
       │
       ├── 遥控器按键 → 换台/切清晰度/打开菜单
       │
       └── 启动流程: Loading黑幕 → 如意登录 → WebView加载 → 劫持频道 → 请求流 → OK后瞬间显示视频
```

### 1.3 关键设计决策
1. **WebView 劫持方案**：用 WebView 打开 `https://www.yangshipin.cn/tv/home`，然后注入 `ysp_bridge.js` 劫持 DOM 来获取频道列表和视频流 URL
2. **如意服务器鉴权**：注册+登录获取 token，心跳保持在线，VIP 状态由服务器决定
3. **全屏黑幕遮罩**：启动时 WebView 设为 INVISIBLE，上面盖黑色 FrameLayout，收到首个 `ok=true` 播放结果时瞬间切换，避免网页闪现

---

## 2. 文件结构

```
f:\YangshipinWrapper4TV-main\
├── app\
│   ├── build.gradle                    # AGP 配置, SDK版本, 依赖
│   └── src\main\
│       ├── AndroidManifest.xml         # 权限/Activity/Provider/Leanback
│       ├── assets\
│       │   └── ysp_bridge.js           # ⭐ WebView注入脚本 (核心劫持逻辑, ~900行)
│       ├── java\com\lwtdzh\yangshipinwrapper4tv\
│       │   ├── MainActivity.java       # ⭐ 主活动 (1514行, UI+WebView+手势+菜单)
│       │   ├── RuyiApi.java            # ⭐ 如意服务器API (462行, 鉴权/加密/签名)
│       │   └── BootReceiver.java       # 开机自启广播接收器
│       └── res\
│           ├── values\strings.xml      # app_name="YSPTV"
│           ├── values\styles.xml       # AppTheme
│           ├── xml\file_paths.xml      # FileProvider路径
│           └── mipmap-anydpi-v26\      # 图标
├── build.gradle                        # 根项目构建
├── settings.gradle                     # 模块包含
├── gradle.properties                   # JDK17, AGP7.3.1配置
└── local.properties                    # sdk.dir (自动生成)
```

---

## 3. 如意服务器 API (RuyiApi.java)

### 3.1 服务器配置（硬编码常量）
```java
BASE_URL    = "http://ry.400239.com"
API_PATH    = "/api.php"
APPID       = 10000
APPKEY      = "4d86cdb33aa6f9dd27c4e6adee49995e"   // 签名盐
RC4_KEY     = "GN8ZGa4DmaHQrHhSTyQ3FwnhCQt68EXQ"    // RC4加密密钥
Charset     = GBK (数据加解密), UTF-8 (签名MD5)
```

### 3.2 请求协议（3个关键步骤）
所有请求走 **GET**（历史原因，PHP server 从 GET 取 data/sign）：

```
GET http://ry.400239.com/api.php?app={APPID}&act={接口名}&data={RC4密文HEX}&sign={MD5签名}
```

#### Step 1: 拼 data 明文（k=v 格式）
```
t={时间戳}&{参数1}={值1}&{参数2}={值2}&...
```

#### Step 2: RC4 加密 + HEX 编码
```java
byte[] keyBytes = RC4_KEY.getBytes(GBK);    // ⚠️ 必须用 getBytes() 不是 ByteBuffer.array()
byte[] dataBytes = plain.getBytes(GBK);
byte[] encrypted = rc4Crypt(keyBytes, dataBytes);   // 自定义RC4实现（与PHP端兼容）
String data = bytesToHex(encrypted).toLowerCase();
```

#### Step 3: 生成签名
```java
// 把 k=v&k=v 字符串末尾加 "&" + APPKEY，然后 MD5 UTF-8
String signInput = plainString + "&" + APPKEY;
String sign = md5(signInput.getBytes(UTF8)).toLowerCase();
```

### 3.3 接口列表与参数名对照

| act (接口名) | 参数名 | 说明 | 成功code | 备注 |
|---|---|---|---|---|
| `user_reg` | **user**, **password**, **markcode**, t | 注册账号 | 200=成功, 115=账号已存在 | ⚠️ 参数名是 `user` **不是** `account` |
| `user_logon` | **account**, **password**, **markcode**, t | 登录 | 200=成功, 122=账号不存在, 113=密码错 | ⚠️ 参数名是 `account` **不是** `user` |
| `motion` | token, t | 心跳检测 | 200=OK | ⚠️ 接口名是 `motion` 不是 `clock` |
| `ini` | t | 版本/配置检查 | 200=OK | 返回 app_bb, app_nshow, app_nurl, compel |

### 3.4 响应格式
```json
// 所有响应都是 JSON:
{"code": 200, "msg": "...", "time": 1789792047}

// code=200 时:
//   login 的 msg 是 RC4-HEX 密文，解密后是 JSON: {"token":"...", "info":{"vip":"999999999", "user":"auto_tvxxx", ...}}
//   heartbeat 的 msg 也是 RC4-HEX 密文，解密后有 vip 字段
//   ini 的 msg 直接是 JSON: {"app_bb":"1.91", "app_nurl":"...", "compel":"0"}

// 错误码参考:
// 106=签名错误, 110=请填写账号, 111=请填写密码, 112=请填写机器码
// 113=账号密码错误, 114=账号被锁定, 115=账号已存在, 122=账号不存在
// 125/127=token失效 (清除token重新注册)
```

### 3.5 密钥编码踩坑（历史教训）
```java
// ❌ 错误写法（会产生 0x00 填充的 ByteBuffer.array()）:
CHARSET_GBK.encode(RC4_KEY).array()

// ✅ 正确写法:
RC4_KEY.getBytes(CHARSET_GBK)
```

### 3.6 自动注册流程（autoRegisterOrLogin）
```
SharedPreferences 有 token?
  ├─ YES → login(username, "tvpass01")
  │         ├─ 成功 → 返回
  │         ├─ 114/102 → 账号被封，保留token（心跳监控）
  │         └─ 其他 → 清除token → doRegister()
  └─ NO  → doRegister()
            ├─ register("auto_" + deviceId前8位, "tvpass01")
            │   ├─ 200或115 → login()
            │   └─ 其他 → 返回错误
            └─ login()
```

---

## 4. WebView Bridge 协议 (ysp_bridge.js)

### 4.1 Bridge 注入时机
```java
// WebViewClient.onPageFinished → scheduleBridgeInjection(2500ms) → injectBridge()
// injectBridge() 把 assets/ysp_bridge.js 通过 evaluateJavascript 注入
```

### 4.2 JS → Java 事件（@JavascriptInterface）
```java
// BridgeCallbacks 类注册为 "YspAndroid" JSInterface
// ⚠️ 方法签名必须: public void xxx(final String arg1, final String arg2) — 多参数需要分开传

onChannels(String json)          // 频道列表获取成功
                                // json: [{"name":"CCTV1", "pid":"600001859", "streamId":"2024078201", "type":"live", "is4K":false}, ...]

onPlayback(String requestId, String json)   // 流请求结果
                                // json: {"ok":true, "pid":"...", "streamId":"...", "quality":"fhd"}
                                // 或 json: {"ok":false, "error":"..."}

onError(String scope, String message)  // 各作用域错误 (channels/playback/...)
```

### 4.3 Java → JS 调用（evaluateJavascript）
```javascript
// Java 侧通过 webView.evaluateJavascript(js, null) 注入执行:
// 找 video 元素 → src 替换成请求到的流地址 → play()
```

### 4.4 JS 内部还会发送但 Java 暂未监听的事件
```javascript
sendEvent("heartbeat", {...})      // 心跳
sendEvent("first_frame", {...})    // 首帧渲染
sendEvent("real_frame", {...})     // 真实帧检测
sendEvent("stalled", {})           // 卡住
sendEvent("ended", {})             // 播放结束
sendEvent("black_screen", {...})   // 黑屏检测
sendEvent("no_video", {})          // 没找到video元素
sendEvent("video_debug", {...})    // 调试信息
```

### 4.5 ysp_bridge.js 核心逻辑（~900行）
```
1. 等待页面加载 → 劫持 Vue 组件 / API 响应
2. 提取频道列表 (Vue组件或直接API调用) → YspAndroid.onChannels()
3. Java 拿到列表后选台 → requestCurrentStream() → JS 侧请求流
4. 流请求完成 → YspAndroid.onPlayback(requestId, json)
5. Java 收到 ok=true → loadingOverlay.setVisibility(GONE) + bridgeWebView.setVisibility(VISIBLE)
```

---

## 5. MainActivity 核心流程

### 5.1 启动顺序
```
onCreate()
  ├─ buildUi()              // 创建 root FrameLayout + overlayText + menuPanel + loadingOverlay(黑色)
  ├─ setupProtocolBridge()  // 创建 INVISIBLE 的 bridgeWebView + loadingOverlay + 加载 yangshipin.cn
  ├─ initRuyi()             // RuyiApi.autoRegisterOrLogin() → register/login
  ├─ showOverlay("Loading Yangshipin...", false)  // 注意：此 overlay 在 loading 完成前会被 WebView 遮挡
  └─ updateStatus()

// 异步回调链:
//   1. WebView 加载完成 → 注入 ysp_bridge.js → JS 劫持频道列表 → onChannels() → onChannelsLoaded()
//   2. onChannelsLoaded() → requestCurrentStream() → JS 请求流
//   3. 流就绪 → onPlayback() → onPlaybackResult() → loading_hidden_webview_shown ✅ (关键切换点)
```

### 5.2 视图层级（buildUi + setupProtocolBridge）
```
root (FrameLayout, BLACK)
  ├─ [0] bridgeWebView       (初始 INVISIBLE, MATCH_PARENT)
  ├─ [1] loadingOverlay      (FrameLayout, MATCH_PARENT, 纯黑, 无文字)
  ├─ [2] gestureTraceView    (全屏手势检测, 透明)
  ├─ [3] menuPanel           (右侧抽屉式频道菜单, 默认 GONE)
  ├─ [4] overlayText         (临时文字提示, 默认 GONE)
  └─ [5] ruyiStatusText      (如意登录状态角标)
```

### 5.3 按键映射（dispatchKeyEvent）
| 按键 | 功能 |
|---|---|
| DPAD_UP/DOWN | 换台 |
| DPAD_LEFT/RIGHT | 切换清晰度 (hd→shd→fhd) |
| DPAD_CENTER / OK | 打开/关闭频道菜单 |
| MENU / 长按OK | 打开设置菜单 |
| BACK | 关闭菜单 |
| 数字键 (0-9) | 快速跳转频道号 |

### 5.4 手势支持
- 点击 → 切换菜单开关
- 上下滑动 → 换台
- 左右滑动（菜单打开时）→ 翻页

### 5.5 清晰度偏好
```java
QUALITY_ORDER = {"hd", "shd", "fhd"}  // 从低到高
qualityIndex(q) >= 0 ? 在列表中 : 默认 fhd
```

### 5.6 自动升级
```java
initRuyi() → checkVersion()
  └─ ini 接口返回 app_nurl (apk下载地址) + app_bb (服务端版本)
     ├─ compel="1" → 强制升级对话框
     └─ compel="0" 且 remote > current → 可选升级对话框
        └─ downloadAndInstall() → 下载APK → FileProvider → Intent(ACTION_VIEW) 安装
```

### 5.7 SharedPreferences
| 文件 | Key | 用途 |
|---|---|---|
| `yangshipin_tv` | `quality` | 清晰度偏好 (hd/shd/fhd) |
| | `channel_pid` | 最后观看频道的 PID (下次启动恢复) |
| | `playback_mode` | hw=硬解 / sw=软解 |
| | `auto_start_on_boot` | 开机自启开关 (默认 true) |
| `ruyi_prefs` | `token` | 如意服务器 token |
| | `username` | 如意账号 (auto_xxx) |

---

## 6. 频道名称映射

```java
// CHANNEL_NAME_MAP (静态初始化块, CCTV系列横杠/无横杠两种写法都覆盖)
// 来源: 服务器返回 CCTV1 或 CCTV-1, 需要映射为 CCTV-1 综合
```

完整映射见 `MainActivity.java:99-137`，共 18 个 CCTV 频道。

---

## 7. 构建与调试

### 7.1 环境要求
- JDK 17 (`gradle.properties` 指定 Adoptium JDK 17)
- Android SDK 33+
- Gradle 7.4 (wrapper 自带)
- 小米电视 / Android TV 真机 (ADB连接)

### 7.2 常用命令
```powershell
# 编译
.\gradlew.bat assembleDebug
.\gradlew.bat clean assembleDebug   # 强制重建

# 安装
$sdk = (Get-Content .\local.properties | Select-String "sdk.dir").Split("=")[1]
$adb = "$sdk\platform-tools\adb.exe"
& $adb install -r ".\app\build\outputs\apk\debug\app-debug.apk"

# 抓日志
& $adb logcat -s "YSPTV:*" "RUYI:*"    # 主要调试标签
```

### 7.3 关键日志标签
| TAG | 来源 | 用途 |
|---|---|---|
| `YSPTV` | MainActivity | 所有业务流程日志 |
| `RUYI` | RuyiApi | 服务器请求/响应/加密日志 |
| `AndroidRuntime` | 系统 | 崩溃堆栈 |

---

## 8. 历史踩坑速查

| # | 问题 | 根因 | 修复 |
|---|---|---|---|
| 1 | register 返回 code=110 "请填写账号" | register 用了 `account` 但服务器要 `user` | 区分: register=`user`, login=`account` |
| 2 | RC4 加密结果与服务器不一致 | `CHARSET_GBK.encode(str).array()` 含 0x00 padding | 改用 `str.getBytes(CHARSET_GBK)` |
| 3 | 心跳接口 404 | 用了 `clock` 但服务器实际文件名是 `motion.php` | 改为 `motion` |
| 4 | 频道名不匹配 | 服务器返回 `CCTV1` 无横杠，映射 key 用了 `CCTV-1` | 每种频道加两种 key (带/不带横杠) |
| 5 | 签名错误 code=106 | 签名输入格式 `key=v&k=vkey` 末尾直接拼 APPKEY (无&) | 按 `Arr_sign.php` 实现: plain + "&" + APPKEY |
| 6 | Bridge 事件参数类型错误 | @JavascriptInterface 多参数 Java 方法签名与 JS 调用不匹配 | `YspAndroid.onPlayback(requestId, json)` 分开两个 String 参数 |

---

## 9. AI 编程助手快速指令参考

当 AI 需要修改本项目时：

```
# 编译验证
RunCommand: cd f:\YangshipinWrapper4TV-main; .\gradlew.bat assembleDebug 2>&1 | Select-Object -Last 5

# 安装到设备
RunCommand: $sdk=(Get-Content f:\YangshipinWrapper4TV-main\local.properties|select-string sdk.dir).ToString().Split("=")[1]; & "$sdk\platform-tools\adb.exe" install -r "f:\YangshipinWrapper4TV-main\app\build\outputs\apk\debug\app-debug.apk"

# 重启App抓日志
RunCommand: $sdk=(Get-Content f:\YangshipinWrapper4TV-main\local.properties|select-string sdk.dir).ToString().Split("=")[1]; $adb="$sdk\platform-tools\adb.exe"; & $adb logcat -c; & $adb shell am force-stop com.lwtdzh.yangshipinwrapper4tv; Start-Sleep 0.5; & $adb shell monkey -p com.lwtdzh.yangshipinwrapper4tv -c android.intent.category.LAUNCHER 1; Start-Sleep 10; & $adb logcat -d -s "YSPTV:*" "RUYI:*"
```

---

## 10. 服务器端文件结构参考

如意 API 服务器 `ry.400239.com`（FTP 2233:XSZwHcDmiRSb）关键目录：
```
/api.php                          # API入口 (分发 act 参数到 extend/api/*.php)
/include/global.php               # 全局配置 (数据库连接, APPKEY, Arr_sign等)
/extend/api/
  ├── ini.php                     # 版本检查
  ├── user_reg.php                # 注册 (参数: user, password, markcode)
  ├── user_logon.php              # 登录 (参数: account, password, markcode)
  ├── motion.php                  # 心跳
  ├── clock.php                   # (不用)
  └── ...其他接口
```

---

*文档生成于 2026-09-19，基于当前代码实际分析，与第三版可用代码对比验证。*