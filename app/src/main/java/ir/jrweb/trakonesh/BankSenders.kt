package ir.jrweb.trakonesh

import android.content.Context

object BankSenders {
    val DEFAULT_SENDERS = setOf(
        "TejaratBank", "Refah Bank", "Bank Maskn", "Bank Mellat", "B.Pasargad",
        "PARSIANBANK", "KESHAVARZI", "SEPAH BANK", "ResalatBank", "B.Karafarin",
        "AyandehBank", "IZBANK", "989999878936", "9830009419", "98700717",
        "98500014", "9850004001", "982000880", "984000505", "9850008505",
        "98500036", "985000350545", "98300069000", "9850001050", "982000",
        "985000157", "9820008955", "9830004860", "98300088", "9830007474",
        "9820004747", "5000474700066", "98300032", "989999960000", "9847150000",
        "98500029", "9850003505", "98987079", "9820004008", "9850008528",
        "9810008528", "982000222", "98100025", "98500096", "9850001833",
        "9830003304", "9820004000", "9830006417", "98100089", "9820003936",
        "9830004293", "9810003477", "9810008919", "9891005303", "30007171",
        "98500041000", "98100090", "9810009898", "98400020", "982000878",
        "98100069", "9850004490", "985000112101", "983000131", "9850004930",
        "98999948108", "98500035", "983000769", "983000300", "98200022222",
        "9899902318"
    ).map { it.lowercase().replace("+", "").trim() }.toSet()

    fun isBankSender(context: Context, sender: String): Boolean {
        val cleanSender = sender.lowercase().replace("+", "").trim()
        val allSenders = DEFAULT_SENDERS + TransactionStorage.getCustomSenders(context)

        if (allSenders.any { cleanSender == it || cleanSender.contains(it) || it.contains(cleanSender) }) {
            return true
        }
        if (cleanSender.startsWith("09") && cleanSender.length == 11) return false
        if (cleanSender.startsWith("989") && cleanSender.length == 12) return false
        return false
    }
}
