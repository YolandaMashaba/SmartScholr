package com.example.smartscholr

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscholr.data.CategoryEntity
import com.example.smartscholr.data.LedgerEntry
import com.example.smartscholr.data.LedgerRepository
import com.example.smartscholr.data.XpRepository
import com.example.smartscholr.ui.CategorySpendAdapter
import com.example.smartscholr.ui.TransactionAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class HomeActivity : AppCompatActivity() {

    private lateinit var app: SmartScholrApplication
    private var userId: Long = -1L

    private var filterYear: Int = 0
    private var filterMonth1: Int = 1

    private val entryDate: Calendar = Calendar.getInstance()
    private var startHour: Int = 0
    private var startMinute: Int = 0
    private var endHour: Int = 0
    private var endMinute: Int = 0
    private var includeEnd: Boolean = false

    private lateinit var layoutMonthChips: LinearLayout
    private lateinit var textIncome: TextView
    private lateinit var textSpent: TextView
    private lateinit var textBalance: TextView
    private lateinit var textRecentBadge: TextView
    private lateinit var rvRecent: RecyclerView
    private lateinit var rvCategory: RecyclerView
    private lateinit var textDate: TextView
    private lateinit var textStart: TextView
    private lateinit var textEnd: TextView
    private lateinit var checkEnd: CheckBox
    private lateinit var rowEndTime: LinearLayout
    private lateinit var editDescription: TextInputEditText
    private lateinit var editAmount: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var headerLogout: TextView

    private var currentPhotoPath: String? = null
    private var capturedPhotoUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            findViewById<ImageView>(R.id.imgPreview).apply {
                visibility = View.VISIBLE
                setImageURI(capturedPhotoUri)
            }
        } else {
            currentPhotoPath = null
            capturedPhotoUri = null
        }
    }

    private val pickGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val file = File(cacheDir, "receipt_${UUID.randomUUID()}.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                withContext(Dispatchers.Main) {
                    currentPhotoPath = file.absolutePath
                    findViewById<ImageView>(R.id.imgPreview).apply {
                        visibility = View.VISIBLE
                        setImageURI(uri)
                    }
                }
            }
        }
    }

    private val recentAdapter = TransactionAdapter()
    private val categoryAdapter = CategorySpendAdapter()
    private val numberFormat = NumberFormat.getNumberInstance(Locale("en", "ZA"))

    private var categories: List<CategoryEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        app = application as SmartScholrApplication

        val cal = Calendar.getInstance()
        filterYear = cal.get(Calendar.YEAR)
        filterMonth1 = cal.get(Calendar.MONTH) + 1
        startHour = cal.get(Calendar.HOUR_OF_DAY)
        startMinute = cal.get(Calendar.MINUTE)
        endHour = startHour
        endMinute = startMinute

        layoutMonthChips = findViewById(R.id.layoutMonthChips)
        textIncome = findViewById(R.id.textIncome)
        textSpent = findViewById(R.id.textSpent)
        textBalance = findViewById(R.id.textBalance)
        textRecentBadge = findViewById(R.id.textRecentBadge)
        rvRecent = findViewById(R.id.rvRecent)
        rvCategory = findViewById(R.id.rvCategory)
        textDate = findViewById(R.id.textDate)
        textStart = findViewById(R.id.textStart)
        textEnd = findViewById(R.id.textEnd)
        checkEnd = findViewById(R.id.checkEnd)
        rowEndTime = findViewById(R.id.rowEndTime)
        editDescription = findViewById(R.id.editDescription)
        editAmount = findViewById(R.id.editAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        headerLogout = findViewById(R.id.headerLogout)

        rvRecent.layoutManager = LinearLayoutManager(this)
        rvRecent.adapter = recentAdapter

        rvCategory.layoutManager = LinearLayoutManager(this)
        rvCategory.adapter = categoryAdapter

        findViewById<Button>(R.id.btnPickDate).setOnClickListener { openDatePicker() }
        findViewById<Button>(R.id.btnStartTime).setOnClickListener { openTimePicker(true) }
        findViewById<Button>(R.id.btnEndTime).setOnClickListener { openTimePicker(false) }
        findViewById<Button>(R.id.btnAddCategory).setOnClickListener { showAddCategoryDialog() }
        findViewById<Button>(R.id.btnIncome).setOnClickListener { saveEntry(isExpense = false) }
        findViewById<Button>(R.id.btnExpense).setOnClickListener { saveEntry(isExpense = true) }
        findViewById<Button>(R.id.btnDateFilter).setOnClickListener { showRangeDatePicker() }
        findViewById<Button>(R.id.btnCamera).setOnClickListener { launchCamera() }
        findViewById<Button>(R.id.btnGallery).setOnClickListener { pickGallery.launch("image/*") }
        
        recentAdapter.setOnItemClickListener(object : TransactionAdapter.OnItemClickListener {
            override fun onItemClick(line: LedgerRepository.LedgerLine) {
                val intent = Intent(this@HomeActivity, TransactionDetailsActivity::class.java).apply {
                    putExtra("entryId", line.entry.id)
                    putExtra("description", line.entry.description)
                    putExtra("category", line.categoryName)
                    putExtra("amount", line.entry.amount)
                    putExtra("isExpense", line.entry.isExpense)
                    putExtra("date", line.entry.startTimeMillis)
                    putExtra("photoPath", line.entry.photoPath)
                }
                startActivity(intent)
            }
        })

        findViewById<TextView>(R.id.headerLevelUp).setOnClickListener {
            startActivity(Intent(this, XpActivity::class.java))
        }
        checkEnd.setOnCheckedChangeListener { _, checked ->
            includeEnd = checked
            rowEndTime.visibility = if (checked) View.VISIBLE else View.GONE
        }

        headerLogout.setOnClickListener {
            lifecycleScope.launch {
                app.authRepository.logout()
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }

        updateDateLabel()
        updateTimeLabels()

        lifecycleScope.launch {
            try {
                val id = withContext(Dispatchers.IO) {
                    try {
                        app.sessionStore.currentUserIdOrNull()
                    } catch (_: Exception) {
                        null
                    }
                }
                if (id == null) {
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }
                userId = id
                app.ledgerRepository.ensureDefaultCategories(userId)
                buildMonthChips()
                loadCategoriesToSpinner { refreshDashboard() }
            } catch (t: Throwable) {
                android.util.Log.e("HomeActivity", "Failed to open dashboard", t)
                Toast.makeText(this@HomeActivity, R.string.error_login, Toast.LENGTH_LONG).show()
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun buildMonthChips() {
        layoutMonthChips.removeAllViews()
        val names = java.text.DateFormatSymbols(Locale.getDefault()).shortMonths
        val chipViews = ArrayList<TextView>(12)
        for (i in 0 until 12) {
            val tv = TextView(this)
            tv.text = names[i].uppercase(Locale.getDefault())
            tv.textSize = 12f
            tv.setPadding(dp(14), dp(10), dp(14), dp(10))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.marginEnd = dp(8)
            tv.layoutParams = lp
            tv.gravity = Gravity.CENTER
            val monthIndex1 = i + 1
            tv.setOnClickListener {
                filterMonth1 = monthIndex1
                highlightMonthChip(chipViews, monthIndex1)
                refreshDashboard()
            }
            layoutMonthChips.addView(tv)
            chipViews.add(tv)
        }
        highlightMonthChip(chipViews, filterMonth1)
    }

    private fun highlightMonthChip(views: List<TextView>, month1: Int) {
        views.forEachIndexed { index, tv ->
            val selected = index + 1 == month1
            if (selected) {
                tv.setBackgroundResource(R.drawable.bg_month_chip_selected)
                tv.setTextColor(ContextCompat.getColor(this, R.color.teal_dark))
            } else {
                tv.setBackgroundResource(R.drawable.bg_month_chip_unselected)
                tv.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    private fun loadCategoriesToSpinner(after: () -> Unit) {
        lifecycleScope.launch {
            categories = app.ledgerRepository.listCategories(userId)
            if (isFinishing || isDestroyed) return@launch
            val names = categories.map { it.name }
            val adapter = ArrayAdapter(
                this@HomeActivity,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
            spinnerCategory.adapter = adapter
            if (isFinishing || isDestroyed) return@launch
            after()
        }
    }

    private fun openDatePicker() {
        DatePickerDialog(
            this,
            { _, y, m, d ->
                entryDate.set(Calendar.YEAR, y)
                entryDate.set(Calendar.MONTH, m)
                entryDate.set(Calendar.DAY_OF_MONTH, d)
                updateDateLabel()
            },
            entryDate.get(Calendar.YEAR),
            entryDate.get(Calendar.MONTH),
            entryDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openTimePicker(isStart: Boolean) {
        val h = if (isStart) startHour else endHour
        val m = if (isStart) startMinute else endMinute
        TimePickerDialog(this, { _, nh, nm ->
            if (isStart) {
                startHour = nh
                startMinute = nm
            } else {
                endHour = nh
                endMinute = nm
            }
            updateTimeLabels()
        }, h, m, true).show()
    }

    private fun updateDateLabel() {
        val fmt = android.text.format.DateFormat.getMediumDateFormat(this)
        textDate.text = fmt.format(entryDate.time)
    }

    private fun updateTimeLabels() {
        textStart.text = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)
        textEnd.text = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)
    }

    private fun launchCamera() {
        val photoFile = File(cacheDir, "receipt_${UUID.randomUUID()}.jpg")
        currentPhotoPath = photoFile.absolutePath
        capturedPhotoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        takePicture.launch(capturedPhotoUri)
    }

    private fun showRangeDatePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()
        picker.addOnPositiveButtonClickListener { range ->
            val start = range.first
            val end = range.second + 86400000L // inclusive
            lifecycleScope.launch {
                val snap = app.ledgerRepository.getSnapshotForRange(userId, start, end)
                updateUiWithSnapshot(snap, "Custom Range")
            }
        }
        picker.show(supportFragmentManager, "range_picker")
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = getString(R.string.category_name_hint)
        input.setSingleLine()
        AlertDialog.Builder(this)
            .setTitle(R.string.add_category)
            .setView(input)
            .setPositiveButton(R.string.save) { d, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            app.ledgerRepository.addCategory(userId, name)
                            loadCategoriesToSpinner {
                                spinnerCategory.setSelection(
                                    categories.indexOfFirst { it.name == name }.coerceAtLeast(0)
                                )
                            }
                            Toast.makeText(
                                this@HomeActivity,
                                R.string.category_added,
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (_: SQLiteConstraintException) {
                            Toast.makeText(
                                this@HomeActivity,
                                R.string.category_exists,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                d.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun startOfDayMillis(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun combineDateTime(day: Calendar, hour: Int, minute: Int): Long {
        val c = day.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, hour)
        c.set(Calendar.MINUTE, minute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    // ── FIXED: XP award is inside saveEntry, refreshDashboard is clean ──
    private fun saveEntry(isExpense: Boolean) {
        val desc = editDescription.text?.toString()?.trim().orEmpty()
        val amount = parseAmount(editAmount.text?.toString().orEmpty())
        if (desc.isEmpty()) {
            Toast.makeText(this, R.string.error_need_description, Toast.LENGTH_SHORT).show()
            return
        }
        if (amount == null) {
            Toast.makeText(this, R.string.error_need_amount, Toast.LENGTH_SHORT).show()
            return
        }
        val catPos = spinnerCategory.selectedItemPosition
        if (catPos == AdapterView.INVALID_POSITION || categories.isEmpty()) {
            Toast.makeText(this, R.string.error_need_category, Toast.LENGTH_SHORT).show()
            return
        }
        val categoryId = categories[catPos].id
        val startMs = combineDateTime(entryDate, startHour, startMinute)
        val endMs = if (includeEnd) combineDateTime(entryDate, endHour, endMinute) else null
        if (endMs != null && endMs < startMs) {
            Toast.makeText(this, R.string.error_time_order, Toast.LENGTH_SHORT).show()
            return
        }
        val dayMs = startOfDayMillis(entryDate)
        val entry = LedgerEntry(
            userId = userId,
            categoryId = categoryId,
            amount = amount,
            isExpense = isExpense,
            description = desc,
            dateMillis = dayMs,
            startTimeMillis = startMs,
            endTimeMillis = endMs,
            photoPath = currentPhotoPath
        )
        lifecycleScope.launch {
            app.ledgerRepository.insertEntry(entry)
            editDescription.text = null
            editAmount.text = null
            currentPhotoPath = null
            findViewById<ImageView>(R.id.imgPreview).visibility = View.GONE

            // ── XP & Streak award ──────────────────────────
            val award = app.xpRepository.awardTransaction(userId, isExpense)
            val msg = buildString {
                append(getString(R.string.entry_saved))
                append("  +${award.xpGained} XP")
                if (award.streakDays > 1) append(" 🔥 ${award.streakDays} day streak!")
                if (award.leveledUp) append(" 🎉 Level up: ${award.newLevelName}!")
            }
            Toast.makeText(this@HomeActivity, msg, Toast.LENGTH_SHORT).show()
            // ───────────────────────────────────────────────

            refreshDashboard()
        }
    }

    private fun parseAmount(raw: String): Double? {
        val s = raw.trim().replace(",", "").replace(" ", "")
        if (s.isEmpty()) return null
        return s.toDoubleOrNull()?.takeIf { it > 0 }
    }

    // ── FIXED: refreshDashboard is clean, no XP code here ──
    private fun refreshDashboard() {
        if (userId < 0) return
        lifecycleScope.launch {
            try {
                val snap = app.ledgerRepository.monthSnapshot(
                    userId,
                    filterYear,
                    filterMonth1
                )
                val count = app.ledgerRepository.countInSelectedMonth(
                    userId,
                    filterYear,
                    filterMonth1
                )
                if (isFinishing || isDestroyed) return@launch
                updateUiWithSnapshot(snap, getString(R.string.recent_badge, count))
            } catch (t: Throwable) {
                android.util.Log.e("HomeActivity", "refreshDashboard", t)
            }
        }
    }

    private fun updateUiWithSnapshot(snap: LedgerRepository.MonthSnapshot, badgeText: String) {
        textIncome.text = "R${numberFormat.format(snap.income)}"
        textSpent.text = "R${numberFormat.format(snap.expense)}"
        textBalance.text = "R${numberFormat.format(snap.balance)}"
        textRecentBadge.text = badgeText
        recentAdapter.submit(snap.recent)
        categoryAdapter.submit(snap.categoryExpenses)

        // Show level in header
        lifecycleScope.launch {
            val xp = app.xpRepository.getOrCreate(userId)
            val level = XpRepository.levelFor(xp.totalXp)
            findViewById<TextView>(R.id.headerLevelUp).text = "${level.third} ${level.first} • Level Up"
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}