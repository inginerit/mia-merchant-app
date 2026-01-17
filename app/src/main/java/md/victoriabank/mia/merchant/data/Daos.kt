package md.victoriabank.mia.merchant.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QRDao {
    @Query("SELECT * FROM qr_codes ORDER BY createdAt DESC")
    fun getAllQRCodes(): Flow<List<QREntity>>

    @Query("SELECT * FROM qr_codes WHERE qrExtensionUUID = :uuid")
    suspend fun getQRByUUID(uuid: String): QREntity?

    @Query("SELECT * FROM qr_codes WHERE status = :status ORDER BY createdAt DESC")
    fun getQRCodesByStatus(status: String): Flow<List<QREntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQR(qr: QREntity)

    @Update
    suspend fun updateQR(qr: QREntity)

    @Query("UPDATE qr_codes SET status = :status WHERE qrExtensionUUID = :uuid")
    suspend fun updateQRStatus(uuid: String, status: String)

    @Delete
    suspend fun deleteQR(qr: QREntity)

    @Query("DELETE FROM qr_codes WHERE status = 'EXPIRED' OR status = 'PAID'")
    suspend fun cleanupOldQRCodes()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY receivedAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE qrExtensionUUID = :uuid ORDER BY receivedAt DESC")
    fun getTransactionsByQR(uuid: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE receivedAt BETWEEN :startDate AND :endDate ORDER BY receivedAt DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE receivedAt < :timestamp")
    suspend fun cleanupOldTransactions(timestamp: Long)
}

@Dao
interface MerchantConfigDao {
    @Query("SELECT * FROM merchant_config WHERE id = 1")
    fun getConfig(): Flow<MerchantConfigEntity?>

    @Query("SELECT * FROM merchant_config WHERE id = 1")
    suspend fun getConfigSync(): MerchantConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: MerchantConfigEntity)

    @Query("DELETE FROM merchant_config")
    suspend fun clearConfig()
}
