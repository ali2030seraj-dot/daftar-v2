package com.daftar.v2.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.util.Log
import java.io.File

object SecurityChecks {

    private const val TAG = "SecurityChecks"

    // ═══════════════════════════════════════════════════════
    //              Root Detection
    // ═══════════════════════════════════════════════════════

    fun isRooted(): Boolean {
        return checkRootFiles() ||
                checkRootCommands() ||
                checkDangerousProps() ||
                checkSuBinary()
    }

    private fun checkRootFiles(): Boolean {
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
            "/su/bin/su",
            "/magisk/.core/bin/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkRootCommands(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun checkDangerousProps(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop ro.debuggable")
            val output = process.inputStream.bufferedReader().readText().trim()
            output == "1"
        } catch (e: Exception) {
            false
        }
    }

    private fun checkSuBinary(): Boolean {
        return try {
            Runtime.getRuntime().exec("su -c id").waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    // ═══════════════════════════════════════════════════════
    //              Debugger Detection
    // ═══════════════════════════════════════════════════════

    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    // ═══════════════════════════════════════════════════════
    //              Emulator Detection
    // ═══════════════════════════════════════════════════════

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.FINGERPRINT.contains("test-keys") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu"))
    }

    // ═══════════════════════════════════════════════════════
    //              Signature Verification
    // ═══════════════════════════════════════════════════════

    fun verifySignature(context: Context, expectedHash: String): Boolean {
        return try {
            val pm = context.packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }

            signatures?.any { sig ->
                val hash = java.security.MessageDigest
                    .getInstance("SHA-256")
                    .digest(sig.toByteArray())
                    .joinToString("") { "%02x".format(it) }
                hash.equals(expectedHash, ignoreCase = true)
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "فشل فحص التوقيع", e)
            false
        }
    }

    // ═══════════════════════════════════════════════════════
    //              الفحص الشامل
    // ═══════════════════════════════════════════════════════

    fun isDeviceSafe(): Boolean {
        val rooted = isRooted()
        val debugger = isDebuggerAttached()
        val emulator = isEmulator()

        if (rooted) Log.w(TAG, "⚠️ الجهاز Root")
        if (debugger) Log.w(TAG, "⚠️ Debugger مرفق")
        if (emulator) Log.w(TAG, "⚠️ محاكي")

        return !rooted && !debugger && !emulator
    }
}