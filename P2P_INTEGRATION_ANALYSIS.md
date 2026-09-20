# P2P 视频流集成分析报告

> 生成时间: 2026-09-19
> 目标项目: `YangshipinWrapper4TV` (Android TV 壳)
> 当前播放方案: **M3U + Media3 ExoPlayer 1.1.1**
> 本地播放器: `/player/play.html` (WebView + DPlayer/hls.js/PeerTube 带 P2P 但已注释)
> 待评估库: **Novage/p2p-media-loader-mobile**

---

## 1. 现状分析

### 1.1 当前播放器架构

```
┌─────────────────────────────────────────────────────────┐
│  MainActivity                                           │
│  ┌──────────────┐  ┌────────────────────┐               │
│  │ bridgeWebView │  │ ExoPlayer (Media3)  │              │
│  │  (已停用,保留) │  │  ✅ 当前在用        │              │
│  └──────────────┘  └────────────────────┘               │
│                            │                             │
│  M3U HTTP → 解析 channels → HlsMediaSource → 播放       │
└─────────────────────────────────────────────────────────┘
```

- 频道源: `http://124.223.198.234:1905/interface.m3u` (643 频道 / 53 分组)
- 当前播放全走 CDN 直拉，**无 P2P 能力**
- `bridgeWebView` 保留了但 `setupProtocolBridge()` 已注释

### 1.2 本地 `/player` 目录里有什么

```
player/
├── play.html              ← 105KB 主页面，多引擎切换
├── css/
│   ├── DPlayer.min.css
│   └── plyr.css
└── js/
    ├── hls.min.js              (hls.js 原生 HLS 解析)
    ├── hlsjs.es.min.js         (p2p-media-loader-core ES module)
    ├── DPlayer.min.js          (弹幕播放器)
    ├── flv.min.js              (FLV 格式支持)
    ├── plyr.polyfilled.js
    ├── peertube/               ← PeerTube 播放器 (内部带 P2P)
    │   ├── peertube-player.js  (2.3MB, 内含 Hls.js + p2p-core)
    │   └── ...
    └── core.es.min.js          (p2p-media-loader-core v2.1.0)
```

**关键发现**: `/player` 目录已经打包了完整的 Web 版 P2P 引擎！

`play.html` 里已经配置了:
```javascript
// P2P 开关（默认 true，移动设备仅 WiFi 自动开启）
const ENABLE_P2P = true;

// 3 个公共 WebTorrent tracker（用于 DHT 发现）
const TRACKERS = [
  'wss://tracker.novage.com.ua',
  'wss://tracker.openwebtorrent.com',
  'wss://tracker.webtorrent.dev',
];

// STUN 服务器（用于 NAT 穿透）
const STUN_SERVERS = [
  'stun:stun.l.google.com:19302',
  'stun:stun1.l.google.com:19302',
];

// 注入方式
HlsJsP2PEngine.injectMixin(window.Hls);
// 之后 hls.js 实例自动带 P2P 分片能力
```

### 1.3 为什么 `/player` 现在没用上

之前为了加速启动 + 摆脱 WebView 黑盒，改成了纯 ExoPlayer。代价是丢了 P2P 能力。

---

## 2. Novage/p2p-media-loader-mobile 详解

### 2.1 是什么

| 项目 | 说明 |
|------|------|
| GitHub Stars | ⭐ 12（**非常年轻的项目**）|
| 语言 | Kotlin |
| 许可证 | Apache 2.0 |
| 构建状态 | GitHub Actions ✓ |
| Gradle 引入 | `implementation("com.github.Novage:p2p-media-loader-mobile:main-SNAPSHOT")` |

**一句话**: 把 `p2p-media-loader-core`（Web 版 JavaScript P2P HLS 引擎）封装成 Android 原生库，让 ExoPlayer 能透明享受 P2P 能力。

### 2.2 工作原理（核心架构）

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Android App                                                             │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │              P2PMediaLoader (Kotlin 核心)                        │    │
│  │                                                                   │    │
│  │  ┌────────────────┐    ┌─────────────────────┐                   │    │
│  │  │ 隐藏 WebView   │    │ Ktor 本地 HTTP 服务   │                   │    │
│  │  │ (GONE)         │    │ 127.0.0.1:8080       │                   │    │
│  │  │                │    │                      │                   │    │
│  │  │ static/index.html │    │   ┌── ManifestHandler │               │    │
│  │  │   ↓            │    │   ├── SegmentHandler   │               │    │
│  │  │ 加载 core JS     │    │   │ (从 WebView P2P  │               │    │
│  │  │ (p2p-core +     │    │   │  缓存取/或拉取)   │               │    │
│  │  │  WebRTC)        │    │   └───────────────────┘               │    │
│  │  │                │    └──────────┬────────────┘                   │    │
│  │  │ JS → 种子跟踪     │             │                                  │    │
│  │  │ WebRTC → 对等    │             │                                  │    │
│  │  │ 分片缓存          │             │                                  │    │
│  │  └─────────┬────────┘             │                                  │    │
│  │            │  WebSocket 通信       │                                  │    │
│  └────────────┼──────────────────────┼──────────────────────────────────┘   │
│               │                      │                                    │
│  ┌────────────┴──────────────────────┴──────────────────────────────────┐   │
│  │  ExoPlayer                                                           │   │
│  │  mediaItem = p2pml.getManifestUrl(originalHlsUrl)                     │   │
│  │              ↓                                                        │   │
│  │  http://127.0.0.1:8080/?manifest=<encode(originalHlsUrl)>            │   │
│  │              ↓                                                        │   │
│  │  Ktor 拦截 → 让 WebView P2P 引擎拉取 → 从本地缓存/对等返回分片        │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐    │
│  │   公网                                                              │    │
│  │   WebTorrent Trackers:                                              │    │
│  │   wss://tracker.novage.com.ua / openwebtorrent / webtorrent.dev     │    │
│  │   + DHT (分布式哈希表) + WebRTC NAT 穿透                             │    │
│  └───────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
```

**一句话**: 它在后台偷偷起两个东西 —— 一个隐藏 WebView 跑 JS P2P 引擎 + 一个本地 HTTP 服务器代理 ExoPlayer 的分片请求。ExoPlayer 完全无感，只管向 `127.0.0.1:8080` 要数据。

### 2.3 库的依赖清单

| 依赖 | 用途 | 体积影响 |
|------|------|---------|
| `ktor-server-core` + `ktor-server-cio` + `ktor-server-cors` | 本地 HTTP 服务器 | ~2MB |
| `okhttp` | 网络请求 | 已有 |
| `kotlinx-serialization-json` | JSON 序列化 | ~1MB |
| `androidx.webkit` | WebView 优化 | 很小 |
| `core.es.min.js` + 相关 JS | P2P 引擎本体 | ~1.5MB |
| **WebView (Android 系统)** | WebRTC 运行时 | 系统自带 |
| **Kotlin 运行时** | 本项目是 Java，需额外引入 | ~3MB |

**APK 增肥**: 保守估计 +7~10MB（Kotlin runtime + Ktor + p2p-core JS + 依赖传递）

### 2.4 版本兼容矩阵

| 项目 | 你的项目 | p2pml 要求 | 兼容? |
|------|---------|-----------|-------|
| Java 版本 | 1.8 | Kotlin → JVM 1.8 | ✅ |
| compileSdk | 33 | 36 | ⚠️ 需升 compileSdk |
| minSdk | **19** | **24** | ❌ **不兼容!** |
| Media3 | 1.1.1 | compileOnly 任意 | ✅ |
| Kotlin 插件 | 无 | Kotlin 2.2.20 | ❌ **需加 Kotlin 插件** |

**致命问题**: 你的项目 `minSdkVersion = 19`，而 p2pml 要求 `minSdk = 24`（Android 7.0 Nougat）。Android 19~23 的旧设备将无法安装。

---

## 3. 集成难度评估

### 3.1 步骤拆解（按难度排序）

| # | 任务 | 预估工时 | 难度 | 备注 |
|---|------|---------|------|------|
| 1 | 升 minSdk 到 24 | 5min | 🟢 | 改 build.gradle 一行；Android 7.0 以下设备 ≈ 2% 市场 |
| 2 | 加 Kotlin 插件 + dependencies | 15min | 🟡 | 项目目前是纯 Java |
| 3 | 加 JitPack Maven 源 | 5min | 🟢 | settings.gradle 一行 |
| 4 | 加 network_security_config | 10min | 🟢 | 允许 127.0.0.1 明文 |
| 5 | 改 ExoPlayer 初始化逻辑 | 30min | 🟠 | 见 3.2 详细代码 |
| 6 | 频道切换时重绑 manifest | 20min | 🟡 | `playCurrentWithExo()` 里多一步 |
| 7 | 生命周期管理（Activity stop/restart） | 20min | 🟡 | 启停 P2P 避免后台耗电 |
| 8 | 错误回退（P2P 挂了自动切 CDN） | 30min | 🟠 | 关键用户体验 |
| 9 | 真机调试 WebRTC/NAT | 2h | 🔴 | 模拟器 NAT 穿透不行，必须真机 + 外网 |
| 10 | ProGuard/R8 混淆配置 | 15min | 🟡 | release 包需要 |

**总计**: 约 5~6 小时工作量（不含真机调试时间）

### 3.2 代码改造示例（核心部分）

**settings.gradle**（加 JitPack）:
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io" }   // ← 新增
    }
}
```

**app/build.gradle**:
```groovy
apply plugin: "com.android.application"
apply plugin: "org.jetbrains.kotlin.android"   // ← 新增

android {
    compileSdkVersion 36                        // ← 升
    defaultConfig {
        minSdkVersion 24                       // ← 升 (原 19)
        targetSdkVersion 33
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = '1.8' }        // ← 新增
}

dependencies {
    // Kotlin runtime (AGP 7 自动加，但显式声明好)
    implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.22'
    
    // p2pml (JitPack)
    implementation 'com.github.Novage:p2p-media-loader-mobile:main-SNAPSHOT'
    
    // Media3 (已有)
    implementation 'androidx.media3:media3-exoplayer:1.1.1'
    implementation 'androidx.media3:media3-exoplayer-hls:1.1.1'
    // ...
}
```

**AndroidManifest.xml**（加 network security config）:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

**res/xml/network_security_config.xml**（新建）:
```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">127.0.0.1</domain>
    </domain-config>
</network-security-config>
```

**MainActivity.java 改造**（伪代码，需适配实际）:
```java
// 新增字段
private P2PMediaLoader p2pml;

// 在 initExoPlayer() 后调用
private void initP2P() {
    if (p2pml != null) return;
    
    String coreConfig = "{\"swarmId\":\"YspTv\"}";  // 同 swarmId 的设备互相分享
    
    p2pml = new P2PMediaLoader(
        () -> Log.i(TAG, "P2P ready"),           // onP2PReady
        err -> Log.e(TAG, "P2P error: " + err),  // onP2PReadyError
        coreConfig,
        8081,                                     // serverPort
        false                                     // debugLogs
    );
    
    // 监听 P2P 事件（可选，用于 UI 显示 peers 数量）
    p2pml.addEventListener(CoreEventMap.OnPeerConnect, params -> {
        Log.i(TAG, "Peer connected: " + params.peerId);
    });
    p2pml.addEventListener(CoreEventMap.OnSegmentLoaded, params -> {
        Log.d(TAG, "Segment from: " + params.downloadSource);  // "server" 或 "peer"
    });
    
    p2pml.start(this, exoPlayer);
}

// 修改 playCurrentWithExo()
private void playCurrentWithExo() {
    Channel ch = channels.get(currentIndex);
    String manifestUrl;
    
    if (p2pml != null) {
        manifestUrl = p2pml.getManifestUrl(ch.streamUrl);  // → http://127.0.0.1:8081/?manifest=...
    } else {
        manifestUrl = ch.streamUrl;
    }
    
    HttpDataSource.Factory hds = new DefaultHttpDataSource.Factory()
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(15000);
    
    MediaItem mediaItem = MediaItem.fromUri(manifestUrl);
    HlsMediaSource hlsSource = new HlsMediaSource.Factory(hds).createMediaSource(mediaItem);
    
    exoPlayer.setMediaSource(hlsSource);
    exoPlayer.prepare();
    exoPlayer.setPlayWhenReady(true);
}

// 生命周期里启停
@Override protected void onStart() { super.onStart(); if (p2pml != null) p2pml.applyDynamicConfig("{\"isP2PDisabled\": false}"); }
@Override protected void onStop()  { super.onStop(); if (p2pml != null) p2pml.applyDynamicConfig("{\"isP2PDisabled\": true}"); }
@Override protected void onDestroy() {
    super.onDestroy();
    if (p2pml != null) p2pml = null;  // Kotlin GC 处理
}
```

### 3.3 当前 `/player` 目录能不能直接借过来

**结论**: ❌ **不能直接用 `/player/play.html` 作为 P2P 方案**

原因:
1. `play.html` 是 Web 播放器，必须塞在 WebView 里 → 恢复之前"用 WebView 播放"的模式 → 启动慢、黑盒、ExoPlayer 的硬解优势全丢
2. 我们已经辛辛苦苦把播放切到 ExoPlayer 上（首帧 1.3 秒），再切回去是倒退
3. WebView + DPlayer 在 TV Box 遥控器交互上远不如原生菜单体验

**但 `/player` 里的 JS 资产有用**: `core.es.min.js` 就是 `p2p-media-loader-core` 的 JS，可以直接当自定义 JS 塞进 p2pml 的 WebView（用 `customEngineImplementationPath`）。

---

## 4. 风险与权衡

### 4.1 P2P 能带来什么好处

| 好处 | 说明 | 对本项目的实际价值 |
|------|------|-------------------|
| **减轻 CDN 压力** | 热门频道的分片 70~90% 可由其他观众提供 | ⭐⭐⭐ 如果频道源是自己的服务器，省带宽钱 |
| **弱网加速** | 多人同时看同一频道时，peer 距离近延迟低 | ⭐⭐ IPTV 场景同小区/同城市用户互相加速 |
| **直播边缘缓存** | 热点分片在 DHT 里有多份副本 | ⭐⭐⭐ |
| **降首帧时间** | 缓存命中时跳过 HTTP RTT | ⭐⭐ 但首次 peer 握手有额外开销 |
| **防链路中断** | CDN 某节点挂了还能从 peer 拿 | ⭐⭐ |

### 4.2 P2P 的代价和风险

| 风险 | 严重性 | 说明 |
|------|--------|------|
| **隐私问题** | 🔴 高 | WebRTC 会暴露用户公网 IP、局域网 IP、NAT 类型。如果是商用 IPTV，用户可能介意 |
| **minSdk 降到 24** | 🟡 中 | 你的项目原来支持 Android 4.4（API 19）的老盒子，升 minSdk=24 会让这些老设备装不上。但 TV Box 市场实际 Android 7.0+ 占比 > 98% |
| **APK 增 7~10MB** | 🟡 中 | 带 Kotlin runtime + Ktor + JS bundle |
| **WebView 内存开销** | 🟡 中 | 虽然是 GONE 的隐藏 WebView，但仍占 ~30~50MB RAM（chromium 进程）。对 1GB RAM 的低配盒子不友好 |
| **启动变慢** | 🟡 中 | 多了一个 WebView + Ktor 的初始化，预计 +500ms ~ +2s |
| **公网 tracker 可达性** | 🟡 中 | 国内运营商对 `wss://tracker.novage.com.ua` 等服务器有概率 QoS/墙。`openwebtorrent.com` 相对稳定 |
| **NAT 穿透失败** | 🟡 中 | WebRTC 在对称型 NAT + UDP 被屏蔽时只能回退 TURN。国内运营商 IPv4 大多是 CGNAT，穿透成功率 ~70% |
| **频道源不一定是 HLS** | 🟢 低 | 643 频道全部是 `.m3u8` ✅ |
| **p2pml 库太年轻** | 🟡 中 | 12 Stars，作者只有一人。生产环境风险自担 |

### 4.3 如果不用 p2pml，还有什么选择

| 方案 | 说明 | 是否可行 |
|------|------|---------|
| **WebRTC 原生** | Google WebRTC 直接集成进 ExoPlayer | 🔴 工作量爆炸，无现成方案 |
| **hls.js + WebView 混合** | 保留 WebView 跑 hls.js+p2p-core，ExoPlayer 只做 UI | 🟡 能做但架构更复杂，两套播放并存 |
| **Ktor 自己写本地代理** | 参考 p2pml 的思路自己实现 | 🟠 纯 Java 可行，但要重写 p2p-core 的桥接 |
| **放弃 P2P，优化 CDN** | 换 CDN / 加缓冲 / 预加载策略 | ✅ 最简单可靠 |
| **CDN-CDN 级 P2P** | 如网宿 WebP2P、蓝汛 | 🟡 商业方案，要钱 |

### 4.4 推荐决策矩阵

```
是否值得加 P2P？
│
├─ 频道源是自己的服务器 + 有带宽成本压力?
│   ├─ YES → 值得试，优先用 p2pml
│   └─ NO  → 继续看
│
├─ 机顶盒平均内存 ≥ 2GB?
│   ├─ YES → 能接受 WebView 开销
│   └─ NO  → 建议放弃 p2pml 或自己写轻量版
│
├─ 能接受放弃 Android 6.0 及以下设备?
│   ├─ YES → minSdk 升 24，没问题
│   └─ NO  → 考虑不升 minSdk 但运行时检测并跳过 P2P
│
└─ 开发周期 ≤ 1 天?
    ├─ YES → p2pml 集成
    └─ NO  → 先用 /player/ 里的 hls.js+p2p-core 手动桥接，同时评估 p2pml
```

---

## 5. 备选方案: 轻量 P2P（零 Kotlin 依赖）

如果不想引入整个 Kotlin runtime + Ktor，可以考虑**直接桥接 `/player/js/core.es.min.js` 到现有 WebView**:

```
┌───────────────────────────────────────────┐
│  现有 bridgeWebView (已 GONE, 不显示)      │
│  └─ 注入 p2p-media-loader-core (JS)       │
│     └─ 注入 hls.js + HlsJsP2PEngine       │
│        └─ 所有 m3u8 请求自动走 P2P 轨道     │
│                                           │
│  + 新增: 把 bridgeWebView 的 onRequest     │
│    暴露给 ExoPlayer (WebViewClient 拦截)    │
│    不现实 — 太难跨边界                       │
└───────────────────────────────────────────┘
```

**实际可行的轻量方案**: 不用 ExoPlayer，退回用 WebView + play.html，但在 play.html 里加**原生遥控按键桥接** (`addJavascriptInterface`)。这样 P2P 免费，代价是放弃 ExoPlayer 硬解优势。

---

## 6. 结论与建议

### 6.1 最终结论

**p2pml 可行，但不建议现在集成**。原因按权重:

| 优先级 | 理由 |
|--------|------|
| 1 | **库太新** (12 Stars)，生产稳定性存疑，出 bug 可能自己要修 Kotlin |
| 2 | **minSdk 从 19 升 24**，影响潜在 Android 4.4 盒子（虽然实际不多） |
| 3 | **WebView 内存开销** 对低配 TV Box 是硬伤 |
| 4 | **Kotlin runtime 引入** 是架构层面的大变更，需要全项目团队达成共识 |
| 5 | **APK +7~10MB** |

### 6.2 分阶段建议

**Phase 1（立即做）**: 优化现有 CDN 播放（零成本 P2P 之外的加速）
- ExoPlayer 加 `setLoadControl` 预拉前 5 片
- 换台时 `player.prepare()` + `player.setPlayWhenReady(false)` 等缓冲够再自动 play
- 多清晰度切换 `setABRManager`
- 这个收益立竿见影，1~2 天能搞定

**Phase 2（等条件成熟）**: 给 bridgeWebView 加 P2P JS 注入
- 利用现有 bridgeWebView（已保留）
- 注入 `core.es.min.js` + `hlsjs.es.min.js`（都在 `/player/js/` 里，不用下载）
- 用 Hls.js 代替 ExoPlayer 播放，但保持**原生菜单系统**（WebView 只做播放层，菜单还是 Java UI）
- 这样: 原生菜单 + Web 版 P2P + 不引入 Kotlin + 不降 minSdk
- 工作量预估 2~3 天（主要是调通 WebView 和 Java 的双向通信）

**Phase 3（长期）**: p2pml 成熟后再集成
- 等 p2pml Stars 过 100、发布 1.0 版
- 等 Android 7.0 以下盒子彻底消失
- 届时一次性把 p2pml + 自有 CDN + metrics 全部打通

### 6.3 如果 Phase 2 也不想做

那就老老实实 **Phase 1 优化 ExoPlayer 就够了**。IPTV 直播场景下，643 频道里真正热门可能就前 30 个，小众频道 P2P 也没 peer 可以连。P2P 在直播场景的边际收益不如 VOD 大。

---

## 附录: 技术对照表

| 维度 | p2pml | /player/ Web 方案 | 纯 ExoPlayer (当前) |
|------|-------|-----------------|-------------------|
| P2P 能力 | ✅ WebRTC + 公开 trackers | ✅ 同 p2p-core (相同引擎) | ❌ |
| 原生播放层 | ✅ ExoPlayer 硬解 | ❌ WebView 软解 | ✅ ExoPlayer 硬解 |
| 原生菜单 | ✅ 完整 | ⚠️ 需 JS 桥接 | ✅ 完整 |
| minSdk | 24 | 19 (WebView) | 19 |
| APK 体积 | +7~10MB | +2~3MB (JS 资产) | 0 |
| 内存占用 | +40~80MB (WebView 隐藏) | +60~100MB (WebView 显示) | +15MB (ExoPlayer) |
| 启动速度 | +500~1500ms | +1000~2000ms | 基准 |
| 开发量 | 5~6h | 2~3h | 0 |
| 维护成本 | 高 (Kotlin + 库更新) | 低 (纯 JS) | 低 |
| 国内可达性 | ⚠️ 公共 tracker 不稳定 | ⚠️ 同左 | ✅ CDN 直拉 |

---

*文档完*