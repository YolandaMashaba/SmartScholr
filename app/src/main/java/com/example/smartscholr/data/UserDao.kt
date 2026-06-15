package com.example.smartscholr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE LOWER(username) = :normalizedUsername LIMIT 1")
    suspend fun findByUsernameNormalized(normalizedUsername: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): User?

    @Update
    suspend fun update(user: User): Int
}
