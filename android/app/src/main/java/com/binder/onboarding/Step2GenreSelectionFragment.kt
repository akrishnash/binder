package com.binder.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.binder.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class Step2GenreSelectionFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var genresGroup: ChipGroup
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    
    private val genres = listOf(
        "Sci-Fi", "Noir", "Fantasy", "Mystery", "Romance", "Thriller",
        "Horror", "Historical Fiction", "Literary Fiction", "Young Adult",
        "Biography", "Memoir", "Poetry", "Philosophy", "Science", "History",
        "Art & Design", "Graphic Novels", "Comedy", "Drama"
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step2, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        genresGroup = view.findViewById(R.id.genresGroup)
        backButton = view.findViewById(R.id.backButton)
        nextButton = view.findViewById(R.id.nextButton)
        
        // Set up genre chips with Tinder-like styling
        genres.forEach { genre ->
            val chip = Chip(context).apply {
                text = genre
                isCheckable = true
                chipBackgroundColor = resources.getColorStateList(
                    R.color.chip_background_selector,
                    null
                )
                setTextColor(resources.getColorStateList(
                    R.color.chip_text_selector,
                    null
                ))
                chipStrokeWidth = 2f
                chipStrokeColor = resources.getColorStateList(
                    R.color.chip_border_selector,
                    null
                )
                setPadding(20, 14, 20, 14)
                textSize = 15f
            }
            genresGroup.addView(chip)
        }
        
        backButton.setOnClickListener {
            activity.goToPreviousStep()
        }
        
        nextButton.setOnClickListener {
            validateAndProceed()
        }
    }
    
    private fun validateAndProceed() {
        val selectedGenres = mutableListOf<String>()
        for (i in 0 until genresGroup.childCount) {
            val chip = genresGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                selectedGenres.add(chip.text.toString())
            }
        }
        
        if (selectedGenres.isEmpty()) {
            Toast.makeText(context, "Please select at least one genre", Toast.LENGTH_SHORT).show()
            return
        }
        
        activity.updateGenres(selectedGenres)
        activity.goToNextStep()
    }
}
