package com.example.smartscholr

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.example.smartscholr.data.AppDatabase
import com.example.smartscholr.data.AuthRepository
import com.example.smartscholr.data.LedgerRepository
import com.example.smartscholr.data.XpRepository
import com.example.smartscholr.session.SessionStore

class SmartScholrApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var sessionStore: SessionStore
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var ledgerRepository: LedgerRepository
        private set
    lateinit var xpRepository: XpRepository
        private set

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        sessionStore = SessionStore(this)
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "smartscholr.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
        authRepository = AuthRepository(database.userDao(), sessionStore)
        ledgerRepository = LedgerRepository(database)
        xpRepository = XpRepository(database)
    }
}

fun Application.scholrApp(): SmartScholrApplication = this as SmartScholrApplication