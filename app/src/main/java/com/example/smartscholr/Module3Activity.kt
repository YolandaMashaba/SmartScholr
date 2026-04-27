package com.example.smartscholr

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Module3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module3)

        findViewById<TextView>(R.id.headerTitle).text = "Module 3"
        findViewById<ImageButton>(R.id.headerBack).setOnClickListener {
            finish()
        }
    }
}