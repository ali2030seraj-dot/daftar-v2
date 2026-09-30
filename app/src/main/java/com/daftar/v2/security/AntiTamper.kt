package com.daftar.v2.security

import android.content.Context
import android.util.Log
import com.daftar.v2.security.license.TrialManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐⭐⭐ فاحص مركزي لسلامة التطبيق.
 *
 * يُستدعى عند الإقلاع + عند كل عودة من الخلفية.
 * يكشف:
 *  - تعديل APK
 *  - Debugger
 *  - Root (اختياري)
 *  - إعادة التثبيت
 */
@Singleton
class AntiTamper @Inject constructor(
    private val trialManager: TrialManager
) {

    companion object {
        private const val TAG = "AntiTamper"
    }

    /**
     * ⭐ نتيجة فحص شامل.
     */
    data class Result(
        val isSafe: Boolean,
        val reason: String? = null,
        val isRooted: Boolean = false,
        val isDebuggerAttached: Boolean = false,
        val isSignatureValid: Boolean = true,
        val wasReinstalled: Boolean = false
    )

    /**
     * ⭐ فحص شامل.
     */
    suspend fun check(context: Context): Result {
        val signatureValid = IntegrityChecker.verifySignature(context)
        if (!signatureValid) {
            Log.e(TAG, "🚨 توقيع غير صالح")
            return Result(
                isSafe = false,
                reason = "تم تعديل التطبيق",
                isSignatureValid = false
            )
        }

        val debugger = IntegrityChecker.isDebuggerAttached()
        if (debugger && !IntegrityChecker.isDebuggable(context)) {
            Log.e(TAG, "🚨 Debugger متصل")
            return Result(
                isSafe = false,
                reason = "Debugger متصل",
                isDebuggerAttached = true
            )
        }

        val rooted = IntegrityChecker.isDeviceRootedFull(context)
        val reinstalled = trialManager.wasReinstalled()

        return Result(
            isSafe = true,
            isRooted = rooted,
            isDebuggerAttached = debugger,
            isSignatureValid = signatureValid,
            wasReinstalled = reinstalled
        )
    }
}