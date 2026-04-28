package com.example.smartscholr.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscholr.R

class LevelAdapter(
    private val levels: List<LevelRow>
) : RecyclerView.Adapter<LevelAdapter.VH>() {

    data class LevelRow(
        val icon: String,
        val name: String,
        val xpRequired: Int,
        val currentXp: Int
    ) {
        val reached get() = currentXp >= xpRequired
        val xpAway get() = (xpRequired - currentXp).coerceAtLeast(0)
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: TextView = view.findViewById(R.id.textLevelIcon)
        val name: TextView = view.findViewById(R.id.textLevelRowName)
        val sub: TextView = view.findViewById(R.id.textLevelRowSub)
        val status: TextView = view.findViewById(R.id.textLevelStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_level, parent, false)
    )

    override fun getItemCount() = levels.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = levels[position]
        holder.icon.text = row.icon
        holder.name.text = row.name
        holder.sub.text = "${row.xpRequired}+ XP"
        if (row.reached) {
            holder.status.text = "Reached ✓"
            holder.status.setBackgroundResource(R.drawable.bg_month_chip_selected)
            holder.status.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                    holder.itemView.context, R.color.income_green
                )
            )
        } else {
            holder.status.text = "${row.xpAway} XP away"
            holder.status.background = null
            holder.status.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                    holder.itemView.context, R.color.text_secondary
                )
            )
        }
    }
}