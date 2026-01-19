# Binder 📚

**A Tinder for Book Lovers** - Connect with fellow readers through shared literary interests.

Binder is a mobile dating app that helps book enthusiasts find meaningful connections based on their reading preferences, favorite genres, and book collections. Swipe through profiles of fellow readers, discover mutual interests, and find your next reading buddy (or more).

## 🌟 Features

### Core Functionality
- **Multi-Step Onboarding**: Create a comprehensive profile with age, gender, interests, genres, and book selections
- **Book Search**: Search and select your favorite books using the Open Library API
- **Profile Matching**: Discover other book lovers based on shared interests
- **Swipe Interface**: Tinder-like card swiping interface for browsing profiles
- **Mutual Matches**: Get notified when you and someone else like each other
- **Profile Management**: Edit your profile, upload photos, and manage your book collection
- **Discovery Feed**: Browse profiles of other book enthusiasts

### Technical Features
- **Dual Platform Support**: Available as both React Native/Expo app and Native Android (Kotlin)
- **Cloud Backend**: Powered by Supabase for authentication, database, and storage
- **Real-time Data**: Live updates for matches and profile changes
- **Photo Storage**: Secure profile photo uploads via Supabase Storage
- **Smooth Animations**: Fluid transitions and interactions throughout the app

## 📱 Platforms

### React Native/Expo Version
Cross-platform mobile app built with React Native and Expo SDK 54.

**Location**: Root directory (`/`)

**Features**:
- iOS and Android support
- Hot reload development
- Easy deployment with Expo

### Native Android (Kotlin) Version
Native Android app built with Kotlin for optimal Android performance.

**Location**: `android/` directory

**Features**:
- Pure native Android experience
- Material Design UI
- Optimized performance
- Full Android feature support

## 🚀 Quick Start

### Prerequisites
- **For React Native/Expo**: Node.js 18+, npm or yarn
- **For Android**: Android Studio, JDK 17+, Android SDK API 24+
- **Supabase Account**: [Create one for free](https://supabase.com)

### 1. Clone the Repository

```bash
git clone https://github.com/akrishnash/binder.git
cd binder
```

### 2. Setup Supabase Database

1. Create a new project at [supabase.com](https://supabase.com)
2. Go to SQL Editor in your Supabase dashboard
3. Run the following SQL files in order:
   - `supabase-schema.sql` - Creates profiles table
   - `supabase-matches-schema.sql` - Creates likes and matches tables
   - `supabase-storage-schema.sql` - Creates storage bucket for photos

### 3. Configure Supabase Credentials

Update `config/supabase.js` with your Supabase project credentials:

```javascript
const SUPABASE_URL = 'https://your-project.supabase.co';
const SUPABASE_ANON_KEY = 'your-anon-key-here';
```

You can find these in your Supabase project settings under API.

### 4. Install Dependencies

**For React Native/Expo:**
```bash
npm install
```

**For Android (Native):**
1. Open Android Studio
2. Open the `android/` folder as a project
3. Wait for Gradle sync to complete
4. Install required SDKs if prompted

## 🏃 Running the App

### React Native/Expo Version

```bash
# Start the development server
npm start

# Or run on specific platform
npm run android  # Android emulator
npm run ios      # iOS simulator
npm run web      # Web browser
```

Scan the QR code with Expo Go app on your device, or press `i` for iOS simulator, `a` for Android emulator.

### Native Android Version

1. Open `android/` folder in Android Studio
2. Connect an Android device or start an emulator
3. Click the green Run button (▶️) or press `Shift+F10`
4. Select your device/emulator
5. Wait for build and installation to complete

Or use command line:
```bash
cd android
./gradlew installDebug
```

## 📁 Project Structure

```
binder/
├── App.js                          # Main React Native app entry
├── app.json                        # Expo configuration
├── package.json                    # Node.js dependencies
├── babel.config.js                 # Babel configuration
│
├── components/                     # React Native components
│   └── onboarding/
│       ├── OnboardingFlow.js      # Main onboarding container
│       ├── Step1BasicInfo.js      # Step 1: Age, Gender, Interests
│       ├── Step2GenreSelection.js # Step 2: Genre selection
│       └── Step3MysteryShelf.js   # Step 3: Book search
│
├── config/                         # Configuration files
│   └── supabase.js                # Supabase client setup
│
├── android/                        # Native Android (Kotlin) app
│   ├── app/
│   │   └── src/main/java/com/binder/
│   │       ├── MainActivity.kt
│   │       ├── onboarding/        # Onboarding fragments
│   │       ├── discovery/         # Discovery/match screens
│   │       ├── profile/           # Profile screens
│   │       └── utils/             # Supabase service, ProfileManager
│   └── build.gradle.kts           # Gradle configuration
│
├── assets/                         # Images and static assets
│
└── Database Schemas/
    ├── supabase-schema.sql        # Profiles table schema
    ├── supabase-matches-schema.sql # Likes and matches tables
    ├── supabase-storage-schema.sql # Storage bucket for photos
    ├── supabase-migration.sql     # Migration scripts
    └── FIX-PHOTOS-NOW.sql         # Photo storage fixes
```

## 🗄️ Database Schema

### Profiles Table
Stores user profile information:
- `id` (TEXT, PRIMARY KEY) - User ID
- `username` (TEXT) - Display name
- `age` (INTEGER) - User age
- `gender` (TEXT) - Gender identity
- `interests` (TEXT[]) - Array of interests
- `genres` (TEXT[]) - Array of favorite genres
- `books` (JSONB) - Array of book objects (title, author, cover, etc.)
- `photo_uri` (TEXT) - Profile photo URL
- `bio` (TEXT) - User biography
- `city` (TEXT) - User location
- `pages_read_today` (INTEGER) - Daily reading tracker
- `created_at` (TEXT) - Account creation timestamp
- `updated_at` (TEXT) - Last update timestamp

### Likes Table
Tracks when a user likes another user:
- `id` (TEXT, PRIMARY KEY) - Like ID
- `liker_id` (TEXT) - User who liked
- `liked_id` (TEXT) - User who was liked
- `created_at` (TEXT) - Like timestamp

### Matches Table
Stores mutual likes (matches):
- `id` (TEXT, PRIMARY KEY) - Match ID
- `user1_id` (TEXT) - First user
- `user2_id` (TEXT) - Second user
- `created_at` (TEXT) - Match timestamp

## 🛠️ Technologies

### React Native/Expo Version
- **React Native** 0.76.5 - Cross-platform mobile framework
- **Expo SDK** ~54.0.0 - Development platform
- **React** 18.3.1 - UI library
- **@supabase/supabase-js** ^2.90.1 - Backend integration
- **Open Library API** - Book search functionality

### Native Android Version
- **Kotlin** - Programming language
- **Android SDK** API 24+ (Android 7.0+)
- **Material Design** - UI components
- **Supabase Android SDK** - Backend integration
- **Retrofit/OkHttp** - HTTP client
- **Glide** - Image loading

### Backend & Database
- **Supabase** - Backend as a Service (BaaS)
  - PostgreSQL database
  - Authentication
  - Storage (profile photos)
  - Real-time subscriptions

### APIs
- **Open Library API** - Book metadata and covers (free, no API key required)

## 📚 API Reference

### Supabase Service (Android)
The app uses a `SupabaseService` singleton for all backend operations:

- `getAllProfiles()` - Fetch all user profiles
- `getProfilesExcluding(userIds)` - Get profiles excluding specific users
- `getProfile(userId)` - Get a specific user profile
- `saveProfile(profile)` - Create or update a profile
- `saveLike(likerId, likedId)` - Like a user
- `checkMutualLike(likerId, likedId)` - Check for mutual match
- `createMatch(user1Id, user2Id)` - Create a match
- `getMatches(userId)` - Get all matches for a user

## 🧪 Development

### Debugging

**React Native/Expo:**
- Enable remote debugging in Expo Go
- Check Metro bundler console for logs
- Use React Native Debugger for advanced debugging

**Android:**
- Use Android Studio's built-in debugger
- Check Logcat for app logs
- Use breakpoints in Kotlin code

### Testing

The app includes demo profiles in `supabase-schema.sql` for testing:
- **Anurag** (developer) - ID: `developer`
- **Fiza** - ID: `fiza`
- **Sarah** - ID: `demo1`
- **Alex** - ID: `demo2`
- **Jordan** - ID: `demo3`

## 📝 Scripts

### React Native/Expo
```bash
npm start          # Start Expo development server
npm run android    # Run on Android
npm run ios        # Run on iOS
npm run web        # Run on web
```

### Android (Native)
```bash
cd android
./gradlew installDebug    # Build and install debug APK
./gradlew clean           # Clean build artifacts
./gradlew build           # Build release APK
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is private and not licensed for public use.

## 👤 Author

**Anurag**
- GitHub: [@akrishnash](https://github.com/akrishnash)

## 🙏 Acknowledgments

- [Open Library](https://openlibrary.org/) for free book metadata API
- [Supabase](https://supabase.com) for backend infrastructure
- [Expo](https://expo.dev) for React Native tooling
- Android Material Design team for UI guidelines

## 📖 Additional Documentation

- [SETUP_INSTRUCTIONS.md](SETUP_INSTRUCTIONS.md) - Detailed setup guide
- [android/README.md](android/README.md) - Android-specific documentation
- [android/QUICK_START.md](android/QUICK_START.md) - Quick start for Android
- Database SQL files in root directory

## 🐛 Troubleshooting

### React Native/Expo Issues

**PlatformConstants Error:**
- Clear Expo Go cache on your device
- Run `npx expo start --clear --reset-cache`
- Restart Expo Go app

**Metro Bundler Issues:**
- Delete `node_modules` and `package-lock.json`
- Run `npm install` again
- Clear Metro cache: `npx expo start --clear`

### Android Issues

**Gradle Sync Fails:**
- Check JDK version (must be 17+)
- Invalidate caches: File > Invalidate Caches / Restart
- Check internet connection for dependency downloads

**Build Errors:**
- Clean project: `./gradlew clean`
- Check Android SDK versions in `build.gradle.kts`
- Ensure all required SDKs are installed

**Supabase Connection Issues:**
- Verify credentials in `config/supabase.js`
- Check Supabase project is active
- Verify database schemas are applied
- Check network connectivity

---

**Happy Reading & Matching! 📚💕**
