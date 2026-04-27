package com.example.smartscholr

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Module3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module3)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}