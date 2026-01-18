package com.binder.utils

import android.content.Context
import com.binder.models.Book
import com.binder.models.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.engine.android.Android
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
    
    private val client: SupabaseClient by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
            install(Postgrest)
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
            
            val profileRow = ProfileRow(
                id = if (profile.id.startsWith("local-")) null else profile.id, // Use text_id for new profiles
                text_id = profile.id, // Always set text_id
                username = profile.username,
                age = profile.age,
                gender = profile.gender,
                interests = profile.interests,
                genres = profile.genres,
                books = booksJson,
                photo_uri = profile.photoUri,
                bio = profile.bio,
                city = profile.city,
                pages_read_today = profile.pagesReadToday,
                created_at = null, // Use text version
                created_at_text = profile.createdAt,
                updated_at = null,
                updated_at_text = profile.createdAt
            )
            
            if (profile.id.startsWith("local-")) {
                // New profile - insert using text_id
                android.util.Log.d("SupabaseService", "Inserting new profile")
                client.from("profiles").insert(profileRow)
            } else {
                // Existing profile - update by text_id or id
                android.util.Log.d("SupabaseService", "Updating existing profile")
                client.from("profiles").update(profileRow) {
                    filter {
                        // Try text_id first, fallback to id
                        or {
                            eq("text_id", profile.id)
                            eq("id", profile.id)
                        }
                    }
                }
            }
            
            android.util.Log.d("SupabaseService", "Profile saved successfully: ${profile.id}")
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
            photoUri = row.photo_uri,
            bio = row.bio,
            currentlyReading = emptyList(), // Can be enhanced later
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
}
