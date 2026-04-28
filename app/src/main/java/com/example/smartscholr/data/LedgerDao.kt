package com.example.smartscholr.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LedgerDao {

    @Insert
    fun insert(entry: LedgerEntry): Long

    @Query(
        """
        SELECT * FROM ledger_entries
        WHERE userId = :userId
        AND startTimeMillis >= :fromMillis AND startTimeMillis < :toMillis
        ORDER BY startTimeMillis DESC
        LIMIT :limit
        """
    )
    fun listRecentInRange(
        userId: Long,
        fromMillis: Long,
        toMillis: Long,
        limit: Int
    ): List<LedgerEntry>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM ledger_entries
        WHERE userId = :userId AND startTimeMillis >= :from AND startTimeMillis < :to AND isExpense = 0
        """
    )
    fun sumIncomeInRange(userId: Long, from: Long, to: Long): Double

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM ledger_entries
        WHERE userId = :userId AND startTimeMillis >= :from AND startTimeMillis < :to AND isExpense = 1
        """
    )
    fun sumExpenseInRange(userId: Long, from: Long, to: Long): Double

    @Query(
        """
        SELECT categoryId, SUM(amount) as total FROM ledger_entries
        WHERE userId = :userId AND startTimeMillis >= :from AND startTimeMillis < :to AND isExpense = 1
        GROUP BY categoryId
        """
    )
    fun sumExpensesByCategoryInRange(
        userId: Long,
        from: Long,
        to: Long
    ): List<CategoryExpenseSum>

    @Query("DELETE FROM ledger_entries WHERE id = :entryId")
    fun deleteById(entryId: Long)

    @Query("UPDATE ledger_entries SET photoPath = :path WHERE id = :entryId")
    fun updatePhotoPath(entryId: Long, path: String?)

    @Query(
        """
        SELECT COUNT(*) FROM ledger_entries
        WHERE userId = :userId AND startTimeMillis >= :from AND startTimeMillis < :to
        """
    )
    fun countInRange(userId: Long, from: Long, to: Long): Int
}

data class CategoryExpenseSum(
    @ColumnInfo(name = "categoryId") val categoryId: Long?,
    @ColumnInfo(name = "total") val total: Double
)
