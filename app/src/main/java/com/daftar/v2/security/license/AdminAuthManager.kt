package com.daftar.v2.security.license

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يدير المصادقة على لوحة الإدارة.
 *
 * كلمة المرور الافتراضية: 123456
 */
@Singleton
class AdminAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "AdminAuthManager"
        private const val PREFS_NAME = "daftar_admin_prefs"
        private const val KEY_PASSWORD = "admin_password_plain"
        private const val KEY_SESSION = "admin_session_active"
        private const val KEY_SESSION_TIME = "admin_session_time"

        // ✅ كلمة المرور الافتراضية — تم التعديل
        private const val DEFAULT_PASSWORD = "123456"

        // مدة الجلسة: 15 دقيقة
        private const val SESSION_TIMEOUT_MS = 15L * 60L * 1000L
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * يتحقق من كلمة المرور.
     * مقارنة مباشرة بدون hash — أبسط وأضمن.
     */
    fun verifyPassword(password: String): Boolean {
        val cleanInput = password.trim()
        val storedPassword = prefs.getString(KEY_PASSWORD, null)

        Log.d(TAG, "═══════════════════════════")
        Log.d(TAG, "Input: '$cleanInput'")
        Log.d(TAG, "Input length: ${cleanInput.length}")
        Log.d(TAG, "Stored: '$storedPassword'")

        val result = if (storedPassword == null) {
            // أول استخدام — استخدم الافتراضية
            val match = cleanInput == DEFAULT_PASSWORD
            Log.d(TAG, "Default: '$DEFAULT_PASSWORD'")
            Log.d(TAG, "Match: $match")
            match
        } else {
            val match = cleanInput == storedPassword
            Log.d(TAG, "Match: $match")
            match
        }

        Log.d(TAG, "═══════════════════════════")
        return result
    }

    /**
     * يغيّر كلمة المرور.
     */
    fun changePassword(oldPassword: String, newPassword: String): Boolean {
        if (!verifyPassword(oldPassword)) return false
        if (newPassword.length < 4) return false

        prefs.edit()
            .putString(KEY_PASSWORD, newPassword.trim())
            .apply()
        return true
    }

    /**
     * يبدأ جلسة إدارية.
     */
    fun startSession() {
        prefs.edit()
            .putBoolean(KEY_SESSION, true)
            .putLong(KEY_SESSION_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * هل الجلسة الإدارية نشطة؟
     */
    fun isSessionActive(): Boolean {
        val active = prefs.getBoolean(KEY_SESSION, false)
        if (!active) return false

        val startTime = prefs.getLong(KEY_SESSION_TIME, 0L)
        val elapsed = System.currentTimeMillis() - startTime

        if (elapsed > SESSION_TIMEOUT_MS) {
            endSession()
            return false
        }
        return true
    }

    /**
     * ينهي الجلسة الإدارية.
     */
    fun endSession() {
        prefs.edit()
            .putBoolean(KEY_SESSION, false)
            .putLong(KEY_SESSION_TIME, 0L)
            .apply()
    }

    /**
     * هل كلمة المرور افتراضية؟
     */
    fun isUsingDefaultPassword(): Boolean {
        return prefs.getString(KEY_PASSWORD, null) == null
    }
}