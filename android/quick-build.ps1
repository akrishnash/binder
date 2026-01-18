# Quick build script - sets up everything and builds

Write-Host "=== Quick Build Script ===" -ForegroundColor Cyan

# Set Android SDK path
$sdkPath = "C:\Users\anurag\AppData\Local\Android\Sdk"
if (Test-Path $sdkPath) {
    $env:ANDROID_HOME = $sdkPath
    Write-Host "Set ANDROID_HOME = $sdkPath" -ForegroundColor Green
} else {
    Write-Host "ERROR: Android SDK not found at $sdkPath" -ForegroundColor Red
    exit 1
}

# Create local.properties if needed
if (-not (Test-Path "local.properties")) {
    $sdkPathForward = $sdkPath -replace '\\', '/'
    "sdk.dir=$sdkPathForward" | Out-File -FilePath "local.properties" -Encoding ASCII
    Write-Host "Created local.properties" -ForegroundColor Green
}

# Ensure Gradle wrapper JAR exists
if (-not (Test-Path "gradle\wrapper\gradle-wrapper.jar")) {
    Write-Host "Downloading Gradle wrapper JAR..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "gradle\wrapper" -Force | Out-Null
    try {
        Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar" -UseBasicParsing
        Write-Host "Downloaded wrapper JAR" -ForegroundColor Green
    } catch {
        Write-Host "Failed to download. Trying alternative..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri "https://github.com/gradle/gradle/raw/v8.9.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar" -UseBasicParsing
    }
}

# Build
Write-Host "`nBuilding APK..." -ForegroundColor Yellow
& .\gradlew.bat assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n=== BUILD SUCCESSFUL ===" -ForegroundColor Green
    Write-Host "APK: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
    
    # Try to install
    $adbPath = "$sdkPath\platform-tools\adb.exe"
    if (Test-Path $adbPath) {
        $devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }
        if ($devices) {
            Write-Host "`nInstalling on device..." -ForegroundColor Yellow
            & $adbPath install -r "app\build\outputs\apk\debug\app-debug.apk"
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Launching app..." -ForegroundColor Yellow
                & $adbPath shell am start -n com.binder/.SplashActivity
                Write-Host "Done!" -ForegroundColor Green
            }
        } else {
            Write-Host "`nNo devices connected. Connect device and run:" -ForegroundColor Yellow
            Write-Host "  .\install.bat" -ForegroundColor Cyan
        }
    }
} else {
    Write-Host "`nBuild failed!" -ForegroundColor Red
    exit 1
}
