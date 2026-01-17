package md.victoriabank.mia.merchant.api

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking
import md.victoriabank.mia.merchant.BuildConfig
import md.victoriabank.mia.merchant.utils.SecurePreferences
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(private val context: Context) {
    
    private val securePrefs = SecurePreferences(context)
    
    companion object {
        private const val TAG = "RetrofitClient"
        private const val TIMEOUT = 30L
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val token = securePrefs.getAccessToken()
        val request = if (token != null && !chain.request().url.encodedPath.contains("/identity/token")) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
            // Dacă primim 401 și avem refresh token, încercăm să reînnoim token-ul
            if (response.code == 401) {
                val refreshToken = securePrefs.getRefreshToken()
                if (refreshToken != null) {
                    try {
                        val newToken = runBlocking {
                            refreshAccessToken(refreshToken)
                        }
                        if (newToken != null) {
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to refresh token", e)
                    }
                }
            }
            return null
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
        .build()

    private fun getBaseUrl(isTestMode: Boolean): String {
        return if (isTestMode) {
            BuildConfig.BASE_URL_TEST
        } else {
            BuildConfig.BASE_URL_PROD
        }
    }

    fun getApi(isTestMode: Boolean = true): VictoriaBankApi {
        return Retrofit.Builder()
            .baseUrl(getBaseUrl(isTestMode))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VictoriaBankApi::class.java)
    }

    private suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val response = getApi().getToken(
                grantType = "refresh_token",
                refreshToken = refreshToken
            )
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    securePrefs.saveTokens(
                        accessToken = tokenResponse.accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        expiresIn = tokenResponse.expiresIn
                    )
                    tokenResponse.accessToken
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token", e)
            null
        }
    }

    // Funcție helper pentru autentificare
    suspend fun authenticate(username: String, password: String): Result<Boolean> {
        return try {
            val response = getApi().getToken(
                grantType = "password",
                username = username,
                password = password
            )
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    securePrefs.saveTokens(
                        accessToken = tokenResponse.accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        expiresIn = tokenResponse.expiresIn
                    )
                    Result.success(true)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Authentication failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error", e)
            Result.failure(e)
        }
    }

    fun clearTokens() {
        securePrefs.clearTokens()
    }
}
