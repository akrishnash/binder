# Binder - A Tinder for Book Lovers 📚

A React Native mobile app built with Expo that helps book lovers connect through their reading preferences.

## Features

- **3-Step Onboarding Flow**:
  1. Basic Info (Age, Gender, Interests)
  2. Genre Selection (Grid of book genres)
  3. My Mystery Shelf (Search and select 3 books)

- **Smooth Animations**: Powered by React Native Reanimated for seamless transitions
- **Supabase Integration**: Stores user profiles in the cloud

## Setup

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Supabase

1. Create a Supabase project at [supabase.com](https://supabase.com)
2. Run the SQL schema from `supabase-schema.sql` in your Supabase SQL Editor
3. Get your project URL and anon key from Supabase settings
4. Update `config/supabase.js` with your credentials:

```javascript
const SUPABASE_URL = 'YOUR_SUPABASE_URL';
const SUPABASE_ANON_KEY = 'YOUR_SUPABASE_ANON_KEY';
```

### 3. Run the App

```bash
npm start
```

Then press `i` for iOS simulator, `a` for Android emulator, or scan the QR code with Expo Go.

## Project Structure

```
├── App.js                    # Main app component
├── components/
│   └── onboarding/
│       ├── OnboardingFlow.js    # Main onboarding container
│       ├── Step1BasicInfo.js    # Step 1: Age, Gender, Interests
│       ├── Step2GenreSelection.js # Step 2: Genre selection
│       └── Step3MysteryShelf.js  # Step 3: Book search & selection
├── config/
│   └── supabase.js          # Supabase client configuration
└── supabase-schema.sql      # Database schema

```

## Technologies

- **Expo SDK 54**
- **React Native**
- **React Native Reanimated** - Smooth animations
- **Supabase** - Backend and database
- **Open Library API** - Book search functionality

## Database Schema

The `profiles` table stores:
- `age` (Integer)
- `gender` (Text)
- `interests` (Array of Text)
- `genres` (Array of Text)
- `books` (JSONB - Array of book objects with title, author, cover, etc.)
- `created_at` (Timestamp)
- `updated_at` (Timestamp)

## Notes

- The app uses the Open Library API for book searches (free, no API key required)
- Make sure to configure your Supabase credentials before running
- The onboarding flow includes smooth slide animations between steps
