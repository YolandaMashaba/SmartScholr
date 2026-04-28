package com.example.smartscholr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    fun insert(user: User): Long

    /** [normalizedUsername] must already be trimmed + lowercase (Locale.ROOT) for email-as-key. */
    @Query("SELECT * FROM users WHERE LOWER(username) = :normalizedUsername LIMIT 1")
    fun findByUsernameNormalized(normalizedUsername: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getById(id: Long): User?
}
