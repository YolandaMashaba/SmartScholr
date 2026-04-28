package com.example.smartscholr

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val app = application as SmartScholrApplication
        val name = findViewById<TextInputEditText>(R.id.inputRegisterName)
        val email = findViewById<TextInputEditText>(R.id.inputRegisterEmail)
        val pass = findViewById<TextInputEditText>(R.id.inputRegisterPassword)
        val terms = findViewById<MaterialCheckBox>(R.id.checkTerms)
        val signUp = findViewById<MaterialButton>(R.id.btnSignUp)
        val google = findViewById<MaterialButton>(R.id.btnGoogleSignUp)
        val login = findViewById<TextView>(R.id.linkGoLogin)

        signUp.setOnClickListener {
            val dn = name.text?.toString()?.trim().orEmpty()
            val em = email.text?.toString()?.trim().orEmpty().lowercase(Locale.ROOT)
            val p = pass.text?.toString().orEmpty()
            if (dn.isEmpty() || em.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, R.string.error_fill_register, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!terms.isChecked) {
                Toast.makeText(this, R.string.error_accept_terms, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val r = app.authRepository.register(
                    username = em,
                    password = p,
                    displayName = dn,
                    email = em
                )
                if (r.isSuccess) {
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    val msg = if (r.exceptionOrNull()?.message == "USERNAME_TAKEN") {
                        R.string.error_username_taken
                    } else {
                        R.string.error_register
                    }
                    Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        google.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon_google, Toast.LENGTH_SHORT).show()
        }
        login.setOnClickListener {
            finish()
        }
    }
}
