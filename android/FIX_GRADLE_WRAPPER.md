# Fix Gradle Wrapper Error

## Quick Fix (Choose One)

### Option 1: Use Gradle (if installed)
```powershell
cd android
gradle wrapper --gradle-version 8.2
```

### Option 2: Download Wrapper JAR Manually

1. **Create the directory:**
   ```powershell
   mkdir -p gradle\wrapper
   ```

2. **Download the JAR:**
   - Go to: https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar
   - Save as: `gradle\wrapper\gradle-wrapper.jar`

   Or use PowerShell:
   ```powershell
   Invoke-WebRequest -Uri "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
   ```

### Option 3: Use Setup Script
```powershell
cd android
.\setup-gradle-wrapper.ps1
```

### Option 4: Use Android Studio
1. Open the `android` folder in Android Studio
2. Android Studio will automatically generate the Gradle wrapper
3. Then you can use command line

## Verify

After setup, verify the file exists:
```powershell
Test-Path gradle\wrapper\gradle-wrapper.jar
```

Should return `True`.

Then try building again:
```powershell
.\gradlew.bat assembleDebug
```
