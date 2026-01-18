package com.binder.discovery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.binder.R
import com.binder.models.Book
import com.binder.models.UserProfile
import com.binder.profile.CardViewActivity
import com.binder.utils.ProfileManager
import com.bumptech.glide.Glide
import kotlin.math.abs

class DiscoveryActivity : AppCompatActivity() {
    
    private lateinit var cardStack: FrameLayout
    private lateinit var currentCard: CardView
    private var currentCardIndex = 0
    private var xDown = 0f
    private var yDown = 0f
    private var cardStartX = 0f
    private var cardStartY = 0f
    
    private val demoProfiles = mutableListOf<UserProfile>()
    private val currentUserProfile: UserProfile? by lazy {
        ProfileManager.getProfile(this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discovery)
        
        cardStack = findViewById(R.id.cardStack)
        
        // Generate demo profiles
        generateDemoProfiles()
        
        // Show first card
        showNextCard()
    }
    
    private fun generateDemoProfiles() {
        val demoBooks = listOf(
            Book("1", "Dune", "Frank Herbert", 123456, "https://covers.openlibrary.org/b/id/123456-M.jpg"),
            Book("2", "The Seven Husbands of Evelyn Hugo", "Taylor Jenkins Reid", 234567, "https://covers.openlibrary.org/b/id/234567-M.jpg"),
            Book("3", "Project Hail Mary", "Andy Weir", 345678, "https://covers.openlibrary.org/b/id/345678-M.jpg"),
            Book("4", "The Midnight Library", "Matt Haig", 456789, "https://covers.openlibrary.org/b/id/456789-M.jpg"),
            Book("5", "Circe", "Madeline Miller", 567890, "https://covers.openlibrary.org/b/id/567890-M.jpg"),
            Book("6", "The Song of Achilles", "Madeline Miller", 678901, "https://covers.openlibrary.org/b/id/678901-M.jpg"),
            Book("7", "1984", "George Orwell", 789012, "https://covers.openlibrary.org/b/id/789012-M.jpg"),
            Book("8", "The Handmaid's Tale", "Margaret Atwood", 890123, "https://covers.openlibrary.org/b/id/890123-M.jpg")
        )
        
        demoProfiles.addAll(listOf(
            UserProfile(
                id = "demo1",
                age = 24,
                gender = "Sarah",
                interests = listOf("Fiction", "Poetry", "Art"),
                genres = listOf("Sci-Fi", "Fantasy", "Literary Fiction"),
                books = listOf(demoBooks[0], demoBooks[2], demoBooks[4]),
                createdAt = "2024-01-01",
                bio = "Looking for someone to finish 'Dune' with.",
                currentlyReading = listOf(demoBooks[0]),
                favoriteBooks = listOf(demoBooks[2], demoBooks[4]),
                city = "New York"
            ),
            UserProfile(
                id = "demo2",
                age = 28,
                gender = "Alex",
                interests = listOf("History", "Philosophy", "Travel"),
                genres = listOf("Historical Fiction", "Biography", "Philosophy"),
                books = listOf(demoBooks[1], demoBooks[3], demoBooks[5]),
                createdAt = "2024-01-02",
                bio = "Bookworm seeking intellectual conversations.",
                currentlyReading = listOf(demoBooks[1]),
                favoriteBooks = listOf(demoBooks[3], demoBooks[5]),
                city = "San Francisco"
            ),
            UserProfile(
                id = "demo3",
                age = 26,
                gender = "Jordan",
                interests = listOf("Science", "Non-fiction", "Cooking"),
                genres = listOf("Science", "Biography", "Memoir"),
                books = listOf(demoBooks[2], demoBooks[6], demoBooks[0]),
                createdAt = "2024-01-03",
                bio = "Sci-fi enthusiast and coffee lover.",
                currentlyReading = listOf(demoBooks[2]),
                favoriteBooks = listOf(demoBooks[6], demoBooks[0]),
                city = "Seattle"
            ),
            UserProfile(
                id = "demo4",
                age = 30,
                gender = "Morgan",
                interests = listOf("Art", "Music", "Poetry"),
                genres = listOf("Poetry", "Literary Fiction", "Art & Design"),
                books = listOf(demoBooks[4], demoBooks[5], demoBooks[7]),
                createdAt = "2024-01-04",
                bio = "Poetry and prose, that's my thing.",
                currentlyReading = listOf(demoBooks[4]),
                favoriteBooks = listOf(demoBooks[5], demoBooks[7]),
                city = "Portland"
            ),
            UserProfile(
                id = "demo5",
                age = 22,
                gender = "Riley",
                interests = listOf("Young Adult", "Romance", "Drama"),
                genres = listOf("Young Adult", "Romance", "Drama"),
                books = listOf(demoBooks[1], demoBooks[3], demoBooks[4]),
                createdAt = "2024-01-05",
                bio = "YA books are my escape from reality.",
                currentlyReading = listOf(demoBooks[1]),
                favoriteBooks = listOf(demoBooks[3], demoBooks[4]),
                city = "Austin"
            )
        ))
    }
    
    private fun showNextCard() {
        if (currentCardIndex >= demoProfiles.size) {
            // No more cards
            findViewById<TextView>(R.id.noMoreCardsText).visibility = View.VISIBLE
            cardStack.visibility = View.GONE
            return
        }
        
        val profile = demoProfiles[currentCardIndex]
        currentCard = createCardView(profile)
        cardStack.removeAllViews()
        cardStack.addView(currentCard)
        
        // Set up swipe listener
        currentCard.setOnTouchListener { view, event ->
            handleSwipe(view, event)
        }
    }
    
    private fun createCardView(profile: UserProfile): CardView {
        val card = layoutInflater.inflate(R.layout.card_discovery, null, false) as CardView
        
        // Load profile data into card
        val photoView = card.findViewById<ImageView>(R.id.cardPhoto)
        val nameText = card.findViewById<TextView>(R.id.cardName)
        val ageText = card.findViewById<TextView>(R.id.cardAge)
        val bioText = card.findViewById<TextView>(R.id.cardBio)
        val genresText = card.findViewById<TextView>(R.id.cardGenres)
        
        // Photo placeholder
        photoView.setImageResource(R.drawable.ic_profile_placeholder)
        
        // Name (username or gender) and age
        val displayName = if (profile.username.isNotEmpty()) {
            profile.username
        } else {
            profile.gender
        }
        nameText.text = displayName
        ageText.text = "${profile.age}"
        
        // Bio
        bioText.text = profile.bio
        
        // Genres
        genresText.text = profile.genres.take(3).joinToString(" • ")
        
        // Click to view full card
        card.setOnClickListener {
            val intent = android.content.Intent(this, CardViewActivity::class.java).apply {
                putExtra("profile", profile)
            }
            startActivity(intent)
        }
        
        return card
    }
    
    private fun handleSwipe(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                xDown = event.x
                yDown = event.y
                cardStartX = view.x
                cardStartY = view.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - xDown
                val deltaY = event.y - yDown
                
                // Only allow horizontal swipes
                if (abs(deltaX) > abs(deltaY)) {
                    view.x = cardStartX + deltaX
                    view.rotation = deltaX / 10f // Slight rotation for effect
                    
                    // Change opacity based on swipe distance
                    val alpha = 1f - (abs(deltaX) / 1000f).coerceIn(0f, 0.5f)
                    view.alpha = alpha
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - xDown
                val threshold = 150f
                
                if (abs(deltaX) > threshold) {
                    if (deltaX > 0) {
                        // Right swipe - Like
                        swipeCardRight(view)
                    } else {
                        // Left swipe - Dislike
                        swipeCardLeft(view)
                    }
                } else {
                    // Snap back
                    snapCardBack(view)
                }
                return true
            }
        }
        return false
    }
    
    private fun swipeCardRight(view: View) {
        val screenWidth = resources.displayMetrics.widthPixels
        val animator = ObjectAnimator.ofFloat(view, "x", view.x, screenWidth.toFloat() + 200f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onSwipeComplete(true)
                }
            })
        }
        animator.start()
        
        // Show like indicator
        showSwipeFeedback(true)
    }
    
    private fun swipeCardLeft(view: View) {
        val screenWidth = resources.displayMetrics.widthPixels
        val animator = ObjectAnimator.ofFloat(view, "x", view.x, -screenWidth.toFloat() - 200f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onSwipeComplete(false)
                }
            })
        }
        animator.start()
        
        // Show dislike indicator
        showSwipeFeedback(false)
    }
    
    private fun snapCardBack(view: View) {
        val animatorX = ObjectAnimator.ofFloat(view, "x", view.x, cardStartX)
        val animatorY = ObjectAnimator.ofFloat(view, "y", view.y, cardStartY)
        val animatorRotation = ObjectAnimator.ofFloat(view, "rotation", view.rotation, 0f)
        val animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", view.alpha, 1f)
        
        animatorX.duration = 200
        animatorY.duration = 200
        animatorRotation.duration = 200
        animatorAlpha.duration = 200
        
        animatorX.start()
        animatorY.start()
        animatorRotation.start()
        animatorAlpha.start()
    }
    
    private fun showSwipeFeedback(isLike: Boolean) {
        val feedbackText = findViewById<TextView>(if (isLike) R.id.likeFeedback else R.id.dislikeFeedback)
        feedbackText.visibility = View.VISIBLE
        feedbackText.alpha = 1f
        
        val animator = ObjectAnimator.ofFloat(feedbackText, "alpha", 1f, 0f).apply {
            duration = 500
            startDelay = 200
        }
        animator.start()
        
        feedbackText.postDelayed({
            feedbackText.visibility = View.GONE
        }, 700)
    }
    
    private fun onSwipeComplete(isLike: Boolean) {
        if (isLike) {
            // Check for match
            val profile = demoProfiles[currentCardIndex]
            val matchScore = calculateMatchScore(currentUserProfile, profile)
            
            if (matchScore > 50) {
                // Show match dialog
                showMatchDialog(profile, matchScore)
            }
        }
        
        currentCardIndex++
        showNextCard()
    }
    
    private fun calculateMatchScore(user1: UserProfile?, user2: UserProfile): Int {
        if (user1 == null) return 0
        
        var score = 0
        
        // Genre match (40 points max)
        val commonGenres = user1.genres.intersect(user2.genres.toSet()).size
        score += (commonGenres * 13).coerceAtMost(40)
        
        // Book match (40 points max)
        val user1BookIds = user1.books.map { it.id }.toSet()
        val user2BookIds = user2.books.map { it.id }.toSet()
        val commonBooks = user1BookIds.intersect(user2BookIds).size
        score += (commonBooks * 13).coerceAtMost(40)
        
        // Interest match (20 points max)
        val commonInterests = user1.interests.intersect(user2.interests.toSet()).size
        score += (commonInterests * 10).coerceAtMost(20)
        
        return score
    }
    
    private fun showMatchDialog(profile: UserProfile, matchScore: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("It's a Match! ✨")
            .setMessage("You and ${profile.gender} have ${matchScore}% compatibility!\n\nYou both love similar books and genres.")
            .setPositiveButton("View Profile") { _, _ ->
                val intent = android.content.Intent(this, CardViewActivity::class.java).apply {
                    putExtra("profile", profile)
                }
                startActivity(intent)
            }
            .setNegativeButton("Continue") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
