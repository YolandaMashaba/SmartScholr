package com.example.smartscholr.data

import android.database.sqlite.SQLiteConstraintException
import com.example.smartscholr.security.PasswordHasher
import com.example.smartscholr.session.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

class AuthRepository(
    private val userDao: UserDao,
    private val sessionStore: SessionStore
) {

    val sessionUserId: Flow<Long?> = sessionStore.sessionUserId

    companion object {
        const val ERR_NO_USER = "NO_USER"
        const val ERR_BAD_PASSWORD = "BAD_PASSWORD"
        const val ERR_BAD_USER_ROW = "BAD_USER_ROW"
    }

    suspend fun register(
        username: String,
        password: String,
        displayName: String,
        email: String?
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val key = username.trim().lowercase(Locale.ROOT)
            val (salt, hash) = PasswordHasher.hash(password)
            val id = userDao.insert(
                User(
                    username = key,
                    passwordSaltB64 = salt,
                    passwordHashB64 = hash,
                    displayName = displayName,
                    email = email?.trim()?.lowercase(Locale.ROOT)?.ifBlank { null }
                )
            )
            if (id < 1L) {
                return@withContext Result.failure(IllegalStateException("INSERT_FAILED"))
            }
            sessionStore.setSessionUserId(id)
            Result.success(id)
        } catch (_: SQLiteConstraintException) {
            Result.failure(IllegalStateException("USERNAME_TAKEN"))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun login(username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val key = username.trim().lowercase(Locale.ROOT)
            if (key.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("EMPTY_USER"))
            }
            val u = userDao.findByUsernameNormalized(key)
                ?: return@withContext Result.failure(Exception(ERR_NO_USER))
            if (!PasswordHasher.verify(password, u.passwordSaltB64, u.passwordHashB64)) {
                return@withContext Result.failure(Exception(ERR_BAD_PASSWORD))
            }
            if (u.id < 1L) {
                return@withContext Result.failure(Exception(ERR_BAD_USER_ROW))
            }
            sessionStore.setSessionUserId(u.id)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun logout() = sessionStore.clearSession()

    suspend fun currentUser(): User? = withContext(Dispatchers.IO) {
        val id = sessionStore.currentUserIdOrNull() ?: return@withContext null
        userDao.getById(id)
    }
}
