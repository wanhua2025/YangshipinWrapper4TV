package com.lwtdzh.yangshipinwrapper4tv;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import xyz.doikki.videoplayer.ijk.IjkPlayer;
import xyz.doikki.videoplayer.player.AbstractPlayer;

public class IjkPlayerHelper {

    public interface IjkPlayerCallback {
        void onPrepared();
        void onError(String msg);
        void onInfo(int what, int extra);
        void onCompletion();
        void onVideoSizeChanged(int width, int height);
    }

    private static boolean sLibLoaded = false;
    private static boolean sLibAvailable = false;

    private IjkPlayer mPlayer;
    private TextureView mTextureView;
    private IjkPlayerCallback mCallback;
    private Surface mSurface;
    private boolean mPrepared = false;
    private boolean mSurfaceReady = false;
    private IjkMediaPlayer mNativePlayer;

    public static boolean isAvailable() {
        loadLibrariesOnce();
        return sLibAvailable;
    }

    private static synchronized void loadLibrariesOnce() {
        if (sLibLoaded) return;
        try {
            IjkMediaPlayer.loadLibrariesOnce(new IjkLibLoader() {
                @Override
                public void loadLibrary(String s) throws UnsatisfiedLinkError, SecurityException {
                    try {
                        System.loadLibrary(s);
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            });
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
            sLibAvailable = true;
        } catch (Throwable th) {
            th.printStackTrace();
            sLibAvailable = false;
        }
        sLibLoaded = true;
    }

    public IjkPlayerHelper(TextureView textureView, IjkPlayerCallback callback) {
        this.mTextureView = textureView;
        this.mCallback = callback;
        loadLibrariesOnce();
        mPlayer = new IjkPlayer(textureView.getContext());
        mPlayer.initPlayer();
        applyLiveOptimizations();
        mPlayer.setPlayerEventListener(new AbstractPlayer.PlayerEventListener() {
            @Override
            public void onPrepared() {
                mPrepared = true;
                if (mCallback != null) mCallback.onPrepared();
            }

            @Override
            public void onInfo(int what, int extra) {
                if (mCallback != null) mCallback.onInfo(what, extra);
            }

            @Override
            public void onError() {
                if (mCallback != null) mCallback.onError("IjkPlayer error");
            }

            @Override
            public void onCompletion() {
                if (mCallback != null) mCallback.onCompletion();
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                if (mCallback != null) mCallback.onVideoSizeChanged(width, height);
            }
        });
        // 使用 SurfaceTextureListener 替代 OnLayoutChangeListener
        // 修复 Android 4.4 上 getSurfaceTexture() 返回 null 导致的崩溃
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                mSurface = new Surface(surfaceTexture);
                mSurfaceReady = true;
                if (mPlayer != null) {
                    mPlayer.setSurface(mSurface);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                if (mPlayer != null && mSurface != null) {
                    mPlayer.setSurface(mSurface);
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                mSurfaceReady = false;
                if (mSurface != null) {
                    mSurface.release();
                    mSurface = null;
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                // no-op
            }
        });
        // 兼容某些设备上 SurfaceTextureListener 不触发的问题
        // 检查 SurfaceTexture 是否已经就绪
        SurfaceTexture existing = mTextureView.getSurfaceTexture();
        if (existing != null) {
            mSurface = new Surface(existing);
            mSurfaceReady = true;
            if (mPlayer != null) {
                mPlayer.setSurface(mSurface);
            }
        }
    }

    public void playUrl(String url) {
        if (mPlayer == null) return;
        mPrepared = false;
        mPlayer.reset();
        // reset 会清除 IJK option，重新应用直播优化参数
        applyLiveOptimizations();
        // 只在 SurfaceTexture 已就绪时才设置 Surface
        if (mSurface != null && mSurfaceReady) {
            mPlayer.setSurface(mSurface);
        } else {
            // 尝试从 TextureView 获取 SurfaceTexture
            SurfaceTexture st = mTextureView != null ? mTextureView.getSurfaceTexture() : null;
            if (st != null) {
                mSurface = new Surface(st);
                mSurfaceReady = true;
                mPlayer.setSurface(mSurface);
            }
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0");
        mPlayer.setDataSource(url, headers);
        mPlayer.prepareAsync();
    }

    /**
     * 从 doikki 的 IjkPlayer 反射拿到底层的 IjkMediaPlayer 实例
     */
    private IjkMediaPlayer getNativeIjkPlayer() {
        if (mPlayer == null) return null;
        if (mNativePlayer != null) return mNativePlayer;
        try {
            Field f = xyz.doikki.videoplayer.ijk.IjkPlayer.class.getDeclaredField("mMediaPlayer");
            f.setAccessible(true);
            Object obj = f.get(mPlayer);
            if (obj instanceof IjkMediaPlayer) {
                mNativePlayer = (IjkMediaPlayer) obj;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return mNativePlayer;
    }

    /**
     * TVBOX 同款直播优化方案：低内存 + 丢坏帧 + 小缓冲
     * 专为 Android 4.4 1G内存 优化，其它版本同样受益
     */
    private void applyLiveOptimizations() {
        IjkMediaPlayer mp = getNativeIjkPlayer();
        if (mp == null) return;
        try {
            // ======== 核心优化：丢坏帧防卡顿 ========
            // framedrop=3 激进丢帧（跳过非参考帧+延迟过大的帧）
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 3);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 0);

            // ======== 限制帧率，降低CPU/GPU/内存 ========
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30);

            // ======== 直播专用小缓冲（关键！1G内存不爆） ========
            // 只缓存500ms，直播不需要大缓存防抖动
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 500);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 1);

            // ======== 单线程解码，1G内存友好 ========
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", "1");

            // ======== 立即flush不堆包 ========
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);

            // ======== 激进模式，牺牲一点画质换流畅 ========
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0);

            // ======== 网络优化 ========
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);

            // ======== 直播专用：关闭无限缓冲 ========
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 0);

            // ======== 减少ffmpeg探测时间，加快起播 ========
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 256 * 1000);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1 * 1000 * 1000);

        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        mSurfaceReady = false;
        if (mTextureView != null) {
            mTextureView.setSurfaceTextureListener(null);
            mTextureView = null;
        }
    }

    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }
}