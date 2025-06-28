#!/bin/bash

# Check if ADB is installed and running
if ! command -v adb &> /dev/null
then
    echo "ADB (Android Debug Bridge) could not be found. Please install it and ensure it's in your PATH."
    exit 1
fi

# Get connected devices
DEVICES=$(adb devices | grep "device$" | awk '{print $1}')
NUM_DEVICES=$(echo "$DEVICES" | wc -l)

DEVICE_SERIAL=""

if [ "$NUM_DEVICES" -eq 1 ]; then
    DEVICE_SERIAL="$DEVICES"
    echo "Found one device: $DEVICE_SERIAL"
elif [ "$NUM_DEVICES" -gt 1 ]; then
    echo "More than one device/emulator connected:"
    echo "$DEVICES"
    read -p "Please enter the serial number of the device you want to target: " USER_DEVICE_SERIAL
    if [ -z "$USER_DEVICE_SERIAL" ]; then
        echo "No device serial provided. Exiting."
        exit 1
    fi
    DEVICE_SERIAL="$USER_DEVICE_SERIAL"
else
    echo "No Android device found. Please ensure your device is connected via USB and ADB debugging is enabled."
    exit 1
fi

# Check if an APK file path is provided as an argument
if [ -z "$1" ]; then
    echo "Usage: $0 <path_to_apk_file> [device_serial]"
    echo "Example: $0 my_app.apk A75259FRCNAAPDV00FB"
    exit 1
fi

APK_PATH="$1"
APK_FILENAME=$(basename "$APK_PATH")
DEVICE_DOWNLOAD_PATH="/sdcard/Download/$APK_FILENAME"

echo "Transferring $APK_FILENAME to the device ($DEVICE_SERIAL)..."

# Push the APK to the device's /sdcard/Download folder
echo "Pushing APK to device: $APK_PATH -> $DEVICE_DOWNLOAD_PATH"
adb -s "$DEVICE_SERIAL" push "$APK_PATH" "$DEVICE_DOWNLOAD_PATH"
if [ $? -ne 0 ]; then
    echo "Failed to push APK to device."
    exit 1
fi

echo "APK successfully transferred to: $DEVICE_DOWNLOAD_PATH"
echo "Please now go to your Crosscall Core-Z5 device and use the file manager application (or any app authorized for unknown sources) to navigate to the 'Download' folder and install the APK manually."
echo "Ensure 'Install from unknown sources' is enabled for your file manager application."

# Optional: Remove the APK from the device's Downloads folder after installation
# echo "Removing APK from device..."
# adb -s "$DEVICE_SERIAL" shell rm "$DEVICE_DOWNLOAD_PATH"
# if [ $? -ne 0 ]; then
#     echo "Failed to remove APK from device. You may need to remove it manually."
# fi
