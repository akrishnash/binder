#!/bin/bash
# Bash script to build and run Android app via command line

echo "=== Binder Android Build Script ==="

# Check if Android SDK is installed
if [ -z "$ANDROID_HOME" ]; then
    echo "ERROR: ANDROID_HOME not set!"
    echo "Please set ANDROID_HOME to your Android SDK path"
    echo "Example: export ANDROID_HOME=\$HOME/Android/Sdk"
    exit 1
fi

echo "Android SDK found at: $ANDROID_HOME"

# Navigate to android directory
cd "$(dirname "$0")"

# Make gradlew executable
chmod +x gradlew

# Clean build
echo ""
echo "Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo ""
echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Build successful!"

# Find the APK
APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" | head -n 1)

if [ -n "$APK_PATH" ]; then
    echo ""
    echo "APK created at: $APK_PATH"
    
    # Check for connected devices
    echo ""
    echo "Checking for connected devices..."
    ADB_PATH="$ANDROID_HOME/platform-tools/adb"
    
    if [ -f "$ADB_PATH" ]; then
        DEVICES=$($ADB_PATH devices | grep -v "List" | grep "device$")
        
        if [ -n "$DEVICES" ]; then
            echo "Found device(s):"
            echo "$DEVICES"
            
            echo ""
            echo "Installing APK..."
            $ADB_PATH install -r "$APK_PATH"
            
            if [ $? -eq 0 ]; then
                echo ""
                echo "Launching app..."
                $ADB_PATH shell am start -n com.binder/.MainActivity
                echo ""
                echo "App launched!"
            else
                echo "Installation failed!"
            fi
        else
            echo "No devices found. Connect a device or start an emulator."
            echo "APK location: $APK_PATH"
        fi
    else
        echo "ADB not found. APK location: $APK_PATH"
    fi
else
    echo "APK not found!"
fi
