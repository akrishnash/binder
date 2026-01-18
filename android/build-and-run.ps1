# PowerShell script to build and run Android app via command line

Write-Host "=== Binder Android Build Script ===" -ForegroundColor Cyan

# Try to find Android SDK
$sdkPath = $env:ANDROID_HOME

if (-not $sdkPath) {
    $possiblePaths = @(
        "$env:LOCALAPPDATA\Android\Sdk",
        "$env:USERPROFILE\AppData\Local\Android\Sdk",
        "C:\Android\Sdk"
    )
    
    foreach ($path in $possiblePaths) {
        if (Test-Path $path) {
            $sdkPath = $path
            $env:ANDROID_HOME = $sdkPath
            Write-Host "Found and set ANDROID_HOME = $sdkPath" -ForegroundColor Green
            break
        }
    }
}

if (-not $sdkPath) {
    Write-Host "ERROR: ANDROID_HOME not set and SDK not found!" -ForegroundColor Red
    Write-Host "Please set ANDROID_HOME to your Android SDK path" -ForegroundColor Yellow
    Write-Host "Example: `$env:ANDROID_HOME = 'C:\Users\YourName\AppData\Local\Android\Sdk'" -ForegroundColor Yellow
    exit 1
}

Write-Host "Android SDK found at: $sdkPath" -ForegroundColor Green

# Navigate to android directory
Set-Location $PSScriptRoot

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
        Write-Host "Failed to download wrapper JAR" -ForegroundColor Red
        exit 1
    }
}

# Clean build
Write-Host "`nCleaning previous builds..." -ForegroundColor Yellow
& .\gradlew.bat clean

# Build debug APK
Write-Host "`nBuilding debug APK..." -ForegroundColor Yellow
& .\gradlew.bat assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Build successful!" -ForegroundColor Green

# Find the APK
$apkPath = Get-ChildItem -Path "app\build\outputs\apk\debug" -Filter "*.apk" | Select-Object -First 1

if ($apkPath) {
    Write-Host "`nAPK created at: $($apkPath.FullName)" -ForegroundColor Green
    
    # Check for connected devices
    Write-Host "`nChecking for connected devices..." -ForegroundColor Yellow
    $adbPath = "$sdkPath\platform-tools\adb.exe"
    
    if (Test-Path $adbPath) {
        $devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }
        
        if ($devices) {
            Write-Host "Found device(s):" -ForegroundColor Green
            $devices | ForEach-Object { Write-Host "  $_" }
            
            Write-Host "`nInstalling APK..." -ForegroundColor Yellow
            & $adbPath install -r $apkPath.FullName
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "`nLaunching app..." -ForegroundColor Yellow
                & $adbPath shell am start -n com.binder/.SplashActivity
                Write-Host "`nApp launched!" -ForegroundColor Green
            } else {
                Write-Host "Installation failed!" -ForegroundColor Red
            }
        } else {
            Write-Host "No devices found. Connect a device or start an emulator." -ForegroundColor Yellow
            Write-Host "APK location: $($apkPath.FullName)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "ADB not found. APK location: $($apkPath.FullName)" -ForegroundColor Yellow
    }
} else {
    Write-Host "APK not found!" -ForegroundColor Red
}
