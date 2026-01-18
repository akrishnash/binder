package com.binder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.binder.onboarding.OnboardingActivity
import com.binder.utils.ProfileManager

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        val logoText = findViewById<TextView>(R.id.logoText)
        val taglineText = findViewById<TextView>(R.id.taglineText)
        
        // Animate logo fade in
        val logoAnimation = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }
        logoText.startAnimation(logoAnimation)
        
        // Animate tagline fade in after delay
        Handler(Looper.getMainLooper()).postDelayed({
            val taglineAnimation = AlphaAnimation(0f, 1f).apply {
                duration = 800
                fillAfter = true
            }
            taglineText.startAnimation(taglineAnimation)
        }, 500)
        
        // Navigate to onboarding or main screen after animation
        Handler(Looper.getMainLooper()).postDelayed({
            val profile = ProfileManager.getProfile(this)
            if (profile == null) {
                // First time - go to onboarding
                startActivity(Intent(this, OnboardingActivity::class.java))
            } else {
                // Profile exists - go to main screen
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 2500)
    }
}
