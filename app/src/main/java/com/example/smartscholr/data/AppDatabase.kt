package com.example.smartscholr.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, CategoryEntity::class, LedgerEntry::class, XpEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun xpDao(): XpDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS user_xp (
                        userId INTEGER NOT NULL PRIMARY KEY,
                        totalXp INTEGER NOT NULL DEFAULT 0,
                        streakDays INTEGER NOT NULL DEFAULT 0,
                        lastLogDateMillis INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }
    }
}