package com.example.smartscholr

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.smartscholr.ui.BrandTexts
import com.example.smartscholr.validation.InputValidators
import com.example.smartscholr.validation.ValidationResult
import com.example.smartscholr.validation.resolve
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = findViewById<View>(R.id.loginRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        BrandTexts.applyScholrWordmark(this, findViewById(R.id.textBrandWordmark))

        bindLoginFooter()

        findViewById<View>(R.id.textForgotPassword).setOnClickListener {
            Snackbar.make(findViewById(R.id.loginRoot), R.string.snackbar_feature_soon, Snackbar.LENGTH_SHORT).show()
        }

        val tilUsername = findViewById<TextInputLayout>(R.id.textInputLayoutUsername)
        val tilPassword = findViewById<TextInputLayout>(R.id.textInputLayoutPassword)
        val editUsername = findViewById<TextInputEditText>(R.id.editUsername)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)

        findViewById<MaterialButton>(R.id.buttonLogin).setOnClickListener {
            tilUsername.error = null
            tilPassword.error = null

            val usernameRaw = editUsername.text?.toString()
            val passwordRaw = editPassword.text?.toString()

            val userCheck = InputValidators.validateUsername(usernameRaw)
            if (userCheck is ValidationResult.Invalid) {
                tilUsername.error = getString(userCheck.resolve())
                return@setOnClickListener
            }

            val passCheck = InputValidators.validatePassword(passwordRaw)
            if (passCheck is ValidationResult.Invalid) {
                tilPassword.error = getString(passCheck.resolve())
                return@setOnClickListener
            }

            val trimmedUser = usernameRaw!!.trim().toString()

            lifecycleScope.launch {
                val repo = applicationContext.requireAuthRepository()
                val password = editPassword.text?.toString().orEmpty()
                val result = repo.login(trimmedUser, password)
                result.fold(
                    onSuccess = {
                        val user = repo.userForSession()
                        if (user == null) {
                            Snackbar.make(root, R.string.error_login_failed, Snackbar.LENGTH_LONG).show()
                            return@launch
                        }
                        val next = if (repo.goalsConfigured(user)) {
                            Intent(this@LoginActivity, HomeActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, FinancialGoalsActivity::class.java)
                        }
                        startActivity(next)
                        finish()
                    },
                    onFailure = {
                        Snackbar.make(root, R.string.error_login_failed, Snackbar.LENGTH_LONG).show()
                    }
                )
            }
        }

        findViewById<MaterialButton>(R.id.buttonGoogle).setOnClickListener {
            Snackbar.make(findViewById(R.id.loginRoot), R.string.snackbar_feature_soon, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun bindLoginFooter() {
        val tv = findViewById<android.widget.TextView>(R.id.textFooterLogin)
        val prefix = getString(R.string.footer_login_prefix)
        val action = getString(R.string.footer_create_one)
        val full = prefix + action
        val ss = SpannableString(full)
        val orange = ContextCompat.getColor(this, R.color.terracotta)
        ss.setSpan(ForegroundColorSpan(orange), prefix.length, full.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = orange
                }
            },
            prefix.length,
            full.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv.text = ss
        tv.movementMethod = LinkMovementMethod.getInstance()
    }
}
