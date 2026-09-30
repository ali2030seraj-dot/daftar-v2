package com.daftar.v2.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.util.Log
import java.io.File
import java.security.MessageDigest

/**
 * ⭐⭐⭐ فاحص سلامة التطبيق.
 *
 * يتحقق من:
 *  1. توقيع APK (لم يُعدَّل)
 *  2. Debugger متصل؟
 *  3. التطبيق قابل للتصحيح؟
 *  4. Root detection
 */
object IntegrityChecker {

    private const val TAG = "IntegrityChecker"

    /**
     * ⭐ بصمة توقيع تطبيق دفتر V2.
     *
     * Keystore: daftar-key.jks
     * Alias:    daftar
     * SHA-256:  3C:16:9C:D0:9E:C3:85:59:8C:66:14:20:60:EB:6A:F1:
     *           11:16:D3:47:42:01:29:23:90:6D:1B:11:85:EF:56:20
     *
     * ⚠️ إذا غيّرت keystore، يجب تحديث هذه القيمة.
     * ⚠️ إذا كانت فارغة "" → الفحص معطّل (للتطوير فقط)
     */
    private const val EXPECTED_SIGNATURE =
        "3C169CD09EC385598C66142060EB6AF11116D34742012923906D1B1185EF5620"

    /**
     * ⭐ يعيد true إذا كان التطبيق سليماً.
     */
    fun isAppSafe(context: Context): Boolean {
        // في وضع debug نتجاهل الفحص
        if (isDebuggable(context)) {
            Log.d(TAG, "🛠️ Debug mode — تم تخطي الفحص")
            return true
        }

        // 1. فحص التوقيع
        if (!verifySignature(context)) {
            Log.e(TAG, "🚨 توقيع APK غير صحيح — التطبيق معدّل!")
            return false
        }

        // 2. فحص Debugger
        if (isDebuggerAttached()) {
            Log.e(TAG, "🚨 Debugger متصل!")
            return false
        }

        Log.d(TAG, "✅ التطبيق سليم")
        return true
    }

    // ═══════════════════════════════════════════════════════
    //              فحص التوقيع
    // ═══════════════════════════════════════════════════════

    fun verifySignature(context: Context): Boolean {
        // لو لم تُضبط البصمة، تخطى
        if (EXPECTED_SIGNATURE.isBlank()) {
            Log.w(TAG, "⚠️ EXPECTED_SIGNATURE فارغة — تخطي فحص التوقيع")
            return true
        }

        return try {
            val pm = context.packageManager
            val packageName = context.packageName

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val info = pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                info.signatures
            }

            if (signatures.isNullOrEmpty()) {
                Log.e(TAG, "لا توجد signatures!")
                return false
            }

            val md = MessageDigest.getInstance("SHA-256")
            signatures.any { sig ->
                val hash = md.digest(sig.toByteArray())
                    .joinToString("") { "%02X".format(it) }
                hash.equals(EXPECTED_SIGNATURE, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل فحص التوقيع", e)
            false
        }
    }

    // ═══════════════════════════════════════════════════════
    //              فحص Debugger
    // ═══════════════════════════════════════════════════════

    fun isDebuggerAttached(): Boolean {
        return try {
            Debug.isDebuggerConnected() || Debug.waitingForDebugger()
        } catch (e: Exception) {
            false
        }
    }

    fun isDebuggable(context: Context): Boolean {
        return try {
            (context.applicationInfo.flags and
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (e: Exception) {
            false
        }
    }

    // ═══════════════════════════════════════════════════════
    //              فحص Root
    // ═══════════════════════════════════════════════════════

    fun isDeviceRooted(): Boolean {
        return checkSuBinary() || checkBuildTags()
    }

    private fun checkSuBinary(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return paths.any {
            try {
                File(it).exists()
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun checkBuildTags(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }

    /**
     * نسخة كاملة تفحص Root packages (تحتاج Context).
     */
    fun isDeviceRootedFull(context: Context): Boolean {
        if (checkSuBinary() || checkBuildTags()) return true

        val packages = listOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su",
            "com.thirdparty.superuser"
        )
        return packages.any {
            try {
                context.packageManager.getPackageInfo(it, 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}