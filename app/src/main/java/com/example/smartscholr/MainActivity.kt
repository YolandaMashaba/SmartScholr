package com.example.smartscholr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        navView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_reports -> ReportsFragment()
                R.id.nav_rewards -> RewardsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> DashboardFragment()
            }
            loadFragment(fragment)
            true
        }

        // Set default fragment
        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.nav_dashboard
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.main_fragment_container, fragment)
            .commit()
    }
}
