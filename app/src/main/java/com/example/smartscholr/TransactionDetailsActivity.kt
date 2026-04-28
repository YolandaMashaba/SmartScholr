package com.example.smartscholr

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.example.smartscholr.R
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

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
        val image: ImageView = findViewById(R.id.detailImage)
        val btnBack: View = findViewById(R.id.btnBack)

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
            image.load(File(photoPath))
        } else {
            image.visibility = View.GONE
        }

        btnBack.setOnClickListener { finish() }
    }
}