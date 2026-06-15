package com.example.smartscholr

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartscholr.data.XpRepository
import com.example.smartscholr.ui.BadgeAdapter
import com.example.smartscholr.ui.LevelAdapter
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class XpActivity : AppCompatActivity() {

    private lateinit var app: SmartScholrApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xp)

        app = application as SmartScholrApplication

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        lifecycleScope.launch {
            val userId = app.sessionStore.currentUserIdOrNull() ?: return@launch
            val xp = app.xpRepository.getOrCreate(userId)

            val currentLevel = XpRepository.levelFor(xp.totalXp)
            val nextLevel = XpRepository.nextLevel(xp.totalXp)
            val xpToNext = XpRepository.xpToNext(xp.totalXp)

            // Level number (index + 1)
            val levelIndex = XpRepository.LEVELS.indexOfFirst {
                it.first == currentLevel.first
            } + 1

            // Progress % toward next level
            val progress = if (nextLevel != null) {
                val range = nextLevel.second - currentLevel.second
                val earned = xp.totalXp - currentLevel.second
                ((earned.toFloat() / range) * 100).toInt().coerceIn(0, 100)
            } else 100

            // Bind views
            findViewById<TextView>(R.id.textLevelNum).text = levelIndex.toString()
            findViewById<TextView>(R.id.textLevelName).text = currentLevel.first
            findViewById<TextView>(R.id.textXpToNext).text =
                if (nextLevel != null) "$xpToNext XP to ${nextLevel.first}"
                else "Max level reached!"
            findViewById<android.widget.ProgressBar>(R.id.xpProgressBar).progress = progress
            findViewById<CircularProgressIndicator>(R.id.xpCircularProgress).progress = progress
            findViewById<TextView>(R.id.textTotalXp).text = "${xp.totalXp} XP"
            findViewById<TextView>(R.id.textStreakBadge).text = "${xp.streakDays} day streak"
            findViewById<TextView>(R.id.textStreakCount).text = "${xp.streakDays} days"

            // Display App Open Streak
            findViewById<TextView>(R.id.textAppOpenStreak).text = "${xp.appOpenStreakDays} days"
            findViewById<TextView>(R.id.textAppOpenStreakBadge).text = "${xp.appOpenStreakDays} day open streak"

            // Streak day circles
            buildStreakDots(xp.streakDays)

            // Badges
            val badges = app.xpRepository.getBadges(userId)
            val rvBadges = findViewById<RecyclerView>(R.id.rvBadges)
            rvBadges.layoutManager = LinearLayoutManager(this@XpActivity, LinearLayoutManager.HORIZONTAL, false)
            rvBadges.adapter = BadgeAdapter(badges)

            // Levels list
            val levelRows = XpRepository.LEVELS.map { (name, xpReq, icon) ->
                LevelAdapter.LevelRow(icon, name, xpReq, xp.totalXp)
            }
            val rv = findViewById<RecyclerView>(R.id.rvLevels)
            rv.layoutManager = LinearLayoutManager(this@XpActivity)
            rv.adapter = LevelAdapter(levelRows)
        }
    }

    private fun buildStreakDots(streakDays: Int) {
        val container = findViewById<LinearLayout>(R.id.layoutStreakDays)
        container.removeAllViews()
        val days = listOf("M", "T", "W", "T", "F", "S", "S")
        val dp = resources.displayMetrics.density
        days.forEachIndexed { index, label ->
            val done = index < streakDays.coerceAtMost(7)
            val tv = TextView(this)
            tv.text = label
            tv.textSize = 10f
            tv.gravity = android.view.Gravity.CENTER
            val size = (36 * dp).toInt()
            val lp = LinearLayout.LayoutParams(size, size)
            lp.marginEnd = (6 * dp).toInt()
            tv.layoutParams = lp
            tv.setBackgroundResource(
                if (done) R.drawable.bg_month_chip_selected
                else R.drawable.bg_month_chip_unselected
            )
            tv.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                    this,
                    if (done) R.color.teal_dark else R.color.white
                )
            )
            container.addView(tv)
        }
    }
}