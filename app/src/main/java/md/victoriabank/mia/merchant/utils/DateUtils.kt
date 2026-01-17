package md.victoriabank.mia.merchant.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    private const val ISO_FORMAT_WITH_TZ = "yyyy-MM-dd'T'HH:mm:ssXXX"
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val TIME_FORMAT = "HH:mm:ss"
    private const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
    private const val DISPLAY_TIME_FORMAT = "HH:mm"
    private const val DISPLAY_DATETIME_FORMAT = "dd MMM yyyy, HH:mm"

    fun getCurrentDateISO(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDateTimeISO(): String {
        val sdf = SimpleDateFormat(ISO_FORMAT_WITH_TZ, Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateForAPI(date: Date): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(date)
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val parser = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val date = parser.parse(dateString)
            val formatter = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale("ro", "MD"))
            date?.let { formatter.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDateTimeForDisplay(dateTimeString: String): String {
        return try {
            val parser = SimpleDateFormat(ISO_FORMAT_WITH_TZ, Locale.getDefault())
            val date = parser.parse(dateTimeString)
            val formatter = SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale("ro", "MD"))
            date?.let { formatter.format(it) } ?: dateTimeString
        } catch (e: Exception) {
            // Încearcă și fără timezone
            try {
                val parser2 = SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
                val date = parser2.parse(dateTimeString)
                val formatter = SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale("ro", "MD"))
                date?.let { formatter.format(it) } ?: dateTimeString
            } catch (e2: Exception) {
                dateTimeString
            }
        }
    }

    fun parseISODate(dateString: String): Date? {
        return try {
            val sdf = SimpleDateFormat(ISO_FORMAT_WITH_TZ, Locale.getDefault())
            sdf.parse(dateString)
        } catch (e: Exception) {
            try {
                val sdf = SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
                sdf.parse(dateString)
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun getDateDaysAgo(days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.time
    }

    fun getExpiryTimestamp(ttlMinutes: Int): Long {
        return System.currentTimeMillis() + (ttlMinutes * 60 * 1000L)
    }

    fun getRemainingTimeString(expiryTimestamp: Long): String {
        val remaining = expiryTimestamp - System.currentTimeMillis()
        if (remaining <= 0) return "Expirat"

        val minutes = remaining / 1000 / 60
        val seconds = (remaining / 1000) % 60

        return if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }

    fun isExpired(expiryTimestamp: Long): Boolean {
        return System.currentTimeMillis() >= expiryTimestamp
    }
}
