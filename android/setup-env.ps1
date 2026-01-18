# Setup environment variables for Android development

Write-Host "=== Android Environment Setup ===" -ForegroundColor Cyan

# Common Android SDK locations
$possiblePaths = @(
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\AppData\Local\Android\Sdk",
    "C:\Android\Sdk",
    "$env:ProgramFiles\Android\Android Studio\sdk"
)

$sdkPath = $null

# Try to find Android SDK
foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $sdkPath = $path
        Write-Host "Found Android SDK at: $sdkPath" -ForegroundColor Green
        break
    }
}

if (-not $sdkPath) {
    Write-Host "Android SDK not found in common locations." -ForegroundColor Yellow
    $sdkPath = Read-Host "Enter your Android SDK path"
    
    if (-not (Test-Path $sdkPath)) {
        Write-Host "Path does not exist!" -ForegroundColor Red
        exit 1
    }
}

# Set environment variables for current session
$env:ANDROID_HOME = $sdkPath
$env:ANDROID_SDK_ROOT = $sdkPath
$env:PATH = "$sdkPath\platform-tools;$sdkPath\tools;$sdkPath\tools\bin;$env:PATH"

Write-Host "`nEnvironment variables set for this session:" -ForegroundColor Green
Write-Host "  ANDROID_HOME = $env:ANDROID_HOME"
Write-Host "  PATH updated with Android tools"

# Check Java
Write-Host "`nChecking Java..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1 | Select-Object -First 1
if ($LASTEXITCODE -eq 0) {
    Write-Host "Java found: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "Java not found in PATH!" -ForegroundColor Red
    Write-Host "Please install JDK 17+ and add it to PATH" -ForegroundColor Yellow
}

# Check ADB
Write-Host "`nChecking ADB..." -ForegroundColor Yellow
$adbPath = "$sdkPath\platform-tools\adb.exe"
if (Test-Path $adbPath) {
    Write-Host "ADB found" -ForegroundColor Green
    Write-Host "`nConnected devices:" -ForegroundColor Cyan
    & $adbPath devices
} else {
    Write-Host "ADB not found. Platform tools may not be installed." -ForegroundColor Yellow
}

Write-Host "`n=== Setup Complete ===" -ForegroundColor Green
Write-Host "To make these changes permanent, add to your PowerShell profile:" -ForegroundColor Yellow
Write-Host "  `$env:ANDROID_HOME = '$sdkPath'" -ForegroundColor Cyan
Write-Host "  `$env:PATH += ';$sdkPath\platform-tools;$sdkPath\tools'" -ForegroundColor Cyan
