(function () {
  "use strict";

  var activePlaybackRequestId = "";

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

  function applyTvLayout() {
    try {
      if (!document.getElementById("ysp-tv-wrapper-style")) {
        var style = document.createElement("style");
        style.id = "ysp-tv-wrapper-style";
        style.textContent = [
          "html,body,#app{margin:0!important;padding:0!important;width:100vw!important;height:100vh!important;overflow:hidden!important;background:#000!important;}",
          ".tv-home,.tv,.tv-main,.tv-main-con,.tv-main-con-l,.tv-main-con-l-vid{width:100vw!important;height:100vh!important;max-width:none!important;margin:0!important;padding:0!important;background:#000!important;}",
          ".tv-main-con-r,.tv-zhan,.header,.footer,.public-com,.activity-com,[class*=Footer],[class*=footer]{display:none!important;}",
          ".tv-main-con-l{width:100vw!important;max-width:none!important;float:none!important;}",
          ".tv-main-con-l-vid>div,.tv-main-con-l-vid .img,.tv-main-con-l-vid video{width:100vw!important;height:100vh!important;object-fit:contain!important;background:#000!important;}",
          "video{width:100vw!important;height:100vh!important;object-fit:contain!important;background:#000!important;}",
          ".control,.controlBar,.control-bar,.poster,.loading,.play-btn{opacity:0!important;pointer-events:none!important;}"
        ].join("\n");
        document.head.appendChild(style);
      }
      var videos = document.getElementsByTagName("video");
      for (var i = 0; i < videos.length; i++) {
        videos[i].setAttribute("playsinline", "true");
        videos[i].setAttribute("webkit-playsinline", "true");
        videos[i].style.width = "100vw";
        videos[i].style.height = "100vh";
        videos[i].style.objectFit = "contain";
      }
    } catch (ignored2) {
    }
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

  function playChannel(requestId, pid, streamId, quality, attempt) {
    var requestKey = String(requestId);
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
        if (attempt < 30) {
          setTimeout(function () {
            playChannel(requestId, pid, streamId, quality, attempt + 1);
          }, 500);
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
      component.selectIndex = match.group;
      if (!component.tvIndex || String(component.tvIndex.pid) !== String(match.channel.pid)) {
        component.changeTV(match.channel);
      } else {
        component.setTvConfig(
          match.channel.pid,
          match.channel.streamId,
          match.channel.coverUrl,
          match.channel.viewRights,
          match.channel.payType
        );
      }
      setTimeout(function () {
        if (requestKey !== activePlaybackRequestId) {
          return;
        }
        applyTvLayout();
        applyOfficialQuality(component, quality);
        ensureVideoPlaying(component);
        try {
          YspAndroid.onPlayback(String(requestId), JSON.stringify({
            ok: true,
            pid: String(pid),
            streamId: String(streamId),
            quality: String(quality || "")
          }));
        } catch (ignored) {
        }
      }, 1600);
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
