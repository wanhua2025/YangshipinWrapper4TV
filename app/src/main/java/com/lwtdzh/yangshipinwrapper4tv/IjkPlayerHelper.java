package com.lwtdzh.yangshipinwrapper4tv;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
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

    private IjkMediaPlayer mNativePlayer;
    private SurfaceView mSurfaceView;
    private IjkPlayerCallback mCallback;
    private Surface mSurface;
    private boolean mPrepared = false;
    private boolean mSurfaceReady = false;
    private boolean mUseSurfaceView = false;

    private TextureView mTextureView;
    private IjkPlayer mDoikkiPlayer;

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

    public IjkPlayerHelper(SurfaceView surfaceView, IjkPlayerCallback callback) {
        this.mSurfaceView = surfaceView;
        this.mCallback = callback;
        this.mUseSurfaceView = true;
        loadLibrariesOnce();
        initNativePlayer();
    }

    public IjkPlayerHelper(TextureView textureView, IjkPlayerCallback callback) {
        this.mTextureView = textureView;
        this.mCallback = callback;
        this.mUseSurfaceView = false;
        loadLibrariesOnce();
        mDoikkiPlayer = new IjkPlayer(textureView.getContext());
        mDoikkiPlayer.initPlayer();
        setupDoikkiListeners();
        applyLiveOptimizations();
        setupTextureViewListener();
    }

    private void initNativePlayer() {
        try {
            mNativePlayer = new IjkMediaPlayer();
            applyLiveOptimizations();
            mNativePlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    mPrepared = true;
                    if (mCallback != null) mCallback.onPrepared();
                }
            });
            mNativePlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {
                    if (mCallback != null) mCallback.onError("ijk error what=" + what + " extra=" + extra);
                    return true;
                }
            });
            mNativePlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                    if (mCallback != null) mCallback.onInfo(what, extra);
                    return true;
                }
            });
            mNativePlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {
                    if (mCallback != null) mCallback.onCompletion();
                }
            });
            mNativePlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height,
                        int sarNum, int sarDen) {
                    if (mCallback != null) mCallback.onVideoSizeChanged(width, height);
                }
            });
            if (mSurfaceView != null) {
                SurfaceHolder holder = mSurfaceView.getHolder();
                holder.addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        mSurface = holder.getSurface();
                        mSurfaceReady = true;
                        if (mNativePlayer != null) {
                            try {
                                mNativePlayer.setSurface(mSurface);
                            } catch (Throwable ignored) {}
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                        if (mNativePlayer != null) {
                            try {
                                mNativePlayer.setSurface(holder.getSurface());
                            } catch (Throwable ignored) {}
                        }
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                        mSurfaceReady = false;
                        if (mSurface != null) {
                            mSurface.release();
                            mSurface = null;
                        }
                    }
                });
                if (mSurfaceView.getHolder().getSurface() != null
                        && mSurfaceView.getHolder().getSurface().isValid()) {
                    mSurface = mSurfaceView.getHolder().getSurface();
                    mSurfaceReady = true;
                    mNativePlayer.setSurface(mSurface);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void setupDoikkiListeners() {
        if (mDoikkiPlayer == null) return;
        mDoikkiPlayer.setPlayerEventListener(new AbstractPlayer.PlayerEventListener() {
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
    }

    private void setupTextureViewListener() {
        if (mTextureView == null) return;
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                mSurface = new Surface(surfaceTexture);
                mSurfaceReady = true;
                if (mDoikkiPlayer != null) {
                    mDoikkiPlayer.setSurface(mSurface);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                if (mDoikkiPlayer != null && mSurface != null) {
                    mDoikkiPlayer.setSurface(mSurface);
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
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
        });
        SurfaceTexture existing = mTextureView.getSurfaceTexture();
        if (existing != null) {
            mSurface = new Surface(existing);
            mSurfaceReady = true;
            if (mDoikkiPlayer != null) {
                mDoikkiPlayer.setSurface(mSurface);
            }
        }
    }

    public void playUrl(String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0");
        mPrepared = false;
        if (mUseSurfaceView && mNativePlayer != null) {
            try {
                mNativePlayer.stop();
                mNativePlayer.release();
            } catch (Throwable ignored) {}
            initNativePlayer();
            applyLiveOptimizations();
            try {
                if (mSurface != null && mSurfaceReady) {
                    mNativePlayer.setSurface(mSurface);
                }
                mNativePlayer.setDataSource(url, headers);
                mNativePlayer.prepareAsync();
            } catch (Throwable th) {
                th.printStackTrace();
                if (mCallback != null) mCallback.onError("ijk prepare error: " + th.getMessage());
            }
        } else if (mDoikkiPlayer != null) {
            try {
                mDoikkiPlayer.reset();
                applyLiveOptimizations();
                if (mSurface != null && mSurfaceReady) {
                    mDoikkiPlayer.setSurface(mSurface);
                }
                mDoikkiPlayer.setDataSource(url, headers);
                mDoikkiPlayer.prepareAsync();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private IjkMediaPlayer getNativeIjkPlayer() {
        if (mUseSurfaceView) return mNativePlayer;
        if (mDoikkiPlayer == null) return null;
        if (mNativePlayer != null) return mNativePlayer;
        try {
            Field f = IjkPlayer.class.getDeclaredField("mMediaPlayer");
            f.setAccessible(true);
            Object obj = f.get(mDoikkiPlayer);
            if (obj instanceof IjkMediaPlayer) {
                mNativePlayer = (IjkMediaPlayer) obj;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return mNativePlayer;
    }

    private void applyLiveOptimizations() {
        IjkMediaPlayer mp = getNativeIjkPlayer();
        if (mp == null) return;
        try {
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 0);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 3000);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "skip_loop_filter", 48);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "skip_frame", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "avioflags", "direct");
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync", "audio");
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30);

            int cpuCount = Runtime.getRuntime().availableProcessors();
            int codecThreads = Math.max(2, Math.min(4, cpuCount));
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", String.valueOf(codecThreads));
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "low-power", 1);

            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 0);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 8 * 1024 * 1024);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 32 * 1000);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 200 * 1000);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect_streamed", 1);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 10 * 1000 * 1000);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", 60 * 1000 * 1000L);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 0);
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flags", "low_delay");
            mp.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "packet-buffering", 0);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void stop() {
        if (mUseSurfaceView && mNativePlayer != null) {
            try { mNativePlayer.stop(); } catch (Throwable ignored) {}
        } else if (mDoikkiPlayer != null) {
            mDoikkiPlayer.stop();
        }
    }

    public void release() {
        if (mUseSurfaceView && mNativePlayer != null) {
            try { mNativePlayer.release(); } catch (Throwable ignored) {}
            mNativePlayer = null;
        } else if (mDoikkiPlayer != null) {
            mDoikkiPlayer.release();
            mDoikkiPlayer = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        mSurfaceReady = false;
    }

    public boolean isPlaying() {
        if (mUseSurfaceView && mNativePlayer != null) {
            try { return mNativePlayer.isPlaying(); } catch (Throwable ignored) { return false; }
        }
        return mDoikkiPlayer != null && mDoikkiPlayer.isPlaying();
    }

    public void pause() {
        if (mUseSurfaceView && mNativePlayer != null) {
            try { mNativePlayer.pause(); } catch (Throwable ignored) {}
        } else if (mDoikkiPlayer != null) {
            try { mDoikkiPlayer.pause(); } catch (Throwable ignored) {}
        }
    }

    public void start() {
        if (mUseSurfaceView && mNativePlayer != null) {
            try { mNativePlayer.start(); } catch (Throwable ignored) {}
        } else if (mDoikkiPlayer != null) {
            try { mDoikkiPlayer.start(); } catch (Throwable ignored) {}
        }
    }
}