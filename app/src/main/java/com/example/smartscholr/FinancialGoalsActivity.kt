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
import com.example.smartscholr.validation.InputValidators
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.Locale

class FinancialGoalsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_financial_goals)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = findViewById<View>(R.id.financialGoalsRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tilMin = findViewById<TextInputLayout>(R.id.textInputLayoutMinSpend)
        val tilMax = findViewById<TextInputLayout>(R.id.textInputLayoutMaxGoal)
        val editMin = findViewById<TextInputEditText>(R.id.editMinSpend)
        val editMax = findViewById<TextInputEditText>(R.id.editMaxGoal)

        lifecycleScope.launch {
            val repo = applicationContext.requireAuthRepository()
            val user = repo.userForSession()
            if (user == null) {
                Snackbar.make(root, R.string.error_not_signed_in, Snackbar.LENGTH_LONG).show()
                startActivity(Intent(this@FinancialGoalsActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            user.minMonthlySpendingLimit?.let {
                editMin.setText(String.format(Locale.getDefault(), "%.2f", it))
            }
            user.maxSavingsGoal?.let {
                editMax.setText(String.format(Locale.getDefault(), "%.2f", it))
            }
        }

        findViewById<MaterialButton>(R.id.buttonSaveGoals).setOnClickListener {
            tilMin.error = null
            tilMax.error = null

            val minRaw = editMin.text?.toString().orEmpty()
            val maxRaw = editMax.text?.toString().orEmpty()

            val minOk = validateMoneyField(tilMin, minRaw)
            val maxOk = validateMoneyField(tilMax, maxRaw)
            if (!minOk || !maxOk) return@setOnClickListener

            val min = InputValidators.parsePositiveMoney(minRaw)!!
            val max = InputValidators.parsePositiveMoney(maxRaw)!!

            lifecycleScope.launch {
                val repo = applicationContext.requireAuthRepository()
                val uid = repo.userForSession()?.id ?: run {
                    Snackbar.make(root, R.string.error_not_signed_in, Snackbar.LENGTH_LONG).show()
                    return@launch
                }
                repo.updateGoals(uid, min, max)
                startActivity(Intent(this@FinancialGoalsActivity, HomeActivity::class.java))
                finish()
            }
        }
    }

    private fun validateMoneyField(til: TextInputLayout, raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            til.error = getString(R.string.validation_money_required)
            return false
        }
        val parsed = InputValidators.parsePositiveMoney(raw)
        if (parsed == null) {
            til.error = getString(R.string.validation_money_invalid)
            return false
        }
        return true
    }
}
