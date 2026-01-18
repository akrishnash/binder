# Android Setup Instructions

## Step 1: Clean Everything
```bash
# Delete node_modules and lock file
rm -rf node_modules
rm package-lock.json

# On Windows PowerShell:
Remove-Item -Recurse -Force node_modules
Remove-Item package-lock.json -ErrorAction SilentlyContinue
```

## Step 2: Reinstall Dependencies
```bash
npm install
```

## Step 3: Clear Expo Cache and Start
```bash
npx expo start --clear
```

## Step 4: On Your Android Device
1. Open Expo Go app
2. Scan the QR code
3. Check the console logs - they will show:
   - `[App] Component initializing...`
   - `[OnboardingFlow] Component initialized`
   - All step navigation and form updates

## If Still Getting PlatformConstants Error:

The error suggests cached native modules. Try:

1. **Close Expo Go completely** on your Android device
2. **Clear Expo Go cache** (Android Settings > Apps > Expo Go > Clear Cache)
3. **Restart Expo Go**
4. **Run**: `npx expo start --clear --reset-cache`
5. **Reconnect** your device

## Console Logs to Look For:

When the app starts, you should see:
```
[index.js] Registering root component...
[App] Component initializing...
[App] Component mounted
[OnboardingFlow] Component initialized
[OnboardingFlow] Component mounted, current step: 1
```

If you see these logs, the app is working. If you see PlatformConstants error, the issue is with cached native modules in Expo Go.
