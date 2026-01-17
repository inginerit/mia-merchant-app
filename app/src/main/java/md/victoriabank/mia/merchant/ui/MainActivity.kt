package md.victoriabank.mia.merchant.ui

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import md.victoriabank.mia.merchant.R
import md.victoriabank.mia.merchant.ui.fragments.*
import md.victoriabank.mia.merchant.utils.SecurePreferences

class MainActivity : AppCompatActivity() {

    private lateinit var securePrefs: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        securePrefs = SecurePreferences(this)

        val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        setupBottomNavigation(bottomNavigation)

        // Verificăm dacă sunt configurate credențialele
        if (!securePrefs.hasCredentials()) {
            loadFragment(SettingsFragment())
        } else {
            loadFragment(GenerateQRFragment())
        }
    }

    private fun setupBottomNavigation(bottomNavigation: BottomNavigationView) {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_generate_qr -> {
                    loadFragment(GenerateQRFragment())
                    true
                }
                R.id.nav_qr_list -> {
                    loadFragment(QRListFragment())
                    true
                }
                R.id.nav_reports -> {
                    loadFragment(ReportsFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}