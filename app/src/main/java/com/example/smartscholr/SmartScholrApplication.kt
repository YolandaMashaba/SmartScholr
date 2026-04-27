package com.example.smartscholr

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.smartscholr.data.AppDatabase
import com.example.smartscholr.data.AuthRepository
import com.example.smartscholr.session.SessionStore

class SmartScholrApplication : Application() {

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "smartscholr.db")
            .fallbackToDestructiveMigration()
            .build()
        authRepository = AuthRepository(db.userDao(), SessionStore(this))
    }
}

fun Context.requireAuthRepository(): AuthRepository =
    (applicationContext as SmartScholrApplication).authRepository
