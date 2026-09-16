(function () {
  "use strict";

  var activePlaybackRequestId = "";
  var videoWatchdogTimer = null;
  var videoWatchdogLastFrameTime = 0;
  var videoWatchdogLastTimeUpdate = 0;

  function installMseCompatibilityPatch() {
    if (window.__yspTvMseCompatibilityPatchInstalled) {
      return;
    }
    if (!window.MediaSource || !window.MediaSource.prototype || !window.MediaSource.prototype.addSourceBuffer) {
      return;
    }
    window.__yspTvMseCompatibilityPatchInstalled = true;
    var originalAddSourceBuffer = window.MediaSource.prototype.addSourceBuffer;
    window.MediaSource.prototype.addSourceBuffer = function (type) {
      var sourceBuffer = originalAddSourceBuffer.apply(this, arguments);
      try {
        patchVideoSourceBuffer(sourceBuffer, type);
      } catch (ignored) {
      }
      return sourceBuffer;
    };
  }

  function patchVideoSourceBuffer(sourceBuffer, type) {
    if (!sourceBuffer || sourceBuffer.__yspTvVideoAppendPatched || !sourceBuffer.appendBuffer) {
      return;
    }
    if (!/video/i.test(String(type || ""))) {
      return;
    }
    sourceBuffer.__yspTvVideoAppendPatched = true;
    var originalAppendBuffer = sourceBuffer.appendBuffer;
    sourceBuffer.appendBuffer = function (data) {
      try {
        var offset = Number(this.timestampOffset || 0);
        var adjustedTo = this.__yspTvAdjustedTimestampOffset;
        if (offset < 0 && (adjustedTo == null || Math.abs(offset - adjustedTo) > 0.001)) {
          var adjusted = offset + 0.08;
          this.timestampOffset = adjusted;
          this.__yspTvAdjustedTimestampOffset = adjusted;
        }
      } catch (ignored) {
      }
      return originalAppendBuffer.call(this, data);
    };
  }

  installMseCompatibilityPatch();
  function sendError(scope, error) {
    var message = "";
    try {
      message = error && (error.stack || error.message || String(error));
    } catch (ignored) {
      message = "unknown error";
    }
    try {
      YspAndroid.onError(scope, message);
    } catch (ignored2) {
    }
  }

  function captureWebpackRequire() {
    if (window.__ysp_tv_wreq) {
      return window.__ysp_tv_wreq;
    }
    if (!window.webpackJsonp || !window.webpackJsonp.push) {
      throw new Error("Yangshipin webpack runtime is not ready");
    }
    window.webpackJsonp.push([
      [String(Date.now())],
      {
        __ysp_tv_capture__: function (module, exports, require) {
          window.__ysp_tv_wreq = require;
        }
      },
      [["__ysp_tv_capture__"]]
    ]);
    if (!window.__ysp_tv_wreq) {
      throw new Error("Unable to capture Yangshipin webpack require");
    }
    return window.__ysp_tv_wreq;
  }

  function collectChannels(root) {
    var result = [];
    var seen = {};

    function visit(value) {
      if (!value) {
        return;
      }
      if (Object.prototype.toString.call(value) === "[object Array]") {
        for (var i = 0; i < value.length; i++) {
          visit(value[i]);
        }
        return;
      }
      if (typeof value !== "object") {
        return;
      }
      if (value.dataTvChannelList && Object.prototype.toString.call(value.dataTvChannelList) === "[object Array]") {
        for (var j = 0; j < value.dataTvChannelList.length; j++) {
          var channel = value.dataTvChannelList[j];
          if (!channel || !channel.pid || !channel.streamId || !channel.channelName) {
            continue;
          }
          if (String(channel.payType || "") !== "879") {
            continue;
          }
          var key = String(channel.pid) + ":" + String(channel.streamId);
          if (seen[key]) {
            continue;
          }
          seen[key] = true;
          result.push({
            name: String(channel.channelName),
            pid: String(channel.pid),
            streamId: String(channel.streamId),
            type: String(channel.channelType || ""),
            payType: String(channel.payType || ""),
            is4K: !!channel.is4K
          });
        }
      }
      var keys = Object.keys(value);
      for (var k = 0; k < keys.length; k++) {
        visit(value[keys[k]]);
      }
    }

    visit(root);
    return result;
  }

  var _tvLayoutScheduled = false;
  var _tvLayoutDone = false;

  function applyTvLayout() {
    if (_tvLayoutScheduled) {
      return;
    }
    _tvLayoutScheduled = true;
    try {
      if (!_tvLayoutDone && !document.getElementById("ysp-tv-wrapper-style")) {
        var style = document.createElement("style");
        style.id = "ysp-tv-wrapper-style";
        style.textContent = [
          "html,body,#app{margin:0!important;padding:0!important;width:100vw!important;height:100vh!important;overflow:hidden!important;background:#000!important;}",
          ".tv-home,.tv,.tv-main,.tv-main-con,.tv-main-con-l,.tv-main-con-l-vid{position:fixed!important;top:0!important;right:0!important;bottom:0!important;left:0!important;width:100vw!important;height:100vh!important;max-width:none!important;max-height:none!important;margin:0!important;padding:0!important;background:#000!important;overflow:hidden!important;}",
          ".tv-main-con-r,.tv-zhan,.header,.footer,.public-com,.activity-com,[class*=Footer],[class*=footer]{display:none!important;}",
          ".tv-main-con-l{float:none!important;}",
          ".tv-main-con-l-vid,.tv-main-con-l-vid *{max-width:none!important;max-height:none!important;}",
          "video{position:fixed!important;top:0!important;right:0!important;bottom:0!important;left:0!important;width:100vw!important;height:100vh!important;object-fit:contain!important;background:#000!important;opacity:1!important;visibility:visible!important;playsinline:true!important;webkit-playsinline:true!important;}",
          ".control,.controlBar,.control-bar,.poster,.loading,.play-btn{opacity:0!important;pointer-events:none!important;}"
        ].join("\n");
        document.head.appendChild(style);
        _tvLayoutDone = true;
      }
      if (_tvLayoutDone) {
        var videos = document.getElementsByTagName("video");
        for (var i = 0; i < videos.length; i++) {
          if (!videos[i].hasAttribute("ysp-laidout")) {
            videos[i].setAttribute("ysp-laidout", "1");
            videos[i].setAttribute("playsinline", "true");
            videos[i].setAttribute("webkit-playsinline", "true");
          }
        }
      }
    } catch (ignored2) {
    }
    _tvLayoutScheduled = false;
  }

  function findTvComponent() {
    var root = document.getElementById("app");
    var rootVue = root && root.__vue__;
    var seen = [];

    function visit(vm) {
      if (!vm || seen.indexOf(vm) >= 0) {
        return null;
      }
      seen.push(vm);
      if (vm.changeTV && vm.setTvConfig && vm.tabA && vm.tabB) {
        return vm;
      }
      var children = vm.$children || [];
      for (var i = 0; i < children.length; i++) {
        var found = visit(children[i]);
        if (found) {
          return found;
        }
      }
      return null;
    }

    return visit(rootVue);
  }

  function findChannelInOfficialComponent(component, pid, streamId) {
    var groups = [component.tabA || [], component.tabB || []];
    var requestedPid = pid == null ? "" : String(pid);
    var requestedStreamId = streamId == null ? "" : String(streamId);
    for (var g = 0; g < groups.length; g++) {
      for (var i = 0; i < groups[g].length; i++) {
        var channel = groups[g][i];
        var channelPid = channel.pid == null ? "" : String(channel.pid);
        var channelStreamId = channel.streamId == null ? "" : String(channel.streamId);
        if (requestedPid && requestedStreamId) {
          if (channelPid === requestedPid && channelStreamId === requestedStreamId) {
            return { channel: channel, group: g };
          }
        } else if ((requestedPid && channelPid === requestedPid)
            || (requestedStreamId && channelStreamId === requestedStreamId)) {
          return { channel: channel, group: g };
        }
      }
    }
    return null;
  }

  function getOfficialPlayer(component) {
    var player = component && component.$refs && component.$refs.player;
    if (Object.prototype.toString.call(player) === "[object Array]") {
      return player[0];
    }
    return player || null;
  }

  function applyOfficialQuality(component, quality) {
    if (!quality) {
      return;
    }
    var player = getOfficialPlayer(component);
    if (player && player.onChangeQuality) {
      player.onChangeQuality({ fn: String(quality) });
    }
  }

  function ensureVideoPlaying(component) {
    try {
      var player = getOfficialPlayer(component);
      if (player && player.myVideo && player.myVideo.videoPlayFunc) {
        player.myVideo.videoPlayFunc();
      }
      var videos = document.getElementsByTagName("video");
      for (var i = 0; i < videos.length; i++) {
        if (videos[i].paused) {
          videos[i].play();
        }
      }
    } catch (ignored) {
    }
  }

  function waitForVideoPlaying(requestId, pid, streamId, quality, maxWaitMs) {
    maxWaitMs = maxWaitMs || 3000;
    var deadline = Date.now() + maxWaitMs;
    var videos = document.getElementsByTagName("video");
    var video = videos.length > 0 ? videos[videos.length - 1] : null;

    function done() {
      notifyPlayback(requestId, pid, streamId, quality);
    }

    if (video && !video.paused && video.readyState >= 2) {
      done();
      return;
    }

    function onPlaying() {
      cleanup();
      done();
    }
    function onTimeout() {
      cleanup();
      ensureVideoPlaying(null);
      var vids = document.getElementsByTagName("video");
      for (var i = 0; i < vids.length; i++) {
        if (vids[i].paused) { vids[i].play(); }
      }
      done();
    }
    function cleanup() {
      if (video) {
        video.removeEventListener("playing", onPlaying);
        video.removeEventListener("canplay", onPlaying);
        video.removeEventListener("timeupdate", onPlaying);
      }
    }

    if (!video) {
      var timer = setInterval(function () {
        var v = document.getElementsByTagName("video");
        if (v.length > 0) {
          video = v[v.length - 1];
          clearInterval(timer);
          attachListeners();
        } else if (Date.now() >= deadline) {
          clearInterval(timer);
          onTimeout();
        }
      }, 80);
    } else {
      attachListeners();
    }

    function attachListeners() {
      if (!video) { onTimeout(); return; }
      video.addEventListener("playing", onPlaying, { once: true });
      video.addEventListener("canplay", onPlaying, { once: true });
      video.addEventListener("timeupdate", onPlaying, { once: true });
      setTimeout(onTimeout, Math.max(0, deadline - Date.now()));
      if (!video.paused && video.readyState >= 2) {
        cleanup();
        done();
      }
    }
  }

  async function loadChannels() {
    try {
      applyTvLayout();
      var require = captureWebpackRequire();
      var api = require("03ef");
      var page = await api.h("PG00000004");
      var channels = collectChannels(page && page.data);
      YspAndroid.onChannels(JSON.stringify(channels));
    } catch (error) {
      sendError("channels", error);
    }
  }

  function isSameOfficialChannel(component, channel) {
    if (!component || !component.tvIndex || !channel) {
      return false;
    }
    return String(component.tvIndex.pid) === String(channel.pid)
        && String(component.tvIndex.streamId) === String(channel.streamId);
  }

  function notifyPlayback(requestId, pid, streamId, quality) {
    try {
      YspAndroid.onPlayback(String(requestId), JSON.stringify({
        ok: true,
        pid: String(pid),
        streamId: String(streamId),
        quality: String(quality || "")
      }));
    } catch (ignored) {
    }
    startVideoWatchdog();
  }

  function startVideoWatchdog() {
    stopVideoWatchdog();
    videoWatchdogLastTimeUpdate = Date.now();
    var video = getCurrentVideo();
    if (video) {
      video.addEventListener("timeupdate", onVideoTimeUpdate);
      video.addEventListener("playing", onVideoTimeUpdate);
    }
    videoWatchdogTimer = setInterval(checkVideoAlive, 1500);
  }

  function stopVideoWatchdog() {
    if (videoWatchdogTimer) {
      clearInterval(videoWatchdogTimer);
      videoWatchdogTimer = null;
    }
    var video = getCurrentVideo();
    if (video) {
      video.removeEventListener("timeupdate", onVideoTimeUpdate);
      video.removeEventListener("playing", onVideoTimeUpdate);
    }
  }

  function onVideoTimeUpdate() {
    videoWatchdogLastTimeUpdate = Date.now();
  }

  function getCurrentVideo() {
    var videos = document.getElementsByTagName("video");
    for (var i = videos.length - 1; i >= 0; i--) {
      if (videos[i].videoWidth > 0) {
        return videos[i];
      }
    }
    return videos.length > 0 ? videos[videos.length - 1] : null;
  }

  function checkVideoAlive() {
    var video = getCurrentVideo();
    if (!video) {
      return;
    }
    var now = Date.now();
    var hasAudio = !video.muted;
    var hasVideoFrames = video.videoWidth > 0 && video.videoHeight > 0;
    var timeProgressing = (now - videoWatchdogLastTimeUpdate) < 4000;

    if (!video.paused && !video.ended) {
      if (hasAudio && hasVideoFrames && !timeProgressing) {
        console.warn("[YSP] watchdog: video playing but time frozen, forcing refresh");
        try {
          video.currentTime = video.currentTime;
        } catch (ignored) {}
        forceVideoRepaint();
      }
    }
  }

  function forceVideoRepaint() {
    applyTvLayout();
    var video = getCurrentVideo();
    if (!video) return;
    try {
      var s = video.style;
      var origDisplay = s.display;
      s.display = "none";
      video.offsetHeight;
      s.display = origDisplay;
    } catch (e) {}
    try {
      var player = findTvComponent();
      if (player) {
        var p = getOfficialPlayer(player);
        if (p && p.myVideo && p.myVideo.videoPlayFunc) {
          p.myVideo.videoPlayFunc();
        }
      }
    } catch (e) {}
    if (video.paused) {
      try { video.play(); } catch (e) {}
    }
  }

  function playChannel(requestId, pid, streamId, quality, mode, attempt) {
    var requestKey = String(requestId);
    if (typeof mode === "number") {
      attempt = mode;
      mode = "channel";
    }
    mode = mode || "channel";
    if (attempt == null) {
      activePlaybackRequestId = requestKey;
      attempt = 0;
    } else {
      attempt = attempt || 0;
    }
    try {
      if (requestKey !== activePlaybackRequestId) {
        return;
      }
      applyTvLayout();
      var component = findTvComponent();
      if (!component || !component.tabA || !component.tabB) {
        if (attempt < 40) {
          setTimeout(function () {
            playChannel(requestId, pid, streamId, quality, mode, attempt + 1);
          }, 150);
          return;
        }
        throw new Error("Official TV component is not ready");
      }
      var match = findChannelInOfficialComponent(component, pid, streamId);
      if (!match) {
        throw new Error("Channel not found in official player component: " + pid);
      }
      if (requestKey !== activePlaybackRequestId) {
        return;
      }
      if (mode === "quality") {
        applyOfficialQuality(component, quality);
        setTimeout(function () {
          if (requestKey !== activePlaybackRequestId) {
            return;
          }
          applyTvLayout();
          waitForVideoPlaying(requestId, pid, streamId, quality);
        }, 80);
        return;
      }
      component.selectIndex = match.group;
      if (!isSameOfficialChannel(component, match.channel)) {
        component.changeTV(match.channel);
      }
      setTimeout(function () {
        if (requestKey !== activePlaybackRequestId) {
          return;
        }
        if (quality && String(quality) !== "fhd") {
          applyOfficialQuality(component, quality);
          setTimeout(function () {
            if (requestKey !== activePlaybackRequestId) {
              return;
            }
            applyTvLayout();
            waitForVideoPlaying(requestId, pid, streamId, quality);
          }, 80);
          return;
        }
        applyTvLayout();
        waitForVideoPlaying(requestId, pid, streamId, quality);
      }, 80);
    } catch (error) {
      try {
        YspAndroid.onPlayback(String(requestId), JSON.stringify({
          ok: false,
          error: error && (error.message || String(error)) || "playback failed"
        }));
      } catch (ignored2) {
      }
      sendError("playback", error);
    }
  }

  window.YspTvBridge = {
    loadChannels: loadChannels,
    playChannel: playChannel
  };

  loadChannels();
}());