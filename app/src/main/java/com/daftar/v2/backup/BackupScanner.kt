package com.daftar.v2.backup

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ يفحص وجود نسخ احتياطية سابقة على الجهاز.
 *
 * يساعد في اكتشاف مستخدم قديم أعاد تثبيت التطبيق.
 */
@Singleton
class BackupScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "BackupScanner"
        private const val EXTENSION = ".daftar"
    }

    data class BackupInfo(
        val uri: Uri,
        val name: String,
        val timestamp: Long,
        val source: String
    )

    /**
     * ⭐ يفحص وجود نسخ احتياطية.
     */
    suspend fun scanForBackups(): List<BackupInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<BackupInfo>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            results.addAll(scanViaMediaStore())
        } else {
            results.addAll(scanDirectly())
        }

        Log.d(TAG, "📂 وجدت ${results.size} نسخة احتياطية")
        results
    }

    /**
     * Android 10+: MediaStore
     */
    private fun scanViaMediaStore(): List<BackupInfo> {
        val results = mutableListOf<BackupInfo>()

        try {
            val projection = arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.DATE_ADDED
            )
            val selection = "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
            val args = arrayOf("%$EXTENSION")

            context.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                args,
                "${MediaStore.Downloads.DATE_ADDED} DESC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val name = cursor.getString(1) ?: continue
                    val dateAdded = cursor.getLong(2) * 1000L

                    results.add(
                        BackupInfo(
                            uri = Uri.withAppendedPath(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                id.toString()
                            ),
                            name = name,
                            timestamp = dateAdded,
                            source = "MediaStore"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "تعذّر فحص MediaStore: ${e.message}")
        }

        return results
    }

    /**
     * Android 9 وأقل
     */
    private fun scanDirectly(): List<BackupInfo> {
        val results = mutableListOf<BackupInfo>()

        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            downloadsDir?.listFiles()?.forEach { file ->
                if (file.name.endsWith(EXTENSION) && file.isFile) {
                    results.add(
                        BackupInfo(
                            uri = Uri.fromFile(file),
                            name = file.name,
                            timestamp = file.lastModified(),
                            source = "Downloads"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "تعذّر فحص Downloads: ${e.message}")
        }

        return results
    }
}