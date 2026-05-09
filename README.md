# YangshipinWrapper4TV

YangshipinWrapper4TV is an Android TV and mobile wrapper for the free live-TV
channels exposed by the official Yangshipin/CCTV live page:

https://www.yangshipin.cn/tv/home

The project goal is to provide a native, remote-control-friendly full-screen TV
experience for free CCTV, CGTN, and local satellite channels without depending
on the rendered HTML layout of the website. The app targets Android 4.4+
(`minSdkVersion 19`) so it can run on older TV boxes as well as newer Android TV
devices and phones.

## What This Project Does

- Loads the official Yangshipin live-TV protocol runtime from the official page.
- Discovers playable free channels from the protocol data, not from page DOM
  tags.
- Drives the official Yangshipin web player for the selected channel and
  quality.
- Shows that player as the full-screen video surface, with native Android TV
  controls layered above it.
- Provides TV remote controls and mobile touch gestures over the same playback
  model.
- Remembers the last selected stream quality, channel, and decoder-mode setting.

## Official Source

The original web experience is the Yangshipin/CCTV TV live page:

- Official page: `https://www.yangshipin.cn/tv/home`
- Page title: `央视频 - 有品质的视频社交媒体`

This app is only a wrapper around free live-channel access exposed by that
official site. It does not ship a private channel database or hard-coded stream
URLs.

## Principle And Mechanism

The app intentionally avoids scraping rendered HTML tags because class names,
DOM positions, and component structure can change frequently. Instead, it uses
the same protocol modules that the official Yangshipin page loads:

- Channel list: `https://capi.yangshipin.cn/api/oms/pc/page/PG00000004?...`
- Stream auth: `https://player-api.yangshipin.cn/v1/player/auth`
- Stream URL: `https://player-api.yangshipin.cn/v1/player/get_live_info`

Yangshipin signs `get_live_info` with its JavaScript/WASM runtime (`cKey`,
`yspticket`, and OpenAPI headers), and the official page decodes the live video
through its own web player path. Emulator and FFmpeg checks showed that feeding
the signed `_web.m3u8` URLs directly into normal native Android HLS players can
produce audio-only playback, black/gray frames, or decode errors.

The implementation therefore uses the official Yangshipin player as the video
surface instead of scraping HTML or replaying the HLS URL in a native player:

1. A full-screen `WebView` loads the official Yangshipin TV page.
2. `app/src/main/assets/ysp_bridge.js` captures the page's webpack module
   runtime.
3. The bridge calls the official page module that loads `PG00000004`.
4. It walks the returned protocol data and extracts channel records from
   `dataTvChannelList`.
5. It keeps only free channels with `payType=879`.
6. When the user selects a channel or quality, the bridge locates the official
   TV Vue component and asks it to change channel or quality.
7. Native Android views draw the TV menu, status text, channel overlay, remote
   handling, and touch gesture trace above the WebView.

This keeps the native UI stable while allowing Yangshipin's own protocol,
signature, and playback code to stay responsible for request and decode
details.

Free channels are filtered by `payType=879`, covering CCTV/CGTN and local
satellite channels. Paid/VIP channels with `payType=880` are not shown in the TV
menu.

## Project Layout

- `app/src/main/java/com/lwtdzh/yangshipinwrapper4tv/MainActivity.java`: native
  Android UI, full-screen WebView host, TV remote handling, touch gestures,
  persistence, and cleanup.
- `app/src/main/assets/ysp_bridge.js`: protocol and playback bridge that runs
  inside the official Yangshipin `WebView`.
- `scripts/build-debug.sh`: dependency-light debug APK build script using the
  local Android SDK tools.
- `TESTING.md`: emulator validation notes and tested behavior.

## Controls

- Up/Down: previous/next channel.
- Left/Right: change stream quality.
- OK/Enter: open the menu. From playback this enters the channel list directly;
  from the channel list, use Left to return to the first-level menu.
- Back: close the menu; from playback, exit the Activity and clean up playback/bridge resources.
- Menu: open the channel menu.
- Number keys: jump to a 1-based channel number.

The first-level menu contains `Settings` and `Channels`. `Channels` opens the
playable channel list. `Settings` contains the decoder-mode switcher, defaulting
to `HW`; selecting it toggles between `HW` and `SW`. The selected mode is saved
in `SharedPreferences`.

Touch screens are also supported:

- Tap empty playback space: same as OK, opens the channel menu.
- Swipe up/down/left/right: same as TV remote direction keys.
- A visible gesture trace is drawn while the finger moves.
- Drag inside the menu: scroll channel list.
- Tap a channel inside the menu: switch to that channel and keep the menu open.
- Swipe left inside the channel/settings menu: return to the first-level menu.
- Swipe right inside the first-level menu: enter the highlighted section.
- Tap outside the menu: close the menu.

The default quality is `fhd` (`1080P Blu-ray`). The last selected quality,
channel, and decoder mode are saved in `SharedPreferences`.

## Build

```sh
./scripts/build-debug.sh
```

The app supports Android 4.4 and later with `minSdkVersion 19`.
