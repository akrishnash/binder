package com.binder.profile

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.binder.R
import com.binder.models.Book
import com.binder.models.UserProfile
import com.binder.utils.GenreColorMapper
import com.bumptech.glide.Glide

class CardViewActivity : AppCompatActivity() {
    
    private lateinit var readingPulse: ProgressBar
    private var pulseAnimator: ValueAnimator? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_view_simple)
        
        @Suppress("DEPRECATION")
        var profile = intent.getSerializableExtra("profile") as? UserProfile
        
        if (profile == null) {
            finish()
            return
        }
        
        // If viewing own profile, reload from ProfileManager to get latest data (e.g., updated photo)
        val currentUserProfile = com.binder.utils.ProfileManager.getProfile(this)
        if (currentUserProfile != null && profile.id == currentUserProfile.id) {
            profile = currentUserProfile
            android.util.Log.d("CardViewActivity", "Reloading own profile to show latest changes")
        }
        
        displayCard(profile)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        pulseAnimator?.cancel()
    }
    
    private fun displayCard(profile: UserProfile) {
        val photoImageView = findViewById<ImageView>(R.id.cardPhoto)
        val nameText = findViewById<TextView>(R.id.cardName)
        val ageText = findViewById<TextView>(R.id.cardAge)
        val genresText = findViewById<TextView>(R.id.cardGenres)
        val bioText = findViewById<TextView>(R.id.cardBio)
        val booksTitleText = findViewById<TextView>(R.id.cardBooksTitle)
        val booksText = findViewById<TextView>(R.id.cardBooks)
        
        // Photo - handle all URI types including content:// from image picker
        android.util.Log.d("CardViewActivity", "Loading photo for ${profile.username}, photoUri: ${profile.photoUri}")
        if (!profile.photoUri.isNullOrEmpty()) {
            // Handle Android resource URIs
            if (profile.photoUri.startsWith("android.resource://")) {
                try {
                    val uri = android.net.Uri.parse(profile.photoUri)
                    val resourceName = uri.lastPathSegment
                    android.util.Log.d("CardViewActivity", "Parsing Android resource URI, resourceName: $resourceName")
                    if (resourceName != null) {
                        val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
                        android.util.Log.d("CardViewActivity", "Resource ID for $resourceName: $resourceId")
                        if (resourceId != 0) {
                            Glide.with(this)
                                .load(resourceId)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .into(photoImageView)
                            android.util.Log.d("CardViewActivity", "Loaded photo from resource: $resourceName")
                        } else {
                            android.util.Log.w("CardViewActivity", "Resource not found: $resourceName")
                            photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    } else {
                        android.util.Log.w("CardViewActivity", "Resource name is null")
                        photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CardViewActivity", "Error loading Android resource photo", e)
                    e.printStackTrace()
                    photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                }
            } else {
                // Regular URI (file://, http://, https://, data:image, or content:// from image picker)
                android.util.Log.d("CardViewActivity", "Loading photo from URI: ${profile.photoUri}")
                try {
                    val loadTarget = when {
                        profile.photoUri.startsWith("data:image") -> {
                            // Base64 data URI - decode and load
                            try {
                                val base64Data = profile.photoUri.substringAfter(",")
                                val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.NO_WRAP)
                                imageBytes
                            } catch (e: Exception) {
                                android.util.Log.e("CardViewActivity", "Error decoding base64 image", e)
                                null
                            }
                        }
                        profile.photoUri.startsWith("http://") || profile.photoUri.startsWith("https://") -> {
                            profile.photoUri // HTTP URL can be used directly
                        }
                        profile.photoUri.startsWith("file://") -> {
                            android.net.Uri.parse(profile.photoUri)
                        }
                        else -> {
                            android.net.Uri.parse(profile.photoUri) // content://
                        }
                    }
                    
                    if (loadTarget != null) {
                        Glide.with(this)
                            .load(loadTarget)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(photoImageView)
                        android.util.Log.d("CardViewActivity", "Loaded photo from URI successfully")
                    } else {
                        photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CardViewActivity", "Error loading photo from URI", e)
                    e.printStackTrace()
                    photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }
        } else {
            android.util.Log.w("CardViewActivity", "Photo URI is null or empty for ${profile.username}")
            photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
        }
        
        // Name and age - make age more prominent
        val displayName = if (profile.username.isNotEmpty()) {
            profile.username
        } else {
            profile.gender
        }
        nameText.text = displayName
        ageText.text = "${profile.age}"
        
        // Genres
        val genresDisplay = if (profile.genres.isNotEmpty()) {
            profile.genres.joinToString(" • ")
        } else {
            profile.interests.joinToString(" • ")
        }
        genresText.text = genresDisplay
        
        // Bio - FULL bio, no truncation
        if (profile.bio.isNotEmpty()) {
            bioText.text = profile.bio
        } else {
            // Default message
            bioText.text = "Looking for someone to share book recommendations with!"
        }
        
        // Books - show ALL favorite books
        if (profile.books.isNotEmpty()) {
            booksTitleText.visibility = View.VISIBLE
            booksText.visibility = View.VISIBLE
            val booksDisplay = profile.books.mapIndexed { index, book ->
                "${index + 1}. ${book.title} by ${book.author}"
            }.joinToString("\n")
            booksText.text = booksDisplay
        } else {
            booksTitleText.visibility = View.GONE
            booksText.visibility = View.GONE
        }
    }
}
