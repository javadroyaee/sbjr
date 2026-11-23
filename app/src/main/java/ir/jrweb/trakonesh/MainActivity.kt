package ir.jrweb.trakonesh

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvDailyStats: TextView
    private lateinit var tvCustomSendersList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvDailyStats = findViewById(R.id.tvDailyStats)
        tvCustomSendersList = findViewById(R.id.tvCustomSendersList)

        val btnPermissions = findViewById<Button>(R.id.btnGrantPermissions)
        val btnResetDaily = findViewById<Button>(R.id.btnResetDaily)
        val etCustomSender = findViewById<EditText>(R.id.etCustomSender)
        val btnAddSender = findViewById<Button>(R.id.btnAddSender)
        val btnTestDeposit = findViewById<Button>(R.id.btnTestDeposit)
        val btnTestWithdraw = findViewById<Button>(R.id.btnTestWithdraw)

        TransactionMonitorService.startOrUpdate(this)

        btnPermissions.setOnClickListener { requestAllPermissions() }

        btnResetDaily.setOnClickListener {
            TransactionStorage.resetDaily(this)
            TransactionMonitorService.startOrUpdate(this)
            refreshUI()
            Toast.makeText(this, "آمار امروز صفر شد", Toast.LENGTH_SHORT).show()
        }

        btnAddSender.setOnClickListener {
            val input = etCustomSender.text.toString().trim()
            if (input.isNotEmpty()) {
                TransactionStorage.addCustomSender(this, input)
                etCustomSender.text.clear()
                refreshUI()
                Toast.makeText(this, "سرشماره جدید اضافه شد", Toast.LENGTH_SHORT).show()
            }
        }

        btnTestDeposit.setOnClickListener {
            val intent = Intent(this, PopupActivity::class.java).apply {
                putExtra("BANK_NAME", "بانک ملت")
                putExtra("IS_DEPOSIT", true)
                putExtra("AMOUNT", "۲,۵۰۰,۰۰۰ تومان")
                putExtra("BODY", "واریز مبلغ ۲۵,۰۰۰,۰۰۰ ریال به حساب شما")
            }
            startActivity(intent)
        }

        btnTestWithdraw.setOnClickListener {
            val intent = Intent(this, PopupActivity::class.java).apply {
                putExtra("BANK_NAME", "بانک تجارت")
                putExtra("IS_DEPOSIT", false)
                putExtra("AMOUNT", "۴۵۰,۰۰۰ تومان")
                putExtra("BODY", "برداشت مبلغ ۴,۵۰۰,۰۰۰ ریال از حساب شما")
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        val smsGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val overlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true

        tvStatus.text = if (smsGranted && overlayGranted) {
            "وضعیت: فعال و آماده دریافت پیامک‌های بانکی"
        } else {
            "وضعیت: نیازمند تایید مجوز پیامک و نمایش روی سایر برنامه‌ها"
        }

        val (dep, wit) = TransactionStorage.getDailySummary(this)
        tvDailyStats.text = "آمار امروز:\nواریز: $dep  |  برداشت: $wit"

        val customList = TransactionStorage.getCustomSenders(this)
        tvCustomSendersList.text = if (customList.isEmpty()) {
            "سرشماره‌های افزوده شده: ندارد"
        } else {
            "سرشماره‌های افزوده شده: ${customList.joinToString(", ")}"
        }
    }

    private fun requestAllPermissions() {
        val perms = mutableListOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        ActivityCompat.requestPermissions(this, perms.toTypedArray(), 101)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
        TransactionMonitorService.startOrUpdate(this)
    }
}
