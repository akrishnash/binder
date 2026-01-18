package com.binder.onboarding

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.binder.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class Step2AgeGenderFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var ageInput: EditText
    private lateinit var genderGroup: ChipGroup
    private lateinit var nextButton: Button
    private lateinit var questionText: TextView
    
    private val genders = listOf("Male", "Female", "Non-binary", "Prefer not to say")
    private var selectedGender: String? = null
    private var age: Int? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step2_age_gender, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        questionText = view.findViewById(R.id.questionText)
        ageInput = view.findViewById(R.id.ageInput)
        genderGroup = view.findViewById(R.id.genderGroup)
        nextButton = view.findViewById(R.id.nextButton)
        
        // Animate question
        val questionAnim = AlphaAnimation(0f, 1f).apply {
            duration = 800
            fillAfter = true
        }
        questionText.startAnimation(questionAnim)
        
        // Next button starts visible but disabled
        nextButton.isEnabled = false
        
        // Set up gender chips
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
                textSize = 16f
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedGender = gender
                    // Uncheck others
                    for (i in 0 until genderGroup.childCount) {
                        val child = genderGroup.getChildAt(i) as? Chip
                        if (child != chip) {
                            child?.isChecked = false
                        }
                    }
                    checkCanProceed()
                }
            }
            genderGroup.addView(chip)
        }
        
        // Age input listener
        ageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                age = s?.toString()?.toIntOrNull()
                checkCanProceed()
            }
        })
        
        ageInput.requestFocus()
        
        nextButton.setOnClickListener {
            if (age != null && selectedGender != null) {
                activity.updateAge(age!!)
                activity.updateGender(selectedGender!!)
                activity.goToNextStep()
            }
        }
    }
    
    private fun checkCanProceed() {
        val canProceed = age != null && selectedGender != null
        nextButton.isEnabled = canProceed
    }
}
