/**
 * PeerTube Standalone Player - 简化独立版
 * 基于 PeerTube 官方播放器构建，提供简单易用的 API
 * 完整支持: HLS(m3u8) 码率切换、字幕、章节、倍速、P2P、视频滤镜等原生功能
 * 默认中文界面
 */

const PEERTUBE_ZH_TRANSLATIONS = {
  "Quality": "画质",
  "Auto": "自动",
  "Speed": "速度",
  "Subtitles/CC": "字幕",
  "Peers": "观众",
  "peers": "个观众",
  "peer": "个观众",
  "no peers": "无观众",
  "Go to the video page": "进入视频页面",
  "Settings": "设置",
  "Watching this video may reveal your IP address to others.": "观看这个视频可能会暴露你的 IP 地址给其他人。",
  "Copy the video URL": "复制视频网址",
  "Copy the video URL at the current time": "复制视频当前时间的网址",
  "Copy embed code": "复制嵌入代码",
  "Copy magnet URI": "复制磁力链接",
  "Total downloaded: ": "下载总量： ",
  "Total uploaded: ": "上传总量： ",
  "From servers: ": "来自服务器： ",
  "From peers: ": "来自观众： ",
  "Normal mode": "一般模式",
  "Stats for nerds": "详细统计信息",
  "Theater mode": "剧场模式",
  "Video UUID": "视频 UUID",
  "Viewport / Frames": "视区 / 帧数",
  "Resolution": "分辨率",
  "Volume": "音量",
  "Codecs": "编解码器",
  "Color": "色彩",
  "Go back to the live": "返回直播",
  "Connection Speed": "连接速度",
  "Network Activity": "网络活动",
  "Total Transfered": "传输总量",
  "Download Breakdown": "下载明细",
  "Buffer Progress": "缓冲进度",
  "Buffer State": "缓冲状态",
  "Live Latency": "实时延迟",
  "P2P": "对等网络",
  "{1} seconds": "{1} 秒",
  "enabled": "已启用",
  "Playlist: {1}": "播放列表：{1}",
  "disabled": "已禁用",
  "  off": "  已关闭",
  "Player mode": "播放器模式",
  "Play in loop": "循环播放",
  "This live is not currently streaming.": "此直播当前未开启。",
  "This live has ended.": "此次直播已经结束了。",
  "The video failed to play, will try to fast forward.": "播放此视频失败，将尝试快进。",
  " (muted)": " （已静音）",
  "{1} from servers · {2} from peers": "{1} 来自服务器 · {2} 来自对等用户",
  "Previous video": "上一个视频",
  "Video page (new window)": "视频页面（新窗口）",
  "Next video": "下一个视频",
  "This video is password protected": "该视频受密码保护",
  "You need a password to watch this video.": "您需要密码才能观看该视频。",
  "Incorrect password, please enter a correct password": "密码错误，请输入正确密码",
  "Cancel": "取消",
  "Up Next": "接下来",
  "Autoplay is suspended": "自动播放已暂停",
  "{1} (from edge: {2})": "{1}（来自边缘：{2}）",
  "Disable subtitles": "禁用字幕",
  "Enable {1} subtitle": "启用{1}字幕",
  "{1} (auto-generated)": "{1} (自动生成)",
  "Go back": "返回",
  "Audio only": "仅音频",
  "Sensitive content": "敏感内容",
  "This video contains sensitive content.": "此视频包含敏感内容。",
  "This video contains sensitive content, including:": "此视频包含敏感内容，包括：",
  "Learn more": "了解更多",
  "Content warning": "内容警告",
  "Violence": "暴力",
  "Shocking Content": "令人震惊的内容",
  "Explicit Sex": "露骨的性内容",
  "Upload speed:": "上传速度：",
  "Download speed:": "下载速度：",
  "Uploader note:": "上传者附注：",
  "Close": "关闭",
  "(skipped {1} buffers) ": "(跳过了 {1} 个缓冲) ",
  "Video Filter": "视频滤镜",
  "Mirror Video": "镜像视频",
  "Mirror": "镜像",
  "Off": "关闭",
  "Audio Player": "音频播放器",
  "Video Player": "视频播放器",
  "Play": "播放",
  "Pause": "暂停",
  "Replay": "重放",
  "Current Time": "当前时间",
  "Duration": "时长",
  "Remaining Time": "剩余时间",
  "Stream Type": "流媒体类型",
  "LIVE": "直播",
  "Loaded": "加载完毕",
  "Progress": "进度",
  "Progress Bar": "进度条",
  "progress bar timing: currentTime={1} duration={2}": "进度条计时：当前时刻 {1}，总时长 {2}",
  "Fullscreen": "全屏",
  "Non-Fullscreen": "退出全屏",
  "Mute": "静音",
  "Unmute": "取消静音",
  "Playback Rate": "播放速度",
  "Subtitles": "字幕",
  "subtitles off": "字幕已关闭",
  "Captions": "辅助字幕",
  "captions off": "辅助字幕已关闭",
  "Chapters": "章节",
  "Descriptions": "注释",
  "descriptions off": "关闭描述",
  "Audio Track": "音轨",
  "Volume Level": "音量",
  "You aborted the media playback": "用户中止了媒体播放",
  "A network error caused the media download to fail part-way.": "网络错误导致视频下载中途失败。",
  "The media could not be loaded, either because the server or network failed or because the format is not supported.": "媒体因格式不支持或者服务器或网络的问题无法加载。",
  "The media playback was aborted due to a corruption problem or because the media used features your browser did not support.": "由于媒体文件损坏或是该媒体使用了你的浏览器不支持的功能，播放已中止。",
  "No compatible source was found for this media.": "无法找到此媒体兼容的来源。",
  "The media is encrypted and we do not have the keys to decrypt it.": "媒体已被加密，我们没有用以解密的密钥。",
  "Play Video": "播放视频",
  "Close Modal Dialog": "关闭弹窗",
  "Modal Window": "弹窗",
  "This is a modal window": "这是一个弹窗",
  "This modal can be closed by pressing the Escape key or activating the close button.": "可以按下 Esc 按键或点击关闭按钮来关闭此弹窗。",
  ", opens captions settings dialog": "，打开辅助字幕设置对话框",
  ", opens subtitles settings dialog": "，打开字幕设置对话框",
  ", opens descriptions settings dialog": "，打开注释设置对话框",
  ", selected": "，已选择",
  "captions settings": "辅助字幕设置",
  "subtitles settings": "字幕设置",
  "descriptions settings": "注释设置",
  "Text": "文本",
  "White": "白",
  "Black": "黑",
  "Red": "红",
  "Green": "绿",
  "Blue": "蓝",
  "Yellow": "黄",
  "Magenta": "紫红",
  "Cyan": "青",
  "Background": "背景",
  "Window": "窗口",
  "Transparent": "透明",
  "Semi-Transparent": "半透明",
  "Opaque": "不透明",
  "Font Size": "字体尺寸",
  "Text Edge Style": "字体边缘样式",
  "None": "无",
  "Raised": "凸起",
  "Depressed": "凹陷",
  "Uniform": "均匀",
  "Dropshadow": "投影",
  "Font Family": "字体系列",
  "Proportional Sans-Serif": "比例无衬线体",
  "Monospace Sans-Serif": "等宽无衬线体",
  "Proportional Serif": "比例衬线体",
  "Monospace Serif": "等宽衬线体",
  "Casual": "休闲",
  "Script": "手写体",
  "Small Caps": "小型大写字母",
  "Reset": "重置",
  "restore all settings to the default values": "恢复全部设置至默认值",
  "Done": "完成",
  "Caption Settings Dialog": "辅助字幕设置对话框",
  "Beginning of dialog window. Escape will cancel and close the window.": "开始对话框。按下 Esc 将取消和关闭该窗口。",
  "End of dialog window.": "结束对话框。",
  "{1} is loading.": "正在加载 {1}。",
  "This live has not started yet.": "这场直播还没有开始。",
  "Uses P2P, others may know you are watching this video.": "使用对等网络时，其他人可能会知道你正在观看此视频。",
  "{1} / {2} dropped of {3}": "{1} / 丢帧 {2}，共 {3} 帧",
  "P2P Media Loader (v2)": "P2P 媒体加载器 (v2)",
  "Web Video": "网页视频"
};

class PeerTubeStandalonePlayer {
  constructor(container, options = {}) {
    this.container = typeof container === 'string' 
      ? document.querySelector(container) 
      : container;
    
    if (!this.container) {
      throw new Error('Container element not found');
    }

    this.options = {
      autoplay: false,
      controls: true,
      muted: false,
      loop: false,
      p2pEnabled: true,
      theme: 'lucide',
      language: 'zh-Hans-CN',
      poster: '',
      startTime: 0,
      inactivityTimeout: 2000,
      theaterButton: false,
      peertubeLink: false,
      playbackRates: [0.25, 0.5, 0.75, 1, 1.25, 1.5, 1.75, 2, 2.5, 3],
      trackerAnnounce: [
        'wss://tracker.novage.com.ua',
        'wss://tracker.openwebtorrent.com'
      ],
      stunServers: [
        'stun:stun.l.google.com:19302',
        'stun:global.stun.twilio.com:3478'
      ],
      ...options
    };

    this.playerInstance = null;
    this.vjsPlayer = null;
    this._initPromise = null;
    this._eventListeners = {};
  }

  async load(source, videoOptions = {}) {
    if (this._initPromise) {
      await this._initPromise;
    }

    this._initPromise = this._initPlayer(source, videoOptions);
    return this._initPromise;
  }

  async _initPlayer(source, videoOptions) {
    const { PeerTubePlayer, videojs } = await import('/v/peertube/peertube-player.js');

    const lang = this.options.language;
    if (lang && (lang.startsWith('zh') || lang === 'zh-Hans-CN' || lang === 'zh-CN')) {
      videojs.addLanguage('zh', PEERTUBE_ZH_TRANSLATIONS);
      videojs.addLanguage('zh-CN', PEERTUBE_ZH_TRANSLATIONS);
      videojs.addLanguage('zh-Hans-CN', PEERTUBE_ZH_TRANSLATIONS);
    }

    while (this.container.firstChild) {
      this.container.removeChild(this.container.firstChild);
    }

    const videoEl = document.createElement('video');
    videoEl.className = 'video-js vjs-peertube-skin';
    videoEl.setAttribute('playsinline', 'true');
    if (this.options.poster) {
      videoEl.poster = this.options.poster;
    }
    this.container.appendChild(videoEl);

    const noopPluginsManager = {
      runHook: (hookName, resultArg, params) => Promise.resolve(resultArg),
      ensurePluginsAreLoaded: () => Promise.resolve(),
      loadPluginsList: () => {}
    };

    const isHLS = typeof source === 'string' && 
      (source.includes('.m3u8') || source.includes('m3u8'));

    const mode = videoOptions.mode || (isHLS ? 'p2p-media-loader' : 'web-video');

    const autoUUID = this._generateVideoUUID(source);
    const videoUUID = videoOptions.uuid || autoUUID;
    const videoShortUUID = videoOptions.shortUUID || videoUUID.substring(0, 8);

    this.playerInstance = new PeerTubePlayer({
      playerElement: () => videoEl,
      controls: this.options.controls,
      controlBar: this.options.controls,
      muted: this.options.muted,
      loop: this.options.loop,
      peertubeLink: () => this.options.peertubeLink,
      enableHotkeys: true,
      inactivityTimeout: this.options.inactivityTimeout,
      videoViewIntervalMs: 30000,
      instanceName: videoOptions.title || this.options.instanceName || 'PeerTube Player',
      theaterButton: this.options.theaterButton,
      authorizationHeader: () => '',
      metricsUrl: '',
      metricsInterval: 0,
      serverUrl: window.location.origin,
      errorNotifier: (msg) => {
        console.error('[PeerTube Player]', msg);
        this._trigger('error', { message: msg });
      },
      language: this.options.language,
      pluginsManager: noopPluginsManager,
      stunServers: this.options.stunServers
    });

    const videoFiles = this._buildVideoFiles(source, videoOptions);

    const loadOptions = {
      mode,
      theme: this.options.theme,
      autoplay: this.options.autoplay,
      forceAutoplay: this.options.autoplay,
      p2pEnabled: this.options.p2pEnabled,
      isLive: videoOptions.isLive || false,
      thumbnails: videoOptions.thumbnails || [],
      videoViewUrl: '',
      embedUrl: '',
      embedTitle: videoOptions.title || 'Video',
      videoUUID: videoUUID,
      videoShortUUID: videoShortUUID,
      duration: videoOptions.duration || 0,
      videoRatio: videoOptions.ratio || 16/9,
      requiresUserAuth: false,
      videoFileToken: () => '',
      requiresPassword: false,
      videoPassword: () => '',
      nextVideo: videoOptions.nextVideo || {
        enabled: false,
        getVideoTitle: () => '',
        displayControlBarButton: false
      },
      previousVideo: videoOptions.previousVideo || {
        enabled: false,
        displayControlBarButton: false
      },
      videoCaptions: videoOptions.captions || [],
      videoChapters: videoOptions.chapters || [],
      storyboard: videoOptions.storyboard || null,
      startTime: this.options.startTime,
      subtitle: videoOptions.subtitle
    };

    if (mode === 'p2p-media-loader') {
      loadOptions.hls = {
        playlistUrl: typeof source === 'string' ? source : videoOptions.playlistUrl,
        segmentsSha256Url: videoOptions.segmentsSha256Url || '',
        trackerAnnounce: videoOptions.trackerAnnounce || this.options.trackerAnnounce || [],
        redundancyBaseUrls: videoOptions.redundancyBaseUrls || [],
        videoFiles: videoFiles,
        swarmId: videoUUID
      };
    } else {
      loadOptions.webVideo = { videoFiles: videoFiles };
    }

    await this.playerInstance.load(loadOptions);
    this.vjsPlayer = this.playerInstance.getPlayer();

    this._bindEvents();
    this._trigger('ready', { uuid: videoUUID });

    return this.vjsPlayer;
  }

  _extractVideoId(url) {
    try {
      const m1 = url.match(/\/(20\d{2})\/(\d{2})\/(\d{2})\/([^\/]+)\/(?:[^\/]+\/)?(?:index|master)\.m3u8/i);
      const m2 = !m1 ? url.match(/\/(20\d{2})(\d{2})(\d{2})\/([^\/]+)\/(?:[^\/]+\/)?(?:index|master)\.m3u8/i) : null;
      const m = m1 || m2;
      if (m) {
        const yyyy = m[1], mm = m[2], dd = m[3];
        const vid = m[4];
        const canonical = `${yyyy}${mm}${dd}-${vid}`;
        const id16 = (function hash16(str) {
          let h1 = 0x811c9dc5 >>> 0;
          let h2 = (~0x811c9dc5) >>> 0;
          for (let i = 0; i < str.length; i++) {
            const c = str.charCodeAt(i);
            h1 ^= c; h1 = (h1 + ((h1<<1)+(h1<<4)+(h1<<7)+(h1<<8)+(h1<<24))) >>> 0;
            h2 ^= (c << 1); h2 = (h2 + ((h2<<1)+(h2<<4)+(h2<<7)+(h2<<8)+(h2<<24))) >>> 0;
          }
          return h1.toString(16).padStart(8,'0') + h2.toString(16).padStart(8,'0');
        })(canonical);
        console.log(`[PeerTube Player] 方法2: 日期+ID 生成16位: ${id16} (${canonical})`);
        return id16;
      }

      const hexMatch = url.match(/\/([a-f0-9]{8})\/(?:[^\/]+\/)?(?:index|master)\.m3u8/i);
      if (hexMatch && hexMatch[1]) {
        console.log(`[PeerTube Player] 回退方法1: 8位hex: ${hexMatch[1]}`);
        return hexMatch[1];
      }

      const cleanUrl = url.replace(/\/(low|medium|high|[\d]+k)\//gi, '/');
      const dirMatch = cleanUrl.match(/\/([^\/]+)\/(?:index|master)\.m3u8$/);
      if (dirMatch && dirMatch[1]) {
        const base = dirMatch[1];
        const id16 = (function hash16(str) {
          let h1 = 0x811c9dc5 >>> 0;
          let h2 = (~0x811c9dc5) >>> 0;
          for (let i = 0; i < str.length; i++) {
            const c = str.charCodeAt(i);
            h1 ^= c; h1 = (h1 + ((h1<<1)+(h1<<4)+(h1<<7)+(h1<<8)+(h1<<24))) >>> 0;
            h2 ^= (c << 1); h2 = (h2 + ((h2<<1)+(h2<<4)+(h2<<7)+(h2<<8)+(h2<<24))) >>> 0;
          }
          return h1.toString(16).padStart(8,'0') + h2.toString(16).padStart(8,'0');
        })(base);
        console.log(`[PeerTube Player] 回退方法3: 目录名→16位: ${id16} (${base})`);
        return id16;
      }

      const idFromB64 = btoa(url).replace(/[^A-Za-z0-9]/g,'').substring(0,16) || ('video' + Date.now().toString(36).slice(-11));
      console.warn('[PeerTube Player] 回退方法4: b64→16位');
      return idFromB64;

    } catch (e) {
      console.error('[PeerTube Player] 提取视频ID失败:', e);
      try {
        return btoa(url).replace(/[^A-Za-z0-9]/g,'').substring(0,16) || ('video' + Math.random().toString(36).substring(2, 18));
      } catch (e2) {
        return 'video' + Date.now().toString(36).slice(-11);
      }
    }
  }

  _generateVideoUUID(source) {
    const url = typeof source === 'string' ? source : (source.playlistUrl || source.src || window.location.href);
    const videoId = this._extractVideoId(url);
    return `video-${videoId}`;
  }

  _buildVideoFiles(source, videoOptions) {
    if (videoOptions.files && Array.isArray(videoOptions.files)) {
      return videoOptions.files;
    }

    if (Array.isArray(source)) {
      return source;
    }

    if (typeof source === 'string') {
      const res = videoOptions.resolution || 720;
      return [{
        fileUrl: source,
        resolution: { id: res, label: res + 'p' },
        size: videoOptions.size || 0,
        fps: videoOptions.fps || 30
      }];
    }

    return [];
  }

  _bindEvents() {
    if (!this.vjsPlayer) return;

    const events = ['play', 'pause', 'ended', 'timeupdate', 'volumechange', 
                   'ratechange', 'seeked', 'waiting', 'playing', 'canplay',
                   'fullscreenchange', 'enterpictureinpicture', 'leavepictureinpicture',
                   'loadedmetadata', 'resolutionchange', 'network-info'];

    events.forEach(event => {
      this.vjsPlayer.on(event, (...args) => {
        this._trigger(event, ...args);
      });
    });

    this.vjsPlayer.on('error', () => {
      const err = this.vjsPlayer.error();
      this._trigger('error', err);
    });
  }

  _trigger(eventName, ...args) {
    if (typeof this.options[eventName] === 'function') {
      this.options[eventName](...args);
    }
    if (this._eventListeners[eventName]) {
      this._eventListeners[eventName].forEach(fn => fn(...args));
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
    this._eventListeners[eventName] = this._eventListeners[eventName]
      .filter(fn => fn !== callback);
    return this;
  }

  play() {
    return this.vjsPlayer?.play();
  }

  pause() {
    return this.vjsPlayer?.pause();
  }

  paused() {
    return this.vjsPlayer?.paused();
  }

  currentTime(time) {
    if (time !== undefined) {
      return this.vjsPlayer?.currentTime(time);
    }
    return this.vjsPlayer?.currentTime();
  }

  duration() {
    return this.vjsPlayer?.duration();
  }

  volume(value) {
    if (value !== undefined) {
      return this.vjsPlayer?.volume(value);
    }
    return this.vjsPlayer?.volume();
  }

  muted(value) {
    if (value !== undefined) {
      return this.vjsPlayer?.muted(value);
    }
    return this.vjsPlayer?.muted();
  }

  playbackRate(rate) {
    if (rate !== undefined) {
      return this.vjsPlayer?.playbackRate(rate);
    }
    return this.vjsPlayer?.playbackRate();
  }

  requestFullscreen() {
    return this.vjsPlayer?.requestFullscreen();
  }

  exitFullscreen() {
    return this.vjsPlayer?.exitFullscreen();
  }

  isFullscreen() {
    return this.vjsPlayer?.isFullscreen();
  }

  async requestPictureInPicture() {
    try {
      if (document.pictureInPictureElement) {
        await document.exitPictureInPicture();
      } else {
        await this.vjsPlayer?.requestPictureInPicture();
      }
    } catch (e) {
      console.error('PiP Error:', e);
    }
  }

  getVideoPlayer() {
    return this.vjsPlayer;
  }

  getPeerTubePlayer() {
    return this.playerInstance;
  }

  showStats() {
    if (this.vjsPlayer && this.vjsPlayer.stats) {
      this.vjsPlayer.stats().show();
    }
  }

  addCaption(caption) {
    if (!this.vjsPlayer) return;
    const track = this.vjsPlayer.addRemoteTextTrack({
      kind: 'captions',
      label: caption.label,
      language: caption.language,
      src: caption.src,
      mode: caption.mode || 'disabled'
    }, false);
    return track;
  }

  destroy() {
    if (this.playerInstance) {
      this.playerInstance.destroy();
      this.playerInstance = null;
      this.vjsPlayer = null;
      this._initPromise = null;
      this._eventListeners = {};
    }
  }
}

if (typeof window !== 'undefined') {
  window.PeerTubeStandalonePlayer = PeerTubeStandalonePlayer;
}

export default PeerTubeStandalonePlayer;
