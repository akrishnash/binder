package com.binder.tribes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.binder.R
import com.binder.utils.TribeService

class TribesAdapter(
    private val onTribeClick: (TribeService.TribeRow) -> Unit
) : RecyclerView.Adapter<TribesAdapter.TribeViewHolder>() {
    
    private var tribes = listOf<TribeService.TribeRow>()
    
    fun updateTribes(newTribes: List<TribeService.TribeRow>) {
        tribes = newTribes
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TribeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tribe, parent, false)
        return TribeViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TribeViewHolder, position: Int) {
        holder.bind(tribes[position])
    }
    
    override fun getItemCount() = tribes.size
    
    inner class TribeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookTitleText: TextView = itemView.findViewById(R.id.bookTitleText)
        private val bookAuthorText: TextView = itemView.findViewById(R.id.bookAuthorText)
        private val cityText: TextView = itemView.findViewById(R.id.cityText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        
        fun bind(tribe: TribeService.TribeRow) {
            bookTitleText.text = tribe.book_title
            bookAuthorText.text = "by ${tribe.book_author}"
            cityText.text = tribe.city
            statusText.text = when (tribe.status) {
                "forming" -> "Forming..."
                "active" -> "Active Sprint"
                "completed" -> "Completed"
                "expired" -> "Expired"
                else -> tribe.status
            }
            
            itemView.setOnClickListener {
                onTribeClick(tribe)
            }
        }
    }
}
