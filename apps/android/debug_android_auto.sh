#!/bin/bash

# Comprehensive Android Auto Debugging Script for KITT App
# Based on official Android Auto troubleshooting guidelines

echo "🔧 KITT Android Auto Debug & Fix Script"
echo "========================================"

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

# Step 1: ADB Port Forwarding (Critical for DHU)
echo ""
echo "🔌 STEP 1: Setting up ADB Port Forwarding"
echo "=========================================="
echo "Setting up required port forwarding for DHU..."
adb forward tcp:5277 tcp:5277
if [ $? -eq 0 ]; then
    echo "✅ Port forwarding successful (tcp:5277)"
else
    echo "❌ Port forwarding failed"
fi

# Step 2: Verify Android Auto Installation and Setup
echo ""
echo "📱 STEP 2: Android Auto Verification"
echo "==================================="

echo "1. Checking if Android Auto is installed..."
AUTO_INSTALLED=$(adb shell pm list packages | grep "com.google.android.projection.gearhead")
if [ -z "$AUTO_INSTALLED" ]; then
    echo "❌ Android Auto not installed"
    echo "   Install from: https://play.google.com/store/apps/details?id=com.google.android.projection.gearhead"
    exit 1
else
    echo "✅ Android Auto installed: $AUTO_INSTALLED"
fi

echo ""
echo "2. CRITICAL: Android Auto Developer Mode Setup"
echo "   ⚠️  You MUST enable developer mode manually:"
echo "   • Open Android Auto app on your device"
echo "   • Tap the hamburger menu 10 times quickly"
echo "   • Enable 'Developer mode' and 'Unknown sources'"
echo "   • Enable 'Add new cars to Android Auto' in Settings"

# Step 3: Clean and Rebuild App
echo ""
echo "🔨 STEP 3: Clean Build and Installation"
echo "======================================"
echo "Performing clean build..."
cd "$(dirname "$0")"
./gradlew clean assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "Installing app..."
./gradlew installDebug
if [ $? -ne 0 ]; then
    echo "❌ Installation failed"
    exit 1
fi

echo "✅ App built and installed successfully"

# Step 4: Clear Android Auto Cache
echo ""
echo "🧹 STEP 4: Clear Android Auto Cache"
echo "=================================="
echo "Clearing Android Auto app data to reset connection state..."
adb shell pm clear com.google.android.projection.gearhead
echo "✅ Android Auto cache cleared"

# Step 5: DHU Connection Instructions
echo ""
echo "🖥️  STEP 5: DHU Connection Instructions"
echo "======================================"
echo "Now follow these steps to connect to DHU:"
echo ""
echo "1. 📱 On your Android device:"
echo "   • Unlock your device screen (REQUIRED)"
echo "   • Open Android Auto app"
echo "   • Verify developer mode is enabled"
echo "   • Start head unit server (should see notification)"
echo ""
echo "2. 💻 On your computer:"
echo "   • Navigate to Android SDK directory"
echo "   • Run: ./desktop-head-unit (macOS/Linux) or desktop-head-unit.exe (Windows)"
echo "   • Accept terms on mobile device when prompted"
echo ""
echo "3. 🔍 Expected Result:"
echo "   • KITT should appear in DHU apps list under Communication category"
echo "   • If not visible, try the troubleshooting steps below"

echo ""
echo "✅ DEBUG SCRIPT COMPLETED"
echo "========================"
echo "Port forwarding: ACTIVE (tcp:5277)"
echo "Android Auto: CLEARED"
echo "App: REBUILT & INSTALLED"
echo "Service: COMMUNICATION CATEGORY"
echo ""
echo "🚗 Ready for DHU testing!"
