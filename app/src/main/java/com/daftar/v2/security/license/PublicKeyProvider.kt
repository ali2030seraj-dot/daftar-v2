package com.daftar.v2.security.license

import android.util.Base64
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يوفر المفتاح العام الخاص بمولّد الأكواد (KeyGen).
 *
 * ملاحظة: هذه الفئة تُعيد المفتاح الخام (32 بايت) فقط.
 * التحقق يتم عبر net.i2p.crypto:eddsa (انظر LicenseValidator).
 */
@Singleton
class PublicKeyProvider @Inject constructor() {

    companion object {
        private const val TAG = "PublicKeyProvider"

        // ✅ المفتاح العام (من KeyGen) — بدون أي مسافات
        private const val PUBLIC_KEY_BASE64 = "xH0FkW8GPlP9b9BsOrdBNDCd6Poc28ZWA8wdoKxivHA="
    }

    private var cachedRawKey: ByteArray? = null

    /**
     * يعيد المفتاح العام الخام (32 بايت).
     */
    fun getRawPublicKey(): ByteArray? {
        if (cachedRawKey != null) return cachedRawKey
        return try {
            // ✅ إزالة أي مسافات أو أسطر جديدة محتملة قبل الفك
            val cleanBase64 = PUBLIC_KEY_BASE64.trim().replace(" ", "").replace("\n", "").replace("\r", "")

            Log.d(TAG, "═══════════════════════════")
            Log.d(TAG, "Raw base64: $PUBLIC_KEY_BASE64")
            Log.d(TAG, "Clean base64: $cleanBase64")
            Log.d(TAG, "Clean length: ${cleanBase64.length}")

            val bytes = Base64.decode(cleanBase64, Base64.NO_WRAP)
            Log.d(TAG, "Decoded size: ${bytes.size}")

            if (bytes.size != 32) {
                Log.e(TAG, "❌ Key size is ${bytes.size}, expected 32")
                Log.e(TAG, "═══════════════════════════")
                return null
            }

            Log.d(TAG, "✅ Key loaded successfully")
            Log.d(TAG, "═══════════════════════════")

            cachedRawKey = bytes
            cachedRawKey
        } catch (e: Exception) {
            Log.e(TAG, "❌ ${e.javaClass.simpleName}: ${e.message}", e)
            Log.e(TAG, "═══════════════════════════")
            null
        }
    }

    /**
     * @deprecated استخدم getRawPublicKey() بدلًا منها.
     * تُركت للتوافق مع الكود القديم.
     */
    @Deprecated("Use getRawPublicKey()")
    fun getPublicKey(): java.security.PublicKey? = null
}