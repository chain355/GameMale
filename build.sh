#!/usr/bin/env bash
# 出包 + 核验版本号：带超时与自动重试
set -uo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
JDK="$ROOT/../.build/jdk"
BT="$ROOT/../.build/sdk/build-tools/37.0.0"
KS="$ROOT/../.build/gamemale.keystore"
OUT="$ROOT/../"
GRADLE="$ROOT/gradlew"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-900}"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-3}"

VC=$(grep -oP 'versionCode\s*=\s*\K[0-9]+' app/build.gradle.kts | head -1)
VN=$(grep -oP 'versionName\s*=\s*"\K[^"]+' app/build.gradle.kts | head -1)
echo "==> target: name=$VN code=$VC"

export JAVA_HOME="$JDK"

attempt=1
while [ "$attempt" -le "$MAX_ATTEMPTS" ]; do
  echo "==> assemble attempt $attempt/$MAX_ATTEMPTS"
  timeout "$TIMEOUT_SECONDS" "$GRADLE" :app:assembleRelease --console=plain
  rc=$?
  APK="$ROOT/app/build/outputs/apk/release/app-release-unsigned.apk"
  if [ $rc -eq 0 ] && [ -f "$APK" ]; then
    BADGE=$("$BT/aapt2" dump badging "$APK" 2>/dev/null | head -1)
    if echo "$BADGE" | grep -q "versionCode='$VC'" && echo "$BADGE" | grep -q "versionName='$VN'"; then
      echo "==> gradle ok and version matches"
      break
    else
      echo "!! 编译产物版本不符：$BADGE"
    fi
  else
    echo "!! 编译失败 rc=$rc (可能超时或环境繁忙)"
  fi
  attempt=$((attempt+1))
  sleep 10
done

if [ "$attempt" -gt "$MAX_ATTEMPTS" ]; then
  echo "!! 多次重试仍失败，放弃出包"
  exit 1
fi

ZIP="$ROOT/app/build/outputs/apk/release/app-release-unsigned.apk"
TMP="/tmp/gamemale-unsigned-$VC.apk"
FINAL="$OUT/Gamemale-$VN.apk"

"$BT/zipalign" -f -p 4 "$ZIP" "$TMP" || exit 1
"$BT/apksigner" sign --ks "$KS" --ks-pass pass:gamemale2026 --key-pass pass:gamemale2026 \
  --v1-signing-enabled true --v2-signing-enabled true --out "$FINAL" "$TMP" || exit 1

BADGE=$("$BT/aapt2" dump badging "$FINAL" | head -1)
if ! echo "$BADGE" | grep -q "versionCode='$VC'" || ! echo "$BADGE" | grep -q "versionName='$VN'"; then
  echo "!! 签名产物版本核验失败：$BADGE"
  exit 1
fi
echo "==> OK: $FINAL"
echo "==> $BADGE"
