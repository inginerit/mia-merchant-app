package md.victoriabank.mia.merchant.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Auth Models
data class TokenRequest(
    val grant_type: String,
    val username: String? = null,
    val password: String? = null,
    val refresh_token: String? = null
)

data class TokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val refreshToken: String,
    val refreshExpiresIn: Int
)

// QR Models
@Parcelize
data class QRCreateRequest(
    val header: QRHeader,
    val extension: QRExtension
) : Parcelable

@Parcelize
data class QRHeader(
    val qrType: String, // DYNM, STAT, HYBR
    val amountType: String, // Fixed, Controlled, Free
    val pmtContext: String = "e" // e-commerce
) : Parcelable

@Parcelize
data class QRExtension(
    val creditorAccount: CreditorAccount,
    val amount: Amount? = null,
    val amountMin: Amount? = null,
    val amountMax: Amount? = null,
    val dba: String? = null,
    val remittanceInfo4Payer: String? = null,
    val creditorRef: String? = null,
    val ttl: TTL? = null
) : Parcelable

@Parcelize
data class CreditorAccount(
    val iban: String
) : Parcelable

@Parcelize
data class Amount(
    val sum: String,
    val currency: String = "MDL"
) : Parcelable

@Parcelize
data class TTL(
    val length: Int,
    val units: String = "mm" // mm = minutes, ss = seconds
) : Parcelable

@Parcelize
data class QRCreateResponse(
    val qrHeaderUUID: String,
    val qrExtensionUUID: String,
    val qrAsText: String,
    val qrAsImage: String // Base64
) : Parcelable

// QR Status Models
data class QRStatusResponse(
    val uuid: String,
    val status: String, // Active, Paid, Expired, Cancelled, Replaced, Inactive
    val statusDtTm: String,
    val lockTtl: TTL?,
    val extensions: List<ExtensionStatus>
)

data class ExtensionStatus(
    val uuid: String,
    val isLast: Boolean,
    val status: String,
    val statusDtTm: String,
    val isHeaderLocked: Boolean,
    val ttl: TTL?,
    val payments: List<PaymentInfo>
)

data class PaymentInfo(
    val system: String,
    val reference: String,
    val amount: Amount,
    val dtTm: String? = null
)

// Signal Models
data class SignalPayload(
    val signalCode: String, // Payment, Expiration, Inactivation
    val signalDtTm: String,
    val qrHeaderUUID: String?,
    val qrExtensionUUID: String?,
    val payment: PaymentInfo?
)

// Reconciliation Models
data class ReconciliationRequest(
    val datefrom: String, // YYYY-MM-DD
    val dateto: String
)

data class ReconciliationResponse(
    val transactionsInfo: List<TransactionInfo>
)

data class TransactionInfo(
    val id: String, // MIA message ID
    val date: String,
    val time: String,
    val payerName: String,
    val payerIdnp: String,
    val beneficiaryIdnp: String,
    val transactionType: String, // QR, RTP, QRREFUND
    val transactionAmount: Double,
    val transactionStatus: String, // Pending, Rejected, Approved
    val destinationBankName: String,
    val transactionMessage: String,
    val paymentType: String, // IPSMIA
    val miaId: String // Reference
)

// Error Response
data class ApiError(
    val traceReference: String,
    val errorCode: String,
    val description: String
)

// Merchant Configuration
@Parcelize
data class MerchantConfig(
    val username: String,
    val password: String,
    val iban: String,
    val isTestMode: Boolean = true,
    val webhookUrl: String = "",
    val defaultTtl: Int = 360 // minutes
) : Parcelable

// QR Type Enums
enum class QRType(val value: String) {
    DYNAMIC("DYNM"),
    STATIC("STAT"),
    HYBRID("HYBR")
}

enum class AmountType(val value: String) {
    FIXED("Fixed"),
    CONTROLLED("Controlled"),
    FREE("Free")
}

enum class QRStatus {
    ACTIVE,
    PAID,
    EXPIRED,
    CANCELLED,
    REPLACED,
    INACTIVE
}

enum class SignalCode(val value: String) {
    PAYMENT("Payment"),
    EXPIRATION("Expiration"),
    INACTIVATION("Inactivation")
}
