# Build and Run via Command Line

## Prerequisites

1. **Android SDK** installed
   - Download from: https://developer.android.com/studio
   - Or install via Android Studio

2. **Set Environment Variables** (Windows PowerShell):
   ```powershell
   $env:ANDROID_HOME = "C:\Users\YourName\AppData\Local\Android\Sdk"
   $env:PATH += ";$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\tools"
   ```

   (Linux/Mac):
   ```bash
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools
   ```

3. **Java JDK 17+** installed
   - Check: `java -version`

## Quick Start

### Windows (PowerShell):
```powershell
cd android
.\build-and-run.ps1
```

### Linux/Mac:
```bash
cd android
chmod +x build-and-run.sh
./build-and-run.sh
```

## Manual Commands

### 1. Navigate to android folder:
```bash
cd android
```

### 2. Create Gradle Wrapper (if needed):
```bash
# Windows
gradlew.bat wrapper

# Linux/Mac
chmod +x gradlew
./gradlew wrapper
```

### 3. Clean build:
```bash
# Windows
gradlew.bat clean

# Linux/Mac
./gradlew clean
```

### 4. Build APK:
```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

### 5. Install on device:
```bash
# Connect device via USB (enable USB debugging)
adb devices  # Check device is connected

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.binder/.MainActivity
```

## Build Release APK

```bash
# Windows
gradlew.bat assembleRelease

# Linux/Mac
./gradlew assembleRelease
```

APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Troubleshooting

**"gradlew not found"**
- Run: `gradle wrapper` (if you have Gradle installed)
- Or download Gradle wrapper manually

**"ANDROID_HOME not set"**
- Find your SDK path (usually in Android Studio settings)
- Set the environment variable

**"No devices found"**
- Enable USB debugging on your Android device
- Settings > About Phone > Tap "Build Number" 7 times
- Settings > Developer Options > Enable "USB Debugging"
- Run: `adb devices` to verify

**Build fails with "SDK not found"**
- Install Android SDK Platform 34 via Android Studio SDK Manager
- Or run: `sdkmanager "platforms;android-34"`

## APK Location

After successful build:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

You can manually install the APK on your device by:
1. Transferring the APK file to your device
2. Opening it on your device
3. Allowing installation from unknown sources (if needed)
