package md.victoriabank.mia.merchant.data

import com.google.gson.annotations.SerializedName

// OAuth Token Response
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

// QR Code Create Request
data class QRCreateRequest(
    val iban: String,
    val amount: String? = null,
    val currency: String = "MDL",
    val ttl: Int? = null,
    val description: String? = null,
    val payerAccount: String? = null
)

// QR Code Create Response
data class QRCreateResponse(
    val qrId: String,
    val qrData: String,
    val expiresAt: String? = null,
    val status: String
)

// QR Status Response
data class QRStatusResponse(
    val qrId: String,
    val status: String,
    val amount: String? = null,
    val payerAccount: String? = null,
    val transactionId: String? = null,
    val completedAt: String? = null
)

// QR Extension Request
data class QRExtension(
    val qrId: String,
    val ttl: Int
)

// Extension Status Response
data class ExtensionStatus(
    val qrId: String,
    val expiresAt: String,
    val status: String
)

// Reconciliation Response
data class ReconciliationResponse(
    val transactions: List<Transaction>
)

data class Transaction(
    val transactionId: String,
    val qrId: String,
    val amount: String,
    val currency: String,
    val payerAccount: String,
    val status: String,
    val completedAt: String
)

// Signal/Webhook Payload
data class SignalPayload(
    val qrId: String,
    val status: String,
    val transactionId: String? = null,
    val amount: String? = null,
    val payerAccount: String? = null,
    val completedAt: String? = null
)
