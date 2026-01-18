package com.binder.onboarding

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.binder.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class Step1BasicInfoFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var ageInput: EditText
    private lateinit var genderGroup: ChipGroup
    private lateinit var interestsGroup: ChipGroup
    private lateinit var nextButton: Button
    
    private val genders = listOf("Male", "Female", "Non-binary", "Prefer not to say")
    private val interests = listOf(
        "Fiction", "Non-fiction", "Poetry", "Biography", "History",
        "Science", "Philosophy", "Art", "Music", "Travel", "Cooking", "Sports"
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step1, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        ageInput = view.findViewById(R.id.ageInput)
        genderGroup = view.findViewById(R.id.genderGroup)
        interestsGroup = view.findViewById(R.id.interestsGroup)
        nextButton = view.findViewById(R.id.nextButton)
        
        // Add focus listener to change EditText background
        ageInput.setOnFocusChangeListener { _, hasFocus ->
            ageInput.background = resources.getDrawable(
                if (hasFocus) R.drawable.edit_text_background_focused
                else R.drawable.edit_text_background,
                null
            )
        }
        
        // Set up gender chips with Tinder-like styling
        genders.forEach { gender ->
            val chip = Chip(context).apply {
                text = gender
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
                setPadding(24, 16, 24, 16)
                textSize = 15f
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Uncheck others
                    for (i in 0 until genderGroup.childCount) {
                        val child = genderGroup.getChildAt(i) as? Chip
                        if (child != chip) {
                            child?.isChecked = false
                        }
                    }
                }
            }
            genderGroup.addView(chip)
        }
        
        // Set up interest chips with Tinder-like styling
        interests.forEach { interest ->
            val chip = Chip(context).apply {
                text = interest
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
                setPadding(20, 12, 20, 12)
                textSize = 14f
            }
            interestsGroup.addView(chip)
        }
        
        nextButton.setOnClickListener {
            validateAndProceed()
        }
    }
    
    private fun validateAndProceed() {
        val ageText = ageInput.text.toString()
        val age = ageText.toIntOrNull()
        
        if (age == null || age < 13 || age > 120) {
            Toast.makeText(context, "Please enter a valid age (13-120)", Toast.LENGTH_SHORT).show()
            return
        }
        
        val selectedGender = genderGroup.checkedChipId.let { id ->
            if (id != View.NO_ID) {
                (genderGroup.findViewById<Chip>(id)?.text?.toString())
            } else null
        }
        
        if (selectedGender == null) {
            Toast.makeText(context, "Please select your gender", Toast.LENGTH_SHORT).show()
            return
        }
        
        val selectedInterests = mutableListOf<String>()
        for (i in 0 until interestsGroup.childCount) {
            val chip = interestsGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                selectedInterests.add(chip.text.toString())
            }
        }
        
        if (selectedInterests.isEmpty()) {
            Toast.makeText(context, "Please select at least one interest", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Update form data
        activity.updateAge(age)
        activity.updateGender(selectedGender)
        activity.updateInterests(selectedInterests)
        
        // Proceed to next step
        activity.goToNextStep()
    }
}
