package com.daftar.v2.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val exporter: BackupExporter
) {

    suspend fun shareBackup(context: Context) {
        try {
            val content = exporter.buildBackupJson()
            val fileName = exporter.suggestedFileName()

            val dir = File(context.cacheDir, "backups").apply { mkdirs() }
            val file = File(dir, fileName)
            file.writeText(content, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - دفتر حساباتي")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة النسخة الاحتياطية"))
        } catch (e: Exception) {
            // نتجاهل الأخطاء هنا، ويمكن تسجيلها لاحقًا
        }
    }
}