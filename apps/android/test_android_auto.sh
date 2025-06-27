#!/bin/bash

# Android Auto Testing Script for KITT App
# This script helps verify that the KittCarAppService is properly registered and working

echo "🚗 KITT Android Auto Testing Script"
echo "===================================="

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK Platform-Tools"
    exit 1
fi

# Check if device is connected
DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep "device" | wc -l)
if [ $DEVICE_COUNT -eq 0 ]; then
    echo "❌ No Android device connected. Please connect a device or start an emulator"
    exit 1
fi

echo "✅ Device connected: $(adb devices | grep device | head -1 | cut -f1)"

# Build and install the app
echo "🔨 Building and installing KITT app..."
cd "$(dirname "$0")"
./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

./gradlew installDebug
if [ $? -ne 0 ]; then
    echo "❌ Installation failed"
    exit 1
fi

echo "✅ App installed successfully"

# Check if the service is properly registered
echo "🔍 Checking CarAppService registration..."
SERVICE_CHECK=$(adb shell dumpsys package com.kitt.android | grep -A 5 -B 5 "KittCarAppService")
if [ -z "$SERVICE_CHECK" ]; then
    echo "❌ KittCarAppService not found in package dump"
    echo "   This might indicate the service is not properly registered"
else
    echo "✅ KittCarAppService found in package registry"
    echo "$SERVICE_CHECK"
fi

# Check for Android Auto related permissions and features
echo "🔍 Checking Android Auto permissions..."
PERMS=$(adb shell dumpsys package com.kitt.android | grep -E "(automotive|car\.templates)")
if [ -z "$PERMS" ]; then
    echo "⚠️  No automotive-specific features detected"
else
    echo "✅ Automotive features detected:"
    echo "$PERMS"
fi

# Check if Android Auto is available on the device
echo "🔍 Checking Android Auto availability..."
AUTO_PACKAGE=$(adb shell pm list packages | grep "com.google.android.projection.gearhead")
if [ -z "$AUTO_PACKAGE" ]; then
    echo "⚠️  Android Auto not installed on device"
    echo "   Install from: https://play.google.com/store/apps/details?id=com.google.android.projection.gearhead"
else
    echo "✅ Android Auto installed on device"
fi

# Launch the app to verify it starts correctly
echo "🚀 Launching KITT app..."
adb shell am start -n com.kitt.android/.MainActivity
sleep 3

# Check if the app is running
APP_RUNNING=$(adb shell ps | grep com.kitt.android)
if [ -z "$APP_RUNNING" ]; then
    echo "❌ App failed to start"
else
    echo "✅ App is running"
fi

# Test the CarAppService directly
echo "🔧 Testing CarAppService directly..."
adb shell am start-service com.kitt.android/.KittCarAppService
sleep 2

echo ""
echo "📱 Next Steps for DHU Testing:"
echo "1. Download Android Auto Desktop Head Unit (DHU) from:"
echo "   https://developer.android.com/training/cars/testing#dhu"
echo "2. Enable Developer Options in Android Auto on your device"
echo "3. Connect device to computer via USB"
echo "4. Start DHU with: ./desktop-head-unit"
echo "5. Look for KITT in the available apps list"
echo ""
echo "🔧 If KITT doesn't appear in DHU:"
echo "- Ensure USB debugging is enabled"
echo "- Check that Android Auto developer mode is enabled"
echo "- Verify the app is installed and CarAppService is working"
echo "- Check logcat for any errors: adb logcat | grep -E '(KITT|CarApp)'"

echo ""
echo "📋 Testing completed!"
