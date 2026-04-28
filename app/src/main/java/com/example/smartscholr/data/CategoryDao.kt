package com.example.smartscholr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDao {

    @Insert
    fun insert(category: CategoryEntity): Long

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name COLLATE NOCASE ASC")
    fun listForUser(userId: Long): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    fun countForUser(userId: Long): Int

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    fun getById(id: Long): CategoryEntity?
}
