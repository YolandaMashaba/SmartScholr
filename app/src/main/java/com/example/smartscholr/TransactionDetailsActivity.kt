package com.example.smartscholr

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.smartscholr.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TransactionDetailsActivity : AppCompatActivity() {

    private var capturedPhotoUri: Uri? = null
    private lateinit var detailImage: ImageView

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val entryId = intent.getLongExtra("entryId", -1L)
            val path = capturedPhotoUri?.let { uri ->
                val file = File(cacheDir, "receipt_${UUID.randomUUID()}.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file.absolutePath
            }
            if (path != null && entryId != -1L) {
                lifecycleScope.launch {
                    val app = application as SmartScholrApplication
                    app.ledgerRepository.updatePhoto(entryId, path)
                    detailImage.visibility = View.VISIBLE
                    detailImage.load(File(path))
                    Toast.makeText(this@TransactionDetailsActivity, "Photo updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val pickGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val entryId = intent.getLongExtra("entryId", -1L)
            lifecycleScope.launch(Dispatchers.IO) {
                val file = File(cacheDir, "receipt_${UUID.randomUUID()}.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val path = file.absolutePath
                if (entryId != -1L) {
                    val app = application as SmartScholrApplication
                    app.ledgerRepository.updatePhoto(entryId, path)
                    withContext(Dispatchers.Main) {
                        detailImage.visibility = View.VISIBLE
                        detailImage.load(file)
                        Toast.makeText(this@TransactionDetailsActivity, "Photo updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        val app = application as SmartScholrApplication
        val entryId = intent.getLongExtra("entryId", -1L)
        val desc = intent.getStringExtra("description") ?: ""
        val category = intent.getStringExtra("category") ?: "—"
        val amount = intent.getDoubleExtra("amount", 0.0)
        val isExpense = intent.getBooleanExtra("isExpense", true)
        val dateMs = intent.getLongExtra("date", 0L)
        val photoPath = intent.getStringExtra("photoPath")

        val title: TextView = findViewById(R.id.detailTitle)
        val cat: TextView = findViewById(R.id.detailCategory)
        val amt: TextView = findViewById(R.id.detailAmount)
        val date: TextView = findViewById(R.id.detailDate)
        detailImage = findViewById(R.id.detailImage)
        val btnBack: View = findViewById(R.id.btnBack)
        val btnDelete: View = findViewById(R.id.btnDelete)
        val btnAttach: View = findViewById(R.id.btnAttach)

        title.text = desc
        cat.text = category
        
        val nf = NumberFormat.getNumberInstance(Locale("en", "ZA"))
        if (isExpense) {
            amt.text = "-R${nf.format(amount)}"
            amt.setTextColor(ContextCompat.getColor(this, R.color.expense_red))
        } else {
            amt.text = "+R${nf.format(amount)}"
            amt.setTextColor(ContextCompat.getColor(this, R.color.income_green))
        }

        val df = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        date.text = df.format(Date(dateMs))

        if (photoPath != null) {
            detailImage.load(File(photoPath))
        } else {
            detailImage.visibility = View.GONE
        }

        btnAttach.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            AlertDialog.Builder(this)
                .setTitle("Attach Photo")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        val photoFile = File(cacheDir, "receipt_${UUID.randomUUID()}.jpg")
                        capturedPhotoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
                        takePicture.launch(capturedPhotoUri)
                    } else {
                        pickGallery.launch("image/*")
                    }
                }
                .show()
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        app.ledgerRepository.deleteEntry(entryId)
                        Toast.makeText(this@TransactionDetailsActivity, "Transaction deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnBack.setOnClickListener { finish() }
    }
}