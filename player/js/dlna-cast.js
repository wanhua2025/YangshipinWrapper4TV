/**
 * DLNA 投屏模块（playapi 独立版）
 * 通过 PHP 代理访问 Go 服务的 DLNA API
 * 依赖：window.DLNA_SOURCE_URL（视频源地址，用于推断 Go 服务）
 */
(function(window){
  'use strict';

  // PHP 代理路径构建器
  function _getBasePath() {
    // 优先从 <base> 标签获取基础路径
    var baseEl = document.querySelector('base[href]');
    if (baseEl) {
      var baseHref = baseEl.getAttribute('href');
      if (baseHref) return new URL(baseHref, window.location.origin).pathname;
    }
    // fallback: 从当前路径推断（兼容普通路径访问）
    var path = window.location.pathname;
    // 如果路径中包含 /http(s)://，说明是路径 URL 形式，使用 /playapi/
    if (/\/https?:\/\//i.test(path)) return '/player/';
    return path.replace(/\/[^\/]*$/, '/');
  }
  function buildApiUrl(path, params) {
    var url = '/api/v2/dlna/' + path;
    if (params) {
      var sep = '?';
      for (var k in params) {
        if (params.hasOwnProperty(k)) {
          url += sep + k + '=' + encodeURIComponent(params[k]);
          sep = '&';
        }
      }
    }
    return url;
  }

  var DLNA = {
    devices: [],
    scanning: false,
    casting: false,
    stopping: false,
    currentDevice: null,
    modal: null,
    onSelectCallback: null,
    statusCheckTimer: null,
    castBtn: null,
    castBtnOriginalHTML: null,
    castBtnOriginalTitle: null,
    videoUrl: '',
    videoName: '',

    init: function() {
      this.createModal();
      this.bindEvents();
      this.checkCastStatus();
      window.addEventListener('beforeunload', function() {
        if (DLNA.casting) {
          navigator.sendBeacon(buildApiUrl('stop'));
        }
      });
    },

    createModal: function() {
      var html = '<div id="dlna-modal" style="display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.6);z-index:99999;align-items:center;justify-content:center;">'
        + '<div style="background:#1e1e1e;color:#e0e0e0;border-radius:12px;padding:24px;max-width:420px;width:90%;box-shadow:0 8px 32px rgba(0,0,0,0.5);font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Microsoft YaHei,sans-serif;">'
        + '<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">'
        + '<h3 style="margin:0;font-size:18px;font-weight:600;">局域网 DLNA 投屏（仅同一 WiFi / 内网设备可用）</h3>'
        + '<button id="dlna-close-btn" style="background:none;border:none;color:#999;font-size:24px;cursor:pointer;padding:0 4px;line-height:1;">&times;</button>'
        + '</div>'
        + '<div id="dlna-status" style="padding:20px;text-align:center;color:#aaa;min-height:100px;display:flex;flex-direction:column;align-items:center;justify-content:center;">'
        + '<div style="width:40px;height:40px;border:3px solid #333;border-top-color:#1a73e8;border-radius:50%;animation:dlna-spin 0.8s linear infinite;margin-bottom:12px;"></div>'
        + '<p id="dlna-status-text" style="margin:0;font-size:14px;">正在后台搜寻投屏设备...</p>'
        + '<button id="dlna-refresh-btn" style="display:none;margin-top:12px;padding:8px 20px;background:#1a73e8;color:#fff;border:none;border-radius:6px;font-size:13px;cursor:pointer;">🔄 重新扫描设备</button>'
        + '</div>'
        + '<div id="dlna-device-list" style="display:none;max-height:280px;overflow-y:auto;margin-bottom:12px;"></div>'
        + '<div id="dlna-notice" style="display:none;background:#2a2a2a;border-radius:8px;padding:12px;font-size:12px;color:#ff9800;line-height:1.6;">'
        + '⚠️ 说明：本投屏基于 DLNA 协议，由电视直接拉取视频流；关闭网页不会中断电视播放，需要手动点击停止投屏。'
        + '</div>'
        + '<div id="dlna-cast-info" style="display:none;margin-top:12px;padding-top:12px;border-top:1px solid #333;">'
        + '<p id="dlna-casting-device" style="margin:0 0 8px 0;font-size:13px;color:#4caf50;">正在投屏到: <span id="dlna-cast-device-name"></span></p>'
        + '<button id="dlna-stop-btn" style="width:100%;padding:10px;background:#d32f2f;color:#fff;border:none;border-radius:6px;font-size:14px;cursor:pointer;">停止投屏</button>'
        + '</div>'
        + '</div></div>';

      var style = document.createElement('style');
      style.textContent = '@keyframes dlna-spin{to{transform:rotate(360deg)}}'
        + '.dlna-device-item{padding:12px;border-radius:8px;cursor:pointer;margin-bottom:8px;background:#2a2a2a;transition:background 0.2s;display:flex;align-items:center;gap:12px;}'
        + '.dlna-device-item:hover{background:#333;}'
        + '.dlna-device-icon{font-size:28px;}'
        + '.dlna-device-info{flex:1;}'
        + '.dlna-device-name{font-size:14px;font-weight:500;margin-bottom:2px;}'
        + '.dlna-device-model{font-size:12px;color:#888;}'
        + '.dlna-cast-stop-btn{background:#d32f2f!important;color:#fff!important;border:none!important;}'
        + '.dlna-cast-stop-btn:hover{background:#b71c1c!important;}';
      document.head.appendChild(style);

      var div = document.createElement('div');
      div.innerHTML = html;
      document.body.appendChild(div.firstElementChild);
      this.modal = document.getElementById('dlna-modal');

      this.castBtn = document.getElementById('dlnaCastBtn');
      if (this.castBtn) {
        this.castBtnOriginalHTML = this.castBtn.innerHTML;
        this.castBtnOriginalTitle = this.castBtn.getAttribute('title');
      }
    },

    bindEvents: function() {
      var self = this;
      var closeBtn = document.getElementById('dlna-close-btn');
      if (closeBtn) closeBtn.addEventListener('click', function() { self.close(); });
      if (this.modal) {
        this.modal.addEventListener('click', function(e) {
          if (e.target === self.modal) self.close();
        });
      }
      var stopBtn = document.getElementById('dlna-stop-btn');
      if (stopBtn) stopBtn.addEventListener('click', function() { self.stopCast(); });
      var refreshBtn = document.getElementById('dlna-refresh-btn');
      if (refreshBtn) refreshBtn.addEventListener('click', function() { self.forceScan(); });
    },

    open: function(videoUrl, videoName, callback) {
      this.videoUrl = videoUrl || '';
      this.videoName = videoName || 'Unknown';
      this.onSelectCallback = callback;
      this.modal.style.display = 'flex';

      if (this.casting && this.currentDevice) {
        this.showCastInfo(this.currentDevice);
        return;
      }

      var self = this;
      if (this.devices.length > 0) {
        this.showDevices(this.devices);
        this.showStatus('正在后台搜寻投屏设备，已有设备可直接选择', false, true);
        setTimeout(function() { self.scanDevices(true); }, 100);
      } else {
        this.scanDevices(false);
      }
    },

    close: function() {
      this.modal.style.display = 'none';
      if (this.statusCheckTimer) {
        clearInterval(this.statusCheckTimer);
        this.statusCheckTimer = null;
      }
    },

    showStatus: function(text, showLoading, showRefresh) {
      var statusEl = document.getElementById('dlna-status');
      if (!statusEl) return;
      statusEl.style.display = 'flex';
      var listEl = document.getElementById('dlna-device-list');
      if (listEl) listEl.style.display = 'none';
      var noticeEl = document.getElementById('dlna-notice');
      if (noticeEl) noticeEl.style.display = 'none';
      var castInfoEl = document.getElementById('dlna-cast-info');
      if (castInfoEl) castInfoEl.style.display = 'none';
      var textEl = document.getElementById('dlna-status-text');
      if (textEl) textEl.textContent = text;
      var spinner = statusEl.querySelector('div');
      if (spinner) spinner.style.display = showLoading ? 'block' : 'none';
      var refreshBtn = document.getElementById('dlna-refresh-btn');
      if (refreshBtn) refreshBtn.style.display = showRefresh ? 'inline-block' : 'none';
    },

    showDevices: function(devices) {
      document.getElementById('dlna-status').style.display = 'none';
      var listEl = document.getElementById('dlna-device-list');
      listEl.style.display = 'block';
      document.getElementById('dlna-notice').style.display = 'block';

      if (!devices || devices.length === 0) {
        listEl.innerHTML = '<div style="padding:20px;text-align:center;color:#f44336;font-size:13px;line-height:1.8;">'
          + '未发现局域网 DLNA 设备，请确认：<br>'
          + '① 播放器网页与电视连接同一个内网 WiFi / 局域网<br>'
          + '② 电视开启【DLNA / 媒体共享 / 投屏接收】功能<br>'
          + '③ 关闭电视防火墙、路由器 AP 隔离'
          + '</div>';
        document.getElementById('dlna-refresh-btn').style.display = 'inline-block';
        document.getElementById('dlna-status').style.display = 'flex';
        document.getElementById('dlna-status-text').textContent = '';
        var spinner = document.getElementById('dlna-status').querySelector('div');
        if (spinner) spinner.style.display = 'none';
        return;
      }

      var self = this;
      listEl.innerHTML = '';
      devices.forEach(function(dev) {
        var item = document.createElement('div');
        item.className = 'dlna-device-item';
        item.innerHTML = '<div class="dlna-device-icon">📺</div>'
          + '<div class="dlna-device-info">'
          + '<div class="dlna-device-name">' + self.escapeHtml(dev.name || 'Unknown Device') + '</div>'
          + '<div class="dlna-device-model">' + self.escapeHtml(dev.model || dev.manufacturer || '') + '</div>'
          + '</div>';
        item.addEventListener('click', function() {
          self.castToDevice(dev);
        });
        listEl.appendChild(item);
      });
    },

    forceScan: function() {
      this.scanDevices(true);
    },

    scanDevices: function(isRefresh) {
      if (this.scanning) return;
      this.scanning = true;
      this.showStatus(isRefresh ? '正在重新扫描设备...' : '正在后台搜寻投屏设备...', true, false);

      var self = this;
      var apiUrl = buildApiUrl('devices', isRefresh ? {refresh: 1} : {fast: 1});
      fetch(apiUrl, {method: 'GET'})
        .then(function(r) { return r.json(); })
        .then(function(data) {
          self.scanning = false;
          if (data.devices && data.devices.length > 0) {
            self.devices = data.devices;
            self.showDevices(data.devices);
            document.getElementById('dlna-refresh-btn').style.display = 'inline-block';
          } else {
            self.showDevices([]);
          }
        })
        .catch(function(err) {
          self.scanning = false;
          self.showStatus('搜索失败: ' + err.message + '（需视频源服务支持 DLNA）', false, true);
        });
    },

    castToDevice: function(device) {
      if (this.casting) return;
      this.currentDevice = device;
      this.showStatus('正在发起投屏...', true, false);

      var self = this;
      fetch(buildApiUrl('cast'), {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          usn: device.usn,
          video_url: self.videoUrl,
          video_name: self.videoName
        })
      })
        .then(function(r) { return r.json(); })
        .then(function(data) {
          if (data.code === 200) {
            self.casting = true;
            self.updateCastButton(true, device);
            self.showCastInfo(device);
            if (self.onSelectCallback) self.onSelectCallback(true);
            self.showToast('投屏指令已下发，电视正在加载视频资源');
            setTimeout(function() { self.close(); }, 1200);
            self.startStatusCheck();
          } else {
            self.showToast(data.msg || '投屏失败');
            self.showStatus(data.msg || '投屏请求失败：视频地址无法被电视访问，请确认服务内网互通', false, true);
          }
        })
        .catch(function(err) {
          self.showStatus('投屏请求失败: ' + err.message, false, true);
        });
    },

    updateCastButton: function(isCasting, device) {
      if (!this.castBtn) return;
      if (isCasting) {
        this.castBtn.innerHTML = '⏹';
        this.castBtn.classList.add('dlna-cast-stop-btn');
        this.castBtn.setAttribute('title', '停止投屏');
      } else {
        this.castBtn.innerHTML = this.castBtnOriginalHTML || '📺';
        this.castBtn.classList.remove('dlna-cast-stop-btn');
        this.castBtn.setAttribute('title', this.castBtnOriginalTitle || '投屏 (DLNA)｜仅局域网智能电视 / 盒子支持');
      }
    },

    showCastInfo: function(device) {
      document.getElementById('dlna-status').style.display = 'none';
      document.getElementById('dlna-device-list').style.display = 'none';
      document.getElementById('dlna-notice').style.display = 'block';
      document.getElementById('dlna-cast-info').style.display = 'block';
      document.getElementById('dlna-cast-device-name').textContent = device.name || device.model || 'Unknown';
    },

    stopCast: function(callback) {
      if (this.stopping) return;
      this.stopping = true;
      var stopBtn = document.getElementById('dlna-stop-btn');
      if (stopBtn) { stopBtn.disabled = true; stopBtn.textContent = '正在停止...'; }

      var self = this;
      fetch(buildApiUrl('stop'), {method: 'POST'})
        .then(function(r) { return r.json(); })
        .then(function(data) {
          self.stopping = false;
          self.casting = false;
          self.currentDevice = null;
          if (self.statusCheckTimer) { clearInterval(self.statusCheckTimer); self.statusCheckTimer = null; }
          self.updateCastButton(false);
          self.showToast('投屏已终止');
          if (callback) callback();
          else self.scanDevices(false);
          if (stopBtn) { stopBtn.disabled = false; stopBtn.textContent = '停止投屏'; }
        })
        .catch(function(err) {
          self.stopping = false;
          self.casting = false;
          self.currentDevice = null;
          self.updateCastButton(false);
          self.showToast('停止投屏');
          if (callback) callback();
          if (stopBtn) { stopBtn.disabled = false; stopBtn.textContent = '停止投屏'; }
        });
    },

    isCasting: function() {
      return this.casting;
    },

    onStatusChange: function(callback) {
      this._statusChangeCallback = callback;
    },

    startStatusCheck: function() {
      if (this.statusCheckTimer) clearInterval(this.statusCheckTimer);
      this.statusCheckTimer = setInterval(function() {
        DLNA.checkCastStatus(true);
      }, 10000);
    },

    checkCastStatus: function(silent) {
      var self = this;
      fetch(buildApiUrl('status'), {method: 'GET'})
        .then(function(r) { return r.json(); })
        .then(function(data) {
          if (data.active) {
            self.casting = true;
            var devName = data.device_name || data.video_name || '投屏中';
            self.currentDevice = {name: devName, usn: data.device_usn};
            self.updateCastButton(true, self.currentDevice);
            if (!silent) self.startStatusCheck();
            if (self._statusChangeCallback) self._statusChangeCallback({connected: true, device: self.currentDevice});
          } else {
            if (self.casting) {
              self.casting = false;
              self.currentDevice = null;
              self.updateCastButton(false);
              self.showToast('投屏已断开');
              if (self._statusChangeCallback) self._statusChangeCallback({connected: false});
            }
          }
        })
        .catch(function() {});
    },

    showToast: function(msg) {
      var t = document.createElement('div');
      t.textContent = msg;
      t.style.cssText = 'position:fixed;top:20px;left:50%;transform:translateX(-50%);background:#323232;color:#fff;padding:12px 24px;border-radius:8px;z-index:100000;font-size:14px;box-shadow:0 4px 12px rgba(0,0,0,0.3);';
      document.body.appendChild(t);
      setTimeout(function() { t.remove(); }, 2500);
    },

    escapeHtml: function(s) {
      if (!s) return '';
      return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }
  };

  window.DLNACast = DLNA;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() { DLNA.init(); });
  } else {
    DLNA.init();
  }

})(window);