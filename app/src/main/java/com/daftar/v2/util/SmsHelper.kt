package com.daftar.v2.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * ⭐ أداة إرسال الرسائل النصية (SMS).
 * تستخدم Intent لفتح تطبيق الرسائل الافتراضي — بدون صلاحيات حساسة.
 */
object SmsHelper {

    /**
     * ⭐ يفتح تطبيق الرسائل مع الرقم والرسالة جاهزة.
     */
    fun openSmsApp(
        context: Context,
        phoneNumber: String,
        message: String
    ) {
        try {
            val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
            if (cleanPhone.isBlank()) return

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanPhone")
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ⭐⭐⭐ رسالة تذكير — تحدد الاتجاه بوضوح.
     *
     * @param isTheyOweMe  true  = الشخص مدين لي (لك) — أطالبه بالسداد
     *                     false = أنا مدين له (عليك) — أؤكد له أنني سأسدد
     */
    fun buildReminderMessage(
        name: String,
        balanceText: String,
        isTheyOweMe: Boolean,
        storeName: String = ""
    ): String {
        val header = if (storeName.isNotBlank()) "🏪 $storeName\n\n" else ""

        return if (isTheyOweMe) {
            // ⭐⭐ أنا الدائن — أُذكّر الشخص بالمبلغ الذي في ذمته
            buildString {
                append(header)
                append("السلام عليكم ورحمة الله وبركاته\n")
                append("أخي/أختي $name،\n\n")
                append("📌 تذكير ودّي\n\n")
                append("المبلغ المستحق لنا في ذمتكم:\n")
                append("$balanceText\n\n")
                append("نرجو التكرم بسداده في أقرب وقت.\n\n")
                append("شكرًا لتعاملكم معنا.")
            }
        } else {
            // ⭐⭐ أنا المدين — أُطمئن الشخص أنني سأسدد
            buildString {
                append(header)
                append("السلام عليكم ورحمة الله وبركاته\n")
                append("أخي/أختي $name،\n\n")
                append("📌 تذكير ودّي\n\n")
                append("المبلغ المستحق لكم في ذمتنا:\n")
                append("$balanceText\n\n")
                append("نؤكد لكم أننا سنقوم بسداده في أقرب وقت إن شاء الله.\n\n")
                append("شكراً لتفهمكم.")
            }
        }
    }

    /**
     * ⭐ رسالة تأكيد استلام دفعة.
     */
    fun buildPaymentReceivedMessage(
        name: String,
        amountText: String,
        remainingText: String,
        storeName: String = ""
    ): String {
        val header = if (storeName.isNotBlank()) "🏪 $storeName\n\n" else ""
        return "${header}السلام عليكم ورحمة الله وبركاته\n" +
                "أخي/أختي $name،\n\n" +
                "✅ تم استلام دفعتكم بمبلغ:\n" +
                "$amountText\n\n" +
                "المبلغ المتبقي: $remainingText\n\n" +
                "شكرًا لتعاملكم معنا."
    }

    /**
     * ⭐ رسالة تسجيل دين جديد.
     */
    fun buildDebtRegisteredMessage(
        name: String,
        amountText: String,
        statement: String,
        storeName: String = ""
    ): String {
        val header = if (storeName.isNotBlank()) "🏪 $storeName\n\n" else ""
        return "${header}السلام عليكم ورحمة الله وبركاته\n" +
                "أخي/أختي $name،\n\n" +
                "📝 تم تسجيل مبلغ جديد:\n" +
                "$amountText\n\n" +
                "البيان: $statement\n\n" +
                "شكرًا لتعاملكم معنا."
    }

    /**
     * ⭐ يملأ قالباً بالقيم.
     * المتغيرات: {name}, {amount}, {remaining}, {statement}, {store}
     */
    fun fillTemplate(
        template: String,
        name: String = "",
        amount: String = "",
        remaining: String = "",
        statement: String = "",
        storeName: String = ""
    ): String {
        return template
            .replace("{name}", name)
            .replace("{amount}", amount)
            .replace("{remaining}", remaining)
            .replace("{statement}", statement)
            .replace("{store}", storeName)
    }
}