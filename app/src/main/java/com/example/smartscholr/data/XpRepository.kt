package com.example.smartscholr.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class XpRepository(private val db: AppDatabase) {

    private val dao get() = db.xpDao()

    // XP values
    companion object {
        const val XP_LOG_TRANSACTION = 10
        const val XP_LOG_INCOME     = 15  // on top of XP_LOG_TRANSACTION
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
            val streakBonus = if (current.lastLogDateMillis != today) XP_STREAK_BONUS else 0
            gained += streakBonus

            val updated = current.copy(
                totalXp = current.totalXp + gained,
                streakDays = newStreak,
                lastLogDateMillis = today
            )
            dao.upsert(updated)

            val oldLevel = levelFor(current.totalXp).first
            val newLevel = levelFor(updated.totalXp).first
            AwardResult(
                xpGained = gained,
                totalXp = updated.totalXp,
                streakDays = newStreak,
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
            leveledUp = levelFor(updated.totalXp).first != levelFor(current.totalXp).first,
            newLevelName = levelFor(updated.totalXp).first
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
        
        // 4. Savings Master - Level 3+
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
        val leveledUp: Boolean,
        val newLevelName: String
    )
}