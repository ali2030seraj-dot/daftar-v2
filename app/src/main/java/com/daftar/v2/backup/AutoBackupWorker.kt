package com.daftar.v2.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ينشئ نسخة احتياطية تلقائية يوميًا.
 *
 * - يحفظ الملف في مجلد داخلي خاص بالتطبيق.
 * - يحتفظ بـ 7 نسخ فقط (الأقدم يُحذف تلقائيًا).
 * - صامت — لا يُشعِر المستخدم.
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val exporter: BackupExporter
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daftar_auto_backup"
        const val BACKUP_DIR = "auto_backups"
        const val MAX_BACKUPS = 7
    }

    override suspend fun doWork(): Result {
        return try {
            val content = exporter.buildBackupJson()

            val dir = File(applicationContext.filesDir, BACKUP_DIR).apply { mkdirs() }

            val fileName = "auto_backup_${
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            }.json"
            val newFile = File(dir, fileName)
            newFile.writeText(content, Charsets.UTF_8)

            cleanupOldBackups(dir)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun cleanupOldBackups(dir: File) {
        try {
            val files = dir.listFiles()
                ?.filter { it.extension == "json" }
                ?.sortedByDescending { it.lastModified() }
                ?: return

            if (files.size > MAX_BACKUPS) {
                files.drop(MAX_BACKUPS).forEach { it.delete() }
            }
        } catch (_: Exception) {
        }
    }
}