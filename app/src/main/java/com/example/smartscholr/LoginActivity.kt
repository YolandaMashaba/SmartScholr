package com.example.smartscholr

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartscholr.data.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val app = application as SmartScholrApplication
        val email = findViewById<TextInputEditText>(R.id.inputLoginEmail)
        val pass = findViewById<TextInputEditText>(R.id.inputLoginPassword)
        val signIn = findViewById<MaterialButton>(R.id.btnSignIn)
        val google = findViewById<MaterialButton>(R.id.btnGoogleSignIn)
        val forgot = findViewById<TextView>(R.id.linkForgotPassword)
        val create = findViewById<TextView>(R.id.linkCreateAccount)

        signIn.setOnClickListener {
            val e = email.text?.toString()?.trim().orEmpty().lowercase(Locale.ROOT)
            val p = pass.text?.toString().orEmpty()
            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_all, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val r = try {
                    app.authRepository.login(e, p)
                } catch (t: Throwable) {
                    android.util.Log.e("LoginActivity", "Login failed", t)
                    Result.failure(t)
                }
                if (r.isSuccess) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    val msg = when (r.exceptionOrNull()?.message) {
                        AuthRepository.ERR_NO_USER -> R.string.error_no_account_for_email
                        AuthRepository.ERR_BAD_PASSWORD -> R.string.error_wrong_password
                        else -> R.string.error_login
                    }
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
        pass.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                signIn.performClick()
                true
            } else {
                false
            }
        }
        forgot.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon_forgot, Toast.LENGTH_SHORT).show()
        }
        google.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon_google, Toast.LENGTH_SHORT).show()
        }
        create.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
