package com.example.smartscholr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.smartscholr.ui.BrandTexts
import com.example.smartscholr.util.AssetImageLoader
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val content = findViewById<LinearLayout>(R.id.splashContent)
        ViewCompat.setOnApplyWindowInsetsListener(content) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        BrandTexts.applyScholrWordmark(this, findViewById<TextView>(R.id.textAppName))

        findViewById<ImageView>(R.id.imageSplashLogo).also { image ->
            val bitmap = AssetImageLoader.loadBitmap(this, AssetImageLoader.LOGO_ASSET)
            if (bitmap != null) {
                image.setImageBitmap(bitmap)
            } else {
                image.setImageResource(R.mipmap.ic_launcher)
            }
        }

        lifecycleScope.launch {
            val repo = applicationContext.requireAuthRepository()
            val user = repo.userForSession()
            when {
                user != null && repo.goalsConfigured(user) -> {
                    startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                    finish()
                    return@launch
                }

                user != null -> {
                    startActivity(Intent(this@SplashActivity, FinancialGoalsActivity::class.java))
                    finish()
                    return@launch
                }

                else -> Unit
            }
        }

        findViewById<MaterialButton>(R.id.buttonGetStarted).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
