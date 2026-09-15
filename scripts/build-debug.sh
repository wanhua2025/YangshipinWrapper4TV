#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/Users/lwtdzh/Library/Android/sdk}}"
BUILD_TOOLS="${ANDROID_BUILD_TOOLS:-$SDK/build-tools/33.0.1}"
ANDROID_JAR="$SDK/platforms/android-33/android.jar"
OUT_DIR="$ROOT_DIR/build/manual"
APK_DIR="$ROOT_DIR/build/outputs/apk/debug"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR/gen" "$OUT_DIR/classes" "$OUT_DIR/dex" "$APK_DIR"

"$BUILD_TOOLS/aapt" package -f -m \
  -J "$OUT_DIR/gen" \
  -M "$ROOT_DIR/app/src/main/AndroidManifest.xml" \
  -S "$ROOT_DIR/app/src/main/res" \
  -I "$ANDROID_JAR"

javac -encoding UTF-8 -source 1.8 -target 1.8 \
  -bootclasspath "$ANDROID_JAR" \
  -d "$OUT_DIR/classes" \
  $(find "$OUT_DIR/gen" "$ROOT_DIR/app/src/main/java" -name '*.java' | sort)

"$BUILD_TOOLS/d8" --min-api 19 \
  --lib "$ANDROID_JAR" \
  --output "$OUT_DIR/dex" \
  $(find "$OUT_DIR/classes" -name '*.class' | sort)

"$BUILD_TOOLS/aapt" package -f \
  -M "$ROOT_DIR/app/src/main/AndroidManifest.xml" \
  -S "$ROOT_DIR/app/src/main/res" \
  -A "$ROOT_DIR/app/src/main/assets" \
  -I "$ANDROID_JAR" \
  -F "$OUT_DIR/app-unsigned.apk" \
  "$OUT_DIR/dex"

"$BUILD_TOOLS/zipalign" -f 4 "$OUT_DIR/app-unsigned.apk" "$OUT_DIR/app-aligned.apk"

if [ ! -f "$HOME/.android/debug.keystore" ]; then
  mkdir -p "$HOME/.android"
  keytool -genkeypair -v \
    -keystore "$HOME/.android/debug.keystore" \
    -storepass android \
    -alias androiddebugkey \
    -keypass android \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
fi

"$BUILD_TOOLS/apksigner" sign \
  --ks "$HOME/.android/debug.keystore" \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out "$APK_DIR/YangshipinWrapper4TV-debug.apk" \
  "$OUT_DIR/app-aligned.apk"

"$BUILD_TOOLS/apksigner" verify --verbose "$APK_DIR/YangshipinWrapper4TV-debug.apk"
ls -lh "$APK_DIR/YangshipinWrapper4TV-debug.apk"
