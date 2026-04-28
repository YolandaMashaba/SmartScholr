package com.example.smartscholr.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscholr.R
import com.example.smartscholr.data.LedgerRepository
import java.text.NumberFormat
import java.util.Locale

class CategorySpendAdapter(
    private var items: List<LedgerRepository.CategorySpend> = emptyList()
) : RecyclerView.Adapter<CategorySpendAdapter.VH>() {

    private val fmt = NumberFormat.getNumberInstance(Locale("en", "ZA"))

    fun submit(list: List<LedgerRepository.CategorySpend>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category_spend, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = items[position]
        holder.name.text = row.name
        holder.amt.text = "R${fmt.format(row.amount)}"
        if (holder.bar.max != 100) holder.bar.max = 100
        val pct = row.fraction.takeUnless { it.isNaN() || it.isInfinite() } ?: 0f
        holder.bar.progress = (pct * 100f).toInt().coerceIn(0, 100)
    }

    override fun getItemCount(): Int = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.textCategory)
        val amt: TextView = v.findViewById(R.id.textCategoryAmount)
        val bar: ProgressBar = v.findViewById(R.id.progressCategory)
    }
}
