#!/usr/bin/env bash
# 一键构建并安装到已连接设备（不自动启动，手动测试）。
# 用法：./build.sh
set -euo pipefail
cd "$(dirname "$0")"

./gradlew :app:assembleDebug --console=plain

APK=$(ls -t app/build/outputs/apk/debug/app-debug.apk | head -1)
adb install -r "$APK"

echo "✓ 已安装 $APK"
echo "  首次使用需授予全存储权限（一次性）："
echo "    adb shell appops set com.copy.mt.debug MANAGE_EXTERNAL_STORAGE allow"
echo "  启动：adb shell am start -n com.copy.mt.debug/com.copy.mt.MainActivity"
