package com.binder.models

import java.io.Serializable

data class UserProfile(
    val id: String,
    val username: String = "",
    val age: Int,
    val gender: String,
    val interests: List<String>,
    val genres: List<String>,
    val books: List<Book>,
    val createdAt: String,
    val photoUri: String? = null,
    val bio: String = "",
    val currentlyReading: List<Book> = emptyList(),
    val favoriteBooks: List<Book> = emptyList(),
    val city: String = "",
    val pagesReadToday: Int = 0
) : Serializable

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverId: Int?,
    val coverUrl: String?
) : Serializable
