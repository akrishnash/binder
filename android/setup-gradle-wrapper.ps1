# Setup Gradle Wrapper

Write-Host "=== Setting up Gradle Wrapper ===" -ForegroundColor Cyan

# Check if Gradle is installed
$gradleInstalled = $false
try {
    $gradleVersion = gradle -v 2>&1 | Select-Object -First 1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Gradle found: $gradleVersion" -ForegroundColor Green
        $gradleInstalled = $true
    }
} catch {
    $gradleInstalled = $false
}

if ($gradleInstalled) {
    Write-Host "`nGenerating Gradle wrapper..." -ForegroundColor Yellow
    gradle wrapper --gradle-version 8.2
    Write-Host "Gradle wrapper created!" -ForegroundColor Green
} else {
    Write-Host "`nGradle not found. Downloading wrapper manually..." -ForegroundColor Yellow
    
    # Create wrapper directory
    $wrapperDir = "gradle\wrapper"
    if (-not (Test-Path $wrapperDir)) {
        New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
    }
    
    # Download gradle-wrapper.jar
    $jarUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
    $jarPath = "$wrapperDir\gradle-wrapper.jar"
    
    Write-Host "Downloading gradle-wrapper.jar..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath -UseBasicParsing
        Write-Host "Downloaded gradle-wrapper.jar" -ForegroundColor Green
    } catch {
        Write-Host "Failed to download. Please download manually:" -ForegroundColor Red
        Write-Host "1. Go to: https://github.com/gradle/gradle/tree/v8.2.0/gradle/wrapper" -ForegroundColor Yellow
        Write-Host "2. Download gradle-wrapper.jar" -ForegroundColor Yellow
        Write-Host "3. Place it in: $jarPath" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "`n=== Setup Complete ===" -ForegroundColor Green
Write-Host "You can now run: .\gradlew.bat assembleDebug" -ForegroundColor Cyan
