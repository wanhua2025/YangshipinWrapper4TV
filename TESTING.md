# Test Report

Validated on AVD `SmsF_API33` with Android API 33.

## Playback Root Cause

The original native-player route could fetch signed Yangshipin HLS URLs, but
those `_web.m3u8` streams did not render reliably in normal Android HLS players:
emulator runs showed audio-only/gray video, and FFmpeg samples reported corrupt
H.264 frames. The fixed app renders video through the official Yangshipin
WebView player and keeps the Android TV/mobile controls as native overlays.

## Protocol And Startup

- Installed a clean app instance.
- Loaded Yangshipin protocol channel list from `PG00000004`.
- Verified `channels_loaded count=59`.
- Verified first startup requested `CCTV1` with default `quality=fhd`.
- Verified WebView playback callback `ok=true`.
- Verified screenshots after 20-45 seconds show real CCTV video frames, not a
  black or gray player.

## TV Remote

- `DPAD_DOWN`: changed from `CCTV1` to `CCTV2`.
- `DPAD_LEFT`: changed quality from `fhd` to `shd`.
- `DPAD_CENTER`: opened the two-level menu directly at the channel list.
- `DPAD_LEFT` from the channel list: returned to the first-level menu with
  `Channels` selected.
- `DPAD_UP` then `DPAD_CENTER` from the first-level menu: opened `Settings`.
- `DPAD_CENTER` in `Settings`: toggled decoder mode between `HW` and `SW`.
- `DPAD_DOWN` inside menu: moved highlight cursor.
- `DPAD_CENTER` inside menu: selected `CCTV4` and kept the menu visible.
- `BACK` inside menu: hid the menu.
- Number keys jumped to local satellite channels and rendered real video.

## Touch Screen

- Tap playback area: opened the channel menu.
- Tap channel row in the menu: selected `江苏卫视` and kept the menu visible.
- Drag inside menu: scrolled the channel list.
- Tap outside menu: hid the menu.
- Swipe down/up: changed channels.
- Swipe right/left: changed stream quality.
- Slow swipe screenshot confirmed the cyan gesture trajectory is drawn.

## Persistence And Cleanup

- Changed quality to `shd`.
- Force-stopped and relaunched the app.
- Verified relaunch requested `江苏卫视` with `quality=shd`.
- Cleared app data and verified the decoder mode defaults to `HW`.
- Toggled decoder mode to `SW`, force-stopped and relaunched the app, and
  verified `Settings` still showed `Decoder Mode  SW`.
- Verified video remained visible after selecting and persisting `SW`.
- Pressed Back from playback.
- Verified `destroy_cleanup_complete` in logcat.

## Two-Level Menu And Decoder Mode Regression

Artifacts are in `build/outputs/`.

- `final-settings-default-hw.png`: clean app data, OK opened Channels by
  default, Left returned to the first-level menu, Settings showed
  `Decoder Mode  HW`.
- `final-settings-toggled-sw.png`: selecting the Settings row changed the saved
  decoder mode to `SW`.
- `final-sw-persisted-playback-after-overlay.png`: after force-stop/relaunch
  with `SW` persisted, the player still rendered real video frames.
- `final-settings-persisted-sw.png`: after relaunch, Settings still showed
  `Decoder Mode  SW`.
- Logcat confirmed `channels_loaded count=59`, `web_playback ok=true`, no
  `protocol_error`, and quality persistence by relaunching with `quality=shd`.

## Latest Patched APK Regression

Artifacts are in `build/outputs/patched-webview-test/`.

- `startup-45s.png`: CCTV1 real video after app launch.
- `remote-channel-down.png`: remote Down changed channel to CCTV2.
- `remote-quality-left.png`: remote Left changed quality.
- `menu-select-stays.png`: OK menu selection changed to CCTV4 and the menu
  stayed visible.
- `menu-back-hidden.png`: Back hid the menu.
- `number-19.png`: numeric channel input selected a local satellite channel
  with real video.
- `touch-menu-open.png`, `touch-menu-scroll.png`, and
  `touch-menu-outside-hide.png`: touch menu open/scroll/outside-dismiss path.
- `touch-swipe-up-channel.png`: touch swipe changed channel.
- `touch-swipe-right-quality.png`: touch swipe changed quality.
- `logcat.log`: no fatal exception, ANR, protocol error, or `ok=false`
  playback result was found; seven playback requests reported `ok=true`.

## Fullscreen And Smooth Channel Switch Regression

Artifacts are in `build/outputs/no-bar-smooth-final-test/`.

- `startup-no-bar.png`: app-specific top status/channel bar is gone; only the
  broadcast video remains.
- `channel-switch-2s.png` and `channel-switch-7s.png`: remote Down switches to
  CCTV2 with the required
  channel-name overlay during loading, then continues as live video without a
  second quality-triggered reload.
- `quality-switch.png`: remote Left changes quality through the official player
  quality path.
- `logcat.log`: no fatal exception, protocol error, or `ok=false` playback
  result was found. The channel switch has one decoder release/create cycle;
  the previous delayed quality re-apply reload was removed.
