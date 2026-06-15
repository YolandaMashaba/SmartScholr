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

class XpRepository(private val db: AppDatabase) {

    private val dao get() = db.xpDao()

    // XP values
    companion object {
        const val XP_LOG_TRANSACTION = 10
        const val XP_LOG_INCOME     = 15  // on top of XP_LOG_TRANSACTION
        const val XP_LOGIN          = 5
        const val XP_APP_OPEN       = 2
        const val XP_UNDER_BUDGET   = 25
        const val XP_STREAK_BONUS   = 25

        val LEVELS = listOf(
            Triple("Penny Saver",   0,    "🪙"),
            Triple("Budget Scout",  100,  "🛡️"),
            Triple("Coin Keeper",   250,  "💵"),
            Triple("Money Mover",   500,  "📈"),
            Triple("Fin Wizard",    1000, "⚡"),
            Triple("Scholar Pro",   2000, "🏆")
        )

        fun levelFor(xp: Int): Triple<String, Int, String> =
            LEVELS.lastOrNull { xp >= it.second } ?: LEVELS.first()

        fun nextLevel(xp: Int): Triple<String, Int, String>? {
            val idx = LEVELS.indexOfLast { xp >= it.second }
            return if (idx + 1 < LEVELS.size) LEVELS[idx + 1] else null
        }

        fun xpToNext(xp: Int): Int {
            val next = nextLevel(xp) ?: return 0
            return (next.second - xp).coerceAtLeast(0)
        }
    }

    suspend fun getOrCreate(userId: Long): XpEntity = withContext(Dispatchers.IO) {
        dao.getOrNull(userId) ?: XpEntity(userId = userId).also { dao.upsert(it) }
    }

    /** Call after any transaction is saved. Pass isExpense=false for income. */
    suspend fun awardTransaction(userId: Long, isExpense: Boolean): AwardResult =
        withContext(Dispatchers.IO) {
            val current = getOrCreate(userId)
            var gained = XP_LOG_TRANSACTION
            if (!isExpense) gained += XP_LOG_INCOME

            // streak logic
            val today = startOfDayMillis()
            val yesterday = today - 86_400_000L
            val newStreak = when {
                current.lastLogDateMillis == today -> current.streakDays // already logged today
                current.lastLogDateMillis == yesterday -> current.streakDays + 1 // continued streak
                else -> 1 // broken or new streak
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
            val oldLevel = levelFor(current.totalXp).first
            val newLevel = levelFor(updated.totalXp).first
            AwardResult(
                xpGained = gained,
                totalXp = updated.totalXp,
                streakDays = newStreak,
                loginStreakDays = current.loginStreakDays,
                appOpenStreakDays = current.appOpenStreakDays,
                leveledUp = newLevel != oldLevel,
                newLevelName = newLevel
            )
        }

    /** Call at end of month to check if user stayed under budget. */
    suspend fun awardUnderBudget(userId: Long): AwardResult = withContext(Dispatchers.IO) {
        val current = getOrCreate(userId)
        val updated = current.copy(totalXp = current.totalXp + XP_UNDER_BUDGET)
        dao.upsert(updated)
        AwardResult(
            xpGained = XP_UNDER_BUDGET,
            totalXp = updated.totalXp,
            streakDays = current.streakDays,
            loginStreakDays = current.loginStreakDays,
            appOpenStreakDays = current.appOpenStreakDays,
            leveledUp = levelFor(updated.totalXp).first != levelFor(current.totalXp).first,
            newLevelName = levelFor(updated.totalXp).first
        )
    }

    suspend fun awardAppOpen(userId: Long): AwardResult = withContext(Dispatchers.IO) {
        val current = getOrCreate(userId)
        val today = startOfDayMillis()

        if (current.lastAppOpenDateMillis == today) {
            return@withContext AwardResult(
                xpGained = 0,
                totalXp = current.totalXp,
                streakDays = current.streakDays,
                loginStreakDays = current.loginStreakDays,
                appOpenStreakDays = current.appOpenStreakDays,
                leveledUp = false,
                newLevelName = levelFor(current.totalXp).first
            )
        }

        val yesterday = today - 86_400_000L
        val newAppOpenStreak = if (current.lastAppOpenDateMillis == yesterday) {
            current.appOpenStreakDays + 1
        } else {
            1
        }

        val gained = XP_APP_OPEN
        val updated = current.copy(
            totalXp = current.totalXp + gained,
            lastAppOpenDateMillis = today,
            appOpenStreakDays = newAppOpenStreak
        )
        dao.upsert(updated)

        AwardResult(
            xpGained = gained,
            totalXp = updated.totalXp,
            streakDays = updated.streakDays,
            loginStreakDays = updated.loginStreakDays,
            appOpenStreakDays = newAppOpenStreak,
            leveledUp = levelFor(updated.totalXp).first != levelFor(current.totalXp).first,
            newLevelName = levelFor(updated.totalXp).first
        )
    }

    suspend fun awardLogin(userId: Long): AwardResult = withContext(Dispatchers.IO) {
        val current = getOrCreate(userId)
        val today = startOfDayMillis()
        
        if (current.lastLoginDateMillis == today) {
            // Already awarded today
            return@withContext AwardResult(
                xpGained = 0,
                totalXp = current.totalXp,
                streakDays = current.streakDays,
                loginStreakDays = current.loginStreakDays,
                appOpenStreakDays = current.appOpenStreakDays,
                leveledUp = false,
                newLevelName = levelFor(current.totalXp).first
            )
        }
        
        val yesterday = today - 86_400_000L
        val newLoginStreak = if (current.lastLoginDateMillis == yesterday) {
            current.loginStreakDays + 1
        } else {
            1
        }
        
        val gained = XP_LOGIN
        val updated = current.copy(
            totalXp = current.totalXp + gained,
            lastLoginDateMillis = today,
            loginStreakDays = newLoginStreak
        )
        dao.upsert(updated)
        
        val oldLevel = levelFor(current.totalXp).first
        val newLevel = levelFor(updated.totalXp).first
        
        AwardResult(
            xpGained = gained,
            totalXp = updated.totalXp,
            streakDays = current.streakDays,
            loginStreakDays = newLoginStreak,
            appOpenStreakDays = current.appOpenStreakDays,
            leveledUp = newLevel != oldLevel,
            newLevelName = newLevel
        )
    }

    private fun startOfDayMillis(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }


    data class Badge(
        val id: String,
        val name: String,
        val description: String,
        val icon: String,
        val isEarned: Boolean = false
    )

    suspend fun getBadges(userId: Long): List<Badge> = withContext(Dispatchers.IO) {
        val xp = getOrCreate(userId)
        val entriesCount = db.ledgerDao().countAllForUser(userId)
        
        // In a real app, we might store earned badges in DB, 
        // but for this task we'll derive some from existing data.
        
        val badges = mutableListOf<Badge>()
        
        // 1. Budget King - Stay under budget (represented by having positive balance in latest month)
        // We can check if they have at least one transaction and positive balance
        val cal = Calendar.getInstance()
        val snap = db.ledgerDao().sumIncomeInRange(userId, 0, Long.MAX_VALUE) - 
                   db.ledgerDao().sumExpenseInRange(userId, 0, Long.MAX_VALUE)
        
        badges.add(Badge(
            id = "budget_king",
            name = "Budget King",
            description = "Keep a positive total balance",
            icon = "👑",
            isEarned = snap > 0
        ))
        
        // 2. Consistent Logger - Logged at least 10 entries
        badges.add(Badge(
            id = "consistent_logger",
            name = "Consistent Logger",
            description = "Log at least 10 entries",
            icon = "📝",
            isEarned = entriesCount >= 10
        ))
        
        // 3. Streak Starter - 3 day streak
        badges.add(Badge(
            id = "streak_starter",
            name = "Streak Starter",
            description = "Maintain a 3-day streak",
            icon = "🔥",
            isEarned = xp.streakDays >= 3
        ))
        
        // 4. Daily Devotee - 3 day login streak
        badges.add(Badge(
            id = "daily_devotee",
            name = "Daily Devotee",
            description = "Log in 3 days in a row",
            icon = "📱",
            isEarned = xp.loginStreakDays >= 3
        ))
        
        // 5. Savings Master - Level 3+
        val level = levelFor(xp.totalXp)
        val levelIdx = LEVELS.indexOfFirst { it.first == level.first }
        badges.add(Badge(
            id = "savings_master",
            name = "Savings Master",
            description = "Reach Level 3 (Coin Keeper)",
            icon = "💰",
            isEarned = levelIdx >= 2
        ))

        badges
    }

    data class AwardResult(
        val xpGained: Int,
        val totalXp: Int,
        val streakDays: Int,
        val loginStreakDays: Int = 0,
        val appOpenStreakDays: Int = 0,
        val leveledUp: Boolean,
        val newLevelName: String
    )
}