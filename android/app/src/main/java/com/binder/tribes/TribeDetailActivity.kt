package com.binder.tribes

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.binder.R
import com.binder.models.UserProfile
import com.binder.utils.ProfileManager
import com.binder.utils.TribeService
import kotlinx.coroutines.launch

class TribeDetailActivity : AppCompatActivity() {
    
    private lateinit var bookTitleText: TextView
    private lateinit var bookAuthorText: TextView
    private lateinit var cityText: TextView
    private lateinit var statusText: TextView
    private lateinit var membersContainer: LinearLayout
    private lateinit var joinButton: Button
    private lateinit var sprintTimeText: TextView
    
    private var tribeId: String? = null
    private var currentUser: UserProfile? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tribe_detail)
        
        tribeId = intent.getStringExtra("tribe_id")
        currentUser = ProfileManager.getProfile(this)
        
        if (tribeId == null) {
            finish()
            return
        }
        
        initializeViews()
        loadTribeDetails()
    }
    
    private fun initializeViews() {
        bookTitleText = findViewById(R.id.bookTitleText)
        bookAuthorText = findViewById(R.id.bookAuthorText)
        cityText = findViewById(R.id.cityText)
        statusText = findViewById(R.id.statusText)
        membersContainer = findViewById(R.id.membersContainer)
        joinButton = findViewById(R.id.joinButton)
        sprintTimeText = findViewById(R.id.sprintTimeText)
        
        val bookTitle = intent.getStringExtra("book_title") ?: ""
        val bookAuthor = intent.getStringExtra("book_author") ?: ""
        val city = intent.getStringExtra("city") ?: ""
        val status = intent.getStringExtra("status") ?: "forming"
        
        bookTitleText.text = bookTitle
        bookAuthorText.text = "by $bookAuthor"
        cityText.text = "📍 $city"
        
        when (status) {
            "forming" -> {
                statusText.text = "🟡 Forming - Need more members"
                statusText.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
            }
            "active" -> {
                statusText.text = "🟢 Active 48-Hour Sprint"
                statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                joinButton.visibility = View.GONE
            }
            "completed" -> {
                statusText.text = "✅ Sprint Completed"
                statusText.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                joinButton.visibility = View.GONE
            }
        }
        
        joinButton.setOnClickListener {
            joinTribe()
        }
    }
    
    private fun loadTribeDetails() {
        val tribeId = this.tribeId ?: return
        
        lifecycleScope.launch {
            try {
                // Load members
                val membersResult = TribeService.getTribeMembers(tribeId)
                membersResult.onSuccess { memberIds ->
                    loadMemberProfiles(memberIds)
                    
                    // Check if current user is already a member
                    val isMember = memberIds.contains(currentUser?.id)
                    if (isMember) {
                        joinButton.visibility = View.GONE
                    } else {
                        joinButton.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TribeDetailActivity", "Error loading tribe details", e)
            }
        }
    }
    
    private fun loadMemberProfiles(memberIds: List<String>) {
        membersContainer.removeAllViews()
        
        lifecycleScope.launch {
            memberIds.forEach { memberId ->
                try {
                    val profileResult = com.binder.utils.SupabaseService.getProfile(memberId)
                    profileResult.onSuccess { profile ->
                        profile?.let { addMemberView(it) }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TribeDetailActivity", "Error loading member profile", e)
                }
            }
        }
    }
    
    private fun addMemberView(profile: UserProfile) {
        val memberView = layoutInflater.inflate(R.layout.item_tribe_member, membersContainer, false)
        
        val nameText = memberView.findViewById<TextView>(R.id.memberNameText)
        val ageText = memberView.findViewById<TextView>(R.id.memberAgeText)
        val photoImageView = memberView.findViewById<ImageView>(R.id.memberPhotoImageView)
        
        nameText.text = profile.username
        ageText.text = "${profile.age} years old"
        
        // Load photo
        if (!profile.photoUri.isNullOrEmpty()) {
            if (profile.photoUri.startsWith("data:image")) {
                // Base64 image
                val base64Data = profile.photoUri.substringAfter(",")
                val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                photoImageView.setImageBitmap(bitmap)
            } else {
                com.bumptech.glide.Glide.with(this)
                    .load(profile.photoUri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(photoImageView)
            }
        }
        
        membersContainer.addView(memberView)
    }
    
    private fun joinTribe() {
        val tribeId = this.tribeId ?: return
        val userId = currentUser?.id ?: return
        
        joinButton.isEnabled = false
        joinButton.text = "Joining..."
        
        lifecycleScope.launch {
            try {
                val result = TribeService.joinTribe(tribeId, userId)
                result.onSuccess {
                    Toast.makeText(this@TribeDetailActivity, "Successfully joined the tribe!", Toast.LENGTH_SHORT).show()
                    joinButton.visibility = View.GONE
                    loadTribeDetails() // Reload to show updated member list
                }.onFailure { e ->
                    Toast.makeText(this@TribeDetailActivity, "Failed to join tribe. Please try again.", Toast.LENGTH_SHORT).show()
                    joinButton.isEnabled = true
                    joinButton.text = "Join Tribe"
                    android.util.Log.e("TribeDetailActivity", "Error joining tribe", e)
                }
            } catch (e: Exception) {
                Toast.makeText(this@TribeDetailActivity, "Error joining tribe. Please try again.", Toast.LENGTH_SHORT).show()
                joinButton.isEnabled = true
                joinButton.text = "Join Tribe"
                android.util.Log.e("TribeDetailActivity", "Exception joining tribe", e)
            }
        }
    }
}
