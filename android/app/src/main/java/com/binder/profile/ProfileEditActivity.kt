package com.binder.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.binder.R
import com.binder.models.Book
import com.binder.models.UserProfile
import com.binder.utils.ProfileManager
import com.bumptech.glide.Glide

class ProfileEditActivity : AppCompatActivity() {
    
    private lateinit var photoImageView: ImageView
    private lateinit var bioInput: EditText
    private lateinit var currentlyReadingContainer: LinearLayout
    private lateinit var favoriteBooksContainer: LinearLayout
    private lateinit var addCurrentlyReadingButton: Button
    private lateinit var addFavoriteBookButton: Button
    private lateinit var saveButton: Button
    private lateinit var viewCardButton: Button
    
    private var profile: UserProfile? = null
    private var photoUri: Uri? = null
    private val currentlyReadingBooks = mutableListOf<Book>()
    private val favoriteBooks = mutableListOf<Book>()
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                photoUri = uri
                Glide.with(this)
                    .load(uri)
                    .into(photoImageView)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)
        
        // Load existing profile
        profile = ProfileManager.getProfile(this)
        if (profile == null) {
            // If no profile exists, go back to onboarding
            finish()
            return
        }
        
        initializeViews()
        loadProfileData()
        setupListeners()
    }
    
    private fun initializeViews() {
        photoImageView = findViewById(R.id.photoImageView)
        bioInput = findViewById(R.id.bioInput)
        currentlyReadingContainer = findViewById(R.id.currentlyReadingContainer)
        favoriteBooksContainer = findViewById(R.id.favoriteBooksContainer)
        addCurrentlyReadingButton = findViewById(R.id.addCurrentlyReadingButton)
        addFavoriteBookButton = findViewById(R.id.addFavoriteBookButton)
        saveButton = findViewById(R.id.saveButton)
        viewCardButton = findViewById(R.id.viewCardButton)
    }
    
    private fun loadProfileData() {
        profile?.let { p ->
            bioInput.setText(p.bio)
            currentlyReadingBooks.clear()
            currentlyReadingBooks.addAll(p.currentlyReading)
            favoriteBooks.clear()
            favoriteBooks.addAll(p.favoriteBooks)
            
            // Load photo if exists
            if (!p.photoUri.isNullOrEmpty()) {
                photoUri = Uri.parse(p.photoUri)
                Glide.with(this)
                    .load(p.photoUri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(photoImageView)
            }
            
            updateCurrentlyReadingUI()
            updateFavoriteBooksUI()
        }
    }
    
    private fun setupListeners() {
        photoImageView.setOnClickListener {
            openImagePicker()
        }
        
        addCurrentlyReadingButton.setOnClickListener {
            // Open book search (reuse Step3 search functionality)
            val intent = Intent(this, BookSearchActivity::class.java).apply {
                putExtra("mode", "currently_reading")
            }
            startActivityForResult(intent, REQUEST_ADD_CURRENTLY_READING)
        }
        
        addFavoriteBookButton.setOnClickListener {
            val intent = Intent(this, BookSearchActivity::class.java).apply {
                putExtra("mode", "favorite")
            }
            startActivityForResult(intent, REQUEST_ADD_FAVORITE)
        }
        
        saveButton.setOnClickListener {
            saveProfile()
        }
        
        viewCardButton.setOnClickListener {
            viewCard()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            @Suppress("DEPRECATION")
            val book = data.getSerializableExtra("book") as? Book
            if (book != null) {
                when (requestCode) {
                    REQUEST_ADD_CURRENTLY_READING -> {
                        if (!currentlyReadingBooks.any { it.id == book.id }) {
                            currentlyReadingBooks.add(book)
                            updateCurrentlyReadingUI()
                        }
                    }
                    REQUEST_ADD_FAVORITE -> {
                        if (!favoriteBooks.any { it.id == book.id }) {
                            favoriteBooks.add(book)
                            updateFavoriteBooksUI()
                        }
                    }
                }
            }
        }
    }
    
    private fun updateCurrentlyReadingUI() {
        currentlyReadingContainer.removeAllViews()
        currentlyReadingBooks.forEach { book ->
            val bookView = layoutInflater.inflate(R.layout.item_profile_book, currentlyReadingContainer, false)
            val titleText = bookView.findViewById<TextView>(R.id.bookTitle)
            val authorText = bookView.findViewById<TextView>(R.id.bookAuthor)
            val removeButton = bookView.findViewById<Button>(R.id.removeButton)
            
            titleText.text = book.title
            authorText.text = "by ${book.author}"
            
            removeButton.setOnClickListener {
                currentlyReadingBooks.remove(book)
                updateCurrentlyReadingUI()
            }
            
            currentlyReadingContainer.addView(bookView)
        }
    }
    
    private fun updateFavoriteBooksUI() {
        favoriteBooksContainer.removeAllViews()
        favoriteBooks.forEach { book ->
            val bookView = layoutInflater.inflate(R.layout.item_profile_book, favoriteBooksContainer, false)
            val titleText = bookView.findViewById<TextView>(R.id.bookTitle)
            val authorText = bookView.findViewById<TextView>(R.id.bookAuthor)
            val removeButton = bookView.findViewById<Button>(R.id.removeButton)
            
            titleText.text = book.title
            authorText.text = "by ${book.author}"
            
            removeButton.setOnClickListener {
                favoriteBooks.remove(book)
                updateFavoriteBooksUI()
            }
            
            favoriteBooksContainer.addView(bookView)
        }
    }
    
    private fun saveProfile() {
        val bio = bioInput.text.toString().trim()
        
        val updatedProfile = profile!!.copy(
            bio = bio,
            currentlyReading = currentlyReadingBooks,
            favoriteBooks = favoriteBooks,
            photoUri = photoUri?.toString()
        )
        
        ProfileManager.saveProfile(this, updatedProfile)
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
    }
    
    private fun viewCard() {
        val currentProfile = profile?.copy(
            bio = bioInput.text.toString().trim(),
            currentlyReading = currentlyReadingBooks,
            favoriteBooks = favoriteBooks,
            photoUri = photoUri?.toString()
        ) ?: return
        
        val intent = Intent(this, CardViewActivity::class.java).apply {
            putExtra("profile", currentProfile)
        }
        startActivity(intent)
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    companion object {
        private const val REQUEST_ADD_CURRENTLY_READING = 1001
        private const val REQUEST_ADD_FAVORITE = 1002
    }
}
