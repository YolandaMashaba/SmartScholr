package com.example.smartscholr.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordSaltB64: String,
    val passwordHashB64: String,
    val displayName: String,
    val email: String?,
    /** Minimum planned monthly spending floor (budget discipline). */
    val minMonthlySpendingLimit: Double?,
    /** Maximum savings goal amount per month (target ceiling). */
    val maxSavingsGoal: Double?,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
