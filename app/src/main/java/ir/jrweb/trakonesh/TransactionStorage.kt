package ir.jrweb.trakonesh

import android.content.Context
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TransactionStorage {
    private const val PREFS_NAME = "jrweb_trakonesh_prefs"
    private const val KEY_DATE = "current_date"
    private const val KEY_DEPOSIT = "daily_deposit"
    private const val KEY_WITHDRAW = "daily_withdraw"
    private const val KEY_CUSTOM_SENDERS = "custom_senders"

    private fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun checkAndResetIfNewDay(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_DATE, "")
        val today = todayString()
        if (savedDate != today) {
            prefs.edit()
                .putString(KEY_DATE, today)
                .putLong(KEY_DEPOSIT, 0L)
                .putLong(KEY_WITHDRAW, 0L)
                .apply()
        }
    }

    fun addTransaction(context: Context, isDeposit: Boolean, amountToman: Long) {
        checkAndResetIfNewDay(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (isDeposit) {
            val current = prefs.getLong(KEY_DEPOSIT, 0L)
            prefs.edit().putLong(KEY_DEPOSIT, current + amountToman).apply()
        } else {
            val current = prefs.getLong(KEY_WITHDRAW, 0L)
            prefs.edit().putLong(KEY_WITHDRAW, current + amountToman).apply()
        }
    }

    fun getDailySummary(context: Context): Pair<String, String> {
        checkAndResetIfNewDay(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val formatter = DecimalFormat("#,###")
        val dep = formatter.format(prefs.getLong(KEY_DEPOSIT, 0L))
        val wit = formatter.format(prefs.getLong(KEY_WITHDRAW, 0L))
        return Pair("$dep تومان", "$wit تومان")
    }

    fun resetDaily(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_DATE, todayString())
            .putLong(KEY_DEPOSIT, 0L)
            .putLong(KEY_WITHDRAW, 0L)
            .apply()
    }

    fun addCustomSender(context: Context, sender: String) {
        val clean = sender.lowercase().replace("+", "").trim()
        if (clean.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_CUSTOM_SENDERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        existing.add(clean)
        prefs.edit().putStringSet(KEY_CUSTOM_SENDERS, existing).apply()
    }

    fun getCustomSenders(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_CUSTOM_SENDERS, emptySet()) ?: emptySet()
    }
}
