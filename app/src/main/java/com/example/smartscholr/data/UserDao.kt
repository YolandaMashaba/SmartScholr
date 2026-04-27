package com.example.smartscholr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    /** Called only from a background dispatcher via repository. */
    @Insert
    fun insert(user: User): Long

    /** Avoid suspend DAO methods here due to KSP issues with AGP built-in Kotlin + Room. */
    @Query(
        """
        UPDATE users SET
        minMonthlySpendingLimit = :minSpend,
        maxSavingsGoal = :maxGoal,
        updatedAtMillis = :updatedAt
        WHERE id = :userId
        """
    )
    fun updateFinancialGoals(
        userId: Long,
        minSpend: Double?,
        maxGoal: Double?,
        updatedAt: Long
    )

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getById(id: Long): User?
}
