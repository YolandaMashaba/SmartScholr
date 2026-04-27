package com.example.smartscholr.ui

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.smartscholr.R

object BrandTexts {
    fun applyScholrWordmark(context: Context, textView: TextView) {
        val smart = context.getString(R.string.app_name_smart)
        val scholr = context.getString(R.string.app_name_scholr)
        val full = smart + scholr
        val ss = SpannableString(full)
        val white = ContextCompat.getColor(context, R.color.white)
        val orange = ContextCompat.getColor(context, R.color.terracotta)
        ss.setSpan(ForegroundColorSpan(white), 0, smart.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(
            ForegroundColorSpan(orange),
            smart.length,
            full.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = ss
    }
}
