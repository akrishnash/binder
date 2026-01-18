package com.binder.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.binder.R
import com.binder.models.Book
import com.binder.models.UserProfile
import com.binder.profile.CardViewActivity
import com.binder.utils.ProfileManager

class OnboardingActivity : AppCompatActivity() {
    
    private var currentStep = 1
    private val formData = OnboardingData()
    
    data class OnboardingData(
        var username: String = "",
        var age: Int? = null,
        var gender: String? = null,
        var interests: MutableList<String> = mutableListOf(),
        var genres: MutableList<String> = mutableListOf(),
        var books: MutableList<Book> = mutableListOf(),
        var photoUri: String? = null
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        showStep(1)
    }
    
    fun showStep(step: Int) {
        currentStep = step
        val fragment: Fragment = when (step) {
            1 -> Step1UsernameFragment()
            2 -> Step2AgeGenderFragment()
            3 -> Step3InterestsFragment()
            4 -> Step4PhotoFragment()
            5 -> Step5BooksFragment()
            6 -> Step6SuccessFragment()
            else -> Step1UsernameFragment()
        }
        
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
            setReorderingAllowed(true)
        }
    }
    
    fun updateUsername(username: String) {
        formData.username = username
    }
    
    fun updateAge(age: Int) {
        formData.age = age
    }
    
    fun updateGender(gender: String) {
        formData.gender = gender
    }
    
    fun updateInterests(interests: List<String>) {
        formData.interests.clear()
        formData.interests.addAll(interests)
    }
    
    fun updateGenres(genres: List<String>) {
        formData.genres.clear()
        formData.genres.addAll(genres)
        // Also set as interests for compatibility
        formData.interests.clear()
        formData.interests.addAll(genres)
    }
    
    fun updateBooks(books: List<Book>) {
        formData.books.clear()
        formData.books.addAll(books)
    }
    
    fun updatePhotoUri(photoUri: String?) {
        formData.photoUri = photoUri
    }
    
    fun getFormData(): OnboardingData = formData
    
    fun goToNextStep() {
        if (currentStep < 6) {
            showStep(currentStep + 1)
        } else {
            submitProfile()
        }
    }
    
    fun goToPreviousStep() {
        if (currentStep > 1) {
            showStep(currentStep - 1)
        }
    }
    
    fun submitProfile() {
        val profile = UserProfile(
            id = "local-${System.currentTimeMillis()}",
            username = formData.username,
            age = formData.age ?: 0,
            gender = formData.gender ?: "",
            interests = formData.interests,
            genres = formData.genres,
            books = formData.books,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                .format(java.util.Date()),
            photoUri = formData.photoUri
        )
        
        android.util.Log.d("OnboardingActivity", "Submitting profile: ${profile.id}, username: ${profile.username}")
        android.util.Log.d("OnboardingActivity", "Profile will be saved to Supabase database")
        
        // Save profile locally and sync to Supabase
        // NOTE: syncToSupabase=true ensures the profile is saved to the database
        ProfileManager.saveProfile(this, profile, syncToSupabase = true)
        
        // Show success screen (Step 6)
        showStep(6)
    }
    
    fun goToMainScreen() {
        // Ensure profile is saved before navigating
        val profile = ProfileManager.getProfile(this)
        if (profile == null) {
            // If somehow profile wasn't saved, save it now
            submitProfile()
        }
        
        val intent = Intent(this, com.binder.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
