package com.daftar.v2.backup

import android.content.Context
import android.util.Base64
import android.util.Log
import com.daftar.v2.branding.BrandingManager
import com.daftar.v2.data.db.dao.ExpenseCategoryDao
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.SettingsDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.ExpenseCategoryEntity
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.data.entity.SettingsEntity
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRestorer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: ExpenseCategoryDao,
    private val settingsDao: SettingsDao,
    private val brandingManager: BrandingManager
) {

    companion object {
        private const val TAG = "DataRestorer"
        private const val BRANDING_DIR = "branding"
        private const val LOGO_FILE_NAME = "logo.png"

        private const val KEY_TRIAL_START = "trial_started_at"
        private const val KEY_TRIAL_END = "trial_ends_at"
        private const val KEY_TAMPER_FLAG = "_sys_integrity_flag"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ═══════════════════════════════════════════════════════
    //              الدالة الرئيسية
    // ═══════════════════════════════════════════════════════

    suspend fun restoreFromJson(
        plainJson: String,
        mode: ImportMode = ImportMode.REPLACE
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 بدء استعادة البيانات — الوضع: $mode")

            val backup: BackupFile = json.decodeFromString(plainJson)

            Log.d(
                TAG,
                "📦 people=${backup.people.size}, " +
                        "transactions=${backup.transactions.size}, " +
                        "expenses=${backup.expenses.size}, " +
                        "categories=${backup.categories.size}, " +
                        "settings=${backup.settings.size}"
            )

            when (mode) {
                ImportMode.REPLACE -> restoreReplace(backup)
                ImportMode.MERGE -> restoreMerge(backup)
            }

            // استعادة العلامة التجارية
            backup.branding?.let { b ->
                try {
                    brandingManager.setStoreName(b.storeName)
                    brandingManager.setStorePhone(b.storePhone)
                    brandingManager.setStoreAddress(b.storeAddress)
                    b.logoBase64?.let { restoreLogo(it) }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ فشل استعادة العلامة التجارية: ${e.message}")
                }
            }

            Log.d(TAG, "✅ تم الاسترداد بنجاح")
            RestoreResult.Success

        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل الاسترداد: ${e.message}", e)
            RestoreResult.Failure(e.message ?: "فشل غير معروف")
        }
    }

    // ═══════════════════════════════════════════════════════
    //              REPLACE
    // ═══════════════════════════════════════════════════════

    private suspend fun restoreReplace(backup: BackupFile) {
        Log.d(TAG, "🧹 مسح البيانات الحالية (REPLACE)")

        transactionDao.deleteAll()
        expenseDao.deleteAll()
        categoryDao.deleteAll()
        personDao.deleteAll()

        // ⭐⭐⭐ الإصلاح: نسخ القائمة قبل الحلقة
        val currentSettings = settingsDao.getAll().toList()
        for (setting in currentSettings) {
            settingsDao.delete(setting.key)
        }

        insertAll(backup, preserveIds = true)
    }

    // ═══════════════════════════════════════════════════════
    //              MERGE
    // ═══════════════════════════════════════════════════════

    private suspend fun restoreMerge(backup: BackupFile) {
        Log.d(TAG, "🔀 دمج البيانات (MERGE)")

        var addedPeople = 0
        var skippedPeople = 0
        var addedTransactions = 0
        var skippedTransactions = 0
        var addedExpenses = 0
        var skippedExpenses = 0

        // ⭐ نسخ كل القوائم أولاً (defensive)
        val categories = backup.categories.toList()
        val people = backup.people.toList()
        val transactions = backup.transactions.toList()
        val expenses = backup.expenses.toList()
        val settings = backup.settings.toList()

        // ═════ 1) التصنيفات ═════
        for (cat in categories) {
            val existing = categoryDao.findByName(cat.name)
            if (existing == null) {
                categoryDao.insert(
                    ExpenseCategoryEntity(
                        id = 0,
                        name = cat.name,
                        createdAt = cat.createdAt
                    )
                )
            }
        }

        // ═════ 2) الأشخاص ═════
        val oldToNewPersonId = mutableMapOf<Long, Long>()

        for (p in people) {
            val candidates = personDao.findByNameTypeCurrency(
                name = p.name,
                type = parsePersonType(p.type),
                currency = p.currency,
                excludeId = null
            )
            val existing = candidates.firstOrNull()

            if (existing != null) {
                oldToNewPersonId[p.id] = existing.id
                skippedPeople++
            } else {
                val newId = personDao.insert(
                    PersonEntity(
                        id = 0,
                        name = p.name,
                        phone = p.phone,
                        note = "",
                        type = parsePersonType(p.type),
                        currency = p.currency,
                        isArchived = p.isArchived,
                        createdAt = p.createdAt,
                        updatedAt = p.updatedAt
                    )
                )
                oldToNewPersonId[p.id] = newId
                addedPeople++
            }
        }

        // ═════ 3) المعاملات ═════
        for (tx in transactions) {
            val newPersonId = oldToNewPersonId[tx.personId] ?: continue

            val txType = parseTransactionType(tx.type)
            val amount = when (txType) {
                TransactionType.DEBT -> tx.amountCents
                TransactionType.PAYMENT -> tx.paymentCents
            }

            val duplicate = transactionDao.findDuplicate(
                personId = newPersonId,
                type = tx.type,
                amountCents = amount,
                statement = tx.statement,
                timestamp = tx.timestamp
            )

            if (duplicate != null) {
                skippedTransactions++
            } else {
                val voicePath = restoreVoiceFile(tx.voiceFileName, tx.voiceBase64)

                transactionDao.insert(
                    TransactionEntity(
                        id = 0,
                        personId = newPersonId,
                        type = txType,
                        amountCents = amount,
                        statement = tx.statement,
                        note = tx.note,
                        timestamp = tx.timestamp,
                        isVoided = tx.isVoided,
                        voicePath = voicePath
                    )
                )
                addedTransactions++
            }
        }

        // ═════ 4) المصروفات ═════
        for (e in expenses) {
            val duplicate = expenseDao.findDuplicate(
                statement = e.statement,
                amountCents = e.amountCents,
                currency = e.currency,
                timestamp = e.timestamp
            )

            if (duplicate != null) {
                skippedExpenses++
            } else {
                expenseDao.insert(
                    ExpenseEntity(
                        id = 0,
                        statement = e.statement,
                        categoryId = e.categoryId,
                        amountCents = e.amountCents,
                        currency = e.currency,
                        note = e.note,
                        timestamp = e.timestamp,
                        isVoided = e.isVoided
                    )
                )
                addedExpenses++
            }
        }

        // ═════ 5) الإعدادات — مع حماية تاريخ التجربة ═════
        for (s in settings) {
            when (s.key) {
                KEY_TRIAL_START, KEY_TRIAL_END, KEY_TAMPER_FLAG -> {
                    restoreTrialSettingSafely(s.key, s.value)
                }
                else -> {
                    val existing = settingsDao.get(s.key)
                    if (existing == null) {
                        settingsDao.put(SettingsEntity(key = s.key, value = s.value))
                    }
                }
            }
        }

        Log.d(
            TAG,
            """
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━
            📊 ملخص الدمج:
            👥 أشخاص: +$addedPeople (تم تجاهل $skippedPeople)
            💰 معاملات: +$addedTransactions (تم تجاهل $skippedTransactions)
            🧾 مصروفات: +$addedExpenses (تم تجاهل $skippedExpenses)
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━
            """.trimIndent()
        )
    }

    // ═══════════════════════════════════════════════════════
    //              REPLACE helpers
    // ═══════════════════════════════════════════════════════

    private suspend fun insertAll(backup: BackupFile, preserveIds: Boolean) {
        // ⭐ نسخ القوائم
        val categories = backup.categories.toList()
        val people = backup.people.toList()
        val transactions = backup.transactions.toList()
        val expenses = backup.expenses.toList()
        val settings = backup.settings.toList()

        // 1) التصنيفات
        for (cat in categories) {
            categoryDao.insert(
                ExpenseCategoryEntity(
                    id = if (preserveIds) cat.id else 0,
                    name = cat.name,
                    createdAt = cat.createdAt
                )
            )
        }

        // 2) الأشخاص
        for (p in people) {
            personDao.insert(
                PersonEntity(
                    id = if (preserveIds) p.id else 0,
                    name = p.name,
                    phone = p.phone,
                    note = "",
                    type = parsePersonType(p.type),
                    currency = p.currency,
                    isArchived = p.isArchived,
                    createdAt = p.createdAt,
                    updatedAt = p.updatedAt
                )
            )
        }

        // 3) المعاملات
        for (tx in transactions) {
            val voicePath = restoreVoiceFile(tx.voiceFileName, tx.voiceBase64)
            val txType = parseTransactionType(tx.type)
            val amount = when (txType) {
                TransactionType.DEBT -> tx.amountCents
                TransactionType.PAYMENT -> tx.paymentCents
            }
            transactionDao.insert(
                TransactionEntity(
                    id = if (preserveIds) tx.id else 0,
                    personId = tx.personId,
                    type = txType,
                    amountCents = amount,
                    statement = tx.statement,
                    note = tx.note,
                    timestamp = tx.timestamp,
                    isVoided = tx.isVoided,
                    voicePath = voicePath
                )
            )
        }

        // 4) المصروفات
        for (e in expenses) {
            expenseDao.insert(
                ExpenseEntity(
                    id = if (preserveIds) e.id else 0,
                    statement = e.statement,
                    categoryId = e.categoryId,
                    amountCents = e.amountCents,
                    currency = e.currency,
                    note = e.note,
                    timestamp = e.timestamp,
                    isVoided = e.isVoided
                )
            )
        }

        // 5) الإعدادات — مع حماية تاريخ التجربة
        for (s in settings) {
            when (s.key) {
                KEY_TRIAL_START, KEY_TRIAL_END, KEY_TAMPER_FLAG -> {
                    restoreTrialSettingSafely(s.key, s.value)
                }
                else -> {
                    settingsDao.put(SettingsEntity(key = s.key, value = s.value))
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //              حماية تاريخ التجربة
    // ═══════════════════════════════════════════════════════

    private suspend fun restoreTrialSettingSafely(key: String, restoredValue: String) {
        try {
            val existingValue = settingsDao.get(key)
            val existingTs = existingValue?.toLongOrNull()
            val restoredTs = restoredValue.toLongOrNull()

            when {
                existingValue == null -> {
                    settingsDao.put(SettingsEntity(key, restoredValue))
                    Log.d(TAG, "⏪ استرجاع $key من النسخة")
                }
                existingTs != null && restoredTs != null && restoredTs < existingTs -> {
                    settingsDao.put(SettingsEntity(key, restoredValue))
                    Log.w(TAG, "🚨 تاريخ تجربة أقدم في النسخة — نستخدمه: $key")
                }
                else -> {
                    Log.d(TAG, "🛡️ تاريخ التجربة الحالي أقدم — نبقيه: $key")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل حماية $key", e)
        }
    }

    // ═══════════════════════════════════════════════════════
    //              أدوات مساعدة
    // ═══════════════════════════════════════════════════════

    private fun parsePersonType(raw: String): PersonType =
        try {
            PersonType.valueOf(raw)
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ PersonType غير معروف: $raw")
            PersonType.CREDITOR
        }

    private fun parseTransactionType(raw: String): TransactionType =
        try {
            TransactionType.valueOf(raw)
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ TransactionType غير معروف: $raw")
            TransactionType.DEBT
        }

    private fun restoreVoiceFile(fileName: String?, base64: String?): String? {
        if (fileName.isNullOrBlank() || base64.isNullOrBlank()) return null
        return try {
            val dir = File(context.filesDir, "voices").apply { mkdirs() }
            val file = File(dir, fileName)
            if (!file.exists()) {
                file.writeBytes(Base64.decode(base64, Base64.NO_WRAP))
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ فشل استعادة الملف الصوتي: ${e.message}")
            null
        }
    }

    private fun restoreLogo(base64: String) {
        try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val dir = File(context.filesDir, BRANDING_DIR).apply { mkdirs() }
            val file = File(dir, LOGO_FILE_NAME)
            file.writeBytes(bytes)
            Log.d(TAG, "✅ تم استعادة الشعار")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ فشل استعادة الشعار: ${e.message}")
        }
    }
}

sealed class RestoreResult {
    object Success : RestoreResult()
    data class Failure(val message: String) : RestoreResult()
}