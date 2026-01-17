package md.victoriabank.mia.merchant.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import md.victoriabank.mia.merchant.R
import md.victoriabank.mia.merchant.api.RetrofitClient
import md.victoriabank.mia.merchant.data.*
import md.victoriabank.mia.merchant.services.SignalPollingService
import md.victoriabank.mia.merchant.ui.adapters.QRAdapter
import md.victoriabank.mia.merchant.ui.adapters.TransactionAdapter
import md.victoriabank.mia.merchant.utils.*
import java.util.*

class GenerateQRFragment : Fragment() {
    private lateinit var securePrefs: SecurePreferences
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var database: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_generate_qr, container, false)
        securePrefs = SecurePreferences(requireContext())
        retrofitClient = RetrofitClient(requireContext())
        database = AppDatabase.getDatabase(requireContext())
        setupUI(view)
        return view
    }

    private fun setupUI(view: View) {
        val spinnerQRType = view.findViewById<Spinner>(R.id.spinner_qr_type)
        val etAmount = view.findViewById<EditText>(R.id.et_amount)
        val etAmountMin = view.findViewById<EditText>(R.id.et_amount_min)
        val etAmountMax = view.findViewById<EditText>(R.id.et_amount_max)
        val etTTL = view.findViewById<EditText>(R.id.et_ttl)
        val etDescription = view.findViewById<EditText>(R.id.et_description)
        val btnGenerate = view.findViewById<Button>(R.id.btn_generate_qr)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val ivQRCode = view.findViewById<ImageView>(R.id.iv_qr_code)
        val tvQRStatus = view.findViewById<TextView>(R.id.tv_qr_status)

        spinnerQRType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            arrayOf("DYNM - Dinamic", "STAT - Fixed", "STAT - Controlled", "STAT - Free"))

        spinnerQRType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { etAmount.visibility = View.VISIBLE; etAmountMin.visibility = View.GONE; etAmountMax.visibility = View.GONE; etTTL.visibility = View.VISIBLE }
                    1 -> { etAmount.visibility = View.VISIBLE; etAmountMin.visibility = View.GONE; etAmountMax.visibility = View.GONE; etTTL.visibility = View.GONE }
                    2 -> { etAmount.visibility = View.GONE; etAmountMin.visibility = View.VISIBLE; etAmountMax.visibility = View.VISIBLE; etTTL.visibility = View.GONE }
                    3 -> { etAmount.visibility = View.GONE; etAmountMin.visibility = View.GONE; etAmountMax.visibility = View.GONE; etTTL.visibility = View.GONE }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnGenerate.setOnClickListener {
            val iban = securePrefs.getIban() ?: return@setOnClickListener showError("IBAN not configured")
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val (qrType, amountType) = when (spinnerQRType.selectedItemPosition) {
                        0 -> "DYNM" to "Fixed"; 1 -> "STAT" to "Fixed"; 2 -> "STAT" to "Controlled"; 3 -> "STAT" to "Free"; else -> "DYNM" to "Fixed"
                    }
                    val extension = QRExtension(
                        creditorAccount = CreditorAccount(iban),
                        amount = if (spinnerQRType.selectedItemPosition <= 1) Amount(etAmount.text.toString()) else null,
                        amountMin = if (spinnerQRType.selectedItemPosition == 2) Amount(etAmountMin.text.toString()) else null,
                        amountMax = if (spinnerQRType.selectedItemPosition == 2) Amount(etAmountMax.text.toString()) else null,
                        dba = "Merchant", remittanceInfo4Payer = etDescription.text.toString(),
                        creditorRef = "REF-${System.currentTimeMillis()}",
                        ttl = if (qrType == "DYNM") TTL(etTTL.text.toString().toIntOrNull() ?: 360) else null
                    )
                    val api = retrofitClient.getApi(securePrefs.isTestMode())
                    val response = api.createQR("Bearer ${securePrefs.getAccessToken()}", 512, 512, QRCreateRequest(QRHeader(qrType, amountType), extension))
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        response.body()?.let { qrResponse ->
                            val bitmap = QRCodeGenerator.generateQRCode(qrResponse.qrAsText, 512, 512)
                            ivQRCode.setImageBitmap(bitmap); ivQRCode.visibility = View.VISIBLE
                            val ttlMinutes = if (qrType == "DYNM") etTTL.text.toString().toIntOrNull() ?: 360 else 0
                            val expiryTimestamp = if (qrType == "DYNM") DateUtils.getExpiryTimestamp(ttlMinutes) else 0
                            database.qrDao().insertQR(QREntity(qrResponse.qrExtensionUUID, qrResponse.qrHeaderUUID, qrResponse.qrAsText, qrResponse.qrAsImage,
                                qrType, amountType, iban, etAmount.text.toString(), ttlMinutes, System.currentTimeMillis(), expiryTimestamp, "ACTIVE", etDescription.text.toString()))
                            if (qrType == "DYNM") SignalPollingService.startPolling(requireContext(), qrResponse.qrExtensionUUID, qrResponse.qrHeaderUUID, expiryTimestamp, 20)
                            tvQRStatus.text = "QR generat cu succes!"; tvQRStatus.visibility = View.VISIBLE
                        }
                    } else showError("Error: ${response.code()}")
                } catch (e: Exception) { progressBar.visibility = View.GONE; showError("Error: ${e.message}") }
            }
        }
    }
    private fun showError(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

class QRListFragment : Fragment() {
    private lateinit var database: AppDatabase
    private lateinit var adapter: QRAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_qr_list, container, false)
        database = AppDatabase.getDatabase(requireContext())
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = QRAdapter()
        recyclerView.adapter = adapter
        lifecycleScope.launch { database.qrDao().getAllQRCodes().collectLatest { adapter.submitList(it) } }
        return view
    }
}

class ReportsFragment : Fragment() {
    private lateinit var retrofitClient: RetrofitClient
    private lateinit var securePrefs: SecurePreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)
        retrofitClient = RetrofitClient(requireContext())
        securePrefs = SecurePreferences(requireContext())
        var dateFrom = DateUtils.formatDateForAPI(DateUtils.getDateDaysAgo(7))
        var dateTo = DateUtils.getCurrentDateISO()
        
        view.findViewById<Button>(R.id.btn_date_from).setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                dateFrom = DateUtils.formatDateForAPI(Calendar.getInstance().apply { set(year, month, day) }.time)
                (it as Button).text = "From: $dateFrom"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        view.findViewById<Button>(R.id.btn_date_to).setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                dateTo = DateUtils.formatDateForAPI(Calendar.getInstance().apply { set(year, month, day) }.time)
                (it as Button).text = "To: $dateTo"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        view.findViewById<Button>(R.id.btn_load_reports).setOnClickListener {
            val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
            val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_reports)
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val response = retrofitClient.getApi(securePrefs.isTestMode()).getTransactions("Bearer ${securePrefs.getAccessToken()}", dateFrom, dateTo)
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) response.body()?.let { recyclerView.adapter = TransactionAdapter(it.transactionsInfo); recyclerView.layoutManager = LinearLayoutManager(context) }
                } catch (e: Exception) { progressBar.visibility = View.GONE; Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
        return view
    }
}

class SettingsFragment : Fragment() {
    private lateinit var securePrefs: SecurePreferences
    private lateinit var retrofitClient: RetrofitClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        securePrefs = SecurePreferences(requireContext())
        retrofitClient = RetrofitClient(requireContext())
        
        view.findViewById<EditText>(R.id.et_username).setText(securePrefs.getUsername())
        view.findViewById<EditText>(R.id.et_password).setText(securePrefs.getPassword())
        view.findViewById<EditText>(R.id.et_iban).setText(securePrefs.getIban())
        view.findViewById<Switch>(R.id.switch_test_mode).isChecked = securePrefs.isTestMode()
        view.findViewById<EditText>(R.id.et_webhook_url).setText(securePrefs.getWebhookUrl())
        view.findViewById<EditText>(R.id.et_default_ttl).setText(securePrefs.getDefaultTTL().toString())
        
        view.findViewById<Button>(R.id.btn_save_settings).setOnClickListener {
            val username = view.findViewById<EditText>(R.id.et_username).text.toString()
            val password = view.findViewById<EditText>(R.id.et_password).text.toString()
            val iban = view.findViewById<EditText>(R.id.et_iban).text.toString()
            if (username.isBlank() || password.isBlank() || iban.isBlank()) return@setOnClickListener Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                if (retrofitClient.authenticate(username, password).isSuccess) {
                    securePrefs.saveCredentials(username, password, iban)
                    securePrefs.setTestMode(view.findViewById<Switch>(R.id.switch_test_mode).isChecked)
                    securePrefs.setWebhookUrl(view.findViewById<EditText>(R.id.et_webhook_url).text.toString())
                    securePrefs.setDefaultTTL(view.findViewById<EditText>(R.id.et_default_ttl).text.toString().toIntOrNull() ?: 360)
                    Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(context, "Auth failed!", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}
