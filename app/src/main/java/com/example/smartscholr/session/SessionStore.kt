package com.example.smartscholr.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_preferences")

private object SessionKeys {
    val USER_ID = longPreferencesKey("session_user_id")
}

class SessionStore(private val context: Context) {

    private val store get() = context.applicationContext.sessionDataStore

    val sessionUserId: Flow<Long?> = store.data.map { prefs ->
        val raw = prefs[SessionKeys.USER_ID]
        if (raw != null && raw >= 1L) raw else null
    }

    suspend fun setSessionUserId(id: Long?) {
        store.edit { prefs ->
            if (id == null || id < 1L) {
                prefs.remove(SessionKeys.USER_ID)
            } else {
                prefs[SessionKeys.USER_ID] = id
            }
        }
    }

    suspend fun clearSession() {
        store.edit { prefs ->
            prefs.remove(SessionKeys.USER_ID)
        }
    }

    suspend fun currentUserIdOrNull(): Long? {
        val prefs = store.data.first()
        val raw = prefs[SessionKeys.USER_ID]
        return if (raw != null && raw >= 1L) raw else null
    }
}
