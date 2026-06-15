package com.example.smartscholr.data

import androidx.room.withTransaction
import java.util.Calendar
import java.util.TimeZone

class XpRepository(
    private val xpDao: XpDao,
    private val db: AppDatabase
) {
    /**
     * Awards [amount] XP to the user and updates their daily streak.
     * Streak logic:
     *  - If last log was "today" (same calendar day) -> streak unchanged, just add XP
     *  - If last log was "yesterday" -> streak += 1
     *  - Otherwise (gap of 2+ days, or first-ever log) -> streak resets to 1
     *
     * Returns the updated XpEntity so the UI can immediately reflect new totals.
     */
    suspend fun addXp(userId: Long, amount: Int): XpEntity {
        require(amount > 0) { "XP amount must be positive" }

        return db.withTransaction {
            val current = xpDao.getOrNull(userId)
            val now = System.currentTimeMillis()

            val updated = if (current == null) {
                // First-ever XP for this user
                XpEntity(
                    userId = userId,
                    totalXp = amount,
                    streakDays = 1,
                    lastLogDateMillis = now
                )
            } else {
                val newStreak = when (daysBetween(current.lastLogDateMillis, now)) {
                    0 -> current.streakDays              // already logged today
                    1 -> current.streakDays + 1          // consecutive day
                    else -> 1                            // gap -> reset (or 0L = never logged)
                }

                current.copy(
                    totalXp = current.totalXp + amount,
                    streakDays = if (current.lastLogDateMillis == 0L) 1 else newStreak,
                    lastLogDateMillis = now
                )
            }

            xpDao.upsert(updated)
            updated
        }
    }

    /**
     * Returns the number of calendar days between two timestamps,
     * using local timezone, ignoring time-of-day.
     * 0 = same day, 1 = consecutive day, 2+ = streak broken.
     */
    private fun daysBetween(fromMillis: Long, toMillis: Long): Int {
        if (fromMillis == 0L) return Int.MAX_VALUE // "never logged" -> always treat as broken

        val fromDay = startOfDay(fromMillis)
        val toDay = startOfDay(toMillis)
        val diffMillis = toDay - fromDay
        return (diffMillis / (24 * 60 * 60 * 1000L)).toInt()
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}