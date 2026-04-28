package com.example.smartscholr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface XpDao {

    @Query("SELECT * FROM user_xp WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: Long): XpEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XpEntity)

    @Query("SELECT * FROM user_xp WHERE userId = :userId LIMIT 1")
    suspend fun getOrNull(userId: Long): XpEntity?
}