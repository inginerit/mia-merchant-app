package md.victoriabank.mia.merchant.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QRCodeGenerator {

    fun generateQRCode(
        text: String,
        width: Int = 512,
        height: Int = 512,
        backgroundColor: Int = Color.WHITE,
        foregroundColor: Int = Color.BLACK
    ): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }

            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) foregroundColor else backgroundColor
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
