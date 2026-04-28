package com.example.smartscholr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_xp")
data class XpEntity(
    @PrimaryKey val userId: Long,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val lastLogDateMillis: Long = 0L
)