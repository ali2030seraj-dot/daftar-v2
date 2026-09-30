package com.daftar.v2.cloud

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.daftar.v2.backup.BackupExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ وضع النسخ الاحتياطي.
 */
enum class BackupMode {
    AUTO,           // تلقائي (بعد الموافقة)
    ASK_EACH_TIME,  // اطلب إذني قبل كل نسخة
    OFF             // معطل
}

@Singleton
class CloudBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exporter: BackupExporter
) {

    companion object {
        private const val TAG = "CloudBackupManager"
        private const val PREFS_NAME = "cloud_backup_prefs"
        private const val KEY_FOLDER_URI = "cloud_folder_uri"
        private const val KEY_LAST_BACKUP_AT = "cloud_last_backup_at"
        private const val KEY_MODE = "cloud_mode"
        private const val KEY_PASSWORD = "cloud_password"
        private const val KEY_CONSENT_GIVEN = "cloud_consent_given"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ═══════════════════════════════════════════════════════
    //              منتقي المجلد
    // ═══════════════════════════════════════════════════════

    fun createFolderPickerIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        }
    }

    fun saveFolderUri(uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            prefs.edit().putString(KEY_FOLDER_URI, uri.toString()).apply()
            Log.d(TAG, "✅ تم حفظ المجلد: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل الحفظ: ${e.message}", e)
        }
    }

    fun getFolderUri(): Uri? {
        val s = prefs.getString(KEY_FOLDER_URI, null) ?: return null
        return try { Uri.parse(s) } catch (e: Exception) { null }
    }

    fun getFolderName(): String {
        val uri = getFolderUri() ?: return "لم يتم الاختيار"
        return try {
            val doc = DocumentFile.fromTreeUri(context, uri)
            doc?.name ?: uri.lastPathSegment ?: "مجلد"
        } catch (e: Exception) {
            "مجلد"
        }
    }

    // ═══════════════════════════════════════════════════════
    //              الوضع والإذن
    // ═══════════════════════════════════════════════════════

    fun getMode(): BackupMode {
        val name = prefs.getString(KEY_MODE, BackupMode.OFF.name) ?: BackupMode.OFF.name
        return try { BackupMode.valueOf(name) } catch (e: Exception) { BackupMode.OFF }
    }

    fun setMode(mode: BackupMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun hasConsent(): Boolean = prefs.getBoolean(KEY_CONSENT_GIVEN, false)

    fun giveConsent() {
        prefs.edit().putBoolean(KEY_CONSENT_GIVEN, true).apply()
    }

    fun revokeConsent() {
        prefs.edit().putBoolean(KEY_CONSENT_GIVEN, false).apply()
    }

    // ═══════════════════════════════════════════════════════
    //              كلمة المرور
    // ═══════════════════════════════════════════════════════

    fun getLastBackupAt(): Long = prefs.getLong(KEY_LAST_BACKUP_AT, 0L)

    fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)

    fun setPassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // ═══════════════════════════════════════════════════════
    //              تنفيذ النسخ
    // ═══════════════════════════════════════════════════════

    suspend fun performBackup(password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val folderUri = getFolderUri() ?: return@withContext false
            val folder = DocumentFile.fromTreeUri(context, folderUri)
            if (folder == null || !folder.canWrite()) return@withContext false

            val content = exporter.exportEncryptedJson(password)
            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
            val fileName = "Daftar_Auto_$dateStr.daftar"

            cleanupOldBackups(folder)

            val newFile = folder.createFile("application/octet-stream", fileName)
                ?: return@withContext false

            context.contentResolver.openOutputStream(newFile.uri)?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return@withContext false

            prefs.edit().putLong(KEY_LAST_BACKUP_AT, System.currentTimeMillis()).apply()
            Log.d(TAG, "✅ تم النسخ: $fileName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل: ${e.message}", e)
            false
        }
    }

    private fun cleanupOldBackups(folder: DocumentFile) {
        try {
            val backups = folder.listFiles()
                .filter { it.name?.startsWith("Daftar_Auto_") == true }
                .sortedByDescending { it.lastModified() }
            if (backups.size > 7) {
                backups.drop(7).forEach { it.delete() }
            }
        } catch (_: Exception) {}
    }

    // ═══════════════════════════════════════════════════════
    //              ⭐ قائمة الملفات السحابية (جديد)
    // ═══════════════════════════════════════════════════════

    /**
     * ⭐ يسرد جميع ملفات النسخ الاحتياطي في المجلد السحابي.
     */
    suspend fun listCloudFiles(): List<CloudFileInfo> = withContext(Dispatchers.IO) {
        try {
            val folderUri = getFolderUri() ?: return@withContext emptyList()
            val folder = DocumentFile.fromTreeUri(context, folderUri)
                ?: return@withContext emptyList()

            folder.listFiles()
                .filter { it.isFile && it.name?.endsWith(".daftar") == true }
                .map { file ->
                    CloudFileInfo(
                        uri = file.uri,
                        name = file.name ?: "backup.daftar",
                        sizeBytes = file.length(),
                        lastModified = file.lastModified()
                    )
                }
                .sortedByDescending { it.lastModified }
        } catch (e: Exception) {
            Log.e(TAG, "فشل سرد الملفات: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * ⭐ يقرأ محتوى ملف سحابي.
     */
    suspend fun readCloudFile(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل قراءة الملف: ${e.message}", e)
            null
        }
    }

    /**
     * ⭐ يحذف ملف نسخة سحابية.
     */
    suspend fun deleteCloudFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = DocumentFile.fromSingleUri(context, uri)
            file?.delete() ?: false
        } catch (e: Exception) {
            Log.e(TAG, "فشل الحذف: ${e.message}", e)
            false
        }
    }

    /**
     * ⭐ يصدّر ملف نسخة سحابية إلى مجلد محلي على الجهاز.
     */
    suspend fun exportCloudFileToDevice(
        cloudUri: Uri,
        destinationUri: Uri
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // اقرأ من السحابة
            val content = context.contentResolver.openInputStream(cloudUri)?.use { input ->
                input.readBytes()
            } ?: return@withContext false

            // اكتب إلى الجهاز
            context.contentResolver.openOutputStream(destinationUri, "wt")?.use { out ->
                out.write(content)
                out.flush()
            } ?: return@withContext false

            Log.d(TAG, "✅ تم التصدير إلى الجهاز")
            true
        } catch (e: Exception) {
            Log.e(TAG, "فشل التصدير: ${e.message}", e)
            false
        }
    }

    /**
     * ⭐ ينشئ ملفاً سحابياً جديداً من محتوى نصي.
     */
    suspend fun writeCloudFile(
        fileName: String,
        content: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val folderUri = getFolderUri() ?: return@withContext false
            val folder = DocumentFile.fromTreeUri(context, folderUri)
                ?: return@withContext false

            val file = folder.createFile("application/octet-stream", fileName)
                ?: return@withContext false

            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return@withContext false

            true
        } catch (e: Exception) {
            Log.e(TAG, "فشل الكتابة: ${e.message}", e)
            false
        }
    }
}