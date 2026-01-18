package com.binder.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.binder.R
import com.binder.discovery.DiscoveryActivity
import com.binder.profile.CardViewActivity
import com.binder.models.UserProfile
import com.binder.utils.ProfileManager

class Step6SuccessFragment : Fragment() {
    
    private lateinit var activity: OnboardingActivity
    private lateinit var successText: TextView
    private lateinit var viewCardButton: Button
    private lateinit var startBinderingButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step6_success, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as OnboardingActivity
        
        successText = view.findViewById(R.id.successText)
        viewCardButton = view.findViewById(R.id.viewCardButton)
        startBinderingButton = view.findViewById(R.id.startBinderingButton)
        
        // Animate success text
        val successAnim = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }
        successText.startAnimation(successAnim)
        
        // Start Bindering button is always visible, animate it in
        startBinderingButton.alpha = 1f
        val startButtonAnim = AlphaAnimation(0f, 1f).apply {
            duration = 800
            fillAfter = true
            startOffset = 500
        }
        startBinderingButton.startAnimation(startButtonAnim)
        
        // Animate view card button after delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val buttonAnim = AlphaAnimation(0f, 1f).apply {
                duration = 500
                fillAfter = true
            }
            viewCardButton.startAnimation(buttonAnim)
        }, 1000)
        
        // Primary action - Start Bindering (go to matches)
        startBinderingButton.setOnClickListener {
            // Navigate directly to main screen (which shows matches)
            activity.goToMainScreen()
        }
        
        viewCardButton.setOnClickListener {
            val profile = ProfileManager.getProfile(requireContext())
            profile?.let { p ->
                val intent = Intent(requireContext(), CardViewActivity::class.java).apply {
                    putExtra("profile", p)
                }
                startActivity(intent)
            }
        }
    }
}
