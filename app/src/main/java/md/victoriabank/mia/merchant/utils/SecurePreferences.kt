package md.victoriabank.mia.merchant.utils

import android.content.Context
import android.content.SharedPreferences

class SecurePreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "mia_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IBAN = "iban"
        private const val KEY_TEST_MODE = "test_mode"
        private const val KEY_WEBHOOK_URL = "webhook_url"
        private const val KEY_DEFAULT_TTL = "default_ttl"
    }

    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Int) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000L)
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
            apply()
        }
    }

    fun getAccessToken(): String? {
        val token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        val expiry = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
        return if (token != null && System.currentTimeMillis() < expiry) token else null
    }

    fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
            apply()
        }
    }

    fun saveCredentials(username: String, password: String, iban: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
            putString(KEY_IBAN, iban)
            apply()
        }
    }

    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)
    fun getPassword(): String? = sharedPreferences.getString(KEY_PASSWORD, null)
    fun getIban(): String? = sharedPreferences.getString(KEY_IBAN, null)
    fun setTestMode(isTestMode: Boolean) = sharedPreferences.edit().putBoolean(KEY_TEST_MODE, isTestMode).apply()
    fun isTestMode(): Boolean = sharedPreferences.getBoolean(KEY_TEST_MODE, true)
    fun setWebhookUrl(url: String) = sharedPreferences.edit().putString(KEY_WEBHOOK_URL, url).apply()
    fun getWebhookUrl(): String? = sharedPreferences.getString(KEY_WEBHOOK_URL, null)
    fun setDefaultTTL(ttl: Int) = sharedPreferences.edit().putInt(KEY_DEFAULT_TTL, ttl).apply()
    fun getDefaultTTL(): Int = sharedPreferences.getInt(KEY_DEFAULT_TTL, 360)
    fun hasCredentials(): Boolean = getUsername() != null && getPassword() != null && getIban() != null
}