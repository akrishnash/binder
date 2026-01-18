# Quick Command Line Build Guide

## One-Time Setup

### 1. Set Android SDK Path (Windows PowerShell):
```powershell
cd android
.\setup-env.ps1
```

This will:
- Find your Android SDK
- Set ANDROID_HOME
- Add Android tools to PATH

### 2. Create Gradle Wrapper (if gradlew.bat doesn't work):
```powershell
# If you have Gradle installed globally:
gradle wrapper

# Or download from: https://gradle.org/releases/
```

## Build & Run (3 Commands)

### Option 1: Automated Script
```powershell
cd android
.\build-and-run.ps1
```

### Option 2: Manual Steps

**Step 1: Build**
```powershell
cd android
.\build.bat
```

**Step 2: Install & Run**
```powershell
.\install.bat
```

### Option 3: Pure Command Line

**Build:**
```powershell
cd android
.\gradlew.bat clean assembleDebug
```

**Install:**
```powershell
$env:ANDROID_HOME\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

**Launch:**
```powershell
$env:ANDROID_HOME\platform-tools\adb.exe shell am start -n com.binder/.MainActivity
```

## Linux/Mac Commands

**Build:**
```bash
cd android
chmod +x gradlew
./gradlew clean assembleDebug
```

**Install & Run:**
```bash
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
$ANDROID_HOME/platform-tools/adb shell am start -n com.binder/.MainActivity
```

## All-in-One Command (After Setup)

**Windows:**
```powershell
cd android; .\gradlew.bat assembleDebug; if ($?) { $env:ANDROID_HOME\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk; $env:ANDROID_HOME\platform-tools\adb.exe shell am start -n com.binder/.MainActivity }
```

**Linux/Mac:**
```bash
cd android && ./gradlew assembleDebug && $ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk && $ANDROID_HOME/platform-tools/adb shell am start -n com.binder/.MainActivity
```

## Troubleshooting

**"gradlew.bat not found"**
- The gradlew.bat file should be in the android folder
- If missing, run: `gradle wrapper` (requires Gradle installed)

**"ANDROID_HOME not set"**
- Run: `.\setup-env.ps1`
- Or manually: `$env:ANDROID_HOME = "C:\Users\YourName\AppData\Local\Android\Sdk"`

**"No devices found"**
- Enable USB debugging on your Android device
- Settings > About Phone > Tap "Build Number" 7 times
- Settings > Developer Options > USB Debugging
- Run: `adb devices` to verify

**Build fails**
- Make sure you have Android SDK Platform 34 installed
- Check Java version: `java -version` (needs 17+)
