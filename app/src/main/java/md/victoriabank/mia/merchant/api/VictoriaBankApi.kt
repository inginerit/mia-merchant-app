package md.victoriabank.mia.merchant.api

import md.victoriabank.mia.merchant.data.*
import retrofit2.Response
import retrofit2.http.*

interface VictoriaBankApi {
    
    // Authentication
    @POST("api/identity/token")
    @FormUrlEncoded
    suspend fun getToken(
        @Field("grant_type") grantType: String,
        @Field("username") username: String? = null,
        @Field("password") password: String? = null,
        @Field("refresh_token") refreshToken: String? = null
    ): Response<TokenResponse>

    // QR Code Generation
    @POST("api/v1/qr")
    suspend fun createQR(
        @Header("Authorization") authorization: String,
        @Query("width") width: Int = 300,
        @Query("height") height: Int = 300,
        @Body request: QRCreateRequest
    ): Response<QRCreateResponse>

    // Cancel QR
    @DELETE("api/v1/qr/{qrHeaderUUID}")
    suspend fun cancelQR(
        @Header("Authorization") authorization: String,
        @Path("qrHeaderUUID") qrHeaderUUID: String
    ): Response<Unit>

    // QR Status
    @GET("api/v1/qr/{qrHeaderUUID}/status")
    suspend fun getQRStatus(
        @Header("Authorization") authorization: String,
        @Path("qrHeaderUUID") qrHeaderUUID: String,
        @Query("nbOfExt") nbOfExt: Int = 5,
        @Query("nbOfTxs") nbOfTxs: Int = 5
    ): Response<QRStatusResponse>

    // New Extension (for STAT/HYBR QR)
    @POST("api/v1/qr/{qrHeaderUUID}/extentions")
    suspend fun createExtension(
        @Header("Authorization") authorization: String,
        @Path("qrHeaderUUID") qrHeaderUUID: String,
        @Body extension: QRExtension
    ): Response<QRCreateResponse>

    // Cancel Extension
    @DELETE("api/v1/qr/{qrHeaderUUID}/active-extension")
    suspend fun cancelExtension(
        @Header("Authorization") authorization: String,
        @Path("qrHeaderUUID") qrHeaderUUID: String
    ): Response<Unit>

    // Extension Status
    @GET("api/v1/qr-extensions/{qrExtensionUUID}/status")
    suspend fun getExtensionStatus(
        @Header("Authorization") authorization: String,
        @Path("qrExtensionUUID") qrExtensionUUID: String,
        @Query("nbOfTxs") nbOfTxs: Int = 5
    ): Response<ExtensionStatus>

    // Reverse Transaction (Refund)
    @DELETE("api/v1/transaction/{reference}")
    suspend fun reverseTransaction(
        @Header("Authorization") authorization: String,
        @Path("reference") reference: String
    ): Response<Unit>

    // Reconciliation
    @GET("api/v1/reconciliation/transactions/")
    suspend fun getTransactions(
        @Header("Authorization") authorization: String,
        @Query("datefrom") dateFrom: String,
        @Query("dateto") dateTo: String
    ): Response<ReconciliationResponse>

    // Get Last Signal
    @GET("api/v1/signal/{qrExtensionUUID}")
    suspend fun getLastSignal(
        @Header("Authorization") authorization: String,
        @Path("qrExtensionUUID") qrExtensionUUID: String
    ): Response<SignalPayload>
}
