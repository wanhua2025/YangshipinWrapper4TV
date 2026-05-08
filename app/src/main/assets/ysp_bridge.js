(function () {
  "use strict";

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

  function stopPagePlayback() {
    try {
      var videos = document.getElementsByTagName("video");
      for (var i = 0; i < videos.length; i++) {
        try {
          videos[i].pause();
          videos[i].removeAttribute("src");
          videos[i].load();
        } catch (ignored) {
        }
      }
    } catch (ignored2) {
    }
  }

  async function loadChannels() {
    try {
      var require = captureWebpackRequire();
      var api = require("03ef");
      var page = await api.h("PG00000004");
      var channels = collectChannels(page && page.data);
      stopPagePlayback();
      YspAndroid.onChannels(JSON.stringify(channels));
    } catch (error) {
      sendError("channels", error);
    }
  }

  async function getStream(requestId, pid, streamId, quality) {
    try {
      var require = captureWebpackRequire();
      var player = require("ed4d").default.livePlayer;
      var methods = player.methods;
      var selectedQuality = quality || "fhd";
      var context = {
        videoConfig: { pid: String(pid), vid: String(streamId) },
        oldWatchTime: 0,
        totalWatchTime: 0,
        reloadLive: 0,
        configCanPlay: true,
        myPoster: { finishQuality: function () {} },
        myVideo: { initTimes: function () {} },
        isWasmSupported: methods.isWasmSupported,
        errorInfos: null,
        videoUrl: "",
        videoInfo: null,
        currDef: ""
      };
      await methods.getLiveUrlsByVid.call(
        context,
        String(pid),
        String(streamId),
        selectedQuality,
        0
      );
      stopPagePlayback();
      if (!context.videoUrl) {
        throw new Error("Yangshipin returned no playable stream URL");
      }
      YspAndroid.onStream(String(requestId), JSON.stringify({
        ok: true,
        url: context.videoUrl,
        defn: context.currDef || selectedQuality,
        info: context.videoInfo || {}
      }));
    } catch (error) {
      YspAndroid.onStream(String(requestId), JSON.stringify({
        ok: false,
        error: error && (error.message || String(error)) || "stream request failed"
      }));
    }
  }

  window.YspTvBridge = {
    loadChannels: loadChannels,
    getStream: getStream
  };

  loadChannels();
}());
