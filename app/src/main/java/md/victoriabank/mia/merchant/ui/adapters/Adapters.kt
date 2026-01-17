package md.victoriabank.mia.merchant.ui.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import md.victoriabank.mia.merchant.R
import md.victoriabank.mia.merchant.data.QREntity
import md.victoriabank.mia.merchant.data.TransactionInfo
import md.victoriabank.mia.merchant.utils.DateUtils

// QR Adapter
class QRAdapter : ListAdapter<QREntity, QRAdapter.QRViewHolder>(QRDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_qr_code, parent, false)
        return QRViewHolder(view)
    }

    override fun onBindViewHolder(holder: QRViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class QRViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivQRCode: ImageView = itemView.findViewById(R.id.iv_qr_code)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)

        fun bind(qr: QREntity) {
            // Decodăm imaginea QR din Base64
            try {
                val imageBytes = Base64.decode(qr.qrAsImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ivQRCode.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            tvDescription.text = qr.description ?: "No description"
            tvAmount.text = "${qr.amount} MDL"
            tvStatus.text = qr.status
            tvDate.text = DateUtils.formatDateTimeForDisplay(
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date(qr.createdAt))
            )

            // Colorăm statusul
            val color = when (qr.status) {
                "ACTIVE" -> android.graphics.Color.parseColor("#2dce89")
                "PAID" -> android.graphics.Color.parseColor("#007bff")
                "EXPIRED" -> android.graphics.Color.parseColor("#6c757d")
                "CANCELLED" -> android.graphics.Color.parseColor("#dc3545")
                else -> android.graphics.Color.BLACK
            }
            tvStatus.setTextColor(color)
        }
    }

    class QRDiffCallback : DiffUtil.ItemCallback<QREntity>() {
        override fun areItemsTheSame(oldItem: QREntity, newItem: QREntity): Boolean {
            return oldItem.qrExtensionUUID == newItem.qrExtensionUUID
        }

        override fun areContentsTheSame(oldItem: QREntity, newItem: QREntity): Boolean {
            return oldItem == newItem
        }
    }
}

// Transaction Adapter
class TransactionAdapter(
    private val transactions: List<TransactionInfo>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPayerName: TextView = itemView.findViewById(R.id.tv_payer_name)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)

        fun bind(transaction: TransactionInfo) {
            tvPayerName.text = transaction.payerName
            tvAmount.text = "${transaction.transactionAmount} MDL"
            tvDate.text = "${transaction.date} ${transaction.time}"
            tvStatus.text = transaction.transactionStatus
            tvType.text = transaction.transactionType

            // Colorăm statusul
            val color = when (transaction.transactionStatus) {
                "Approved" -> android.graphics.Color.parseColor("#2dce89")
                "Pending" -> android.graphics.Color.parseColor("#fb6340")
                "Rejected" -> android.graphics.Color.parseColor("#dc3545")
                else -> android.graphics.Color.BLACK
            }
            tvStatus.setTextColor(color)
        }
    }
}
