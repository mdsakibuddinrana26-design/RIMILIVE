#!/usr/bin/env bash
set -eu

cd "$(dirname "$0")/.."
root="$PWD"
export ANDROID_HOME="${ANDROID_HOME:-$root/.android-sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
tools="$ANDROID_HOME/cmdline-tools/latest"

if [ ! -x "$tools/bin/sdkmanager" ]; then
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  archive="$ANDROID_HOME/cmdline-tools/commandlinetools.zip"
  curl -fL --retry 3 \
    https://dl.google.com/android/repository/commandlinetools-linux-16111833_latest.zip \
    -o "$archive"
  echo "e025545c62a8e64c7559119566a569fb1dec5f60  $archive" | sha1sum -c -
  unzip -q -o "$archive" -d "$ANDROID_HOME/cmdline-tools"
  mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$tools"
  rm "$archive"
fi

if [ ! -f "$ANDROID_HOME/platforms/android-36.1/android.jar" ] ||
   [ ! -x "$ANDROID_HOME/build-tools/36.0.0/aapt2" ]; then
  yes | "$tools/bin/sdkmanager" --sdk_root="$ANDROID_HOME" --licenses >/dev/null
  "$tools/bin/sdkmanager" --sdk_root="$ANDROID_HOME" \
    "platforms/android-36.1" "build-tools/36.0.0"
fi

# The imported build explicitly signs debug builds with this untracked keystore.
if [ ! -f debug.keystore ]; then
  keytool -genkeypair -noprompt -keystore debug.keystore \
    -alias androiddebugkey -storepass android -keypass android \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
fi

exec ./gradlew :app:assembleDebug --no-daemon --console=plain "$@"