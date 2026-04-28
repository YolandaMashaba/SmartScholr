package com.example.smartscholr.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class LedgerRepository(
    private val database: AppDatabase
) {
    private val categoryDao get() = database.categoryDao()
    private val ledgerDao get() = database.ledgerDao()

    suspend fun ensureDefaultCategories(userId: Long) = withContext(Dispatchers.IO) {
        if (categoryDao.countForUser(userId) > 0) return@withContext
        for (name in DEFAULT_CATEGORIES) {
            categoryDao.insert(CategoryEntity(userId = userId, name = name))
        }
    }

    suspend fun listCategories(userId: Long): List<CategoryEntity> = withContext(Dispatchers.IO) {
        categoryDao.listForUser(userId)
    }

    suspend fun addCategory(userId: Long, name: String): Long = withContext(Dispatchers.IO) {
        categoryDao.insert(
            CategoryEntity(userId = userId, name = name.trim())
        )
    }

    suspend fun insertEntry(entry: LedgerEntry): Long = withContext(Dispatchers.IO) {
        ledgerDao.insert(entry)
    }

    data class MonthSnapshot(
        val income: Double,
        val expense: Double,
        val balance: Double,
        val recent: List<LedgerLine>,
        val categoryExpenses: List<CategorySpend>
    )

    data class LedgerLine(
        val entry: LedgerEntry,
        val categoryName: String?
    )

    data class CategorySpend(
        val name: String,
        val amount: Double,
        val fraction: Float
    )

    suspend fun countInSelectedMonth(
        userId: Long,
        year: Int,
        month1Based: Int
    ): Int = withContext(Dispatchers.IO) {
        val (from, to) = monthBoundsMillis(year, month1Based)
        ledgerDao.countInRange(userId, from, to)
    }

    suspend fun monthSnapshot(
        userId: Long,
        year: Int,
        month1Based: Int,
        recentLimit: Int = 50
    ): MonthSnapshot = withContext(Dispatchers.IO) {
        val (from, to) = monthBoundsMillis(year, month1Based)
        val income = ledgerDao.sumIncomeInRange(userId, from, to)
        val expense = ledgerDao.sumExpenseInRange(userId, from, to)
        val recentEntries = ledgerDao.listRecentInRange(userId, from, to, recentLimit)
        val catById = categoryDao.listForUser(userId).associateBy { it.id }
        val recent = recentEntries.map { e ->
            LedgerLine(
                e,
                e.categoryId?.let { id -> catById[id]?.name } ?: "—"
            )
        }
        val sums = ledgerDao.sumExpensesByCategoryInRange(userId, from, to)
        val max = (sums.maxOfOrNull { it.total } ?: 0.0).coerceAtLeast(1.0)
        val categoryExpenses = sums.map { row ->
            val name = row.categoryId?.let { catById[it]?.name } ?: "—"
            CategorySpend(
                name = name,
                amount = row.total,
                fraction = (row.total / max).toFloat().coerceIn(0f, 1f)
            )
        }.sortedByDescending { it.amount }
        MonthSnapshot(
            income = income,
            expense = expense,
            balance = income - expense,
            recent = recent,
            categoryExpenses = categoryExpenses
        )
    }

    private fun monthBoundsMillis(year: Int, month1Based: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month1Based - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val from = start.timeInMillis
        start.add(Calendar.MONTH, 1)
        val to = start.timeInMillis
        return from to to
    }

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            "Food", "Books", "Transport", "Entertainment", "Health", "Rent", "Other"
        )
    }
}
