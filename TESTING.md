# Test Report

Validated on AVD `SmsF_API33` with Android API 33.

## Protocol And Startup

- Installed a clean app instance.
- Loaded Yangshipin protocol channel list from `PG00000004`.
- Verified `channels_loaded count=59`.
- Verified first startup requested `CCTV1` with default `quality=fhd`.
- Verified returned HLS stream URL from `player/get_live_info`.

## TV Remote

- `DPAD_DOWN`: changed from `CCTV1` to `CCTV2`.
- `DPAD_LEFT`: changed quality from `fhd` to `shd`.
- `DPAD_CENTER`: opened the channel menu.
- `DPAD_DOWN` inside menu: moved highlight cursor.
- `DPAD_CENTER` inside menu: selected `CCTV4` and kept the menu visible.
- `BACK` inside menu: hid the menu.
- Number keys `2`, `8`: jumped to `北京卫视`.

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
- Pressed Back from playback.
- Verified `destroy_cleanup_complete` in logcat.
