# Create local.properties file with Android SDK path

Write-Host "=== Creating local.properties ===" -ForegroundColor Cyan

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
            Write-Host "Found Android SDK at: $sdkPath" -ForegroundColor Green
            break
        }
    }
}

if (-not $sdkPath) {
    Write-Host "Android SDK not found automatically." -ForegroundColor Yellow
    $sdkPath = Read-Host "Enter your Android SDK path"
    
    if (-not (Test-Path $sdkPath)) {
        Write-Host "Path does not exist!" -ForegroundColor Red
        exit 1
    }
}

# Create local.properties
$content = "sdk.dir=$sdkPath"
$content | Out-File -FilePath "local.properties" -Encoding ASCII

Write-Host "`nCreated local.properties:" -ForegroundColor Green
Write-Host "  $content"
Write-Host "`nYou can now run: .\gradlew.bat assembleDebug" -ForegroundColor Cyan
