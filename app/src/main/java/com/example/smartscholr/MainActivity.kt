package com.example.smartscholr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnModule1).setOnClickListener {
            startActivity(Intent(this, Module1Activity::class.java))
        }

        findViewById<Button>(R.id.btnModule2).setOnClickListener {
            startActivity(Intent(this, Module2Activity::class.java))
        }

        findViewById<Button>(R.id.btnModule3).setOnClickListener {
            startActivity(Intent(this, Module3Activity::class.java))
        }

        findViewById<Button>(R.id.btnModule4).setOnClickListener {
            startActivity(Intent(this, Module4Activity::class.java))
        }
    }
}