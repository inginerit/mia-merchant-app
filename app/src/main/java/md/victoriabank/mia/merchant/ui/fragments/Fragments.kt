package md.victoriabank.mia.merchant.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import md.victoriabank.mia.merchant.R
import md.victoriabank.mia.merchant.api.RetrofitClient
import md.victoriabank.mia.merchant.data.*
import md.victoriabank.mia.merchant.utils.*

class GenerateQRFragment : Fragment() {
    private lateinit var securePrefs: SecurePreferences
    private lateinit var retrofitClient: RetrofitClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_generate_qr, container, false)
        securePrefs = SecurePreferences(requireContext())
        retrofitClient = RetrofitClient(requireContext())
        
        view.findViewById<Button>(R.id.btn_generate_qr).setOnClickListener {
            Toast.makeText(context, "QR Generation - Coming soon!", Toast.LENGTH_SHORT).show()
        }
        return view
    }
}

class QRListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qr_list, container, false)
        view.findViewById<TextView>(android.R.id.text1)?.text = "QR List - Coming soon!"
        return view
    }
}

class ReportsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)
        return view
    }
}

class SettingsFragment : Fragment() {
    private lateinit var securePrefs: SecurePreferences
    private lateinit var retrofitClient: RetrofitClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        securePrefs = SecurePreferences(requireContext())
        retrofitClient = RetrofitClient(requireContext())
        
        view.findViewById<EditText>(R.id.et_username).setText(securePrefs.getUsername())
        view.findViewById<EditText>(R.id.et_password).setText(securePrefs.getPassword())
        view.findViewById<EditText>(R.id.et_iban).setText(securePrefs.getIban())
        view.findViewById<Switch>(R.id.switch_test_mode).isChecked = securePrefs.isTestMode()
        
        view.findViewById<Button>(R.id.btn_save_settings).setOnClickListener {
            val username = view.findViewById<EditText>(R.id.et_username).text.toString()
            val password = view.findViewById<EditText>(R.id.et_password).text.toString()
            val iban = view.findViewById<EditText>(R.id.et_iban).text.toString()
            
            if (username.isBlank() || password.isBlank() || iban.isBlank()) {
                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                if (retrofitClient.authenticate(username, password).isSuccess) {
                    securePrefs.saveCredentials(username, password, iban)
                    securePrefs.setTestMode(view.findViewById<Switch>(R.id.switch_test_mode).isChecked)
                    Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Auth failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return view
    }
}