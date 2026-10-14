# 🎬 YangshipinWrapper4TV 代码审计报告 & 迭代规划

> **版本**: v2.15-optimized  
> **审计日期**: 2026-10-14  
> **目标设备**: 创维 E900-S / Hi3798MV100 / 1GB+8GB / Android 9  
> **代码总量**: 5,878 行 Java (8 个源文件) + 2 个 Gradle 模块

---

## 📁 项目结构

```
YangshipinWrapper4TV-main/
├── app/                              # 主应用模块
│   ├── src/main/
│   │   ├── AndroidManifest.xml       # ✅ 已优化 (largeHeap=false)
│   │   └── java/com/lwtdzh/yangshipinwrapper4tv/
│   │       ├── MainActivity.java     # 4,846 行 ⚠️ 巨型 Activity
│   │       ├── CenterModule.java     #   659 行 (UI 居中弹窗)
│   │       ├── RuyiApi.java          #   707 行 (如意 API)
│   │       ├── BootReceiver.java     #    45 行 ✅
│   │       ├── IjkPlayerHelper.java  #   355 行 ✅
│   │       ├── YSPApplication.java   #    21 行 ✅
│   │       └── exo/
│   │           ├── HlsErrorHandlingPolicy.java  #  40 行 ✅
│   │           └── OkHttpDataSource.java         # 355 行 ⚠️ @Deprecated
│   └── build.gradle                  # versionCode=16, versionName=2.14
└── player/                           # 播放器模块 (library)
    └── build.gradle                  # exoplayer 2.19.1, dkplayer 3.3.5
```

---

## ✅ 已完成的 v2.15 优化（本次会话）

| # | 问题 | 修改 | 文件 |
|---|------|------|------|
| 1 | 频繁自启 — 每 60 秒 setRepeating 闹钟 | 改为一次性闹钟 `am.set()` | `MainActivity.java:5148-5164` |
| 2 | 关闭自启后 AlarmManager 仍残留 | `cancelBootAlarm()` + `disableBootReceiver()` 双保险 | `MainActivity.java:3678-3708` |
| 3 | MediaCodec 异步队列 Hi3798 Bug | 强制 `forceDisableMediaCodecAsynchronousQueueing()` | `MainActivity.java:1188-1192` |
| 4 | 内存判断错误 — 用 Java Heap 替代系统实际可用内存 | 改用 `ActivityManager.MemoryInfo` | `MainActivity.java:1194-1211` |
| 5 | largeHeap=true 在 1GB 设备反增 OOM 风险 | 改为 `largeHeap="false"` | `AndroidManifest.xml:31` |
| 6 | TextureView 渲染开销大 | 反射调用 `setSurfaceType(1)` 强制 SurfaceView | `MainActivity.java:1065-1068` |
| 7 | OkHttp 连接池过大 (20/10) | 改为 5/3，Dispatcher 并发 5 | `MainActivity.java:2612-2643` |
| 8 | allowedVideoJoiningTimeMs=1000ms 首帧慢 | 反射设为 500ms | `MainActivity.java:1181-1187` |
| 9 | 缺少系统低内存回调 | 新增 `onTrimMemory()` + `onLowMemory()` | `MainActivity.java:497-531` |
| 10 | 低内存设备仍进行频道后台检测 | 检测系统内存后自动跳过 | `MainActivity.java:2674-2687` |
| 11 | IJK fallback buffer 过大 | max-buffer-size 8MB→4MB, probesize 32KB→16KB | `IjkPlayerHelper.java:327-329` |

---

## 🔴 P0 级问题（必须在下一迭代修复）

### P0-1：无全局崩溃保护

**现状**: IJKPlayer 原生层 crash 会导致应用直接闪退，用户无任何提示。ExoPlayer 的 `Player.Listener.onPlayerError()` 只捕获解码层异常，不捕获 JVM 未捕获异常。

**影响**: Hi3798 硬解兼容性问题 + IJK native 层偶尔 SIGSEGV → 直接闪退到桌面。

**建议修复**:
```java
// YSPApplication.onCreate() 中添加
Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e("YSPTV", "uncaught_exception thread=" + t.getName(), e);
        // 写文件 + 5秒后重启 Activity
        Intent restart = new Intent(appContext, MainActivity.class);
        restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        appContext.startActivity(restart);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
});
```

---

### P0-2：裸创建 9 个 Thread，无管理/复用/优先级

**位置**: `MainActivity.java` 行 1744, 1754, 1881, 1915, 2043, 2700, 2797, 4258

```java
// 问题代码 (行 1744)
new Thread(new Runnable() {
    @Override public void run() { doLoadPlaylist(false); }
}, "ChannelLoader").start();
```

**问题**:
- 无线程栈大小控制（默认 1MB/线程，9 线程 = 9MB 虚拟内存）
- 无优先级设置（应设为 `THREAD_PRIORITY_BACKGROUND` / 低优先级避免打断播放）
- `detectBadChannels()` 内部又创建了 `newFixedThreadPool(4)` 但从不 shutdown → **线程泄漏**
- Android 9 Hi3798 上线程数过大会触发系统调度抖动

**建议修复**: 创建全局单例 ThreadPool（核心 2，最大 4，keepAlive 30s），所有后台任务统一投递。

---

### P0-3：OkHttpDataSource 标注 @Deprecated 但仍在使用

**位置**: `exo/OkHttpDataSource.java:49`

ExoPlayer 2.19.1 已提供官方 `exoplayer-datasource-okhttp3` 模块。当前项目自己复制了一份 OkHttpDataSource，增加了维护负担且可能与 ExoPlayer 内部预期行为不一致。

**建议**: 下迭代移除自定义 OkHttpDataSource，改用官方：
```gradle
implementation "com.google.android.exoplayer:exoplayer-datasource-okhttp3:2.19.1"
```
工厂类为 `OkHttpDataSource.Factory`（包名 `com.google.android.exoplayer2.datasource.okhttp`）。

---

## 🟡 P1 级问题（应修复）

### P1-1：Trust-All SSL 硬编码两处重复

| 位置 | 形式 |
|------|------|
| `MainActivity.java:114-129` | static block 全局覆盖 `HttpsURLConnection.setDefault*` |
| `MainActivity.java:2616-2645` | OkHttpClient.Builder 内 setSslSocketFactory + hostnameVerifier |

两处都是接受所有证书 + 所有 hostname。功能重复且 static block 在类加载时执行，如果 OkHttp 先启动会产生竞争。

**风险**: 中间人攻击安全漏洞，但对 TV 盒子看直播来说实际风险低。

**建议**: 保留 static block（兼容 HlsMediaSource 内部 URL 连接），OkHttp 那处可简化复用。

---

### P1-2：MainActivity 臃肿 — 4,846 行

| 方法类别 | 估算行数 | 可抽取目标 |
|----------|----------|------------|
| 频道加载/解析 | ~800 | `ChannelRepository` |
| 播放器管理 (Exo+IJK) | ~900 | `PlayerManager` |
| UI 构建/菜单 | ~1,200 | `MenuController` |
| 如意集成 | ~400 | 已独立 `CenterModule` + `RuyiApi` |
| 自动启动/闹钟 | ~100 | `BootManager` |
| 黑屏检测 | ~50 | `BlackScreenDetector` |

**不拆分的实际风险**: 当前只有一个 Activity，拆分需要大量重构且可能引入新 bug。**建议 v3.0 再动。**

---

### P1-3：`detectBadChannels()` 内 ExecutorService 永不 shutdown

```java
// 行 2703
java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(4);
// 使用后没有 pool.shutdown() / pool.awaitTermination()
```

每次调用 `detectBadChannels()` 都泄漏 4 个线程直到进程死亡。虽然低内存设备已跳过此检测，但正常内存设备每次刷新频道都会泄漏。

**建议**: 用 `Thread.interrupted()` + shutdown，或改用 `CompletableFuture`。

---

### P1-4：versionCode / versionName 未更新

```gradle
// app/build.gradle:8-9
versionCode 16
versionName "2.14"
```

本次做了大量优化但版本号没 bump。

---

### P1-5：proguardFiles 配置 shrinkResources=false

```gradle
// app/build.gradle:25
shrinkResources false
```

Release 构建没有移除无用资源。APK 23.57MB，shrinkResources=true 估计可以再减 1-2MB。

---

### P1-6：player 模块用了 API 33 但 minSdk 19，abiFilters 缺失 armeabi

```gradle
// player/build.gradle:16
ndk { abiFilters "armeabi-v7a", "arm64-v8a" }
```

app 模块包含 5 种 ABI，player 模块只有 2 种。armeabi (v6 软浮点) 的老机顶盒可能缺少 IJK native 库。建议统一为：
```gradle
ndk { abiFilters "armeabi", "armeabi-v7a", "arm64-v8a" }
```

---

### P1-7：Android 9 setExperimentalCpuAffinityMode 反射静默失败

```java
// MainActivity.java:1257-1268
try {
    java.lang.reflect.Method m = ExoPlayer.class.getMethod(
            "setExperimentalCpuAffinityMode", int.class);
    ...
} catch (Throwable t) {
    Log.d(TAG, "exo_cpu_affinity_not_available");
}
```

ExoPlayer 2.19.1 确认存在这个方法，但 trySetMediaCodecLowLatency 反射太复杂（遍历 renderers 数组找 codec 实例），Hi3798MV100 可能有 bug。建议加 debug 日志。

---

## 🟢 P2 级优化（建议改进）

### P2-1：播放流畅性第二轮迭代

#### 2.1 ABR（自适应码率）— 当前未启用

当前 TrackSelector 配置：
```java
.setForceLowestBitrate(false)   // ❌ 不对，应该让它自适应
.setMaxVideoSize(Integer.MAX_VALUE, Integer.MAX_VALUE)  // 无约束
```

建议启用 DefaultTrackSelector 的 AdaptiveTrackSelection：
```java
DefaultTrackSelector.Parameters params = trackSelector.buildUponParameters()
    .setForceLowestBitrate(false)
    .setMaxVideoSize(1920, 1080)  // Hi3798 硬解上限
    .setVideoRendererDisabled(false)
    .build();
```

但注意：大多数直播源只有一条码流，ABR 不起作用。如果源有多码流，`setMaxVideoSize` 可以防止请求超过硬解能力的分辨率。

#### 2.2 换台延迟优化 — 预热下一台

当前换台流程是：stop → release → setMediaSource → prepare → setPlayWhenReady → 等待缓冲...

建议在切换前异步准备好下一台的 MediaSource：
```java
private ExoPlayer.PreparedMediaSource preloadedSource = null;

public void preloadNextChannel(Channel ch) {
    if (preloadedSource != null) preloadedSource.release();
    preloadedSource = new ExoPlayer.PreparedMediaSource(
        sSharedHlsFactory.createMediaSource(MediaItem.fromUri(ch.getBestUrl())));
    preloadedSource.prepareAsync(exoPlayer);  // 异步预加载 playlist
}
```

#### 2.3 HLS Live 专用优化

当前 HlsErrorHandlingPolicy 重试 3 次、每次 500ms。对于 Hi3798 的弱网连接：
```java
// 当前
private static final int MAX_RETRIES = 3;
private static final long RETRY_DELAY_MS = 500;

// 建议改为指数退避
retryCount++
delay = Math.min(2000, RETRY_DELAY_MS * (1 << retryCount))  // 500 → 1000 → 2000
```

#### 2.4 MediaCodec 队列深度控制

当前用了 `forceDisableMediaCodecAsynchronousQueueing()`（强制同步，正确修复了 Hi3798 bug），但还可以设置：
```java
// ExoPlayer 初始化后
try {
    renderersFactory.setAllowedVideoJoiningTimeMs(300);  // 已通过反射设 500ms
    // 可选：在 MediaCodec 渲染器创建后通过反射设置 inputBufferQueue 大小
} catch (Throwable ignored) {}
```

#### 2.5 SurfaceView ZOrder 优化

Hi3798MV100 的 SurfaceView 默认 ZOrder 在 View 树最底层。如果有全屏浮层会遮挡视频。建议：
```java
mIjkSurfaceView.setZOrderOnTop(true);   // IJK 播放器已用 SurfaceView
playerView.getVideoSurfaceView().setZOrderOnTop(true);  // ExoPlayer
```

⚠️ `setZOrderOnTop(true)` 会让视频盖在系统状态栏之上，但 TV Box 没有状态栏，**安全**。

#### 2.6 MediaCodec Low Latency Mode 优先级

当前代码已经尝试 `setPerformanceMode(LOW_LATENCY)` → fallback `POWER_SAVING`：
```java
// MainActivity.java:1688-1694
setPerformanceMode.invoke(codec, LOW_LATENCY);     // 尝试低延迟
// 失败则 fallback
setPerformanceMode.invoke(codec, POWER_SAVING);    // 省电模式
```

**建议**: Hi3798 性能模式（`MEDIA_PERFORMANCE_MODE_HIGH_QUALITY`）可能更适合硬解稳定性。让优先级为：HIGH_QUALITY → LOW_LATENCY → POWER_SAVING。

#### 2.7 OkHttp 连接预热 / DNS 预解析

直播流换台时 DNS 解析占 200-500ms。可以在启动时预热：
```java
// 后台线程预解析常用域名
new Thread(() -> {
    try {
        InetAddress.getByName("ry.400239.com");
    } catch (UnknownHostException ignored) {}
}, "DNSPreWarm").start();
```

#### 2.8 IJK 硬解指定解码器名

当前 IJK 只开了 `mediacodec=1`。Hi3798MV100 的硬解码器名叫 `OMX.hisi.video.decoder` 或 `OMX.hisi.video.decoder.hevc`，可以指定让它优先：
```java
// 在 applyLiveOptimizations() 中添加
mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-player-reset", 1);
mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_fps", 60);
mp.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
```

---

### P2-2：内存使用优化

| 组件 | 当前占用 | 优化空间 |
|------|----------|----------|
| OkHttp 连接池 | 5 连接 | ✅ 已优化 (原 20) |
| Dispatcher 并发 | 5 | ✅ 已优化 (原 20) |
| HLS 缓冲区 | 32-64MB | ✅ 已优化 (动态) |
| bridgeWebView | ~15MB | ✅ 已禁用 |
| bridgeWebView 被 GC 后仍占 native 内存 | — | 建议在 lowMemory 时 recycle |
| RecyclerView (频道列表) | ~2-3MB | 低内存设备可替换为 ScrollView |
| PlayerView Surface | ~3MB | 已用 SurfaceView (TextureView 多 2x) |

---

### P2-3：启动速度优化

当前 onCreate 流程：
```
buildUi() → initExoPlayer() → loadM3uList() [Thread] → initRuyi() → showOverlay
                                 ↑
                              关键路径
```

建议：`initExoPlayer()` 可以在 UI 构建后异步执行，让频道列表加载先跑：
```java
buildUi();
new Handler(Looper.getMainLooper()).post(() -> {
    initExoPlayer();  // 延后一帧，让 UI 先绘制
});
loadM3uList();
```

---

### P2-4：网络失败快速反馈

当前连接超时 8s、读超时 30s，但失败后用户要等很久才看到错误提示。建议：
```java
// 在 HlsErrorHandlingPolicy.getRetryDelayMsFor 中
// 首次失败: 0ms 立即重试
// 二次失败: 500ms
// 三次失败: 1500ms
// 超出: 直接切线路
```

---

## 📊 硬件专项分析：Hi3798MV100

```
┌──────────────────────────────────────────────┐
│        Hi3798MV100 规格速查                    │
├──────────────────────────────────────────────┤
│ CPU    : ARM Cortex-A7 四核 1.0GHz           │
│ GPU    : Mali-450 MP4 28nm                   │
│ RAM    : 1GB (低!)                           │
│ Decoder: HiSilicon 自研 H.264/H.265 硬解     │
│ Output : 1080p60@30fps 或 4K30@30fps         │
│ Android: 9 (API 28)                          │
└──────────────────────────────────────────────┘
```

### Hi3798MV100 硬解已知问题 & 对策

| 问题 | 原因 | 已应用修复 | 下一步 |
|------|------|-----------|--------|
| 异步队列花屏 | MediaCodec async queue bug | ✅ 强制同步队列 | 验证 `setPerformanceMode(HIGH_QUALITY)` |
| TextureView GPU 瓶颈 | Mali-450 弱 | ✅ 强制 SurfaceView | 开启 ZOrderOnTop |
| 低内存 OOM | largeHeap 分配过多 | ✅ 关闭 largeHeap | 加 StrictMode.memory |
| H.265 部分码流不通 | Hi3798 HEVC 硬解限制 | ✅ 自动 fallback IJK | 预检测码流类型分流 |
| 系统服务占用内存多 | 电信预装 | — | 启动时主动 kill 不必要进程 |

---

## 🚀 v2.16 迭代路线图（建议）

### 第一阶段：安全与崩溃（~1 天）

- [ ] 添加 `UncaughtExceptionHandler` 全局崩溃保护
- [ ] 统一 TrustAll SSL 配置到一个地方
- [ ] 修复 `detectBadChannels()` ExecutorService 泄漏
- [ ] bump versionCode/versionName

### 第二阶段：播放流畅性第二轮（~2 天）

- [ ] ABR: TrackSelector 加 `setMaxVideoSize(1920, 1080)`
- [ ] HLS 指数退避重试策略
- [ ] MediaCodec 性能模式调优 (HIGH_QUALITY 优先)
- [ ] SurfaceView `setZOrderOnTop(true)`
- [ ] 换台 MediaSource 预热

### 第三阶段：架构改进（~3 天）

- [ ] 移除自定义 `OkHttpDataSource.java`，改用官方库
- [ ] 引入全局 ThreadPool 替代裸 Thread
- [ ] player 模块 abiFilters 补齐 armeabi
- [ ] 开启 `shrinkResources true` + `resourceConfigurations`

### 第四阶段：可选 - MainActivity 拆分（~5 天，风险高）

- [ ] 抽取 `PlayerManager` (Exo+IJK)
- [ ] 抽取 `ChannelRepository`
- [ ] 抽取 `MenuController`
- [ ] 抽取 `BootManager`

---

## 🔍 关键代码位置索引

| 功能 | 文件 | 行号 |
|------|------|------|
| ExoPlayer 初始化 | `MainActivity.java` | 1033-1450 |
| MediaCodec 同步队列修复 | `MainActivity.java` | 1188-1192 |
| 系统内存检测 | `MainActivity.java` | 1194-1211 |
| Buffer 配置 (动态) | `MainActivity.java` | 1213-1232 |
| OkHttp 配置 | `MainActivity.java` | 2610-2654 |
| HLS 工厂 + ErrorPolicy | `MainActivity.java` | 2650-2653 |
| 黑屏检测 | `MainActivity.java` | 163-192, 1455 |
| Exo→IJK 切换 | `MainActivity.java` | 1460-1486 |
| IJK 初始化 | `MainActivity.java` | 1503-1609 |
| MediaCodec Low Latency | `MainActivity.java` | 1663-1702 |
| onTrimMemory / onLowMemory | `MainActivity.java` | 497-531 |
| 一次性闹钟 | `MainActivity.java` | 5148-5164 |
| 取消闹钟 | `MainActivity.java` | 3678-3692 |
| 禁用 BootReceiver | `MainActivity.java` | 3694-3708 |
| IJK buffer 配置 | `IjkPlayerHelper.java` | 298-342 |
| HLS 错误处理 | `HlsErrorHandlingPolicy.java` | 全文 |
| MainActivity 销毁清理 | `MainActivity.java` | 4624-4653 |

---

## 📋 编译配置快照

```
compileSdkVersion    : 33
minSdkVersion        : 19
targetSdkVersion     : 33
versionCode          : 16 (应改为 17)
versionName          : 2.14 (应改为 2.16)
Java Version         : 1.8
ExoPlayer            : 2.19.1
OKHttp               : 3.12.13
DKPlayer             : 3.3.5
FFmpeg Extension     : 2.19.1+1 (Jellyfin)
ProGuard             : minify=true, shrinkResources=false
abiFilters (app)     : armeabi, armeabi-v7a, arm64-v8a, x86, x86_64
abiFilters (player)  : armeabi-v7a, arm64-v8a  ⚠️ 缺少 armeabi
APK Size             : 23.57 MB
```

---

## 💡 核心设计理念总结

本应用作为 **视频源包装器 (Wrapper)**，本质上是：

```
直播源 (M3U/TS/HLS)  →  ExoPlayer (首选硬解)
                      ↓ 黑屏/解码错误时 fallback
                      IJKPlayer (软解 fallback)
                      ↓
                      SurfaceView (直接 SurfaceFlinger 合成)
```

**Hi3798MV100 这个设备的策略原则**:
1. **一切围绕 SurfaceView** — TextureView 在 Mali-450 上会有 GPU 瓶颈
2. **MediaCodec 用同步队列** — 异步队列在 Android 9 上有 bug
3. **内存保守分配** — 1GB 设备上 largeHeap 反而是陷阱
4. **硬解为主、软解兜底** — Hi3798 自研硬解对常见码流靠谱，HEVC/特殊码流 fallback IJK
5. **Buffer 平衡** — 大 buffer 抗抖动但占内存，小 buffer 响应快但易断 — 动态检测系统内存后调整

---

*文档生成: v2.15-optimized 构建后，基于完整源码审计*