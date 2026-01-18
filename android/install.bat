@echo off
REM Install and run the app on connected device

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
        echo Please set ANDROID_HOME or run setup-env.ps1
        pause
        exit /b 1
    )
)

set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK_PATH%" (
    echo APK not found! Run build.bat first.
    pause
    exit /b 1
)

echo Installing APK...
"%ANDROID_HOME%\platform-tools\adb.exe" install -r "%APK_PATH%"

if %ERRORLEVEL% equ 0 (
    echo.
    echo Launching app...
    "%ANDROID_HOME%\platform-tools\adb.exe" shell am start -n com.binder/.SplashActivity
    echo App launched!
) else (
    echo Installation failed!
)

pause
