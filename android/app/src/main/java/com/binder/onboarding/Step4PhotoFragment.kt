package com.binder.onboarding

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.binder.R
import com.bumptech.glide.Glide

class Step4PhotoFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var photoImageView: ImageView
    private lateinit var nextButton: Button
    private lateinit var questionText: TextView
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
                
                // Enable next button
                nextButton.isEnabled = true
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step4_photo, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        questionText = view.findViewById(R.id.questionText)
        photoImageView = view.findViewById(R.id.photoImageView)
        nextButton = view.findViewById(R.id.nextButton)
        
        // Animate question
        val questionAnim = AlphaAnimation(0f, 1f).apply {
            duration = 800
            fillAfter = true
        }
        questionText.startAnimation(questionAnim)
        
        // Next button starts visible but disabled
        nextButton.isEnabled = false
        
        photoImageView.setOnClickListener {
            openImagePicker()
        }
        
        nextButton.setOnClickListener {
            activity.updatePhotoUri(photoUri?.toString())
            activity.goToNextStep()
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
}
