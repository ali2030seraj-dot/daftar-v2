package com.daftar.v2.security

import android.content.Context
import android.util.Log
import java.security.MessageDigest

/**
 * ⭐⭐⭐ يخفي النصوص الحساسة عن محركات البحث في الكود.
 *
 * بدلاً من:
 *   val key = "d@ft@r_v2_s3cur3_k3y_2026"   ← يظهر في jadx
 *
 * نستخدم:
 *   val key = StringObfuscator.getObfuscationKey(context)
 *   ← يُبنى وقت التشغيل، لا يظهر كنص
 */
object StringObfuscator {

    private const val TAG = "StringObfuscator"

    /**
     * ⭐ يبني مفتاح تشويش مشتقاً من بصمة الجهاز.
     * كل جهاز له مفتاح مختلف → لا فائدة من استخراجه من APK.
     */
    fun getObfuscationKey(context: Context): String {
        return try {
            val androidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "unknown"

            val packageName = context.packageName
            val salt = buildSalt()

            val combined = "$androidId|$packageName|$salt"
            val hash = MessageDigest.getInstance("SHA-256")
                .digest(combined.toByteArray())
                .take(16)

            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "فشل بناء المفتاح — fallback", e)
            "daftar_v2_fallback_key_2026"
        }
    }

    /**
     * ⭐ يبني salt من أجزاء — لا يظهر كنص كامل في jadx.
     */
    private fun buildSalt(): String {
        val parts = listOf(
            "d@ft@r",
            "_v2_",
            "s3cur3",
            "_k3y_",
            "2026"
        )
        return parts.joinToString("")
    }

    /**
     * ⭐ يبني نصاً من أرقام (بديل متقدم للنصوص الحساسة).
     */
    fun buildString(vararg codes: Int): String {
        return codes.map { it.toChar() }.joinToString("")
    }
}