package com.example.smartscholr

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = findViewById<View>(R.id.homeRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textWelcome = findViewById<TextView>(R.id.textWelcome)
        val textGoals = findViewById<TextView>(R.id.textGoalsSummary)

        lifecycleScope.launch {
            val repo = applicationContext.requireAuthRepository()
            val user = repo.userForSession()
            if (user == null) {
                Snackbar.make(root, R.string.error_not_signed_in, Snackbar.LENGTH_LONG).show()
                startActivity(
                    Intent(this@HomeActivity, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
                return@launch
            }
            textWelcome.text = getString(R.string.home_welcome, user.displayName)
            val min = user.minMonthlySpendingLimit
            val max = user.maxSavingsGoal
            if (min != null && max != null) {
                textGoals.text = getString(R.string.home_goals_detail, min, max)
            } else {
                textGoals.text = getString(R.string.home_subtitle)
            }
        }

        findViewById<MaterialButton>(R.id.buttonLogout).setOnClickListener {
            lifecycleScope.launch {
                applicationContext.requireAuthRepository().logout()
                startActivity(
                    Intent(this@HomeActivity, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
        }
    }
}
