# Tribe Auto-Matching Feature

## Overview

The Tribe Auto-Matching feature moves the app from 1-on-1 matching to group dynamics, creating "Automatic Squads" (Tribes) when 5 people in the same city are reading the same book. This lowers pressure on individuals and creates a sense of community.

## How It Works

1. **Detection**: When a user updates their "currently reading" books and saves their profile, the system checks if there are 4 other users in the same city reading the same book.

2. **Tribe Formation**: If 5 people (including the current user) are found reading the same book in the same city, a tribe is automatically created.

3. **Notification**: All 5 members receive a notification: "A Tribe is forming for [Book Title]. Join the 48-hour Sprint?"

4. **48-Hour Sprint**: Once 5 members join, the tribe becomes "active" and a 48-hour reading sprint begins.

## Database Schema

### New Tables

1. **tribes** - Stores tribe information
   - `id` - Unique tribe ID
   - `book_id` - Book being read
   - `book_title` - Book title
   - `book_author` - Book author
   - `city` - City where tribe is located
   - `status` - 'forming', 'active', 'completed', 'expired'
   - `sprint_start_time` - When sprint started
   - `sprint_end_time` - When sprint ends

2. **tribe_members** - Tracks tribe membership
   - `id` - Unique member record ID
   - `tribe_id` - Reference to tribe
   - `user_id` - User profile ID
   - `joined_at` - When user joined
   - `status` - 'active', 'left', 'completed'

3. **notifications** - Stores user notifications
   - `id` - Unique notification ID
   - `user_id` - User receiving notification
   - `type` - 'tribe_forming', 'tribe_ready', etc.
   - `title` - Notification title
   - `message` - Notification message
   - `tribe_id` - Reference to tribe (if applicable)
   - `book_id` - Reference to book (if applicable)
   - `read` - Whether notification has been read

### Updated Tables

1. **profiles** - Added `currently_reading` JSONB column
   - Stores the books a user is currently reading
   - Format: `{"books": [{"id": "...", "title": "...", "author": "...", ...}]}`

## Setup Instructions

### 1. Run Database Migrations

Execute these SQL files in your Supabase SQL Editor (in order):

1. `supabase-update-profiles-for-tribes.sql` - Adds `currently_reading` column to profiles
2. `supabase-tribes-schema.sql` - Creates tribes, tribe_members, and notifications tables

### 2. Code Changes

The following files have been added/modified:

**New Files:**
- `android/app/src/main/java/com/binder/tribes/TribesFragment.kt` - Main tribes UI
- `android/app/src/main/java/com/binder/tribes/TribeDetailActivity.kt` - Tribe details screen
- `android/app/src/main/java/com/binder/utils/TribeService.kt` - Tribe business logic
- `android/app/src/main/res/layout/fragment_tribes.xml` - Tribes fragment layout
- `android/app/src/main/res/layout/item_tribe.xml` - Tribe card layout
- `android/app/src/main/res/layout/activity_tribe_detail.xml` - Tribe detail layout
- `android/app/src/main/res/layout/item_tribe_member.xml` - Member card layout
- `android/app/src/main/res/layout/notification_banner.xml` - Notification banner layout

**Modified Files:**
- `android/app/src/main/java/com/binder/utils/SupabaseService.kt` - Added `currently_reading` support
- `android/app/src/main/java/com/binder/utils/ProfileManager.kt` - Integrated tribe detection
- `android/app/src/main/java/com/binder/MainActivity.kt` - Added Tribes tab
- `android/app/src/main/res/menu/bottom_navigation.xml` - Added Tribes menu item
- `android/app/src/main/AndroidManifest.xml` - Added TribeDetailActivity

## Usage

### For Users

1. **Set Currently Reading Books**: 
   - Go to Profile → Edit Profile
   - Add books to "Currently Reading" section
   - Save profile

2. **View Tribes**:
   - Navigate to the "Tribes" tab in bottom navigation
   - See all tribes you're a member of
   - View tribe details by tapping on a tribe card

3. **Join a Tribe**:
   - When you receive a notification about a forming tribe
   - Tap "View" to see tribe details
   - Tap "Join Tribe" to become a member
   - Once 5 members join, the 48-hour sprint automatically starts

4. **Notifications**:
   - Unread tribe notifications appear as banners at the top of the Tribes screen
   - Tap "View" to see tribe details
   - Tap "Dismiss" to mark as read

### For Developers

**TribeService API:**

```kotlin
// Check and create tribe when user updates currentlyReading
TribeService.checkAndCreateTribe(userId, city, currentlyReading)

// Get user's tribes
TribeService.getUserTribes(userId)

// Get tribe members
TribeService.getTribeMembers(tribeId)

// Join a tribe
TribeService.joinTribe(tribeId, userId)

// Get notifications
TribeService.getUserNotifications(userId)

// Mark notification as read
TribeService.markNotificationRead(notificationId)
```

## Key Features

1. **Automatic Detection**: Tribes are automatically created when 5 people in the same city are reading the same book
2. **Real-time Notifications**: Users are notified when tribes form
3. **48-Hour Sprint**: Once 5 members join, a 48-hour reading sprint begins
4. **Community Focus**: Shifts from "looking for a partner" to "part of a community"
5. **City-Based**: Tribes are location-specific, making them more relevant

## Future Enhancements

Potential improvements:
- Chat functionality within tribes
- Reading progress tracking
- Sprint completion celebrations
- Tribe recommendations based on reading history
- Integration with reading challenges
- Social features (comments, discussions)

## Notes

- The minimum tribe size is 5 members
- Tribes are city-specific
- Currently reading books must be set in the user's profile
- The 48-hour sprint starts automatically when the 5th member joins
- Notifications are stored in the database and persist across app sessions
