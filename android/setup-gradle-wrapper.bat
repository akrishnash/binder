@echo off
REM Setup Gradle Wrapper

echo === Setting up Gradle Wrapper ===

REM Check if Gradle is installed
gradle -v >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Gradle found, generating wrapper...
    gradle wrapper --gradle-version 8.2
    echo Gradle wrapper created!
    goto :done
)

echo Gradle not found. Please install Gradle or download wrapper manually.
echo.
echo Option 1: Install Gradle
echo   Download from: https://gradle.org/releases/
echo   Then run: gradle wrapper
echo.
echo Option 2: Download wrapper JAR manually
echo   1. Go to: https://github.com/gradle/gradle/tree/v8.2.0/gradle/wrapper
echo   2. Download gradle-wrapper.jar
echo   3. Place it in: gradle\wrapper\gradle-wrapper.jar
echo.
echo Option 3: Use Android Studio
echo   Open project in Android Studio, it will generate the wrapper automatically

:done
pause
