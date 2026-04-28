package com.example.smartscholr.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException

object AssetImageLoader {
    const val LOGO_ASSET = "images/logo.png"

    fun loadBitmap(context: Context, assetPath: String): Bitmap? = try {
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
    } catch (_: IOException) {
        null
    }
}
