package com.daftar.v2.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ يحمّل الـ APK عبر DownloadManager.
 */
@Singleton
class UpdateDownloader @Inject constructor() {

    fun downloadApk(
        context: Context,
        url: String,
        versionName: String
    ): Long? {
        return try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle("دفتر حساباتي — تحديث $versionName")
                setDescription("جاري تحميل التحديث...")
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    "daftar-v$versionName.apk"
                )
                setMimeType("application/vnd.android.package-archive")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val manager = context.getSystemService<DownloadManager>()
            manager?.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}