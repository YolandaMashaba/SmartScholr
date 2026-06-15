package com.example.smartscholr

import android.content.Intent
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartscholr.util.AssetImageLoader
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(android.os.Looper.getMainLooper())
    private var slideshowRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val app = application as SmartScholrApplication
        val btn = findViewById<MaterialButton>(R.id.btnGetStarted)
        val imgView = findViewById<ImageView>(R.id.imgStudents)


        // Images in assets/images/
        val images = listOf(
            "images/Splash_Students.jpg",
            "images/Splash_Students2.jpg",
            "images/Splash_Students3.jpg",
            "images/Splash_Students4.jpg"
        )


        var currentIndex = 0


        // Load image with left alignment
        fun loadImage(index: Int) {

            val bmp = AssetImageLoader.loadBitmap(
                this,
                images[index]
            )

            if (bmp != null) {

                imgView.scaleType = ImageView.ScaleType.MATRIX

                val scale =
                    imgView.height.toFloat() / bmp.height.toFloat()

                val matrix = Matrix()
                matrix.setScale(scale, scale)

                imgView.imageMatrix = matrix
                imgView.setImageBitmap(bmp)
            }
        }


        // Wait until ImageView has dimensions
        imgView.post {

            // Load first image
            loadImage(currentIndex)


            // Start slideshow
            val runnable = object : Runnable {

                override fun run() {

                    currentIndex =
                        (currentIndex + 1) % images.size


                    imgView.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {


                            loadImage(currentIndex)


                            imgView.animate()
                                .alpha(1f)
                                .setDuration(500)
                                .start()
                        }
                        .start()


                    // Change every 3 seconds
                    handler.postDelayed(
                        this,
                        6000
                    )
                }
            }


            slideshowRunnable = runnable

            handler.postDelayed(
                runnable,
                6000
            )
        }


        // Button navigation
        btn.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }


        // Check logged-in user
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

                startActivity(
                    Intent(
                        this@SplashActivity,
                        HomeActivity::class.java
                    )
                )

                finish()

            } else {

                btn.isEnabled = true
            }
        }
    }


    override fun onDestroy() {

        super.onDestroy()

        // Stop slideshow
        slideshowRunnable?.let {

            handler.removeCallbacks(it)

        }
    }
}