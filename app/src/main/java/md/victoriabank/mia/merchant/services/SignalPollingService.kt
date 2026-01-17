package md.victoriabank.mia.merchant.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import md.victoriabank.mia.merchant.R
import md.victoriabank.mia.merchant.api.RetrofitClient
import md.victoriabank.mia.merchant.data.AppDatabase
import md.victoriabank.mia.merchant.data.TransactionEntity
import md.victoriabank.mia.merchant.ui.MainActivity
import md.victoriabank.mia.merchant.utils.DateUtils
import md.victoriabank.mia.merchant.utils.SecurePreferences
import java.util.concurrent.TimeUnit

class SignalPollingService(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SignalPollingService"
        const val CHANNEL_ID = "MIA_PAYMENT_CHANNEL"
        const val NOTIFICATION_ID = 1001
        
        const val KEY_QR_EXTENSION_UUID = "qr_extension_uuid"
        const val KEY_QR_HEADER_UUID = "qr_header_uuid"
        const val KEY_EXPIRY_TIMESTAMP = "expiry_timestamp"
        const val KEY_POLLING_INTERVAL = "polling_interval"

        fun startPolling(
            context: Context,
            qrExtensionUUID: String,
            qrHeaderUUID: String,
            expiryTimestamp: Long,
            pollingIntervalSeconds: Int = 20
        ) {
            val data = Data.Builder()
                .putString(KEY_QR_EXTENSION_UUID, qrExtensionUUID)
                .putString(KEY_QR_HEADER_UUID, qrHeaderUUID)
                .putLong(KEY_EXPIRY_TIMESTAMP, expiryTimestamp)
                .putInt(KEY_POLLING_INTERVAL, pollingIntervalSeconds)
                .build()

            val pollingRequest = PeriodicWorkRequestBuilder<SignalPollingService>(
                pollingIntervalSeconds.toLong(),
                TimeUnit.SECONDS,
                5, // flexInterval
                TimeUnit.SECONDS
            )
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("polling_$qrExtensionUUID")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "polling_$qrExtensionUUID",
                ExistingPeriodicWorkPolicy.KEEP,
                pollingRequest
            )

            Log.d(TAG, "Polling started for QR: $qrExtensionUUID")
        }

        fun stopPolling(context: Context, qrExtensionUUID: String) {
            WorkManager.getInstance(context).cancelAllWorkByTag("polling_$qrExtensionUUID")
            Log.d(TAG, "Polling stopped for QR: $qrExtensionUUID")
        }
    }

    private val securePrefs = SecurePreferences(applicationContext)
    private val retrofitClient = RetrofitClient(applicationContext)
    private val database = AppDatabase.getDatabase(applicationContext)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val qrExtensionUUID = inputData.getString(KEY_QR_EXTENSION_UUID) ?: return@withContext Result.failure()
                val qrHeaderUUID = inputData.getString(KEY_QR_HEADER_UUID) ?: return@withContext Result.failure()
                val expiryTimestamp = inputData.getLong(KEY_EXPIRY_TIMESTAMP, 0)

                // Verifică dacă QR-ul a expirat
                if (DateUtils.isExpired(expiryTimestamp)) {
                    Log.d(TAG, "QR expired, stopping polling")
                    database.qrDao().updateQRStatus(qrExtensionUUID, "EXPIRED")
                    stopPolling(applicationContext, qrExtensionUUID)
                    return@withContext Result.success()
                }

                Log.d(TAG, "Polling status for QR: $qrExtensionUUID")

                // Verifică statusul QR-ului
                val api = retrofitClient.getApi(securePrefs.isTestMode())
                val response = api.getExtensionStatus(
                    authorization = "Bearer ${securePrefs.getAccessToken()}",
                    qrExtensionUUID = qrExtensionUUID,
                    nbOfTxs = 1
                )

                if (response.isSuccessful) {
                    response.body()?.let { extensionStatus ->
                        Log.d(TAG, "Extension status: ${extensionStatus.status}")

                        when (extensionStatus.status) {
                            "Paid" -> {
                                // Găsim tranzacția
                                val payment = extensionStatus.payments.firstOrNull()
                                if (payment != null) {
                                    // Salvăm tranzacția
                                    val transaction = TransactionEntity(
                                        qrExtensionUUID = qrExtensionUUID,
                                        reference = payment.reference,
                                        amount = payment.amount.sum.toDoubleOrNull() ?: 0.0,
                                        currency = payment.amount.currency,
                                        signalCode = "Payment",
                                        paymentDate = DateUtils.getCurrentDateISO(),
                                        paymentTime = DateUtils.getCurrentDateTimeISO(),
                                        receivedAt = System.currentTimeMillis()
                                    )
                                    database.transactionDao().insertTransaction(transaction)
                                    
                                    // Actualizăm statusul QR
                                    database.qrDao().updateQRStatus(qrExtensionUUID, "PAID")

                                    // Trimitem notificare
                                    showPaymentNotification(payment.amount.sum, payment.amount.currency)

                                    // Oprim polling-ul
                                    stopPolling(applicationContext, qrExtensionUUID)
                                }
                            }
                            "Expired" -> {
                                database.qrDao().updateQRStatus(qrExtensionUUID, "EXPIRED")
                                stopPolling(applicationContext, qrExtensionUUID)
                            }
                            "Cancelled" -> {
                                database.qrDao().updateQRStatus(qrExtensionUUID, "CANCELLED")
                                stopPolling(applicationContext, qrExtensionUUID)
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                }

                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Polling error", e)
                Result.retry()
            }
        }
    }

    private fun showPaymentNotification(amount: String, currency: String) {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Plată primită!")
            .setContentText("Ați primit o plată de $amount $currency")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Plăți MIA",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificări pentru plățile primite prin MIA"
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
