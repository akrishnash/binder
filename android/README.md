# Binder - Native Android (Kotlin)

Native Android app for Binder - A Tinder for Book Lovers.

## Project Structure

```
android/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/binder/
│   │       │   ├── MainActivity.kt
│   │       │   ├── onboarding/
│   │       │   │   ├── OnboardingActivity.kt
│   │       │   │   ├── Step1BasicInfoFragment.kt
│   │       │   │   ├── Step2GenreSelectionFragment.kt
│   │       │   │   └── Step3MysteryShelfFragment.kt
│   │       │   └── models/
│   │       │       └── UserProfile.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── values/
│   │       │   └── drawable/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Setup Instructions

1. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `android` folder

2. **Sync Gradle**
   - Android Studio will automatically sync
   - Wait for dependencies to download

3. **Run the App**
   - Connect an Android device or start an emulator
   - Click Run (green play button) or press Shift+F10

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK API 24+ (Android 7.0+)
