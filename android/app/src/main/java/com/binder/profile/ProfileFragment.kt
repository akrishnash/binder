package com.binder.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.binder.R
import com.binder.models.UserProfile
import com.binder.utils.ProfileManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private lateinit var photoImageView: ImageView
    private lateinit var nameText: TextView
    private lateinit var ageText: TextView
    private lateinit var cityText: TextView
    private lateinit var bioText: TextView
    private lateinit var genresText: TextView
    private lateinit var interestsText: TextView
    private lateinit var editButton: Button
    private lateinit var viewCardButton: Button
    private lateinit var viewDeveloperCardButton: Button
    private lateinit var currentlyReadingContainer: LinearLayout
    private lateinit var favoriteBooksContainer: LinearLayout
    
    private var profile: UserProfile? = null
    private var photoUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                photoUri = uri
                Glide.with(this)
                    .load(uri)
                    .into(photoImageView)
                
                // Save photo URI to profile (will be uploaded to Supabase Storage on save)
                profile?.let { p ->
                    val updatedProfile = p.copy(photoUri = uri.toString())
                    ProfileManager.saveProfile(requireContext(), updatedProfile, syncToSupabase = true)
                    profile = updatedProfile
                }
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Load profile from local cache first (fast)
        profile = ProfileManager.getProfile(requireContext())
        
        if (profile == null) {
            // Should not happen, but handle gracefully
            return
        }
        
        initializeViews(view)
        loadProfileData()
        setupListeners()
        
        // Reload from Supabase to get latest data (e.g., if name was changed in database)
        refreshProfileFromSupabase()
    }
    
    private fun refreshProfileFromSupabase() {
        val currentProfile = profile ?: return
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("ProfileFragment", "🔄 Refreshing profile from Supabase...")
                android.util.Log.d("ProfileFragment", "   Current profile ID: ${currentProfile.id}")
                android.util.Log.d("ProfileFragment", "   Current username: ${currentProfile.username}")
                
                val result = com.binder.utils.SupabaseService.getProfile(currentProfile.id)
                result.onSuccess { updatedProfile ->
                    if (updatedProfile != null) {
                        android.util.Log.d("ProfileFragment", "✅ Profile fetched from Supabase!")
                        android.util.Log.d("ProfileFragment", "   New username: ${updatedProfile.username}")
                        android.util.Log.d("ProfileFragment", "   New age: ${updatedProfile.age}")
                        
                        if (isAdded) {
                            // Update local cache with latest data
                            com.binder.utils.ProfileManager.saveProfile(requireContext(), updatedProfile, syncToSupabase = false)
                            profile = updatedProfile
                            loadProfileData() // Reload UI with updated data
                            android.util.Log.d("ProfileFragment", "✅ UI updated with latest data from Supabase!")
                        }
                    } else {
                        android.util.Log.w("ProfileFragment", "⚠️ Profile not found in Supabase (null returned)")
                    }
                }.onFailure { e ->
                    android.util.Log.e("ProfileFragment", "❌ Failed to refresh profile from Supabase", e)
                    e.printStackTrace()
                    android.util.Log.e("ProfileFragment", "   Error: ${e.message}")
                    // Continue with cached profile on error
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "❌ Exception refreshing profile", e)
                e.printStackTrace()
                // Continue with cached profile on error
            }
        }
    }
    
    private fun initializeViews(view: View) {
        photoImageView = view.findViewById(R.id.profilePhoto)
        nameText = view.findViewById(R.id.profileName)
        ageText = view.findViewById(R.id.profileAge)
        cityText = view.findViewById(R.id.profileCity)
        bioText = view.findViewById(R.id.profileBio)
        genresText = view.findViewById(R.id.profileGenres)
        interestsText = view.findViewById(R.id.profileInterests)
        editButton = view.findViewById(R.id.editProfileButton)
        viewCardButton = view.findViewById(R.id.viewCardButton)
        viewDeveloperCardButton = view.findViewById(R.id.viewDeveloperCardButton)
        currentlyReadingContainer = view.findViewById(R.id.currentlyReadingContainer)
        favoriteBooksContainer = view.findViewById(R.id.favoriteBooksContainer)
    }
    
    private fun loadProfileData() {
        val view = view ?: return
        profile?.let { p ->
            // Photo - handle all URI types (content://, http://, https://, android.resource://)
            android.util.Log.d("ProfileFragment", "Loading photo for ${p.username}, photoUri: ${p.photoUri}")
            if (!p.photoUri.isNullOrEmpty()) {
                try {
                    // Handle Android resource URIs
                    if (p.photoUri.startsWith("android.resource://")) {
                        val uri = android.net.Uri.parse(p.photoUri)
                        val resourceName = uri.lastPathSegment
                        android.util.Log.d("ProfileFragment", "Parsing Android resource URI, resourceName: $resourceName")
                        if (resourceName != null) {
                            val resourceId = resources.getIdentifier(resourceName, "drawable", requireContext().packageName)
                            android.util.Log.d("ProfileFragment", "Resource ID for $resourceName: $resourceId")
                            if (resourceId != 0) {
                                Glide.with(this)
                                    .load(resourceId)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(photoImageView)
                                android.util.Log.d("ProfileFragment", "Loaded photo from resource: $resourceName")
                            } else {
                                android.util.Log.w("ProfileFragment", "Resource not found: $resourceName")
                                photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                            }
                        } else {
                            android.util.Log.w("ProfileFragment", "Resource name is null")
                            photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    } else {
                        // Regular URI (file://, http://, https://, or content:// from image picker)
                        android.util.Log.d("ProfileFragment", "Loading photo from URI: ${p.photoUri}")
                        try {
                            // For HTTP/HTTPS URLs, Glide can load directly from string
                            // For content:// and file://, parse as Uri
                            val loadTarget = when {
                                p.photoUri.startsWith("data:image") -> {
                                    // Base64 data URI - decode and load
                                    try {
                                        val base64Data = p.photoUri.substringAfter(",")
                                        val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.NO_WRAP)
                                        imageBytes
                                    } catch (e: Exception) {
                                        android.util.Log.e("ProfileFragment", "Error decoding base64 image", e)
                                        null
                                    }
                                }
                                p.photoUri.startsWith("http://") || p.photoUri.startsWith("https://") -> {
                                    p.photoUri // Direct URL string for Glide
                                }
                                p.photoUri.startsWith("file://") -> {
                                    // Handle file:// URIs (local storage fallback)
                                    val file = java.io.File(android.net.Uri.parse(p.photoUri).path ?: "")
                                    if (file.exists()) {
                                        android.net.Uri.fromFile(file)
                                    } else {
                                        android.util.Log.w("ProfileFragment", "File does not exist: ${p.photoUri}")
                                        null
                                    }
                                }
                                else -> {
                                    android.net.Uri.parse(p.photoUri) // Parse as Uri for content://
                                }
                            }
                            
                            if (loadTarget != null) {
                                Glide.with(this)
                                    .load(loadTarget)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(photoImageView)
                                android.util.Log.d("ProfileFragment", "Photo loaded from URI: ${p.photoUri}")
                            } else {
                                android.util.Log.w("ProfileFragment", "Could not create load target for: ${p.photoUri}")
                                photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ProfileFragment", "Error loading photo URI: ${p.photoUri}", e)
                            photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ProfileFragment", "Error loading photo: ${p.photoUri}", e)
                    e.printStackTrace()
                    photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
                }
            } else {
                android.util.Log.w("ProfileFragment", "Photo URI is null or empty")
                photoImageView.setImageResource(R.drawable.ic_profile_placeholder)
            }
            
            // Name (username or gender) and age
            val displayName = if (p.username.isNotEmpty()) {
                p.username
            } else {
                p.gender
            }
            nameText.text = displayName
            ageText.text = "${p.age}"
            
            // City
            if (p.city.isNotEmpty()) {
                cityText.text = p.city
                cityText.visibility = View.VISIBLE
            } else {
                cityText.visibility = View.GONE
            }
            
            // Bio
            if (p.bio.isNotEmpty()) {
                bioText.text = p.bio
                bioText.visibility = View.VISIBLE
            } else {
                bioText.visibility = View.GONE
            }
            
            // Genres
            genresText.text = p.genres.joinToString(" • ")
            
            // Interests
            interestsText.text = p.interests.joinToString(" • ")
            
            // Currently Reading
            currentlyReadingContainer.removeAllViews()
            val currentlyReadingLabel = view.findViewById<TextView>(R.id.currentlyReadingLabel)
            if (p.currentlyReading.isNotEmpty()) {
                currentlyReadingLabel.visibility = View.VISIBLE
                p.currentlyReading.forEach { book ->
                    addBookView(book, currentlyReadingContainer)
                }
            } else {
                currentlyReadingLabel.visibility = View.GONE
            }
            
            // Favorite Books
            favoriteBooksContainer.removeAllViews()
            val favoriteBooksLabel = view.findViewById<TextView>(R.id.favoriteBooksLabel)
            if (p.favoriteBooks.isNotEmpty()) {
                favoriteBooksLabel.visibility = View.VISIBLE
                p.favoriteBooks.forEach { book ->
                    addBookView(book, favoriteBooksContainer)
                }
            } else {
                favoriteBooksLabel.visibility = View.GONE
            }
        }
    }
    
    private fun addBookView(book: com.binder.models.Book, container: LinearLayout) {
        val bookView = layoutInflater.inflate(R.layout.item_card_book, container, false)
        val titleText = bookView.findViewById<TextView>(R.id.bookTitle)
        val authorText = bookView.findViewById<TextView>(R.id.bookAuthor)
        
        titleText.text = book.title
        authorText.text = "by ${book.author}"
        
        container.addView(bookView)
    }
    
    private fun setupListeners() {
        // Photo click to upload
        photoImageView.setOnClickListener {
            openImagePicker()
        }
        
        // Edit Profile button
        editButton.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileEditActivity::class.java))
        }
        
        // View Card button
        viewCardButton.setOnClickListener {
            profile?.let { p ->
                val intent = Intent(requireContext(), CardViewActivity::class.java).apply {
                    putExtra("profile", p)
                }
                startActivity(intent)
            }
        }
        
        // View Developer Card button
        viewDeveloperCardButton.setOnClickListener {
            val devProfile = com.binder.utils.DeveloperProfile.getDeveloperProfile(requireContext())
            val intent = Intent(requireContext(), CardViewActivity::class.java).apply {
                putExtra("profile", devProfile)
            }
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Reload profile data when returning from edit screen
        profile = ProfileManager.getProfile(requireContext())
        profile?.let { 
            loadProfileData()
            // Refresh from Supabase to get latest data (in case it was changed in database)
            refreshProfileFromSupabase()
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
}
