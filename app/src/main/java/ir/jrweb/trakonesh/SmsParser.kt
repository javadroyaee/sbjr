package ir.jrweb.trakonesh

import java.text.DecimalFormat

data class TransactionInfo(
    val bankName: String,
    val isDeposit: Boolean,
    val amountToman: Long,
    val formattedAmount: String,
    val rawBody: String
)

object SmsParser {
    fun parse(sender: String, body: String): TransactionInfo? {
        val normalizedBody = body
            .replace("۰", "0").replace("۱", "1").replace("۲", "2")
            .replace("۳", "3").replace("۴", "4").replace("۵", "5")
            .replace("۶", "6").replace("۷", "7").replace("۸", "8")
            .replace("۹", "9").replace("٬", ",")

        val depositKeywords = listOf("واریز", "انتقال به", "افزایش", "+")
        val withdrawKeywords = listOf("برداشت", "خرید", "کسر", "انتقال از", "کاهش", "-")

        val isDeposit = depositKeywords.any { normalizedBody.contains(it) } &&
                !normalizedBody.contains("برداشت") && !normalizedBody.contains("کسر")
        val isWithdraw = withdrawKeywords.any { normalizedBody.contains(it) }

        if (!isDeposit && !isWithdraw) return null

        val regex = Regex("""([0-9]{1,3}(?:,[0-9]{3})+|[0-9]{4,})""")
        val matches = regex.findAll(normalizedBody).mapNotNull {
            it.value.replace(",", "").toLongOrNull()
        }.toList()

        val amountRial = matches.firstOrNull { it >= 1000 } ?: return null
        val amountToman = if (normalizedBody.contains("تومان")) amountRial else amountRial / 10
        val formatter = DecimalFormat("#,###")

        return TransactionInfo(
            bankName = detectBankName(sender, normalizedBody),
            isDeposit = isDeposit,
            amountToman = amountToman,
            formattedAmount = "${formatter.format(amountToman)} تومان",
            rawBody = body
        )
    }

    private fun detectBankName(sender: String, body: String): String {
        val text = "$sender $body".lowercase()
        return when {
            text.contains("mellat") || text.contains("ملت") -> "بانک ملت"
            text.contains("tejarat") || text.contains("تجارت") -> "بانک تجارت"
            text.contains("sepah") || text.contains("سپه") -> "بانک سپه"
            text.contains("melli") || text.contains("ملی") -> "بانک ملی"
            text.contains("pasargad") || text.contains("پاسارگاد") -> "بانک پاسارگاد"
            text.contains("parsian") || text.contains("پارسیان") -> "بانک پارسیان"
            text.contains("keshavarzi") || text.contains("کشاورزی") -> "بانک کشاورزی"
            text.contains("maskan") || text.contains("مسکن") -> "بانک مسکن"
            text.contains("refah") || text.contains("رفاه") -> "بانک رفاه"
            text.contains("resalat") || text.contains("رسالت") -> "بانک رسالت"
            text.contains("saderat") || text.contains("صادرات") -> "بانک صادرات"
            text.contains("ayandeh") || text.contains("آینده") -> "بانک آینده"
            else -> "تراکنش بانکی ($sender)"
        }
    }
}
