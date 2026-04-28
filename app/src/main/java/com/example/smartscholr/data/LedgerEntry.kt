package com.example.smartscholr.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Income or expense line. [startTimeMillis] / [endTimeMillis] are full instants; month filters use [startTimeMillis].
 */
@Entity(
    tableName = "ledger_entries",
    indices = [
        Index("userId"),
        Index("categoryId"),
        Index("startTimeMillis")
    ]
)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryId: Long?,
    val amount: Double,
    val isExpense: Boolean,
    val description: String,
    /** Start of calendar day (local) for quick filters; mirrors date part of [startTimeMillis]. */
    val dateMillis: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val createdAtMillis: Long = System.currentTimeMillis()
)
