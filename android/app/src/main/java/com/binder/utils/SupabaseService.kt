package com.binder.utils

import android.content.Context
import com.binder.models.Book
import com.binder.models.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.android.Android
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object SupabaseService {
    private const val SUPABASE_URL = "https://nslffpqvdnhrlefpurhy.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_2FO0Ogo67lHJ0T4bJMlaeA_OHCdPeuL"
    
    internal val client: SupabaseClient by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
            install(Postgrest)
            install(Storage)
            httpEngine = Android.create()
        }
    }
    
    @Serializable
    data class ProfileRow(
        val id: String? = null, // UUID (existing) or TEXT (new)
        val text_id: String? = null, // TEXT id for new profiles
        val username: String = "",
        val age: Int,
        val gender: String,
        val interests: List<String>,
        val genres: List<String>,
        val books: JsonObject,
        val currently_reading: JsonObject? = null, // Currently reading books for Tribe feature
        val photo_uri: String? = null,
        val bio: String = "",
        val city: String = "",
        val pages_read_today: Int = 0,
        val created_at: String? = null,
        val created_at_text: String? = null, // TEXT timestamp
        val updated_at: String? = null,
        val updated_at_text: String? = null // TEXT timestamp
    )
    
    suspend fun saveProfile(profile: UserProfile): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SupabaseService", "Saving profile: ${profile.id}")
            
            val booksArray = profile.books.map { book ->
                buildJsonObject {
                    put("id", book.id)
                    put("title", book.title)
                    put("author", book.author)
                    put("coverId", book.coverId ?: 0)
                    put("coverUrl", book.coverUrl ?: "")
                }
            }
            val booksJson = buildJsonObject {
                put("books", JsonArray(booksArray))
            }
            
            // Build currentlyReading JSON
            val currentlyReadingArray = profile.currentlyReading.map { book ->
                buildJsonObject {
                    put("id", book.id)
                    put("title", book.title)
                    put("author", book.author)
                    put("coverId", book.coverId ?: 0)
                    put("coverUrl", book.coverUrl ?: "")
                }
            }
            val currentlyReadingJson = buildJsonObject {
                put("books", JsonArray(currentlyReadingArray))
            }
            
            android.util.Log.d("SupabaseService", "Creating ProfileRow with photo_uri: ${profile.photoUri}")
            val profileRow = ProfileRow(
                id = if (profile.id.startsWith("local-")) null else profile.id, // Use text_id for new profiles
                text_id = profile.id, // Always set text_id
                username = profile.username,
                age = profile.age,
                gender = profile.gender,
                interests = profile.interests,
                genres = profile.genres,
                books = booksJson,
                currently_reading = currentlyReadingJson,
                photo_uri = profile.photoUri, // This MUST be set - even if null
                bio = profile.bio,
                city = profile.city,
                pages_read_today = profile.pagesReadToday,
                created_at = null, // Use text version
                created_at_text = profile.createdAt,
                updated_at = null,
                updated_at_text = profile.createdAt
            )
            android.util.Log.d("SupabaseService", "ProfileRow created - photo_uri in row: ${profileRow.photo_uri}")
            
            if (profile.id.startsWith("local-")) {
                // New profile - insert using text_id
                android.util.Log.d("SupabaseService", "Inserting new profile")
                android.util.Log.d("SupabaseService", "  photo_uri being saved: ${profileRow.photo_uri}")
                client.from("profiles").insert(profileRow)
                android.util.Log.d("SupabaseService", "✅ Insert completed")
            } else {
                // Existing profile - update by text_id or id
                android.util.Log.d("SupabaseService", "Updating existing profile")
                android.util.Log.d("SupabaseService", "  photo_uri being saved: ${profileRow.photo_uri}")
                client.from("profiles").update(profileRow) {
                    filter {
                        // Try text_id first, fallback to id
                        or {
                            eq("text_id", profile.id)
                            eq("id", profile.id)
                        }
                    }
                }
                android.util.Log.d("SupabaseService", "✅ Update completed")
            }
            
            android.util.Log.d("SupabaseService", "✅ Profile saved successfully: ${profile.id}")
            android.util.Log.d("SupabaseService", "   photo_uri that was saved: ${profileRow.photo_uri}")
            Result.success(profile.id)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Error saving profile", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun getProfile(profileId: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SupabaseService", "Fetching profile from Supabase: $profileId")
            
            // First try with text_id
            try {
                val result = client.from("profiles")
                    .select {
                        filter { eq("text_id", profileId) }
                    }
                    .decodeSingle<ProfileRow>()
                
                val profile = convertToUserProfile(result)
                android.util.Log.d("SupabaseService", "✅ Profile found by text_id: ${profile.username}")
                return@withContext Result.success(profile)
            } catch (e1: Exception) {
                android.util.Log.d("SupabaseService", "Profile not found by text_id, trying id: ${e1.message}")
            }
            
            // Fallback to id
            try {
                val result = client.from("profiles")
                    .select {
                        filter { eq("id", profileId) }
                    }
                    .decodeSingle<ProfileRow>()
                
                val profile = convertToUserProfile(result)
                android.util.Log.d("SupabaseService", "✅ Profile found by id: ${profile.username}")
                return@withContext Result.success(profile)
            } catch (e2: Exception) {
                android.util.Log.e("SupabaseService", "❌ Profile not found by id either: ${e2.message}")
                e2.printStackTrace()
                return@withContext Result.failure(e2)
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "❌ Error fetching profile", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun getAllProfiles(): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        try {
            val results = client.from("profiles")
                .select()
                .decodeList<ProfileRow>()
            
            val profiles = results.map { convertToUserProfile(it) }
            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProfilesExcluding(excludeId: String): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SupabaseService", "Fetching profiles, excluding: $excludeId")
            
            // Get all profiles first, then filter in code (simpler and more reliable)
            val allResults = client.from("profiles")
                .select()
                .decodeList<ProfileRow>()
            
            android.util.Log.d("SupabaseService", "Fetched ${allResults.size} profiles from database")
            
            // Filter out the excluded profile
            val filteredResults = if (excludeId.isNotEmpty()) {
                allResults.filter { row ->
                    val rowId = row.text_id ?: row.id?.toString() ?: ""
                    rowId != excludeId
                }
            } else {
                allResults
            }
            
            android.util.Log.d("SupabaseService", "Filtered to ${filteredResults.size} profiles")
            
            val profiles = filteredResults.mapNotNull { row ->
                try {
                    convertToUserProfile(row)
                } catch (e: Exception) {
                    android.util.Log.e("SupabaseService", "Error converting profile: ${row.id}", e)
                    e.printStackTrace()
                    null
                }
            }
            
            android.util.Log.d("SupabaseService", "Successfully converted ${profiles.size} profiles")
            Result.success(profiles)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Error fetching profiles", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    private fun convertToUserProfile(row: ProfileRow): UserProfile {
        val booksList = try {
            // Try to get books from JSON structure
            val booksElement = row.books["books"]
            when {
                booksElement is JsonArray -> {
                    booksElement.mapNotNull { bookElement ->
                        try {
                            val bookObj = bookElement.jsonObject
                            Book(
                                id = bookObj["id"]?.jsonPrimitive?.content ?: "",
                                title = bookObj["title"]?.jsonPrimitive?.content ?: "",
                                author = bookObj["author"]?.jsonPrimitive?.content ?: "",
                                coverId = bookObj["coverId"]?.jsonPrimitive?.content?.toIntOrNull(),
                                coverUrl = bookObj["coverUrl"]?.jsonPrimitive?.content?.takeIf { it.isNotEmpty() }
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("SupabaseService", "Error parsing book", e)
                            null
                        }
                    }
                }
                else -> {
                    // Fallback: try to parse as string
                    val booksJson = row.books["books"]?.toString() ?: "[]"
                    try {
                        Json.decodeFromString<List<JsonObject>>(booksJson)
                            .mapNotNull { bookObj ->
                                try {
                                    Book(
                                        id = bookObj["id"]?.jsonPrimitive?.content ?: "",
                                        title = bookObj["title"]?.jsonPrimitive?.content ?: "",
                                        author = bookObj["author"]?.jsonPrimitive?.content ?: "",
                                        coverId = bookObj["coverId"]?.jsonPrimitive?.content?.toIntOrNull(),
                                        coverUrl = bookObj["coverUrl"]?.jsonPrimitive?.content?.takeIf { it.isNotEmpty() }
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                    } catch (e: Exception) {
                        emptyList<Book>()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Error parsing books", e)
            emptyList<Book>()
        }
        
        // Parse currentlyReading books
        val currentlyReadingList = try {
            when (val currentlyReadingJson = row.currently_reading) {
                null -> emptyList<Book>()
                is JsonObject -> {
                    when (val booksValue = currentlyReadingJson["books"]) {
                        is JsonArray -> {
                            booksValue.mapNotNull { bookObj ->
                                try {
                                    val bookJson = bookObj.jsonObject
                                    Book(
                                        id = bookJson["id"]?.jsonPrimitive?.content ?: "",
                                        title = bookJson["title"]?.jsonPrimitive?.content ?: "",
                                        author = bookJson["author"]?.jsonPrimitive?.content ?: "",
                                        coverId = bookJson["coverId"]?.jsonPrimitive?.content?.toIntOrNull(),
                                        coverUrl = bookJson["coverUrl"]?.jsonPrimitive?.content?.takeIf { it.isNotEmpty() }
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                        else -> emptyList<Book>()
                    }
                }
                else -> emptyList<Book>()
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Error parsing currentlyReading", e)
            emptyList<Book>()
        }
        
        // Handle ID - prefer text_id, fallback to id (convert UUID to string if needed)
        val profileId = row.text_id ?: run {
            when (val idValue = row.id) {
                is String -> idValue
                else -> idValue?.toString() ?: ""
            }
        }
        
        return UserProfile(
            id = profileId,
            username = row.username,
            age = row.age,
            gender = row.gender,
            interests = row.interests,
            genres = row.genres,
            books = booksList,
            createdAt = row.created_at_text ?: row.created_at?.toString() ?: "",
            photoUri = row.photo_uri ?: run {
                android.util.Log.w("SupabaseService", "⚠️ photo_uri is NULL in database for profile: ${row.username}")
                null
            },
            bio = row.bio,
            currentlyReading = currentlyReadingList,
            favoriteBooks = booksList,
            city = row.city,
            pagesReadToday = row.pages_read_today
        )
    }
    
    // Matching/Likes functions
    @Serializable
    data class LikeRow(
        val id: String? = null,
        val liker_id: String,
        val liked_id: String,
        val created_at: String? = null
    )
    
    @Serializable
    data class MatchRow(
        val id: String? = null,
        val user1_id: String,
        val user2_id: String,
        val created_at: String? = null
    )
    
    suspend fun saveLike(likerId: String, likedId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SupabaseService", "Saving like: $likerId likes $likedId")
            val likeRow = LikeRow(
                liker_id = likerId,
                liked_id = likedId,
                created_at = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                    .format(java.util.Date())
            )
            client.from("likes").insert(likeRow)
            android.util.Log.d("SupabaseService", "Like saved successfully")
            Result.success(likerId)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Error saving like", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun checkMutualLike(likerId: String, likedId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Check if likedId has already liked likerId
            val existingLikes = client.from("likes")
                .select {
                    filter {
                        eq("liker_id", likedId)
                        eq("liked_id", likerId)
                    }
                }
                .decodeList<LikeRow>()
            
            val isMutual = existingLikes.isNotEmpty()
            android.util.Log.d("SupabaseService", "Mutual like check: $isMutual")
            Result.success(isMutual)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Error checking mutual like", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun createMatch(user1Id: String, user2Id: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Ensure consistent ordering (smaller id first)
            val (id1, id2) = if (user1Id < user2Id) Pair(user1Id, user2Id) else Pair(user2Id, user1Id)
            
            android.util.Log.d("SupabaseService", "Creating match: $id1 <-> $id2")
            val matchRow = MatchRow(
                user1_id = id1,
                user2_id = id2,
                created_at = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                    .format(java.util.Date())
            )
            client.from("matches").insert(matchRow)
            android.util.Log.d("SupabaseService", "Match created successfully: BINDERED!")
            Result.success(id1)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Error creating match", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun getMatches(userId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val matches1 = client.from("matches")
                .select {
                    filter { eq("user1_id", userId) }
                }
                .decodeList<MatchRow>()
            
            val matches2 = client.from("matches")
                .select {
                    filter { eq("user2_id", userId) }
                }
                .decodeList<MatchRow>()
            
            val matchIds = (matches1.map { it.user2_id } + matches2.map { it.user1_id }).distinct()
            Result.success(matchIds)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Error getting matches", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Convert a local photo to base64 data URI
     * @param context Android Context for accessing content resolver
     * @param photoUri Local URI of the photo (content:// or file://)
     * @return Result containing the base64 data URI if successful
     */
    suspend fun convertPhotoToBase64(context: android.content.Context, photoUri: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SupabaseService", "Converting photo to base64: $photoUri")
            
            // Read the photo from URI
            val uri = android.net.Uri.parse(photoUri)
            val inputStream: InputStream = when {
                photoUri.startsWith("content://") -> {
                    context.contentResolver.openInputStream(uri)
                        ?: throw IllegalArgumentException("Cannot open content:// URI: $photoUri")
                }
                photoUri.startsWith("file://") -> {
                    java.io.FileInputStream(uri.path ?: throw IllegalArgumentException("Invalid file:// URI: $photoUri"))
                }
                else -> throw IllegalArgumentException("Unsupported URI scheme: $photoUri")
            }
            
            // Read bytes from input stream
            val bytes = inputStream.use { it.readBytes() }
            android.util.Log.d("SupabaseService", "Read ${bytes.size} bytes from photo")
            
            // Convert to base64
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            val dataUri = "data:image/jpeg;base64,$base64"
            android.util.Log.d("SupabaseService", "✅ Created base64 URI (${base64.length} chars)")
            Result.success(dataUri)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "❌ Error converting photo to base64", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Upload a photo to Supabase Storage and return the public URL
     * @param context Android Context for accessing content resolver
     * @param photoUri Local URI of the photo (content://, file://, or android.resource://)
     * @param userId User ID to use as filename prefix
     * @return Result containing the public URL if successful
     */
    suspend fun uploadPhoto(context: android.content.Context, photoUri: String, userId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SupabaseService", "Uploading photo: $photoUri for user: $userId")
            
            // Generate unique filename: userId_timestamp.jpg
            val timestamp = System.currentTimeMillis()
            val filename = "${userId}_${timestamp}.jpg"
            val filePath = filename // Path within the bucket (no bucket name prefix)
            
            // Read the photo from URI
            val uri = android.net.Uri.parse(photoUri)
            val inputStream: InputStream = when {
                photoUri.startsWith("content://") -> {
                    context.contentResolver.openInputStream(uri)
                        ?: throw IllegalArgumentException("Cannot open content:// URI: $photoUri")
                }
                photoUri.startsWith("file://") -> {
                    java.io.FileInputStream(uri.path ?: throw IllegalArgumentException("Invalid file:// URI: $photoUri"))
                }
                photoUri.startsWith("android.resource://") -> {
                    // For drawable resources, we can't upload them - return the resource URI as-is
                    android.util.Log.w("SupabaseService", "Cannot upload Android resource URI, returning as-is: $photoUri")
                    return@withContext Result.success(photoUri)
                }
                else -> throw IllegalArgumentException("Unsupported URI scheme: $photoUri")
            }
            
            // Read bytes from input stream
            val bytes = inputStream.use { it.readBytes() }
            
            android.util.Log.d("SupabaseService", "Read ${bytes.size} bytes from photo")
            
            // Upload to Supabase Storage
            try {
                android.util.Log.d("SupabaseService", "=== STARTING SUPABASE STORAGE UPLOAD ===")
                android.util.Log.d("SupabaseService", "Bucket: profile-photos")
                android.util.Log.d("SupabaseService", "File path: $filePath")
                android.util.Log.d("SupabaseService", "File size: ${bytes.size} bytes")
                android.util.Log.d("SupabaseService", "User ID: $userId")
                
                val bucket = client.storage.from("profile-photos")
                android.util.Log.d("SupabaseService", "✅ Bucket reference obtained")
                
                android.util.Log.d("SupabaseService", "Calling bucket.upload()...")
                // Upload throws exception on error, so we catch it below
                bucket.upload(filePath, bytes, upsert = true)
                
                android.util.Log.d("SupabaseService", "✅ Upload call completed successfully (no exception thrown)")
                
                // Get public URL (bucket is public, so we can use the direct URL)
                val publicUrl = "${SUPABASE_URL}/storage/v1/object/public/profile-photos/$filename"
                
                android.util.Log.d("SupabaseService", "✅ Photo uploaded successfully: $publicUrl")
                Result.success(publicUrl)
            } catch (storageError: Exception) {
                android.util.Log.e("SupabaseService", "❌ Storage upload failed!")
                android.util.Log.e("SupabaseService", "   Error type: ${storageError.javaClass.simpleName}")
                android.util.Log.e("SupabaseService", "   Error message: ${storageError.message}")
                android.util.Log.e("SupabaseService", "   Bucket: profile-photos")
                android.util.Log.e("SupabaseService", "   File path: $filePath")
                android.util.Log.e("SupabaseService", "   File size: ${bytes.size} bytes")
                storageError.printStackTrace()
                
                // Check if it's a bucket/policy error
                val errorMsg = storageError.message ?: ""
                if (errorMsg.contains("bucket", ignoreCase = true) || 
                    errorMsg.contains("policy", ignoreCase = true) ||
                    errorMsg.contains("permission", ignoreCase = true)) {
                    android.util.Log.e("SupabaseService", "⚠️ This looks like a bucket/policy issue!")
                    android.util.Log.e("SupabaseService", "   Make sure you ran supabase-storage-schema.sql in Supabase SQL Editor")
                }
                
                // FALLBACK 1: Try to save photo as base64 in database (simple, always works)
                // This is a simple solution that doesn't require Supabase Storage
                try {
                    android.util.Log.w("SupabaseService", "⚠️ Supabase Storage failed, using base64 fallback")
                    // Convert bytes to base64
                    val base64Photo = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    // Create a data URI: data:image/jpeg;base64,<base64_string>
                    val base64Uri = "data:image/jpeg;base64,$base64Photo"
                    android.util.Log.d("SupabaseService", "✅ Created base64 URI (${base64Photo.length} chars)")
                    Result.success(base64Uri)
                } catch (base64Error: Exception) {
                    android.util.Log.e("SupabaseService", "❌ Base64 encoding failed", base64Error)
                    // FALLBACK 2: Save photo locally and return a local file URI
                    try {
                        val localFile = java.io.File(context.filesDir, "profile_photos")
                        if (!localFile.exists()) {
                            localFile.mkdirs()
                        }
                        val localPhotoFile = java.io.File(localFile, filename)
                        localPhotoFile.writeBytes(bytes)
                        
                        val localUri = android.net.Uri.fromFile(localPhotoFile).toString()
                        android.util.Log.w("SupabaseService", "⚠️ Using local file fallback: $localUri")
                        Result.success(localUri)
                    } catch (localError: Exception) {
                        android.util.Log.e("SupabaseService", "❌ All fallbacks failed", localError)
                        Result.failure(storageError) // Return original error
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "❌ Error uploading photo", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
