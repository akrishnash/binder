package com.binder.tribes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.binder.R
import com.binder.models.UserProfile
import com.binder.utils.ProfileManager
import com.binder.utils.TribeService
import kotlinx.coroutines.launch

class TribesFragment : Fragment() {
    
    private lateinit var tribesContainer: LinearLayout
    private lateinit var emptyStateText: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var refreshButton: Button
    
    private var currentUser: UserProfile? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tribes, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        currentUser = ProfileManager.getProfile(requireContext())
        
        tribesContainer = view.findViewById(R.id.tribesContainer)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        refreshButton = view.findViewById(R.id.refreshButton)
        
        refreshButton.setOnClickListener {
            loadTribes()
        }
        
        // Load tribes when fragment is created
        loadTribes()
        
        // Check for notifications
        checkNotifications()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload tribes when returning to this fragment
        loadTribes()
    }
    
    private fun loadTribes() {
        val user = currentUser ?: return
        
        loadingIndicator.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE
        tribesContainer.removeAllViews()
        
        lifecycleScope.launch {
            try {
                val result = TribeService.getUserTribes(user.id)
                result.onSuccess { tribes ->
                    loadingIndicator.visibility = View.GONE
                    
                    if (tribes.isEmpty()) {
                        emptyStateText.visibility = View.VISIBLE
                        emptyStateText.text = "No tribes yet. Start reading a book and we'll notify you when a tribe forms!"
                    } else {
                        emptyStateText.visibility = View.GONE
                        tribes.forEach { tribe ->
                            addTribeView(tribe)
                        }
                    }
                }.onFailure { e ->
                    loadingIndicator.visibility = View.GONE
                    emptyStateText.visibility = View.VISIBLE
                    emptyStateText.text = "Error loading tribes. Please try again."
                    android.util.Log.e("TribesFragment", "Error loading tribes", e)
                }
            } catch (e: Exception) {
                loadingIndicator.visibility = View.GONE
                emptyStateText.visibility = View.VISIBLE
                emptyStateText.text = "Error loading tribes. Please try again."
                android.util.Log.e("TribesFragment", "Exception loading tribes", e)
            }
        }
    }
    
    private fun addTribeView(tribe: TribeService.TribeRow) {
        val tribeView = layoutInflater.inflate(R.layout.item_tribe, tribesContainer, false)
        
        val bookTitleText = tribeView.findViewById<TextView>(R.id.bookTitleText)
        val bookAuthorText = tribeView.findViewById<TextView>(R.id.bookAuthorText)
        val cityText = tribeView.findViewById<TextView>(R.id.cityText)
        val statusText = tribeView.findViewById<TextView>(R.id.statusText)
        val membersCountText = tribeView.findViewById<TextView>(R.id.membersCountText)
        val viewButton = tribeView.findViewById<Button>(R.id.viewTribeButton)
        
        bookTitleText.text = tribe.book_title
        bookAuthorText.text = "by ${tribe.book_author}"
        cityText.text = "📍 ${tribe.city}"
        
        when (tribe.status) {
            "forming" -> {
                statusText.text = "🟡 Forming"
                statusText.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
            }
            "active" -> {
                statusText.text = "🟢 Active Sprint"
                statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            }
            "completed" -> {
                statusText.text = "✅ Completed"
                statusText.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            }
            else -> {
                statusText.text = tribe.status
            }
        }
        
        // Load member count
        lifecycleScope.launch {
            val membersResult = TribeService.getTribeMembers(tribe.id ?: "")
            membersResult.onSuccess { members ->
                membersCountText.text = "${members.size} members"
            }
        }
        
        viewButton.setOnClickListener {
            val intent = Intent(requireContext(), TribeDetailActivity::class.java).apply {
                putExtra("tribe_id", tribe.id)
                putExtra("book_title", tribe.book_title)
                putExtra("book_author", tribe.book_author)
                putExtra("city", tribe.city)
                putExtra("status", tribe.status)
            }
            startActivity(intent)
        }
        
        tribesContainer.addView(tribeView)
    }
    
    private fun checkNotifications() {
        val user = currentUser ?: return
        
        lifecycleScope.launch {
            try {
                val result = TribeService.getUserNotifications(user.id)
                result.onSuccess { notifications ->
                    val unreadTribeNotifications = notifications.filter { 
                        !it.read && (it.type == "tribe_forming" || it.type == "tribe_ready")
                    }
                    
                    if (unreadTribeNotifications.isNotEmpty()) {
                        // Show notification banner or dialog
                        showNotificationBanner(unreadTribeNotifications.first())
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TribesFragment", "Error checking notifications", e)
            }
        }
    }
    
    private fun showNotificationBanner(notification: TribeService.NotificationRow) {
        val banner = layoutInflater.inflate(R.layout.notification_banner, tribesContainer, false)
        val messageText = banner.findViewById<TextView>(R.id.notificationMessage)
        val actionButton = banner.findViewById<Button>(R.id.notificationActionButton)
        val dismissButton = banner.findViewById<Button>(R.id.dismissButton)
        
        messageText.text = notification.message
        
        actionButton.setOnClickListener {
            // Mark as read and navigate to tribe
            lifecycleScope.launch {
                notification.id?.let { id ->
                    TribeService.markNotificationRead(id)
                }
                
                if (notification.tribe_id != null) {
                    val intent = Intent(requireContext(), TribeDetailActivity::class.java).apply {
                        putExtra("tribe_id", notification.tribe_id)
                    }
                    startActivity(intent)
                }
                
                tribesContainer.removeView(banner)
            }
        }
        
        dismissButton.setOnClickListener {
            lifecycleScope.launch {
                notification.id?.let { id ->
                    TribeService.markNotificationRead(id)
                }
                tribesContainer.removeView(banner)
            }
        }
        
        // Insert at the top
        tribesContainer.addView(banner, 0)
    }
}
