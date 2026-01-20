package com.binder.utils

import android.util.Log
import com.binder.models.Book
import com.binder.models.UserProfile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.*

object TribeService {
    private const val TAG = "TribeService"
    private const val MIN_TRIBE_SIZE = 5
    private const val SPRINT_DURATION_HOURS = 48L
    
    private val client = SupabaseService.client
    
    @Serializable
    data class TribeRow(
        val id: String? = null,
        val book_id: String,
        val book_title: String,
        val book_author: String,
        val city: String,
        val status: String = "forming", // 'forming', 'active', 'completed', 'expired'
        val sprint_start_time: String? = null,
        val sprint_end_time: String? = null,
        val created_at: String? = null,
        val updated_at: String? = null
    )
    
    @Serializable
    data class TribeMemberRow(
        val id: String? = null,
        val tribe_id: String,
        val user_id: String,
        val joined_at: String? = null,
        val status: String = "active" // 'active', 'left', 'completed'
    )
    
    @Serializable
    data class NotificationRow(
        val id: String? = null,
        val user_id: String,
        val type: String, // 'tribe_forming', 'tribe_ready', 'tribe_invite', etc.
        val title: String,
        val message: String,
        val tribe_id: String? = null,
        val book_id: String? = null,
        val read: Boolean = false,
        val created_at: String? = null
    )
    
    /**
     * Check if a tribe should be formed when a user updates their currentlyReading books
     * This should be called after a user saves their profile with currentlyReading books
     */
    suspend fun checkAndCreateTribe(userId: String, city: String, currentlyReading: List<Book>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (city.isEmpty() || currentlyReading.isEmpty()) {
                Log.d(TAG, "Cannot create tribe: city or currentlyReading is empty")
                return@withContext Result.success(Unit)
            }
            
            // Check each currently reading book
            for (book in currentlyReading) {
                // Find other users in the same city reading the same book
                val matchingUsers = findUsersReadingBook(book.id, city, userId)
                
                Log.d(TAG, "Found ${matchingUsers.size} users in $city reading ${book.title} (excluding current user)")
                
                // If we have at least 4 other users (5 total including current user), create a tribe
                if (matchingUsers.size >= MIN_TRIBE_SIZE - 1) {
                    // Check if a tribe already exists for this book+city
                    val existingTribe = findExistingTribe(book.id, city)
                    
                    if (existingTribe == null) {
                        // Create new tribe
                        val tribeResult = createTribe(book, city, matchingUsers + userId)
                        tribeResult.onSuccess {
                            Log.d(TAG, "✅ Tribe created successfully for ${book.title} in $city")
                        }.onFailure { e ->
                            Log.e(TAG, "Failed to create tribe", e)
                        }
                    } else {
                        // Tribe exists, check if user should be added
                        addUserToTribe(existingTribe.id ?: "", userId)
                        Log.d(TAG, "Tribe already exists, added user to existing tribe")
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for tribe formation", e)
            Result.failure(e)
        }
    }
    
    /**
     * Find users in the same city who are currently reading the same book
     */
    private suspend fun findUsersReadingBook(bookId: String, city: String, excludeUserId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Get all profiles in the same city
            val profiles = client.from("profiles")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("city", city)
                        neq("text_id", excludeUserId) // Exclude current user
                    }
                }
                .decodeList<SupabaseService.ProfileRow>()
            
            // Filter profiles that have this book in their currently_reading
            val matchingUserIds = profiles.filter { profile ->
                val currentlyReading = profile.currently_reading
                if (currentlyReading != null) {
                    try {
                        val booksArray = currentlyReading["books"]
                        if (booksArray != null) {
                            when (booksArray) {
                                is JsonArray -> {
                                    booksArray.any { bookObj ->
                                        try {
                                            val bookJson = bookObj.jsonObject
                                            bookJson["id"]?.jsonPrimitive?.content == bookId
                                        } catch (e: Exception) {
                                            false
                                        }
                                    }
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        false
                    }
                } else {
                    false
                }
            }.mapNotNull { profile ->
                profile.text_id ?: profile.id?.toString()
            }
            
            Log.d(TAG, "Found ${matchingUserIds.size} users reading book $bookId in $city")
            matchingUserIds
        } catch (e: Exception) {
            Log.e(TAG, "Error finding users reading book", e)
            emptyList()
        }
    }
    
    /**
     * Find existing tribe for a book+city combination
     */
    private suspend fun findExistingTribe(bookId: String, city: String): TribeRow? = withContext(Dispatchers.IO) {
        try {
            val tribes = client.from("tribes")
                .select {
                    filter {
                        eq("book_id", bookId)
                        eq("city", city)
                        or {
                            eq("status", "forming")
                            eq("status", "active")
                        }
                    }
                }
                .decodeList<TribeRow>()
            
            tribes.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error finding existing tribe", e)
            null
        }
    }
    
    /**
     * Create a new tribe and add members
     */
    private suspend fun createTribe(book: Book, city: String, userIds: List<String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val now = Date()
            val endTime = Date(now.time + SPRINT_DURATION_HOURS * 60 * 60 * 1000)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val tribe = TribeRow(
                book_id = book.id,
                book_title = book.title,
                book_author = book.author,
                city = city,
                status = "forming",
                sprint_start_time = null, // Will be set when tribe becomes active
                sprint_end_time = null,
                created_at = dateFormat.format(now),
                updated_at = dateFormat.format(now)
            )
            
            val inserted = client.from("tribes")
                .insert(tribe) {
                    select(Columns.ALL)
                }
                .decodeSingle<TribeRow>()
            
            val tribeId = inserted.id ?: return@withContext Result.failure(Exception("Failed to get tribe ID"))
            
            // Add all users to the tribe
            val members = userIds.take(MIN_TRIBE_SIZE).map { userId ->
                TribeMemberRow(
                    tribe_id = tribeId,
                    user_id = userId,
                    joined_at = dateFormat.format(now),
                    status = "active"
                )
            }
            
            // Insert members
            client.from("tribe_members").insert(members)
            
            // Create notifications for all members
            val notifications = members.map { member ->
                NotificationRow(
                    user_id = member.user_id,
                    type = "tribe_forming",
                    title = "A Tribe is Forming!",
                    message = "A Tribe is forming for ${book.title}. Join the 48-hour Sprint?",
                    tribe_id = tribeId,
                    book_id = book.id,
                    read = false,
                    created_at = dateFormat.format(now)
                )
            }
            
            client.from("notifications").insert(notifications)
            
            Log.d(TAG, "✅ Tribe created: $tribeId for ${book.title} in $city with ${members.size} members")
            Result.success(tribeId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating tribe", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add a user to an existing tribe
     */
    suspend fun addUserToTribe(tribeId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val member = TribeMemberRow(
                tribe_id = tribeId,
                user_id = userId,
                joined_at = dateFormat.format(Date()),
                status = "active"
            )
            
            client.from("tribe_members").insert(member)
            
            // Get tribe info for notification
            val tribe = client.from("tribes")
                .select {
                    filter { eq("id", tribeId) }
                }
                .decodeSingle<TribeRow>()
            
            // Create notification
            val notification = NotificationRow(
                user_id = userId,
                type = "tribe_forming",
                title = "A Tribe is Forming!",
                message = "A Tribe is forming for ${tribe.book_title}. Join the 48-hour Sprint?",
                tribe_id = tribeId,
                book_id = tribe.book_id,
                read = false,
                created_at = dateFormat.format(Date())
            )
            
            client.from("notifications").insert(notification)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user to tribe", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all tribes for a user
     */
    suspend fun getUserTribes(userId: String): Result<List<TribeRow>> = withContext(Dispatchers.IO) {
        try {
            // Get all tribe IDs where user is a member
            val memberRows = client.from("tribe_members")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("status", "active")
                    }
                }
                .decodeList<TribeMemberRow>()
            
            val tribeIds = memberRows.map { it.tribe_id }.distinct()
            
            if (tribeIds.isEmpty()) {
                return@withContext Result.success(emptyList())
            }
            
            // Get tribe details - query each tribe individually if needed
            // Note: Supabase Postgrest doesn't support `in` directly, so we'll fetch all and filter
            val allTribes = client.from("tribes")
                .select()
                .decodeList<TribeRow>()
            
            val tribes = allTribes.filter { tribe ->
                tribe.id != null && tribeIds.contains(tribe.id)
            }
            
            Result.success(tribes)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user tribes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get members of a tribe
     */
    suspend fun getTribeMembers(tribeId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val members = client.from("tribe_members")
                .select {
                    filter {
                        eq("tribe_id", tribeId)
                        eq("status", "active")
                    }
                }
                .decodeList<TribeMemberRow>()
            
            Result.success(members.map { it.user_id })
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tribe members", e)
            Result.failure(e)
        }
    }
    
    /**
     * Join a tribe (activate the 48-hour sprint if it's the 5th member)
     */
    suspend fun joinTribe(tribeId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Add user to tribe if not already a member
            addUserToTribe(tribeId, userId)
            
            // Check if tribe now has 5 members
            val membersResult = getTribeMembers(tribeId)
            membersResult.onSuccess { members ->
                if (members.size >= MIN_TRIBE_SIZE) {
                    // Activate the sprint
                    activateTribeSprint(tribeId)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining tribe", e)
            Result.failure(e)
        }
    }
    
    /**
     * Activate the 48-hour sprint for a tribe
     */
    private suspend fun activateTribeSprint(tribeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First fetch the existing tribe to get all required fields
            val existingTribe = client.from("tribes")
                .select {
                    filter { eq("id", tribeId) }
                }
                .decodeSingle<TribeRow>()
            
            val now = Date()
            val endTime = Date(now.time + SPRINT_DURATION_HOURS * 60 * 60 * 1000)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            // Update with all required fields
            val update = TribeRow(
                id = existingTribe.id,
                book_id = existingTribe.book_id,
                book_title = existingTribe.book_title,
                book_author = existingTribe.book_author,
                city = existingTribe.city,
                status = "active",
                sprint_start_time = dateFormat.format(now),
                sprint_end_time = dateFormat.format(endTime),
                created_at = existingTribe.created_at,
                updated_at = dateFormat.format(now)
            )
            
            client.from("tribes")
                .update(update) {
                    filter { eq("id", tribeId) }
                }
            
            // Notify all members that the sprint has started
            val membersResult = getTribeMembers(tribeId)
            membersResult.onSuccess { members ->
                val tribe = client.from("tribes")
                    .select {
                        filter { eq("id", tribeId) }
                    }
                    .decodeSingle<TribeRow>()
                
                val notifications = members.map { userId ->
                    NotificationRow(
                        user_id = userId,
                        type = "tribe_ready",
                        title = "Tribe Sprint Started!",
                        message = "Your tribe for ${tribe.book_title} is ready! The 48-hour sprint has begun.",
                        tribe_id = tribeId,
                        book_id = tribe.book_id,
                        read = false,
                        created_at = dateFormat.format(now)
                    )
                }
                
                client.from("notifications").insert(notifications)
            }
            
            Log.d(TAG, "✅ Tribe sprint activated: $tribeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error activating tribe sprint", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get notifications for a user
     */
    suspend fun getUserNotifications(userId: String): Result<List<NotificationRow>> = withContext(Dispatchers.IO) {
        try {
            val notifications = client.from("notifications")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<NotificationRow>()
            
            // Sort by created_at descending (newest first)
            val sortedNotifications = notifications.sortedByDescending { 
                it.created_at ?: ""
            }
            
            Result.success(sortedNotifications)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications", e)
            Result.failure(e)
        }
    }
    
    /**
     * Mark notification as read
     */
    suspend fun markNotificationRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Fetch the notification first
            val notification = client.from("notifications")
                .select {
                    filter { eq("id", notificationId) }
                }
                .decodeSingle<NotificationRow>()
            
            // Update with all fields, setting read to true
            val update = NotificationRow(
                id = notification.id,
                user_id = notification.user_id,
                type = notification.type,
                title = notification.title,
                message = notification.message,
                tribe_id = notification.tribe_id,
                book_id = notification.book_id,
                read = true,
                created_at = notification.created_at
            )
            
            client.from("notifications")
                .update(update) {
                    filter { eq("id", notificationId) }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read", e)
            Result.failure(e)
        }
    }
}
