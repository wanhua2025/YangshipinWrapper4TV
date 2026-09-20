/**
 * PeerTube Player Standalone - IIFE 版本
 * 可直接通过 <script> 标签引入使用
 */
(function(global) {
  'use strict';

  function checkBigIntSupport() {
    try {
      BigInt(9007199254740991);
      return true;
    } catch (e) {
      return false;
    }
  }

  if (!checkBigIntSupport()) {
    console.error('PeerTube Player requires BigInt support. Please use a modern browser.');
    return;
  }

  class PeerTubePlayerStandalone {
    constructor(container, options = {}) {
      this.container = typeof container === 'string' 
        ? document.querySelector(container) 
        : container;
      
      if (!this.container) {
        throw new Error('PeerTube Player: Container element not found');
      }

      this.options = Object.assign({
        autoplay: false,
        controls: true,
        muted: false,
        loop: false,
        p2pEnabled: false,
        theme: 'lucide',
        language: 'en-US',
        poster: '',
        startTime: 0,
        bigPlayButton: true,
        controlBar: true
      }, options);

      this.playerInstance = null;
      this.vjsPlayer = null;
      this._ready = false;
      this._eventListeners = {};
    }

    async load(source, videoOptions = {}) {
      try {
        await this._loadPlayerModule();
        this._cleanup();

        const videoEl = this._createVideoElement();
        this.container.appendChild(videoEl);

        const pluginsManager = this._createNoopPluginsManager();
        this.playerInstance = new this._PeerTubePlayer({
          playerElement: () => videoEl,
          controls: this.options.controls,
          controlBar: this.options.controlBar,
          muted: this.options.muted,
          loop: this.options.loop,
          peertubeLink: () => false,
          enableHotkeys: true,
          inactivityTimeout: 2000,
          videoViewIntervalMs: 30000,
          instanceName: videoOptions.title || 'PeerTube Player',
          theaterButton: false,
          authorizationHeader: () => '',
          metricsUrl: '',
          metricsInterval: 0,
          serverUrl: window.location.origin,
          errorNotifier: (msg) => {
            console.error('[PeerTube Player]', msg);
            this._trigger('error', { message: msg });
          },
          language: this.options.language,
          pluginsManager: pluginsManager,
          stunServers: [
            'stun:stun.l.google.com:19302',
            'stun:stun1.l.google.com:19302'
          ]
        });

        const loadOptions = this._buildLoadOptions(source, videoOptions);
        await this.playerInstance.load(loadOptions);
        
        this.vjsPlayer = this.playerInstance.getPlayer();
        this._ready = true;
        this._bindEvents();
        this._trigger('ready');

        return this.vjsPlayer;
      } catch (err) {
        // err.stack 包含真实抛出位置，便于定位 instanceof 错误
        console.error('[PeerTube Player] Failed to initialize:', err, '\nStack:', err && err.stack);
        this._trigger('error', err);
        throw err;
      }
    }

    async _loadPlayerModule() {
      if (this._PeerTubePlayer) return;

      // 优先使用预加载的模块（由 <script type="module"> 异步加载）
      if (window._PeerTubePlayerModule) {
        this._PeerTubePlayer = window._PeerTubePlayerModule.PeerTubePlayer;
        this._videojs = window._PeerTubePlayerModule.videojs;
        return;
      }

      // 等待预加载模块完成（轮询，最多等待 15 秒）
      var self = this;
      await new Promise(function(resolve, reject) {
        var elapsed = 0;
        var timer = setInterval(function() {
          if (window._PeerTubePlayerModule) {
            clearInterval(timer);
            self._PeerTubePlayer = window._PeerTubePlayerModule.PeerTubePlayer;
            self._videojs = window._PeerTubePlayerModule.videojs;
            resolve();
            return;
          }
          elapsed += 200;
          if (elapsed >= 15000) {
            clearInterval(timer);
            reject(new Error('PeerTube module preload timeout (15s)'));
          }
        }, 200);
      });
    }

    _getBaseUrl() {
      if (this.options.baseUrl) return this.options.baseUrl;
      
      const scripts = document.getElementsByTagName('script');
      for (let i = 0; i < scripts.length; i++) {
        const src = scripts[i].src;
        if (src && src.includes('peertube-player-standalone')) {
          return src.substring(0, src.lastIndexOf('/') + 1);
        }
      }
      return './';
    }

    _cleanup() {
      if (this.playerInstance) {
        try {
          this.playerInstance.destroy();
        } catch (e) {}
        this.playerInstance = null;
      }
      this.vjsPlayer = null;
      this._ready = false;
      
      while (this.container.firstChild) {
        this.container.removeChild(this.container.firstChild);
      }
    }

    _createVideoElement() {
      const videoEl = document.createElement('video');
      videoEl.className = 'video-js vjs-peertube-skin';
      videoEl.setAttribute('playsinline', 'true');
      videoEl.setAttribute('webkit-playsinline', 'true');
      if (this.options.poster) {
        videoEl.poster = this.options.poster;
      }
      return videoEl;
    }

    _createNoopPluginsManager() {
      return {
        runHook: function(hookName, resultArg, params) {
          return Promise.resolve(resultArg);
        },
        ensurePluginsAreLoaded: function() {
          return Promise.resolve();
        },
        loadPluginsList: function() {}
      };
    }

    _buildLoadOptions(source, videoOptions) {
      const isHLS = typeof source === 'string' && 
        (source.indexOf('.m3u8') !== -1 || source.indexOf('m3u8') !== -1);

      const mode = videoOptions.mode || 'web-video';

      const loadOptions = {
        mode: mode,
        theme: this.options.theme,
        autoplay: this.options.autoplay,
        forceAutoplay: this.options.autoplay,
        p2pEnabled: this.options.p2pEnabled,
        isLive: videoOptions.isLive || false,
        thumbnails: videoOptions.thumbnails || [],
        videoViewUrl: '',
        embedUrl: '',
        embedTitle: videoOptions.title || 'Video',
        videoUUID: videoOptions.uuid || 'standalone-' + Date.now(),
        videoShortUUID: videoOptions.shortUUID || 'standalone',
        duration: videoOptions.duration || 0,
        videoRatio: videoOptions.ratio || 16/9,
        requiresUserAuth: false,
        videoFileToken: function() { return ''; },
        requiresPassword: false,
        videoPassword: function() { return ''; },
        nextVideo: {
          enabled: false,
          getVideoTitle: function() { return ''; },
          displayControlBarButton: false
        },
        previousVideo: {
          enabled: false,
          displayControlBarButton: false
        },
        videoCaptions: videoOptions.captions || [],
        videoChapters: videoOptions.chapters || [],
        storyboard: videoOptions.storyboard || null,
        startTime: this.options.startTime
      };

      const videoFile = {
        fileUrl: typeof source === 'string' ? source : '',
        resolution: { 
          id: videoOptions.resolution || 720, 
          label: (videoOptions.resolution || 720) + 'p' 
        },
        size: 0,
        fps: 30
      };

      if (mode === 'p2p-media-loader' && isHLS) {
        // 与 test-hls.html 一致：HLS 走 p2p-media-loader 时不传 videoFiles，
        // 由 hls.playlistUrl 直接拉流，避免 fileUrl 误判为 mp4 引发 instanceof 错误
        loadOptions.hls = {
          playlistUrl: source,
          segmentsSha256Url: '',
          trackerAnnounce: [],
          redundancyBaseUrls: [],
          videoFiles: videoOptions.files || []
        };
      } else {
        const files = [];
        if (typeof source === 'string') {
          files.push(videoFile);
        } else if (Array.isArray(source)) {
          files.push.apply(files, source);
        }
        loadOptions.webVideo = { videoFiles: files };
      }

      return loadOptions;
    }

    _bindEvents() {
      if (!this.vjsPlayer) return;

      const self = this;
      const events = [
        'play', 'pause', 'ended', 'timeupdate', 'volumechange',
        'ratechange', 'seeked', 'waiting', 'playing', 'canplay',
        'fullscreenchange', 'enterpictureinpicture', 'leavepictureinpicture',
        'loadedmetadata', 'loadeddata', 'progress', 'stalled'
      ];

      events.forEach(function(event) {
        self.vjsPlayer.on(event, function() {
          const args = Array.prototype.slice.call(arguments);
          self._trigger.apply(self, [event].concat(args));
        });
      });

      this.vjsPlayer.on('error', function() {
        const err = self.vjsPlayer.error();
        self._trigger('error', err);
      });
    }

    _trigger(eventName) {
      const args = Array.prototype.slice.call(arguments, 1);
      
      if (typeof this.options[eventName] === 'function') {
        this.options[eventName].apply(this, args);
      }
      
      if (this._eventListeners[eventName]) {
        this._eventListeners[eventName].forEach(function(fn) {
          try {
            fn.apply(null, args);
          } catch (e) {
            console.error('[PeerTube Player] Event handler error:', e);
          }
        });
      }
    }

    on(eventName, callback) {
      if (!this._eventListeners[eventName]) {
        this._eventListeners[eventName] = [];
      }
      this._eventListeners[eventName].push(callback);
      return this;
    }

    off(eventName, callback) {
      if (!this._eventListeners[eventName]) return this;
      if (!callback) {
        delete this._eventListeners[eventName];
        return this;
      }
      this._eventListeners[eventName] = this._eventListeners[eventName]
        .filter(function(fn) { return fn !== callback; });
      return this;
    }

    play() {
      if (!this.vjsPlayer) return Promise.reject(new Error('Player not ready'));
      return this.vjsPlayer.play();
    }

    pause() {
      if (!this.vjsPlayer) return;
      return this.vjsPlayer.pause();
    }

    paused() {
      return this.vjsPlayer ? this.vjsPlayer.paused() : true;
    }

    currentTime(time) {
      if (!this.vjsPlayer) return 0;
      if (time !== undefined) {
        return this.vjsPlayer.currentTime(time);
      }
      return this.vjsPlayer.currentTime();
    }

    duration() {
      return this.vjsPlayer ? this.vjsPlayer.duration() : 0;
    }

    buffered() {
      return this.vjsPlayer ? this.vjsPlayer.buffered() : null;
    }

    volume(value) {
      if (!this.vjsPlayer) return 1;
      if (value !== undefined) {
        return this.vjsPlayer.volume(value);
      }
      return this.vjsPlayer.volume();
    }

    muted(value) {
      if (!this.vjsPlayer) return this.options.muted;
      if (value !== undefined) {
        return this.vjsPlayer.muted(value);
      }
      return this.vjsPlayer.muted();
    }

    playbackRate(rate) {
      if (!this.vjsPlayer) return 1;
      if (rate !== undefined) {
        return this.vjsPlayer.playbackRate(rate);
      }
      return this.vjsPlayer.playbackRate();
    }

    requestFullscreen() {
      if (!this.vjsPlayer) return;
      return this.vjsPlayer.requestFullscreen();
    }

    exitFullscreen() {
      if (!this.vjsPlayer) return;
      return this.vjsPlayer.exitFullscreen();
    }

    isFullscreen() {
      return this.vjsPlayer ? this.vjsPlayer.isFullscreen() : false;
    }

    async requestPictureInPicture() {
      if (!this.vjsPlayer) return;
      try {
        if (document.pictureInPictureElement) {
          await document.exitPictureInPicture();
        } else {
          await this.vjsPlayer.requestPictureInPicture();
        }
      } catch (e) {
        console.error('[PeerTube Player] PiP Error:', e);
      }
    }

    showStats() {
      const statsBtn = this.container.querySelector('.vjs-p2p-info-button');
      if (statsBtn) {
        statsBtn.click();
      } else {
        console.warn('[PeerTube Player] Stats button not available');
      }
    }

    getVideoJSPlayer() {
      return this.vjsPlayer;
    }

    getPeerTubePlayer() {
      return this.playerInstance;
    }

    isReady() {
      return this._ready;
    }

    destroy() {
      this._cleanup();
      this._eventListeners = {};
    }
  }

  PeerTubePlayerStandalone.version = '1.0.0';

  global.PeerTubePlayerStandalone = PeerTubePlayerStandalone;

  if (typeof define === 'function' && define.amd) {
    define(function() { return PeerTubePlayerStandalone; });
  } else if (typeof module !== 'undefined' && module.exports) {
    module.exports = PeerTubePlayerStandalone;
  }

})(typeof window !== 'undefined' ? window : this);
