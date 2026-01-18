package com.binder.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.binder.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class Step3InterestsFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var interestsGroup: ChipGroup
    private lateinit var nextButton: Button
    private lateinit var questionText: TextView
    
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
        return inflater.inflate(R.layout.fragment_step3_interests, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        questionText = view.findViewById(R.id.questionText)
        interestsGroup = view.findViewById(R.id.interestsGroup)
        nextButton = view.findViewById(R.id.nextButton)
        
        // Animate question
        val questionAnim = AlphaAnimation(0f, 1f).apply {
            duration = 800
            fillAfter = true
        }
        questionText.startAnimation(questionAnim)
        
        // Next button starts visible but disabled
        nextButton.isEnabled = false
        
        // Set up genre chips
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
            chip.setOnCheckedChangeListener { _, _ ->
                checkCanProceed()
            }
            interestsGroup.addView(chip)
        }
        
        nextButton.setOnClickListener {
            val selectedGenres = mutableListOf<String>()
            for (i in 0 until interestsGroup.childCount) {
                val chip = interestsGroup.getChildAt(i) as? Chip
                if (chip?.isChecked == true) {
                    selectedGenres.add(chip.text.toString())
                }
            }
            if (selectedGenres.isNotEmpty()) {
                activity.updateGenres(selectedGenres)
                activity.goToNextStep()
            }
        }
    }
    
    private fun checkCanProceed() {
        var hasSelection = false
        for (i in 0 until interestsGroup.childCount) {
            val chip = interestsGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                hasSelection = true
                break
            }
        }
        nextButton.isEnabled = hasSelection
    }
}
