package md.victoriabank.mia.merchant.api

import android.content.Context
import md.victoriabank.mia.merchant.utils.SecurePreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(private val context: Context) {
    private val securePrefs = SecurePreferences(context)
    
    companion object {
        private const val BASE_URL_TEST = "https://test-ipspj.victoriabank.md/"
        private const val BASE_URL_PROD = "https://ips-api-pj.vb.md/"
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
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    fun getApi(isTestMode: Boolean = true): VictoriaBankApi {
        val baseUrl = if (isTestMode) BASE_URL_TEST else BASE_URL_PROD
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VictoriaBankApi::class.java)
    }
    
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
                        expiresIn = tokenResponse.expiresIn
                    )
                    Result.success(true)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Auth failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
