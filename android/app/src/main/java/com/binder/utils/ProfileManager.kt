package com.binder.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.binder.models.UserProfile
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ProfileManager {
    private const val PREFS_NAME = "binder_profile"
    private const val KEY_PROFILE = "user_profile"
    private const val KEY_SYNCED = "profile_synced"
    private val gson = Gson()
    private const val TAG = "ProfileManager"
    
    fun saveProfile(context: Context, profile: UserProfile, syncToSupabase: Boolean = true) {
        // Save locally first
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(profile)
        prefs.edit().putString(KEY_PROFILE, json).apply()
        Log.d(TAG, "Profile saved locally: ${profile.id}, username: ${profile.username}")
        
        // Sync to Supabase in background
        if (syncToSupabase) {
            Log.d(TAG, "Starting Supabase sync for profile: ${profile.id}, username: ${profile.username}")
            android.util.Log.d(TAG, "Photo URI: ${profile.photoUri}")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "Calling SupabaseService.saveProfile for: ${profile.id}")
                    android.util.Log.d(TAG, "Profile data being saved:")
                    android.util.Log.d(TAG, "  - ID: ${profile.id}")
                    android.util.Log.d(TAG, "  - Username: ${profile.username}")
                    android.util.Log.d(TAG, "  - Age: ${profile.age}")
                    android.util.Log.d(TAG, "  - Gender: ${profile.gender}")
                    android.util.Log.d(TAG, "  - Photo URI: ${profile.photoUri}")
                    
                    val result = SupabaseService.saveProfile(profile)
                    result.onSuccess { profileId ->
                        prefs.edit().putBoolean(KEY_SYNCED, true).apply()
                        Log.d(TAG, "✅ Profile successfully synced to Supabase: $profileId")
                        android.util.Log.d(TAG, "✅ Profile with username '${profile.username}' is now in Supabase database!")
                        android.util.Log.d(TAG, "Profile is now available in database for matching")
                    }.onFailure { e ->
                        Log.e(TAG, "❌ Failed to sync profile to Supabase: ${profile.id}", e)
                        e.printStackTrace()
                        android.util.Log.e(TAG, "❌ ERROR: Profile NOT saved to Supabase database!")
                        android.util.Log.e(TAG, "Error details: ${e.message}")
                        android.util.Log.e(TAG, "Stack trace:")
                        e.printStackTrace()
                        prefs.edit().putBoolean(KEY_SYNCED, false).apply()
                        android.util.Log.e(TAG, "Profile NOT saved to database. Error: ${e.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during Supabase sync", e)
                    e.printStackTrace()
                    android.util.Log.e(TAG, "❌ EXCEPTION: Profile NOT saved to Supabase database!")
                    android.util.Log.e(TAG, "Exception: ${e.message}")
                    e.printStackTrace()
                }
            }
        } else {
            Log.d(TAG, "Supabase sync skipped (syncToSupabase=false)")
        }
    }
    
    fun getProfile(context: Context): UserProfile? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PROFILE, null) ?: return null
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun clearProfile(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
    
    suspend fun syncProfileToSupabase(context: Context, profile: UserProfile): Boolean {
        return try {
            val result = SupabaseService.saveProfile(profile)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing profile", e)
            false
        }
    }
}
