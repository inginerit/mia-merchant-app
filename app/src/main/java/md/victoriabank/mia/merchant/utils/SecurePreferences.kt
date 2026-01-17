package md.victoriabank.mia.merchant.utils

import android.content.Context
import android.content.SharedPreferences

class SecurePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mia_prefs", Context.MODE_PRIVATE)
    
    fun saveCredentials(username: String, password: String, iban: String) {
        prefs.edit().apply {
            putString("username", username)
            putString("password", password)
            putString("iban", iban)
            apply()
        }
    }
    
    fun getUsername(): String? = prefs.getString("username", null)
    fun getPassword(): String? = prefs.getString("password", null)
    fun getIban(): String? = prefs.getString("iban", null)
    
    fun saveTokens(accessToken: String, expiresIn: Int) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putLong("token_expires_at", System.currentTimeMillis() + (expiresIn * 1000L))
            apply()
        }
    }
    
    fun getAccessToken(): String? {
        val token = prefs.getString("access_token", null)
        val expiresAt = prefs.getLong("token_expires_at", 0)
        return if (System.currentTimeMillis() < expiresAt) token else null
    }
    
    fun setTestMode(isTest: Boolean) {
        prefs.edit().putBoolean("test_mode", isTest).apply()
    }
    
    fun isTestMode(): Boolean = prefs.getBoolean("test_mode", true)
    
    fun clear() {
        prefs.edit().clear().apply()
    }
}
