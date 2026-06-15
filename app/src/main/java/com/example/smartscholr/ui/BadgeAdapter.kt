package com.example.smartscholr.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscholr.R
import com.example.smartscholr.data.XpRepository

class BadgeAdapter(
    private val badges: List<XpRepository.Badge>
) : RecyclerView.Adapter<BadgeAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: TextView = view.findViewById(R.id.textBadgeIcon)
        val name: TextView = view.findViewById(R.id.textBadgeName)
        val lock: ImageView = view.findViewById(R.id.imgBadgeLocked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
    )

    override fun getItemCount() = badges.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val badge = badges[position]
        holder.icon.text = badge.icon
        holder.name.text = badge.name
        
        if (badge.isEarned) {
            holder.lock.visibility = View.GONE
            holder.icon.alpha = 1.0f
            holder.name.setTextColor(
                androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.section_label)
            )
        } else {
            holder.lock.visibility = View.VISIBLE
            holder.icon.alpha = 0.3f
            holder.name.setTextColor(
                androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_secondary)
            )
        }
    }
}
