package com.example.smartscholr.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, CategoryEntity::class, LedgerEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun ledgerDao(): LedgerDao
}
