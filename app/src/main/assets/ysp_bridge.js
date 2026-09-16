(function () {
  "use strict";

  if (window.__yspBridgeReady) {
    console.log("[YSP] bridge already ready, skip re-inject");
    return;
  }
  window.__yspBridgeReady = true;

  console.log("[YSP] === bridge script STARTED ===");

  var activePlaybackRequestId = "";
  var videoWatchdogTimer = null;
  var videoWatchdogLastTimeUpdate = 0;
  var videoWatchdogLastPlaying = 0;
  var videoFirstFrameReported = false;
  var watchdogStuckCount = 0;
  var canvasBlackDetectTimer = null;
  var lastCanvasBlackResult = null;
  var jsHeartbeatTimer = null;
  var videoElementFingerprint = "";
  var lastPlaybackQuality = "";
  var channelsLoadedReported = false;
  var globalHeartbeatStarted = false;
  var audioRecoverAttempted = false;
  var audioLastPlayingAt = 0;
  var channelChanging = false;
  var channelChangeStartAt = 0;
  var lastChangeTVResult = null;

  var bridgeAvailable = null;
  function checkBridge() {
    if (bridgeAvailable !== null) return bridgeAvailable;
    try {
      bridgeAvailable = typeof YspAndroid !== "undefined" && YspAndroid !== null;
    } catch (e) {
      bridgeAvailable = false;
    }
    if (!bridgeAvailable) {
      console.warn("[YSP] YspAndroid bridge NOT available!");
    } else {
      console.log("[YSP] YspAndroid bridge OK");
    }
    return bridgeAvailable;
  }

  function sendEvent(name, payload) {
    if (!checkBridge()) return;
    try {
      YspAndroid.onEvent(String(name), JSON.stringify(payload || {}));
    } catch (e) {
      console.warn("[YSP] sendEvent error: " + e.message);
    }
  }

  function sendHeartbeat() {
    var v = getCurrentVideo();
    var hasRealFrame = false;
    var hasAudio = false;
    if (v && lastCanvasBlackResult && lastCanvasBlackResult.at > (Date.now() - 3000)) {
      hasRealFrame = !lastCanvasBlackResult.isBlack;
    }
    if (v && typeof v.muted !== "undefined") {
      try { hasAudio = !v.muted; } catch (e) { hasAudio = true; }
    }
    sendEvent("heartbeat", {
      hasVideo: !!v,
      videoWidth: v ? v.videoWidth : 0,
      videoHeight: v ? v.videoHeight : 0,
      paused: v ? v.paused : true,
      ended: v ? v.ended : false,
      readyState: v ? v.readyState : 0,
      hasRealFrame: hasRealFrame,
      hasAudio: hasAudio,
      channelChanging: channelChanging,
      fpsLikely: v && !v.paused && !v.ended && v.videoWidth > 0
    });
  }

  function startGlobalHeartbeat() {
    if (globalHeartbeatStarted) return;
    globalHeartbeatStarted = true;
    jsHeartbeatTimer = setInterval(sendHeartbeat, 2500);
  }

  function installMseCompatibilityPatch() {
    if (window.__yspTvMseCompatibilityPatchInstalled) return;
    if (!window.MediaSource || !window.MediaSource.prototype || !window.MediaSource.prototype.addSourceBuffer) return;
    window.__yspTvMseCompatibilityPatchInstalled = true;
    var originalAddSourceBuffer = window.MediaSource.prototype.addSourceBuffer;
    window.MediaSource.prototype.addSourceBuffer = function (type) {
      var sourceBuffer = originalAddSourceBuffer.apply(this, arguments);
      try { patchVideoSourceBuffer(sourceBuffer, type); } catch (ignored) {}
      return sourceBuffer;
    };
  }

  function patchVideoSourceBuffer(sourceBuffer, type) {
    if (!sourceBuffer || sourceBuffer.__yspTvVideoAppendPatched || !sourceBuffer.appendBuffer) return;
    if (!/video/i.test(String(type || ""))) return;
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
        try {
          if (typeof sourceBuffer.buffered !== "undefined" && sourceBuffer.buffered.length > 0) {
            var oldEnd = sourceBuffer.buffered.end(sourceBuffer.buffered.length - 1);
            if (oldEnd > 3600) {
              sourceBuffer.remove(0, oldEnd - 1800);
            }
          }
        } catch (e) {}
      } catch (ignored) {}
      return originalAppendBuffer.call(this, data);
    };
  }

  installMseCompatibilityPatch();

  function captureWebpackRequire() {
    if (window.__ysp_tv_wreq) return window.__ysp_tv_wreq;
    if (!window.webpackJsonp || !window.webpackJsonp.push) {
      console.warn("[YSP] webpackJsonp not ready, window keys: " + Object.keys(window).filter(function(k){return k.indexOf('webpack')>=0}).join(','));
      throw new Error("Yangshipin webpack runtime is not ready");
    }
    window.webpackJsonp.push([
      [String(Date.now())],
      { __ysp_tv_capture__: function (module, exports, require) { window.__ysp_tv_wreq = require; } },
      [["__ysp_tv_capture__"]]
    ]);
    if (!window.__ysp_tv_wreq) throw new Error("Unable to capture Yangshipin webpack require");
    console.log("[YSP] webpack require captured OK");
    return window.__ysp_tv_wreq;
  }

  function collectChannelsFromVue(component) {
    var result = [];
    var seen = {};
    var groups = [component.tabA || [], component.tabB || []];
    for (var g = 0; g < groups.length; g++) {
      var list = groups[g];
      if (!list) continue;
      for (var i = 0; i < list.length; i++) {
        var channel = list[i];
        if (!channel) continue;
        var pid = channel.pid;
        var streamId = channel.streamId;
        var name = channel.channelName || channel.name;
        if (!pid || !streamId || !name) continue;
        if (String(channel.payType || "") !== "879") continue;
        var key = String(pid) + ":" + String(streamId);
        if (seen[key]) continue;
        seen[key] = true;
        result.push({
          name: String(name),
          pid: String(pid),
          streamId: String(streamId),
          type: String(channel.channelType || ""),
          is4K: !!channel.is4K
        });
      }
    }
    return result;
  }

  function collectChannelsFromApi(pageData) {
    var result = [];
    var seen = {};
    function visit(value) {
      if (!value) return;
      if (Object.prototype.toString.call(value) === "[object Array]") {
        for (var i = 0; i < value.length; i++) visit(value[i]);
        return;
      }
      if (typeof value !== "object") return;
      if (value.dataTvChannelList && Object.prototype.toString.call(value.dataTvChannelList) === "[object Array]") {
        for (var j = 0; j < value.dataTvChannelList.length; j++) {
          var channel = value.dataTvChannelList[j];
          if (!channel || !channel.pid || !channel.streamId || !channel.channelName) continue;
          if (String(channel.payType || "") !== "879") continue;
          var key = String(channel.pid) + ":" + String(channel.streamId);
          if (seen[key]) continue;
          seen[key] = true;
          result.push({
            name: String(channel.channelName),
            pid: String(channel.pid),
            streamId: String(channel.streamId),
            type: String(channel.channelType || ""),
            is4K: !!channel.is4K
          });
        }
      }
      var keys = Object.keys(value);
      for (var k = 0; k < keys.length; k++) visit(value[keys[k]]);
    }
    visit(pageData);
    return result;
  }

  var _tvLayoutScheduled = false;
  var _tvLayoutDone = false;
  var _videoBeforeChange = null;

  function applyTvLayout() {
    if (_tvLayoutScheduled) return;
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
    } catch (ignored2) {}
    _tvLayoutScheduled = false;
  }

  function findTvComponent() {
    var root = document.getElementById("app");
    if (!root) { console.warn("[YSP] findTvComponent: no #app element"); return null; }
    var rootVue = root.__vue__;
    if (!rootVue) { console.warn("[YSP] findTvComponent: #app has no __vue__, vue count on page: " + document.querySelectorAll('*').length); return null; }
    var seen = [];
    var searchCount = 0;
    function visit(vm) {
      if (!vm || seen.indexOf(vm) >= 0) return null;
      seen.push(vm);
      searchCount++;
      if (vm.changeTV && vm.setTvConfig && vm.tabA && vm.tabB) return vm;
      var children = vm.$children || [];
      for (var i = 0; i < children.length; i++) {
        var found = visit(children[i]);
        if (found) return found;
      }
      return null;
    }
    var result = visit(rootVue);
    if (!result) {
      var tabAKeys = Object.keys(rootVue.$data || {}).filter(function(k){return /tab|channel|tv/i.test(k);});
      console.warn("[YSP] findTvComponent: searched " + searchCount + " components, no TV component found. root keys: " + Object.keys(rootVue.$data || {}).slice(0,20).join(',') + " tab-like keys: " + tabAKeys.join(','));
    } else {
      console.log("[YSP] findTvComponent: found TV component after " + searchCount + " searches, tabA=" + (result.tabA ? result.tabA.length : 0) + " tabB=" + (result.tabB ? result.tabB.length : 0));
    }
    return result;
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
          if (channelPid === requestedPid && channelStreamId === requestedStreamId) return { channel: channel, group: g };
        } else if ((requestedPid && channelPid === requestedPid) || (requestedStreamId && channelStreamId === requestedStreamId)) {
          return { channel: channel, group: g };
        }
      }
    }
    return null;
  }

  function getOfficialPlayer(component) {
    var player = component && component.$refs && component.$refs.player;
    if (Object.prototype.toString.call(player) === "[object Array]") return player[0];
    return player || null;
  }

  function applyOfficialQuality(component, quality) {
    if (!quality) return;
    var player = getOfficialPlayer(component);
    if (player && player.onChangeQuality) player.onChangeQuality({ fn: String(quality) });
  }

  function ensureVideoPlaying(component) {
    try {
      if (!component) component = findTvComponent();
      var player = component ? getOfficialPlayer(component) : null;
      if (player && player.myVideo && player.myVideo.videoPlayFunc) player.myVideo.videoPlayFunc();
    } catch (ignored) {}
    try {
      var videos = document.getElementsByTagName("video");
      for (var i = 0; i < videos.length; i++) {
        if (videos[i].paused) videos[i].play();
      }
    } catch (ignored) {}
  }

  function tryRecoverAudio() {
    var v = getCurrentVideo();
    if (!v) return false;
    try {
      v.muted = false;
      if (typeof v.volume !== "undefined" && v.volume < 0.05) v.volume = 1.0;
      if (typeof v.untiled !== "undefined" && v.untiled) v.play();
    } catch (e) {}
    try {
      var component = findTvComponent();
      var player = component ? getOfficialPlayer(component) : null;
      if (player) {
        if (player.mute !== undefined) {
          player.mute = false;
          if (typeof player.$forceUpdate === "function") player.$forceUpdate();
        }
      }
    } catch (e) {}
    return true;
  }

  function isSameOfficialChannel(component, channel) {
    if (!component || !component.tvIndex || !channel) return false;
    return String(component.tvIndex.pid) === String(channel.pid)
        && String(component.tvIndex.streamId) === String(channel.streamId);
  }

  function getCurrentVideo() {
    var videos = document.getElementsByTagName("video");
    for (var i = videos.length - 1; i >= 0; i--) {
      if (videos[i].videoWidth > 0) return videos[i];
    }
    return videos.length > 0 ? videos[videos.length - 1] : null;
  }

  function detectRealVideoFrame() {
    var video = getCurrentVideo();
    if (!video || !video.videoWidth || !video.videoHeight) {
      lastCanvasBlackResult = { isBlack: false, at: Date.now(), canvasError: true };
      sendEvent("real_frame", {
        canvasError: true,
        note: "canvas_unavailable_assuming_real_frame"
      });
      return true;
    }
    if (!video.__ysp_black_detect_canvas) {
      try {
        video.__ysp_black_detect_canvas = document.createElement("canvas");
      } catch (e) { return false; }
    }
    var canvas = video.__ysp_black_detect_canvas;
    try {
      if (!canvas.width) { canvas.width = 160; canvas.height = 90; }
      var ctx = canvas.getContext("2d");
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      var data = ctx.getImageData(0, 0, canvas.width, canvas.height).data;
      var nonBlack = 0;
      var total = 0;
      var rSum = 0, gSum = 0, bSum = 0;
      for (var i = 0; i < data.length; i += 4) {
        var r = data[i], g = data[i+1], b = data[i+2];
        rSum += r; gSum += g; bSum += b;
        total++;
        if (r > 15 || g > 15 || b > 15) nonBlack++;
      }
      var avgNonBlack = nonBlack / total;
      var avgR = rSum / total, avgG = gSum / total, avgB = bSum / total;
      var colorVariance = Math.abs(avgR - avgG) + Math.abs(avgG - avgB) + Math.abs(avgR - avgB);
      var isBlack = avgNonBlack < 0.08 || (avgR < 12 && avgG < 12 && avgB < 12 && colorVariance < 5);
      lastCanvasBlackResult = { isBlack: isBlack, at: Date.now(), avgNonBlack: avgNonBlack };
      if (!isBlack) {
        videoFirstFrameReported = true;
        audioRecoverAttempted = false;
        sendEvent("real_frame", {
          nonBlackRatio: avgNonBlack,
          avgColor: [avgR, avgG, avgB]
        });
      }
      return !isBlack;
    } catch (e) {
      lastCanvasBlackResult = { isBlack: false, at: Date.now(), canvasError: true };
      sendEvent("real_frame", {
        canvasError: true,
        note: "canvas_unavailable_assuming_real_frame"
      });
      return true;
    }
  }

  function reportFirstFrame() {
    if (videoFirstFrameReported) return;
    var v = getCurrentVideo();
    if (!v) return;
    if (v.readyState >= 2 && v.videoWidth > 0 && !v.paused) {
      var hasRealFrame = detectRealVideoFrame();
      if (hasRealFrame) {
        videoFirstFrameReported = true;
        sendEvent("first_frame", {
          videoWidth: v.videoWidth,
          videoHeight: v.videoHeight,
          duration: v.duration,
          realFrame: true
        });
      } else if (v.readyState >= 3 || (v.currentTime && v.currentTime > 0.1)) {
        videoFirstFrameReported = true;
        sendEvent("first_frame", {
          videoWidth: v.videoWidth,
          videoHeight: v.videoHeight,
          duration: v.duration,
          realFrame: false,
          note: "video_ready_but_pending_real_frame"
        });
      }
    }
  }

  function notifyPlayback(requestId, pid, streamId, quality) {
    try {
      YspAndroid.onPlayback(String(requestId), JSON.stringify({
        ok: true, pid: String(pid), streamId: String(streamId), quality: String(quality || "")
      }));
    } catch (ignored) {}
    var v = getCurrentVideo();
    if (v) {
      try {
        var cs = window.getComputedStyle(v);
        var rect = v.getBoundingClientRect();
        sendEvent("video_debug", {
          videoWidth: v.videoWidth, videoHeight: v.videoHeight,
          paused: v.paused, readyState: v.readyState,
          muted: v.muted,
          width: rect.width, height: rect.height,
          opacity: cs.opacity, visibility: cs.visibility,
          display: cs.display, position: cs.position,
          transform: cs.transform, zIndex: cs.zIndex
        });
      } catch (e) {}
    } else {
      sendEvent("video_debug", { error: "no video element found" });
    }
    startVideoWatchdog();
  }

  function startVideoWatchdog() {
    stopVideoWatchdog();
    startGlobalHeartbeat();
    videoFirstFrameReported = false;
    watchdogStuckCount = 0;
    videoElementFingerprint = "";
    audioRecoverAttempted = false;
    audioLastPlayingAt = Date.now();
    var video = getCurrentVideo();
    if (video) {
      attachVideoListeners(video);
    }
    videoWatchdogTimer = setInterval(checkVideoAlive, 1200);
    canvasBlackDetectTimer = setInterval(detectRealVideoFrame, 2000);
  }

  function attachVideoListeners(video) {
    video.addEventListener("timeupdate", onVideoTimeUpdate);
    video.addEventListener("playing", onVideoPlaying);
    video.addEventListener("play", onVideoPlaying);
    video.addEventListener("loadeddata", reportFirstFrameOnce);
    video.addEventListener("canplay", reportFirstFrameOnce);
    video.addEventListener("progress", reportFirstFrameOnce);
    video.addEventListener("stalled", onVideoStalled);
    video.addEventListener("ended", onVideoEnded);
    video.addEventListener("pause", onVideoPaused);
    video.addEventListener("volumechange", onVideoVolumeChange);
  }

  function stopVideoWatchdog() {
    if (videoWatchdogTimer) { clearInterval(videoWatchdogTimer); videoWatchdogTimer = null; }
    var video = getCurrentVideo();
    if (video) {
      video.removeEventListener("timeupdate", onVideoTimeUpdate);
      video.removeEventListener("playing", onVideoPlaying);
      video.removeEventListener("play", onVideoPlaying);
      video.removeEventListener("loadeddata", reportFirstFrameOnce);
      video.removeEventListener("canplay", reportFirstFrameOnce);
      video.removeEventListener("progress", reportFirstFrameOnce);
      video.removeEventListener("stalled", onVideoStalled);
      video.removeEventListener("ended", onVideoEnded);
      video.removeEventListener("pause", onVideoPaused);
      video.removeEventListener("volumechange", onVideoVolumeChange);
    }
    if (canvasBlackDetectTimer) { clearInterval(canvasBlackDetectTimer); canvasBlackDetectTimer = null; }
  }

  function onVideoTimeUpdate() {
    videoWatchdogLastTimeUpdate = Date.now();
    audioLastPlayingAt = Date.now();
    reportFirstFrame();
  }

  function onVideoPlaying() {
    videoWatchdogLastPlaying = Date.now();
    videoWatchdogLastTimeUpdate = Date.now();
    audioLastPlayingAt = Date.now();
    tryRecoverAudio();
    reportFirstFrame();
  }

  function reportFirstFrameOnce() {
    reportFirstFrame();
  }

  function onVideoStalled() {
    sendEvent("stalled", {});
  }

  function onVideoEnded() {
    sendEvent("ended", {});
    var component = findTvComponent();
    if (component && component.tvIndex) {
      try {
        component.changeTV(component.tvIndex);
        setTimeout(function() { ensureVideoPlaying(component); }, 200);
      } catch (e) {
        ensureVideoPlaying(component);
      }
    } else {
      ensureVideoPlaying(null);
    }
  }

  function onVideoPaused() {
    var v = getCurrentVideo();
    if (v && !v.ended && v.readyState >= 2) {
      try { v.play(); } catch (ignored) {}
    }
  }

  function onVideoVolumeChange() {
    audioLastPlayingAt = Date.now();
    try {
      var v = getCurrentVideo();
      if (v && v.muted) { v.muted = false; }
    } catch (e) {}
  }

  function checkVideoAlive() {
    if (channelChanging) {
      return;
    }

    var video = getCurrentVideo();
    if (!video) {
      sendEvent("no_video", {});
      return;
    }

    var newFp = video.videoWidth + "x" + video.videoHeight + "#" + video.currentTime;
    if (newFp !== videoElementFingerprint && video.videoWidth > 0) {
      videoElementFingerprint = newFp;
      attachVideoListeners(video);
    }

    var now = Date.now();
    var hasVideoFrames = video.videoWidth > 0 && video.videoHeight > 0;
    var timeFrozen = (now - videoWatchdogLastTimeUpdate) > 4000;
    var neverPlayed = (now - videoWatchdogLastPlaying) > 6000;
    var actuallyPlaying = !video.paused && !video.ended;
    reportFirstFrame();

    var blackResultFresh = lastCanvasBlackResult && (now - lastCanvasBlackResult.at) < 4000;
    var detectedBlack = blackResultFresh && lastCanvasBlackResult.isBlack;

    if (actuallyPlaying && hasVideoFrames && timeFrozen) {
      watchdogStuckCount++;
      forceVideoRepaint();
      if (watchdogStuckCount >= 2) {
        watchdogStuckCount = 0;
        sendEvent("black_screen", { reason: "time_frozen_stuck" });
        hardRefreshVideo("time_frozen_stuck");
      }
    } else if (!video.paused && !hasVideoFrames && neverPlayed) {
      sendEvent("black_screen", { reason: "no_frames_never_played" });
      ensureVideoPlaying(null);
    } else if (video.ended) {
      sendEvent("black_screen", { reason: "ended" });
      onVideoEnded();
    } else if (video.paused && !video.ended && video.readyState >= 2) {
      ensureVideoPlaying(null);
    } else if (actuallyPlaying && hasVideoFrames && detectedBlack && !videoFirstFrameReported) {
      watchdogStuckCount++;
      if (watchdogStuckCount >= 3) {
        watchdogStuckCount = 0;
        sendEvent("black_screen", { reason: "canvas_black_no_first_frame" });
        hardRefreshVideo("canvas_black_no_first_frame");
      }
    } else {
      watchdogStuckCount = 0;
    }

    if (actuallyPlaying && hasVideoFrames && !audioRecoverAttempted) {
      try {
        if (video.muted) {
          tryRecoverAudio();
          audioRecoverAttempted = true;
        }
      } catch (e) {}
    }
  }

  function forceVideoRepaint() {
    applyTvLayout();
    var video = getCurrentVideo();
    if (!video) return;
    try {
      var s = video.style;
      var origTransform = s.transform;
      s.transform = "translateZ(0.001px)";
      video.offsetHeight;
      s.transform = origTransform;
    } catch (e) {}
    try { video.currentTime = video.currentTime; } catch (ignored) {}
    ensureVideoPlaying(null);
  }

  function hardRefreshVideo(reason) {
    applyTvLayout();
    var component = findTvComponent();
    if (component && component.tvIndex) {
      try {
        component.changeTV(component.tvIndex);
      } catch (e) {}
    }
    setTimeout(function () {
      ensureVideoPlaying(component);
      waitForVideoPlaying(activePlaybackRequestId, "", "", "", 2500);
    }, 200);
  }

  function waitForVideoPlaying(requestId, pid, streamId, quality, maxWaitMs, oldVideo) {
    maxWaitMs = maxWaitMs || 2500;
    var deadline = Date.now() + maxWaitMs;
    var video = getCurrentVideo();
    var seenVideoFingerprint = oldVideo ? getVideoFingerprint(oldVideo) : "";
    var gotNewVideo = oldVideo ? false : true;

    function done() { notifyPlayback(requestId, pid, streamId, quality); }

    if (video && !oldVideo && !video.paused && video.readyState >= 2) {
      done();
      return;
    }
    if (oldVideo && video && video !== oldVideo && !video.paused && video.readyState >= 2) {
      done();
      return;
    }

    function onNewVideoReady() { cleanup(); done(); }
    function onTimeout() { cleanup(); ensureVideoPlaying(null); done(); }
    function cleanup() {
      if (video) {
        video.removeEventListener("playing", onNewVideoReady);
        video.removeEventListener("canplay", onNewVideoReady);
        video.removeEventListener("timeupdate", onNewVideoReady);
      }
    }

    var pollTimer = setInterval(function () {
      var v = getCurrentVideo();
      if (oldVideo) {
        if (v && v !== oldVideo) {
          if (!gotNewVideo) {
            gotNewVideo = true;
            if (seenVideoFingerprint) seenVideoFingerprint = "";
          }
          if (!v.paused && v.readyState >= 2) {
            video = v;
            clearInterval(pollTimer);
            attachListeners();
          } else {
            video = v;
          }
        } else if (!v) {
          if (seenVideoFingerprint) seenVideoFingerprint = "";
        }
      } else {
        if (v && !v.paused && v.readyState >= 2) {
          video = v;
          clearInterval(pollTimer);
          attachListeners();
        } else if (v && !video) {
          video = v;
        }
      }
      if (Date.now() >= deadline) {
        clearInterval(pollTimer);
        onTimeout();
      }
    }, 40);

    function attachListeners() {
      if (!video) { onTimeout(); return; }
      video.addEventListener("playing", onNewVideoReady, { once: true });
      video.addEventListener("canplay", onNewVideoReady, { once: true });
      video.addEventListener("timeupdate", onNewVideoReady, { once: true });
      setTimeout(onTimeout, Math.max(0, deadline - Date.now()));
      if (!video.paused && video.readyState >= 2) { cleanup(); done(); }
    }
  }

  function getVideoFingerprint(v) {
    if (!v) return "";
    try {
      var rect = v.getBoundingClientRect();
      return String(v.videoWidth || "") + "x" + String(v.videoHeight || "")
        + "@" + Math.round(rect.width) + "x" + Math.round(rect.height);
    } catch (e) { return ""; }
  }

  function tryLoadChannelsFromVue() {
    var component = findTvComponent();
    if (!component) return null;
    var channels = collectChannelsFromVue(component);
    if (channels && channels.length > 0) return channels;
    return null;
  }

  async function tryLoadChannelsFromApi() {
    var apiModuleIds = ["03ef", "a1b2", "c3d4"];
    var methodNames = ["h", "getChannelList", "getChannels", "fetchChannels", "getTVChannels"];
    var pageIds = ["PG00000004", "PG00000001"];

    try {
      var require = captureWebpackRequire();
    } catch (e) {
      console.warn("[YSP] cannot capture webpack require: " + e.message);
      return null;
    }

    for (var mi = 0; mi < apiModuleIds.length; mi++) {
      try {
        var mod = require(apiModuleIds[mi]);
        if (!mod) continue;
        for (var ki = 0; ki < methodNames.length; ki++) {
          if (typeof mod[methodNames[ki]] !== "function") continue;
          for (var pi = 0; pi < pageIds.length; pi++) {
            try {
              var result = await mod[methodNames[ki]](pageIds[pi]);
              if (result) {
                var channels = collectChannelsFromApi(result.data || result);
                if (channels && channels.length > 0) {
                  console.log("[YSP] loaded " + channels.length + " channels via API " + apiModuleIds[mi] + "." + methodNames[ki]);
                  return channels;
                }
              }
            } catch (e2) {}
          }
        }
      } catch (e3) {}
    }

    for (var modId in window) {
      if (modId.length <= 5 && /^[a-f0-9]+$/i.test(modId)) {
        try {
          var m = require(modId);
          if (!m) continue;
          for (var k in m) {
            if (typeof m[k] === "function") {
              try {
                var r = await m[k]("PG00000004");
                if (r) {
                  var ch = collectChannelsFromApi(r.data || r);
                  if (ch && ch.length > 0) {
                    console.log("[YSP] discovered channels via " + modId + "." + k);
                    return ch;
                  }
                }
              } catch (e4) {}
            }
          }
        } catch (e5) {}
      }
    }

    return null;
  }

  function loadChannels() {
    startGlobalHeartbeat();
    applyTvLayout();

    if (channelsLoadedReported) return;

    var vueChannels = tryLoadChannelsFromVue();
    if (vueChannels && vueChannels.length > 0) {
      channelsLoadedReported = true;
      try { YspAndroid.onChannels(JSON.stringify(vueChannels)); } catch (ignored) {}
      console.log("[YSP] loaded " + vueChannels.length + " channels from Vue component");
      return;
    }

    tryLoadChannelsFromApi().then(function (apiChannels) {
      if (apiChannels && apiChannels.length > 0) {
        channelsLoadedReported = true;
        try { YspAndroid.onChannels(JSON.stringify(apiChannels)); } catch (ignored) {}
      } else {
        console.warn("[YSP] failed to load channels from both Vue and API, will retry");
      }
    }).catch(function (err) {
      console.warn("[YSP] loadChannels API error: " + (err && err.message));
    });

    setTimeout(loadChannels, 1500);
  }

  function playChannel(requestId, pid, streamId, quality, mode, attempt) {
    var requestKey = String(requestId);
    if (typeof mode === "number") { attempt = mode; mode = "channel"; }
    mode = mode || "channel";
    if (attempt == null) { activePlaybackRequestId = requestKey; attempt = 0; }
    else { attempt = attempt || 0; }

    if (requestKey !== activePlaybackRequestId) return;

    try {
      stopVideoWatchdog();
      channelChanging = true;
      channelChangeStartAt = Date.now();
      applyTvLayout();
      startGlobalHeartbeat();
      var component = findTvComponent();
      if (!component || !component.tabA || !component.tabB) {
        if (attempt < 40) {
          setTimeout(function () { playChannel(requestId, pid, streamId, quality, mode, attempt + 1); }, 80);
          return;
        }
        throw new Error("Official TV component is not ready");
      }

      var match = findChannelInOfficialComponent(component, pid, streamId);
      if (!match) throw new Error("Channel not found in official player: " + pid);
      if (requestKey !== activePlaybackRequestId) return;

      videoFirstFrameReported = false;
      watchdogStuckCount = 0;
      videoElementFingerprint = "";
      audioRecoverAttempted = false;

      if (mode === "quality") {
        applyOfficialQuality(component, quality);
        lastPlaybackQuality = quality;
        setTimeout(function () {
          if (requestKey !== activePlaybackRequestId) return;
          applyTvLayout();
          waitForVideoPlaying(requestId, pid, streamId, quality, 2000);
          channelChanging = false;
        }, 40);
        return;
      }

      component.selectIndex = match.group;

      var needChange = !isSameOfficialChannel(component, match.channel);
      var qualityNeedChange = quality && String(quality) !== "fhd" && String(quality) !== lastPlaybackQuality;

      if (needChange && !qualityNeedChange) {
        _videoBeforeChange = getCurrentVideo();
        component.changeTV(match.channel);
        lastPlaybackQuality = "fhd";
        setTimeout(function () {
          if (requestKey !== activePlaybackRequestId) return;
          applyTvLayout();
          waitForVideoPlaying(requestId, pid, streamId, quality, 3000, _videoBeforeChange);
          channelChanging = false;
        }, 80);
        return;
      }

      if (needChange && qualityNeedChange) {
        _videoBeforeChange = getCurrentVideo();
        component.changeTV(match.channel);
        setTimeout(function () {
          if (requestKey !== activePlaybackRequestId) return;
          applyOfficialQuality(component, quality);
          lastPlaybackQuality = quality;
          setTimeout(function () {
            if (requestKey !== activePlaybackRequestId) return;
            applyTvLayout();
            waitForVideoPlaying(requestId, pid, streamId, quality, 3000, _videoBeforeChange);
            channelChanging = false;
          }, 80);
        }, 80);
        return;
      }

      if (!needChange && qualityNeedChange) {
        applyOfficialQuality(component, quality);
        lastPlaybackQuality = quality;
        setTimeout(function () {
          if (requestKey !== activePlaybackRequestId) return;
          applyTvLayout();
          waitForVideoPlaying(requestId, pid, streamId, quality, 2000);
          channelChanging = false;
        }, 40);
        return;
      }

      if (!needChange && !qualityNeedChange) {
        ensureVideoPlaying(component);
        setTimeout(function () {
          if (requestKey !== activePlaybackRequestId) return;
          applyTvLayout();
          waitForVideoPlaying(requestId, pid, streamId, quality, 2000);
          channelChanging = false;
        }, 40);
        return;
      }
    } catch (error) {
      channelChanging = false;
      try {
        YspAndroid.onPlayback(String(requestId), JSON.stringify({
          ok: false, error: error && (error.message || String(error)) || "playback failed"
        }));
      } catch (ignored2) {}
      try { YspAndroid.onError("playback", error && (error.message || String(error))); } catch (ignored) {}
    }
  }

  window.YspTvBridge = {
    loadChannels: loadChannels,
    playChannel: playChannel,
    reloadPage: function () { try { location.reload(); } catch (ignored) {} },
    hardRefreshVideo: hardRefreshVideo,
    forceRepaint: forceVideoRepaint,
    stopWatchdog: stopVideoWatchdog,
    startWatchdog: startVideoWatchdog,
    tryRecoverAudio: tryRecoverAudio
  };

  function primeEarly() {
    startGlobalHeartbeat();
    applyTvLayout();
    try { captureWebpackRequire(); } catch (e) {}
  }
  primeEarly();

  var poll = setInterval(function () {
    try { captureWebpackRequire(); clearInterval(poll); } catch (e) {}
  }, 100);

  setTimeout(loadChannels, 500);
}());