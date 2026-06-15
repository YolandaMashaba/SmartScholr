package com.example.smartscholr.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "user_xp",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class XpEntity(
    @PrimaryKey val userId: Long,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val lastLogDateMillis: Long = 0L
)