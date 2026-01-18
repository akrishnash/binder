@echo off
REM Simple build script for Windows

echo === Building Binder Android App ===

REM Try to find Android SDK if ANDROID_HOME not set
if "%ANDROID_HOME%"=="" (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
        echo Found Android SDK at: %ANDROID_HOME%
    ) else if exist "%USERPROFILE%\AppData\Local\Android\Sdk" (
        set "ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk"
        echo Found Android SDK at: %ANDROID_HOME%
    ) else (
        echo ERROR: ANDROID_HOME not set and SDK not found!
        echo Run setup-env.ps1 first or set ANDROID_HOME manually
        pause
        exit /b 1
    )
)

REM Build APK
echo.
echo Building APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful!
echo APK location: app\build\outputs\apk\debug\app-debug.apk

REM Check for devices and install
echo.
echo Checking for devices...
"%ANDROID_HOME%\platform-tools\adb.exe" devices

echo.
echo To install on device, run:
echo   "%ANDROID_HOME%\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk

pause
