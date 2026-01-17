package md.victoriabank.mia.merchant.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import md.victoriabank.mia.merchant.R
import md.victoriabank.mia.merchant.databinding.ActivityMainBinding
import md.victoriabank.mia.merchant.ui.fragments.*
import md.victoriabank.mia.merchant.utils.SecurePreferences

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var securePrefs: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        securePrefs = SecurePreferences(this)

        setupBottomNavigation()

        // Verificăm dacă sunt configurate credențialele
        if (!securePrefs.hasCredentials()) {
            // Afișăm fragmentul de configurare
            loadFragment(SettingsFragment())
        } else {
            // Afișăm fragmentul principal (Generate QR)
            loadFragment(GenerateQRFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
