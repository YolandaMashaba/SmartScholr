package com.example.smartscholr

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val app = application as SmartScholrApplication
        val btn = findViewById<MaterialButton>(R.id.btnGetStarted)

        btn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        lifecycleScope.launch {
            val userId = withContext(Dispatchers.IO) {
                try {
                    app.sessionStore.currentUserIdOrNull()
                } catch (_: Exception) {
                    null
                }
            }
            if (isFinishing) return@launch
            if (userId != null) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            } else {
                btn.isEnabled = true
            }
        }
    }
}
