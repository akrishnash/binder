package com.binder.discovery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.binder.R
import com.binder.models.Book
import com.binder.models.UserProfile
import com.binder.profile.CardViewActivity
import com.binder.utils.ProfileManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlin.math.abs

class MatchFragment : Fragment() {
    
    private lateinit var cardStack: FrameLayout
    private var currentCard: CardView? = null
    private var currentCardIndex = 0
    private var xDown = 0f
    private var yDown = 0f
    private var cardStartX = 0f
    private var cardStartY = 0f
    
    private val demoProfiles = mutableListOf<UserProfile>()
    private val currentUserProfile: UserProfile? by lazy {
        ProfileManager.getProfile(requireContext())
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_match, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cardStack = view.findViewById(R.id.cardStack)
        
        // Load profiles from Supabase
        loadProfilesFromSupabase()
    }
    
    override fun onResume() {
        super.onResume()
        // Always reload profiles from Supabase when fragment becomes visible (to get latest data)
        // Reset to beginning so user sees updated profiles
        currentCardIndex = 0
        loadProfilesFromSupabase()
    }
    
    private fun loadProfilesFromSupabase() {
        val currentUser = currentUserProfile
        val excludeId = currentUser?.id ?: ""
        
        android.util.Log.d("MatchFragment", "Loading profiles from Supabase, excluding: $excludeId")
        
        lifecycleScope.launch {
            try {
                val result = com.binder.utils.SupabaseService.getProfilesExcluding(excludeId)
                result.onSuccess { profiles ->
                    // Check if fragment is still attached before updating UI
                    if (!isAdded) {
                        android.util.Log.w("MatchFragment", "Fragment not attached, skipping UI update")
                        return@onSuccess
                    }
                    
                    android.util.Log.d("MatchFragment", "Loaded ${profiles.size} profiles from Supabase")
                    if (profiles.isNotEmpty()) {
                        demoProfiles.clear()
                        demoProfiles.addAll(profiles)
                        showNextCard()
                    } else {
                        android.util.Log.w("MatchFragment", "No profiles from Supabase, using demo profiles")
                        // Fallback to demo profiles if Supabase is empty
                        if (isAdded) {
                            generateDemoProfiles()
                            showNextCard()
                        }
                    }
                }.onFailure { e ->
                    android.util.Log.e("MatchFragment", "Failed to load profiles from Supabase", e)
                    e.printStackTrace()
                    // Fallback to demo profiles on error - only if fragment is attached
                    if (isAdded) {
                        generateDemoProfiles()
                        showNextCard()
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.w("MatchFragment", "Coroutine was cancelled")
                // Don't do anything if cancelled - fragment might be detached
            } catch (e: Exception) {
                android.util.Log.e("MatchFragment", "Exception loading profiles", e)
                e.printStackTrace()
                // Fallback to demo profiles on error - only if fragment is attached
                if (isAdded) {
                    generateDemoProfiles()
                    showNextCard()
                }
            }
        }
    }
    
    private fun generateDemoProfiles() {
        // Check if fragment is attached before using requireContext()
        if (!isAdded) {
            android.util.Log.w("MatchFragment", "Fragment not attached, cannot generate demo profiles")
            return
        }
        
        try {
            // Add developer card first (Anurag)
            val anuragProfile = com.binder.utils.DeveloperProfile.getDeveloperProfile(requireContext())
            demoProfiles.add(anuragProfile)
            
            // Add Fiza's test card
            val fizaProfile = com.binder.utils.DeveloperProfile.getFizaProfile(requireContext())
            demoProfiles.add(fizaProfile)
        } catch (e: IllegalStateException) {
            android.util.Log.e("MatchFragment", "Fragment not attached when generating demo profiles", e)
            return
        }
        
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
                username = "Sarah",
                age = 24,
                gender = "Female",
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
                username = "Alex",
                age = 28,
                gender = "Male",
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
                username = "Jordan",
                age = 26,
                gender = "Non-binary",
                interests = listOf("Science", "Non-fiction", "Cooking"),
                genres = listOf("Science", "Biography", "Memoir"),
                books = listOf(demoBooks[2], demoBooks[6], demoBooks[0]),
                createdAt = "2024-01-03",
                bio = "Sci-fi enthusiast and coffee lover.",
                currentlyReading = listOf(demoBooks[2]),
                favoriteBooks = listOf(demoBooks[6], demoBooks[0]),
                city = "Seattle"
            )
        ))
    }
    
    private fun showNextCard() {
        // Check if fragment is attached before accessing view/context
        if (!isAdded) {
            android.util.Log.w("MatchFragment", "Fragment not attached, cannot show next card")
            return
        }
        
        try {
            if (currentCardIndex >= demoProfiles.size) {
                // No more cards
                val noMoreText = TextView(requireContext()).apply {
                    text = "No more matches!\nCheck back later for new profiles."
                    textSize = 18f
                    setTextColor(resources.getColor(R.color.text_primary, null))
                    gravity = android.view.Gravity.CENTER
                }
                cardStack.removeAllViews()
                cardStack.addView(noMoreText)
                return
            }
            
            if (currentCardIndex < 0 || currentCardIndex >= demoProfiles.size) {
                android.util.Log.e("MatchFragment", "Invalid card index: $currentCardIndex, profiles size: ${demoProfiles.size}")
                return
            }
            
            val profile = demoProfiles[currentCardIndex]
            currentCard = createCardView(profile)
            cardStack.removeAllViews()
            cardStack.addView(currentCard)
            
            setupCardSwipe(currentCard!!)
        } catch (e: IllegalStateException) {
            android.util.Log.e("MatchFragment", "Fragment not attached in showNextCard", e)
        } catch (e: Exception) {
            android.util.Log.e("MatchFragment", "Error showing next card", e)
            e.printStackTrace()
        }
    }
    
    private fun createCardView(profile: UserProfile): CardView {
        val card = layoutInflater.inflate(R.layout.card_discovery, null, false) as CardView
        
        val photoView = card.findViewById<ImageView>(R.id.cardPhoto)
        val nameText = card.findViewById<TextView>(R.id.cardName)
        val ageText = card.findViewById<TextView>(R.id.cardAge)
        val bioText = card.findViewById<TextView>(R.id.cardBio)
        val genresText = card.findViewById<TextView>(R.id.cardGenres)
        
        // Photo
        try {
            android.util.Log.d("MatchFragment", "Loading photo for ${profile.username}, photoUri: ${profile.photoUri}")
            if (!profile.photoUri.isNullOrEmpty()) {
                // Handle Android resource URIs
                if (profile.photoUri.startsWith("android.resource://")) {
                    try {
                        val uri = android.net.Uri.parse(profile.photoUri)
                        val resourceName = uri.lastPathSegment
                        android.util.Log.d("MatchFragment", "Parsing Android resource URI, resourceName: $resourceName")
                        if (resourceName != null) {
                            val resourceId = resources.getIdentifier(resourceName, "drawable", requireContext().packageName)
                            android.util.Log.d("MatchFragment", "Resource ID for $resourceName: $resourceId")
                            if (resourceId != 0) {
                                Glide.with(this)
                                    .load(resourceId)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(photoView)
                                android.util.Log.d("MatchFragment", "Loaded photo from resource: $resourceName")
                            } else {
                                android.util.Log.w("MatchFragment", "Resource not found: $resourceName")
                                photoView.setImageResource(R.drawable.ic_profile_placeholder)
                            }
                        } else {
                            android.util.Log.w("MatchFragment", "Resource name is null")
                            photoView.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MatchFragment", "Error loading Android resource photo", e)
                        e.printStackTrace()
                        photoView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } else {
                    // Regular URI (file://, http://, https://, data:image, or content:// from image picker)
                    android.util.Log.d("MatchFragment", "Loading photo from URI: ${profile.photoUri}")
                    try {
                        val loadTarget = when {
                            profile.photoUri.startsWith("data:image") -> {
                                // Base64 data URI - decode and load
                                try {
                                    val base64Data = profile.photoUri.substringAfter(",")
                                    val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.NO_WRAP)
                                    imageBytes
                                } catch (e: Exception) {
                                    android.util.Log.e("MatchFragment", "Error decoding base64 image", e)
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
                                .into(photoView)
                        } else {
                            photoView.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MatchFragment", "Error loading photo from URI: ${profile.photoUri}", e)
                        e.printStackTrace()
                        photoView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                }
            } else {
                android.util.Log.w("MatchFragment", "Photo URI is null or empty for ${profile.username}")
                photoView.setImageResource(R.drawable.ic_profile_placeholder)
            }
        } catch (e: Exception) {
            android.util.Log.e("MatchFragment", "Error loading photo", e)
            e.printStackTrace()
            photoView.setImageResource(R.drawable.ic_profile_placeholder)
        }
        
        // Name and age
        val displayName = if (profile.username.isNotEmpty()) {
            profile.username
        } else {
            profile.gender
        }
        nameText.text = displayName
        ageText.text = "${profile.age}"
        
        // Bio
        bioText.text = profile.bio
        
        // Genres and books
        val booksText = profile.books.take(3).joinToString(", ") { it.title }
        genresText.text = "${profile.genres.joinToString(" • ")}\n📚 ${booksText}"
        
        return card
    }
    
    private fun setupCardSwipe(card: CardView) {
        var isSwipe = false
        
        card.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xDown = event.rawX
                    yDown = event.rawY
                    cardStartX = card.translationX
                    cardStartY = card.translationY
                    isSwipe = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - xDown
                    val dy = event.rawY - yDown
                    
                    card.translationX = cardStartX + dx
                    card.translationY = cardStartY + dy
                    card.rotation = dx * 0.1f
                    
                    // Show feedback
                    val feedbackText = view.rootView.findViewById<TextView>(R.id.swipeFeedback)
                    if (abs(dx) > 50) {
                        if (dx > 0) {
                            feedbackText?.text = "LIKE"
                            feedbackText?.setTextColor(resources.getColor(R.color.primary_cyan, null))
                        } else {
                            feedbackText?.text = "NOPE"
                            feedbackText?.setTextColor(resources.getColor(R.color.text_primary, null))
                        }
                        feedbackText?.visibility = View.VISIBLE
                    } else {
                        feedbackText?.visibility = View.GONE
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX - xDown
                    val threshold = 200f
                    val profile = demoProfiles.getOrNull(currentCardIndex)
                    val isDeveloper = profile?.id == "developer"
                    
                    // Prevent left swipe on developer card
                    if (isDeveloper && dx < -threshold) {
                        // Show red alert dialog and snap back
                        card.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .rotation(0f)
                            .setDuration(200)
                            .start()
                        
                        // Show red alert dialog
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("⚠️ Cannot Reject Developer")
                            .setMessage("You can't reject the developer, loser")
                            .setPositiveButton("OK", null)
                            .create()
                            .also { dialog ->
                                dialog.setOnShowListener {
                                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                                        .setTextColor(resources.getColor(R.color.error_red, null))
                                }
                                dialog.show()
                            }
                    } else if (abs(dx) > threshold) {
                        // Swipe detected (only right swipe for developer, both for others)
                        val isLike = dx > 0
                        handleSwipe(card, isLike)
                    } else {
                        // Snap back
                        card.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .rotation(0f)
                            .setDuration(200)
                            .start()
                        
                        val feedbackText = view.rootView.findViewById<TextView>(R.id.swipeFeedback)
                        feedbackText?.visibility = View.GONE
                        
                        // If not a swipe, treat as click - expand card
                        if (!isSwipe) {
                            val profile = demoProfiles.getOrNull(currentCardIndex)
                            if (profile != null) {
                                val intent = android.content.Intent(requireContext(), com.binder.profile.CardViewActivity::class.java).apply {
                                    putExtra("profile", profile)
                                }
                                startActivity(intent)
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun handleSwipe(card: CardView, isLike: Boolean) {
        try {
            if (currentCardIndex < 0 || currentCardIndex >= demoProfiles.size) {
                android.util.Log.e("MatchFragment", "Invalid card index in handleSwipe: $currentCardIndex")
                return
            }
            
            val profile = demoProfiles[currentCardIndex]
            
            // Animate card off screen
            val targetX = if (isLike) 1000f else -1000f
            card.animate()
                .translationX(targetX)
                .rotation(if (isLike) 30f else -30f)
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        try {
                            if (isLike) {
                                // Save like and check for mutual match
                                val currentUser = currentUserProfile
                                if (currentUser != null) {
                                    lifecycleScope.launch {
                                        try {
                                            // Save the like
                                            val likeResult = com.binder.utils.SupabaseService.saveLike(
                                                likerId = currentUser.id,
                                                likedId = profile.id
                                            )
                                            
                                            if (likeResult.isSuccess) {
                                                android.util.Log.d("MatchFragment", "Like saved: ${currentUser.id} likes ${profile.id}")
                                                
                                                // Check if mutual like (bindered!)
                                                val mutualResult = com.binder.utils.SupabaseService.checkMutualLike(
                                                    likerId = currentUser.id,
                                                    likedId = profile.id
                                                )
                                                
                                                mutualResult.onSuccess { isMutual ->
                                                    if (isMutual) {
                                                        // BOTH LIKED EACH OTHER - BINDERED!
                                                        val matchResult = com.binder.utils.SupabaseService.createMatch(
                                                            user1Id = currentUser.id,
                                                            user2Id = profile.id
                                                        )
                                                        
                                                        matchResult.onSuccess {
                                                            showBinderedDialog(profile)
                                                        }.onFailure { e ->
                                                            android.util.Log.e("MatchFragment", "Error creating match", e)
                                                        }
                                                    } else {
                                                        // Only current user liked - other user will be notified
                                                        android.util.Log.d("MatchFragment", "Like sent. ${profile.username} will be notified if they check.")
                                                    }
                                                }.onFailure { e ->
                                                    android.util.Log.e("MatchFragment", "Error checking mutual like", e)
                                                }
                                            } else {
                                                android.util.Log.e("MatchFragment", "Failed to save like")
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("MatchFragment", "Error processing like", e)
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                            currentCardIndex++
                            showNextCard()
                        } catch (e: Exception) {
                            android.util.Log.e("MatchFragment", "Error in swipe animation callback", e)
                            e.printStackTrace()
                        }
                    }
                })
                .start()
        } catch (e: Exception) {
            android.util.Log.e("MatchFragment", "Error handling swipe", e)
            e.printStackTrace()
        }
    }
    
    private fun calculateMatchScore(profile: UserProfile): Int {
        val currentUser = currentUserProfile ?: return 0
        var score = 0
        
        // Genre overlap
        val genreOverlap = currentUser.genres.intersect(profile.genres.toSet()).size
        score += genreOverlap * 15
        
        // Book overlap
        val bookOverlap = currentUser.books.intersect(profile.books.toSet()).size
        score += bookOverlap * 20
        
        // Interest overlap
        val interestOverlap = currentUser.interests.intersect(profile.interests.toSet()).size
        score += interestOverlap * 10
        
        return score.coerceIn(0, 100)
    }
    
    private fun showBinderedDialog(profile: UserProfile) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎉 BINDERED! 🎉")
            .setMessage("${profile.username} liked you back! You're now bindered. Start chatting!")
            .setPositiveButton("Awesome!") { dialog, _ -> dialog.dismiss() }
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.primary_cyan, null))
                }
                dialog.show()
            }
    }
    
    private fun showMatchDialog(profile: UserProfile, score: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("It's a Match! 🎉")
            .setMessage("You and ${profile.username} have a ${score}% compatibility score!")
            .setPositiveButton("Nice!") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
