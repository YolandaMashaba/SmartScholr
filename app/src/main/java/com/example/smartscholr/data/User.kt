package com.example.smartscholr.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordSaltB64: String,
    val passwordHashB64: String,
    val displayName: String,
    val email: String?,
    val lastLoginMillis: Long? = System.currentTimeMillis()
)
