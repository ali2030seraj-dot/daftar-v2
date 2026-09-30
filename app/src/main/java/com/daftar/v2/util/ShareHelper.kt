package com.daftar.v2.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * ⭐ أدوات مشاركة التطبيق — رابط التحميل + نسخ الرابط.
 */
object ShareHelper {

    // ⭐⭐⭐ رابط صفحة الإصدارات على GitHub (أحدث إصدار تلقائيًا)
    const val APP_DOWNLOAD_URL =
        "https://github.com/ali2030seraj-dot/daftar-v2/releases/latest"

    /**
     * ⭐ مشاركة التطبيق مع رابط التحميل.
     */
    fun shareApp(
        context: Context,
        appName: String = "دفتر حساباتي"
    ) {
        val message = buildString {
            append("📱 *$appName*\n")
            append("تطبيق إدارة الديون والحسابات\n")
            append("يعمل بدون إنترنت — بياناتك مشفّرة\n\n")
            append("⬇️ حمّل التطبيق من هنا:\n")
            append(APP_DOWNLOAD_URL)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "مشاركة تطبيق $appName")
            putExtra(Intent.EXTRA_TEXT, message)
        }

        val chooser = Intent.createChooser(intent, "شارك التطبيق عبر:")
        context.startActivity(chooser)
    }

    /**
     * ⭐ نسخ رابط التحميل.
     */
    fun copyLink(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
            as ClipboardManager
        val clip = ClipData.newPlainText("رابط التطبيق", APP_DOWNLOAD_URL)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            "تم نسخ رابط التحميل ✅",
            Toast.LENGTH_SHORT
        ).show()
    }
}