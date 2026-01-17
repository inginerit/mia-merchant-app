package md.victoriabank.mia.merchant.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "qr_codes")
@TypeConverters(Converters::class)
data class QREntity(
    @PrimaryKey val qrExtensionUUID: String,
    val qrHeaderUUID: String,
    val qrAsText: String,
    val qrAsImage: String, // Base64
    val qrType: String,
    val amountType: String,
    val iban: String,
    val amount: String?, // JSON
    val ttl: Int,
    val createdAt: Long,
    val expiresAt: Long,
    val status: String, // ACTIVE, PAID, EXPIRED, CANCELLED
    val description: String? = null
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val qrExtensionUUID: String,
    val reference: String,
    val amount: Double,
    val currency: String,
    val signalCode: String,
    val paymentDate: String,
    val paymentTime: String,
    val receivedAt: Long
)

@Entity(tableName = "merchant_config")
data class MerchantConfigEntity(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val password: String, // Encrypted
    val iban: String,
    val isTestMode: Boolean,
    val webhookUrl: String,
    val defaultTtl: Int,
    val certificateData: String? = null
)
