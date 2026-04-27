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
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = findViewById<View>(R.id.registerRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        BrandTexts.applyScholrWordmark(this, findViewById(R.id.textBrandWordmark))

        bindTermsText()
        bindRegisterFooter()

        val tilName = findViewById<TextInputLayout>(R.id.textInputLayoutName)
        val tilUsername = findViewById<TextInputLayout>(R.id.textInputLayoutUsername)
        val tilEmail = findViewById<TextInputLayout>(R.id.textInputLayoutEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.textInputLayoutPassword)
        val editName = findViewById<TextInputEditText>(R.id.editName)
        val editUsername = findViewById<TextInputEditText>(R.id.editUsername)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)

        findViewById<MaterialButton>(R.id.buttonGoogle).setOnClickListener {
            Snackbar.make(findViewById(R.id.registerRoot), R.string.snackbar_feature_soon, Snackbar.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.buttonRegister).setOnClickListener {
            tilName.error = null
            tilUsername.error = null
            tilEmail.error = null
            tilPassword.error = null

            val accepted = findViewById<MaterialCheckBox>(R.id.checkTerms).isChecked
            if (!accepted) {
                Snackbar.make(findViewById(R.id.registerRoot), R.string.error_terms_required, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nameCheck = InputValidators.validateDisplayName(editName.text?.toString())
            if (nameCheck is ValidationResult.Invalid) {
                tilName.error = getString(nameCheck.resolve())
                return@setOnClickListener
            }

            val userCheck = InputValidators.validateUsername(editUsername.text?.toString())
            if (userCheck is ValidationResult.Invalid) {
                tilUsername.error = getString(userCheck.resolve())
                return@setOnClickListener
            }

            val emailCheck = InputValidators.validateOptionalEmail(editEmail.text?.toString())
            if (emailCheck is ValidationResult.Invalid) {
                tilEmail.error = getString(emailCheck.resolve())
                return@setOnClickListener
            }

            val passCheck = InputValidators.validatePassword(editPassword.text?.toString())
            if (passCheck is ValidationResult.Invalid) {
                tilPassword.error = getString(passCheck.resolve())
                return@setOnClickListener
            }

            val displayName = editName.text!!.trim().toString()
            val username = editUsername.text!!.trim().toString()
            val emailRaw = editEmail.text?.toString()?.trim().orEmpty()
            val emailVal = emailRaw.ifBlank { null }
            val password = editPassword.text!!.toString()

            lifecycleScope.launch {
                val repo = applicationContext.requireAuthRepository()
                val registerResult = repo.register(username, password, displayName, emailVal)

                registerResult.fold(
                    onSuccess = {
                        startActivity(Intent(this@RegisterActivity, FinancialGoalsActivity::class.java))
                        finish()
                    },
                    onFailure = { err ->
                        when {
                            err is IllegalStateException && err.message == "USERNAME_TAKEN" ->
                                Snackbar.make(root, R.string.error_username_taken, Snackbar.LENGTH_LONG).show()

                            else ->
                                Snackbar.make(root, R.string.snackbar_feature_soon, Snackbar.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }

    private fun bindTermsText() {
        val tv = findViewById<android.widget.TextView>(R.id.textTerms)
        val prefix = getString(R.string.terms_prefix)
        val link = getString(R.string.terms_link)
        val full = prefix + link
        val ss = SpannableString(full)
        val teal = ContextCompat.getColor(this, R.color.auth_teal_link)
        ss.setSpan(ForegroundColorSpan(teal), prefix.length, full.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Snackbar.make(findViewById(R.id.registerRoot), R.string.snackbar_feature_soon, Snackbar.LENGTH_SHORT).show()
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = teal
                }
            },
            prefix.length,
            full.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv.text = ss
        tv.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun bindRegisterFooter() {
        val tv = findViewById<android.widget.TextView>(R.id.textFooterRegister)
        val prefix = getString(R.string.footer_register_prefix)
        val action = getString(R.string.footer_login_link)
        val full = prefix + action
        val ss = SpannableString(full)
        val orange = ContextCompat.getColor(this, R.color.terracotta)
        ss.setSpan(ForegroundColorSpan(orange), prefix.length, full.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    finish()
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
