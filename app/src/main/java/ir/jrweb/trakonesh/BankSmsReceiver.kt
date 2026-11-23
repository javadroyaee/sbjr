package ir.jrweb.trakonesh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class BankSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            TransactionMonitorService.startOrUpdate(context)
            return
        }

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: return
        val fullBody = messages.joinToString("") { it.messageBody ?: "" }

        if (!BankSenders.isBankSender(context, sender)) return

        val transaction = SmsParser.parse(sender, fullBody) ?: return

        TransactionStorage.addTransaction(context, transaction.isDeposit, transaction.amountToman)
        TransactionMonitorService.startOrUpdate(context)

        val popupIntent = Intent(context, PopupActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("BANK_NAME", transaction.bankName)
            putExtra("IS_DEPOSIT", transaction.isDeposit)
            putExtra("AMOUNT", transaction.formattedAmount)
            putExtra("BODY", transaction.rawBody)
        }
        context.startActivity(popupIntent)
    }
}
