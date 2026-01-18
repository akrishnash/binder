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

class Step1UsernameFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var usernameInput: EditText
    private lateinit var nextButton: Button
    private lateinit var greetingText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step1_username, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        greetingText = view.findViewById(R.id.greetingText)
        usernameInput = view.findViewById(R.id.usernameInput)
        nextButton = view.findViewById(R.id.nextButton)
        
        // Animate greeting
        val greetingAnim = AlphaAnimation(0f, 1f).apply {
            duration = 800
            fillAfter = true
        }
        greetingText.startAnimation(greetingAnim)
        
        // Next button starts visible but disabled
        nextButton.isEnabled = false
        
        // Show next button when text is entered
        usernameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasText = s?.toString()?.trim()?.isNotEmpty() == true
                if (hasText) {
                    // Enable the button
                    nextButton.isEnabled = true
                } else {
                    // Disable the button
                    nextButton.isEnabled = false
                }
            }
        })
        
        // Focus on input after a short delay to allow layout to settle
        usernameInput.post {
            usernameInput.requestFocus()
            // Show keyboard
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(usernameInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
        
        // Listen for keyboard visibility changes and scroll to keep input visible
        val rootView = activity.window.decorView.rootView
        val layoutListener = object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val heightDiff = rootView.rootView.height - rootView.height
                if (heightDiff > 200) { // Keyboard is visible (more than 200dp difference)
                    val scrollView = view.findViewById<android.widget.ScrollView>(R.id.scrollView)
                    scrollView?.post {
                        // Calculate scroll position to show input field above keyboard
                        val scrollY = usernameInput.top - 100 // Leave some space above input
                        scrollView.smoothScrollTo(0, scrollY.coerceAtLeast(0))
                    }
                }
            }
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        
        // Clean up listener when fragment is destroyed
        view.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: android.view.View) {}
            override fun onViewDetachedFromWindow(v: android.view.View) {
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            }
        })
        
        nextButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            if (username.isNotEmpty()) {
                // Hide keyboard
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(usernameInput.windowToken, 0)
                
                activity.updateUsername(username)
                activity.goToNextStep()
            }
        }
        
        // Also handle Enter key on keyboard
        usernameInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                val username = usernameInput.text.toString().trim()
                if (username.isNotEmpty()) {
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(usernameInput.windowToken, 0)
                    activity.updateUsername(username)
                    activity.goToNextStep()
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }
}
