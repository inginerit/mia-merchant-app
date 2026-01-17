package md.victoriabank.mia.merchant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): Map<String, Any>? {
        if (value == null) return null
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Any>?): String? {
        return if (map == null) null else gson.toJson(map)
    }
}

@Database(
    entities = [QREntity::class, TransactionEntity::class, MerchantConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun qrDao(): QRDao
    abstract fun transactionDao(): TransactionDao
    abstract fun merchantConfigDao(): MerchantConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mia_merchant_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
