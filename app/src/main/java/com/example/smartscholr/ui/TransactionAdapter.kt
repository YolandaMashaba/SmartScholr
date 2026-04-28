package com.example.smartscholr.ui

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscholr.R
import com.example.smartscholr.data.LedgerRepository

class TransactionAdapter(
    private var lines: List<LedgerRepository.LedgerLine> = emptyList()
) : RecyclerView.Adapter<TransactionAdapter.VH>() {

    private val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "ZA"))

    fun submit(items: List<LedgerRepository.LedgerLine>) {
        lines = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val line = lines[position]
        val e = line.entry
        holder.title.text = e.description.ifBlank { holder.itemView.context.getString(R.string.unnamed) }
        holder.sub.text = line.categoryName ?: "—"
        if (e.isExpense) {
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.expense_red))
            holder.amount.text = "-R${numberFormat.format(e.amount)}"
            holder.amount.setTypeface(null, Typeface.BOLD)
        } else {
            holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.income_green))
            holder.amount.text = "+R${numberFormat.format(e.amount)}"
            holder.amount.setTypeface(null, Typeface.BOLD)
        }
    }

    override fun getItemCount(): Int = lines.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.textTitle)
        val sub: TextView = v.findViewById(R.id.textSub)
        val amount: TextView = v.findViewById(R.id.textAmount)
    }
}
