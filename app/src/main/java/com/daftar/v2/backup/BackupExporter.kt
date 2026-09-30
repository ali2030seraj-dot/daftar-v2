package com.daftar.v2.backup

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.daftar.v2.branding.BrandingManager
import com.daftar.v2.data.db.dao.ExpenseCategoryDao
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.SettingsDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.domain.audit.AuditAction
import com.daftar.v2.domain.audit.AuditLogger
import com.daftar.v2.util.DateFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: ExpenseCategoryDao,
    private val settingsDao: SettingsDao,
    private val brandingManager: BrandingManager,
    private val auditLogger: AuditLogger
) {

    private val json = Json {
        prettyPrint = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /**
     * يبني نص JSON كامل لكل البيانات + العلامة التجارية + الأصوات.
     */
    suspend fun buildBackupJson(): String = withContext(Dispatchers.IO) {
        val people = personDao.getAll()
        val transactions = transactionDao.getAll()
        val expenses = expenseDao.getAll()
        val categories = categoryDao.getAll()
        val settings = settingsDao.getAll()

        val branding = BackupBranding(
            storeName = brandingManager.getStoreName(),
            storePhone = brandingManager.getStorePhone(),
            storeAddress = brandingManager.getStoreAddress(),
            logoBase64 = readLogoAsBase64()
        )

        val backup = BackupFile(
            people = people.map {
                BackupPerson(
                    id = it.id,
                    name = it.name,
                    phone = it.phone,
                    type = it.type.name,
                    currency = it.currency,
                    isArchived = it.isArchived,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            },
            transactions = transactions.map { tx ->
                val voice = readVoiceAsBase64(tx.voicePath)
                BackupTransaction(
                    id = tx.id,
                    personId = tx.personId,
                    type = tx.type.name,
                    amountCents = if (tx.type == TransactionType.DEBT) tx.amountCents else 0L,
                    paymentCents = if (tx.type == TransactionType.PAYMENT) tx.amountCents else 0L,
                    statement = tx.statement,
                    note = tx.note,
                    timestamp = tx.timestamp,
                    dayName = DateFormatter.dayName(tx.timestamp),
                    date = DateFormatter.date(tx.timestamp),
                    isVoided = tx.isVoided,
                    voiceFileName = tx.voicePath?.let { File(it).name },
                    voiceBase64 = voice
                )
            },
            expenses = expenses.map {
                BackupExpense(
                    id = it.id,
                    statement = it.statement,
                    categoryId = it.categoryId,
                    amountCents = it.amountCents,
                    currency = it.currency,
                    note = it.note,
                    timestamp = it.timestamp,
                    dayName = DateFormatter.dayName(it.timestamp),
                    date = DateFormatter.date(it.timestamp),
                    isVoided = it.isVoided
                )
            },
            categories = categories.map {
                BackupCategory(id = it.id, name = it.name, createdAt = it.createdAt)
            },
            settings = settings.map {
                BackupSetting(key = it.key, value = it.value)
            },
            branding = branding
        )

        auditLogger.log(
            action = AuditAction.BACKUP_EXPORTED,
            entityType = "backup"
        )

        json.encodeToString(backup)
    }

    /**
     * يُصدّر النسخة مشفرة بكلمة مرور.
     */
    suspend fun exportEncryptedJson(password: String): String = withContext(Dispatchers.IO) {
        val plainJson = buildBackupJson()
        val encrypted = BackupCrypto.encrypt(plainJson, password)
        json.encodeToString(encrypted)
    }

    /**
     * اسم الملف المقترح (بالتاريخ والوقت).
     */
    fun suggestedFileName(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US)
        return "Daftar_Backup_${fmt.format(Date())}.daftar"
    }

    /**
     * ⭐⭐⭐ اسم الملف التسلسلي — Daftar_Backup_001.daftar
     *
     * يفحص مجلد `cache/backups` وابحث عن آخر رقم ويضيف 1.
     */
    suspend fun nextSequentialFileName(): String = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.cacheDir, "backups").apply { mkdirs() }

            // ⭐ ابحث عن آخر رقم في الملفات
            val lastNumber = dir.listFiles()
                ?.filter { it.name?.startsWith("Daftar_Backup_") == true }
                ?.mapNotNull { file ->
                    val match = Regex("Daftar_Backup_(\\d+)").find(file.name)
                    match?.groupValues?.getOrNull(1)?.toIntOrNull()
                }
                ?.maxOrNull()
                ?: 0

            val next = lastNumber + 1
            val number = next.toString().padStart(3, '0')
            "Daftar_Backup_$number.daftar"

        } catch (e: Exception) {
            // Fallback — بالتاريخ
            suggestedFileName()
        }
    }

    /**
     * ✅ يكتب النسخة المشفرة إلى Uri.
     */
    suspend fun writeEncryptedToUri(
        uri: Uri,
        password: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val content = exportEncryptedJson(password)

            val stream: OutputStream? = try {
                context.contentResolver.openOutputStream(uri, "wt")
            } catch (e: Exception) {
                null
            } ?: try {
                context.contentResolver.openOutputStream(uri)
            } catch (e: Exception) {
                null
            } ?: try {
                context.contentResolver.openOutputStream(uri, "w")
            } catch (e: Exception) {
                null
            }

            if (stream == null) {
                return@withContext false
            }

            stream.use {
                it.write(content.toByteArray(Charsets.UTF_8))
                it.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ==================== أدوات ====================

    private fun readLogoAsBase64(): String? {
        return try {
            val file = brandingManager.getLogoFile() ?: return null
            if (!file.exists() || file.length() == 0L) return null
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun readVoiceAsBase64(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return try {
            val file = File(path)
            if (!file.exists() || file.length() == 0L) return null
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}