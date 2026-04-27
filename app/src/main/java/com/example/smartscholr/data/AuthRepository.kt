package com.example.smartscholr.data

import android.database.sqlite.SQLiteConstraintException
import com.example.smartscholr.security.PasswordHasher
import com.example.smartscholr.session.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepository(
    private val userDao: UserDao,
    private val sessionStore: SessionStore
) {

    val sessionUserId: Flow<Long?> = sessionStore.sessionUserId

    suspend fun register(
        username: String,
        password: String,
        displayName: String,
        email: String?
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val (saltB64, hashB64) = PasswordHasher.hash(password)
            val user = User(
                username = username,
                passwordSaltB64 = saltB64,
                passwordHashB64 = hashB64,
                displayName = displayName,
                email = email?.takeIf { it.isNotBlank() },
                minMonthlySpendingLimit = null,
                maxSavingsGoal = null
            )
            val id = userDao.insert(user)
            sessionStore.setSessionUserId(id)
            Result.success(id)
        } catch (_: SQLiteConstraintException) {
            Result.failure(IllegalStateException("USERNAME_TAKEN"))
        }
    }

    suspend fun login(username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        val row = userDao.getByUsername(username)
            ?: return@withContext Result.failure(NoSuchElementException("BAD_CREDENTIALS"))
        val ok = PasswordHasher.verify(password, row.passwordSaltB64, row.passwordHashB64)
        if (!ok) return@withContext Result.failure(NoSuchElementException("BAD_CREDENTIALS"))
        sessionStore.setSessionUserId(row.id)
        Result.success(Unit)
    }

    suspend fun logout() {
        sessionStore.clearSession()
    }

    suspend fun userForSession(): User? = withContext(Dispatchers.IO) {
        val uid = sessionStore.currentUserIdOrNull() ?: return@withContext null
        userDao.getById(uid)
    }

    suspend fun updateGoals(userId: Long, minSpend: Double?, maxGoal: Double?) =
        withContext(Dispatchers.IO) {
            userDao.updateFinancialGoals(
                userId = userId,
                minSpend = minSpend,
                maxGoal = maxGoal,
                updatedAt = System.currentTimeMillis()
            )
        }

    fun goalsConfigured(user: User): Boolean =
        user.minMonthlySpendingLimit != null &&
            user.maxSavingsGoal != null &&
            user.minMonthlySpendingLimit!! > 0 &&
            user.maxSavingsGoal!! > 0
}
