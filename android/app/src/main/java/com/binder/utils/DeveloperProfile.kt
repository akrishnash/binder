package com.binder.utils

import android.content.Context
import android.net.Uri
import com.binder.models.Book
import com.binder.models.UserProfile

object DeveloperProfile {
    
    fun getDeveloperProfile(context: Context? = null): UserProfile {
        val anuragBooks = listOf(
            Book(
                id = "anurag1",
                title = "Book of Mirad",
                author = "Unknown",
                coverId = null,
                coverUrl = null
            ),
            Book(
                id = "anurag2",
                title = "Selected Stories",
                author = "Anton Chekhov",
                coverId = null,
                coverUrl = null
            ),
            Book(
                id = "anurag3",
                title = "The Brothers Karamazov",
                author = "Fyodor Dostoevsky",
                coverId = null,
                coverUrl = null
            )
        )
        
        // Build photo URI from drawable resource
        val photoUri = if (context != null) {
            Uri.parse("android.resource://${context.packageName}/drawable/anurag").toString()
        } else {
            null
        }
        
        return UserProfile(
            id = "developer", // Special ID to prevent left swipe
            username = "Anurag",
            age = 27,
            gender = "Male",
            interests = listOf("Philosophy", "History", "Science", "Music", "Culture"),
            genres = listOf("Philosophy", "Classics", "Literary Fiction"),
            books = anuragBooks,
            createdAt = "2024-01-01",
            bio = "Building Binder to connect book lovers. Always reading, always thinking. Looking for someone who appreciates deep conversations and great stories.",
            currentlyReading = listOf(anuragBooks[0]),
            favoriteBooks = anuragBooks,
            city = "",
            pagesReadToday = 42,
            photoUri = photoUri
        )
    }
    
    fun getFizaProfile(context: Context? = null): UserProfile {
        val fizaBooks = listOf(
            Book(
                id = "fiza1",
                title = "B Grade Cooking Books in Hindi",
                author = "Various",
                coverId = null,
                coverUrl = null
            ),
            Book(
                id = "fiza2",
                title = "Some Book on Dumb Feminism",
                author = "Various",
                coverId = null,
                coverUrl = null
            ),
            Book(
                id = "fiza3",
                title = "Santa Banta Jokes Book",
                author = "Various",
                coverId = null,
                coverUrl = null
            )
        )
        
        // Build photo URI from drawable resource
        val photoUri = if (context != null) {
            Uri.parse("android.resource://${context.packageName}/drawable/fiza").toString()
        } else {
            null
        }
        
        return UserProfile(
            id = "fiza",
            username = "Fiza",
            age = 29,
            gender = "Female",
            interests = listOf("Mastikhori", "Kalesh", "Maar Pitayi", "Rona Dhona"),
            genres = listOf("Comedy", "Cooking", "Feminism"),
            books = fizaBooks,
            createdAt = "2024-01-01",
            bio = "Living life with drama, humor, and lots of kalesh! Looking for someone who can handle my energy.",
            currentlyReading = listOf(fizaBooks[0]),
            favoriteBooks = fizaBooks,
            city = "",
            pagesReadToday = 15,
            photoUri = photoUri
        )
    }
}
