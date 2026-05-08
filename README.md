# YangshipinWrapper4TV

Android TV wrapper for the Yangshipin live TV page.

## Protocol

The app does not scrape channel names from rendered HTML tags. It uses the same protocol modules loaded by `https://www.yangshipin.cn/tv/home`:

- Channel list: `https://capi.yangshipin.cn/api/oms/pc/page/PG00000004?...`
- Stream auth: `https://player-api.yangshipin.cn/v1/player/auth`
- Stream URL: `https://player-api.yangshipin.cn/v1/player/get_live_info`

Yangshipin signs `get_live_info` with its JavaScript/WASM runtime (`cKey`, `yspticket`, OpenAPI headers). The app therefore runs a hidden protocol `WebView` to execute the official protocol/signature code and passes only decoded free-channel data plus HLS stream URLs to the native Android TV UI.

Free channels are filtered by `payType=879`, covering CCTV/CGTN and local satellite channels. Paid/VIP channels with `payType=880` are not shown in the TV menu.

## Controls

- Up/Down: previous/next channel.
- Left/Right: change stream quality.
- OK/Enter: open the channel menu, or select the highlighted menu item.
- Back: close the menu; from playback, exit the Activity and clean up playback/bridge resources.
- Menu: open the channel menu.
- Number keys: jump to a 1-based channel number.

Touch screens are also supported:

- Tap empty playback space: same as OK, opens the channel menu.
- Swipe up/down/left/right: same as TV remote direction keys.
- A visible gesture trace is drawn while the finger moves.
- Drag inside the menu: scroll channel list.
- Tap a channel inside the menu: switch to that channel and keep the menu open.
- Tap outside the menu: close the menu.

The default quality is `fhd` (`1080P Blu-ray`). The last selected quality is saved in `SharedPreferences`.

## Build

```sh
./scripts/build-debug.sh
```

The app supports Android 4.4 and later with `minSdkVersion 19`.
