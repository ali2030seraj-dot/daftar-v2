package com.daftar.v2.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log

object StoragePermissionManager {

    private const val TAG = "StoragePermissionMgr"

    /**
     * ⭐ هل نحتاج MANAGE_EXTERNAL_STORAGE؟
     */
    fun needsManagePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !hasManagePermission()
    }

    /**
     * ⭐ فحص الصلاحية.
     */
    fun hasManagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Environment.isExternalStorageManager()
            } catch (e: Exception) {
                Log.w(TAG, "فشل فحص الصلاحية: ${e.message}")
                false
            }
        } else {
            true
        }
    }

    /**
     * ⭐ يفتح شاشة الإعدادات.
     */
    fun requestManagePermission(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            ).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "فشل فتح الإعدادات", e2)
            }
        }
    }
}