package ir.jrweb.trakonesh

import android.app.KeyguardManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PopupActivity : AppCompatActivity() {

    private var autoCloseTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_popup)

        val bankName = intent.getStringExtra("BANK_NAME") ?: "تراکنش بانکی"
        val isDeposit = intent.getBooleanExtra("IS_DEPOSIT", true)
        val amount = intent.getStringExtra("AMOUNT") ?: "۰ تومان"
        val body = intent.getStringExtra("BODY") ?: ""

        val container = findViewById<LinearLayout>(R.id.popupContainer)
        val tvBank = findViewById<TextView>(R.id.tvPopupBankName)
        val tvType = findViewById<TextView>(R.id.tvPopupType)
        val tvAmount = findViewById<TextView>(R.id.tvPopupAmount)
        val tvBody = findViewById<TextView>(R.id.tvPopupBody)
        val btnClose = findViewById<Button>(R.id.btnClosePopup)

        tvBank.text = bankName
        tvAmount.text = amount
        tvBody.text = body

        if (isDeposit) {
            container.setBackgroundResource(R.drawable.bg_popup_deposit)
            tvType.text = "واریز به حساب"
            val greenColor = ContextCompat.getColor(this, R.color.deposit_text)
            tvType.setTextColor(greenColor)
            tvAmount.setTextColor(greenColor)
            playTone(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            container.setBackgroundResource(R.drawable.bg_popup_withdraw)
            tvType.text = "برداشت / کسر از حساب"
            val redColor = ContextCompat.getColor(this, R.color.withdraw_text)
            tvType.setTextColor(redColor)
            tvAmount.setTextColor(redColor)
            playTone(RingtoneManager.TYPE_ALARM)
        }

        // تایمر بستن خودکار بعد از ۱۰ ثانیه
        autoCloseTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                btnClose.text = "تایید و بستن ($sec)"
            }

            override fun onFinish() {
                finish()
            }
        }.start()

        btnClose.setOnClickListener {
            autoCloseTimer?.cancel()
            finish()
        }
    }

    private fun playTone(type: Int) {
        try {
            val uri = RingtoneManager.getDefaultUri(type)
            val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            ringtone?.play()
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        autoCloseTimer?.cancel()
        super.onDestroy()
    }
}
