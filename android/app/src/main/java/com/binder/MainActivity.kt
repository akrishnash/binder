package com.binder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.binder.discovery.DiscoveryActivity
import com.binder.discovery.MatchFragment
import com.binder.profile.ProfileFragment
import com.binder.tribes.TribesFragment
import com.binder.utils.ProfileManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNav: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if profile exists
        val profile = ProfileManager.getProfile(this)
        
        if (profile == null) {
            // First time - go to onboarding
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return
        }
        
        // Profile exists - show main screen
        setContentView(R.layout.activity_main_tabs)
        
        bottomNav = findViewById(R.id.bottomNavigation)
        
        // Set up bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_matches -> {
                    showFragment(MatchFragment())
                    true
                }
                R.id.nav_tribes -> {
                    showFragment(TribesFragment())
                    true
                }
                R.id.nav_profile -> {
                    showFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        
        // Show matches by default
        if (savedInstanceState == null) {
            showFragment(MatchFragment())
            bottomNav.selectedItemId = R.id.nav_matches
        }
    }
    
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
