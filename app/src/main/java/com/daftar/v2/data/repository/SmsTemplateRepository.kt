package com.daftar.v2.data.repository

import com.daftar.v2.data.db.dao.SmsTemplateDao
import com.daftar.v2.data.entity.SmsTemplateEntity
import com.daftar.v2.data.entity.SmsTemplateType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ القوالب الافتراضية.
 */
object DefaultSmsTemplates {

    const val DEBT_REGISTERED = """السلام عليكم ورحمة الله وبركاته
عزيزي {name}،

تم تسجيل مبلغ جديد:
{amount}

البيان: {statement}

شكرًا لتعاملكم معنا."""

    const val PAYMENT_RECEIVED = """السلام عليكم ورحمة الله وبركاته
عزيزي {name}،

تم استلام دفعتكم بمبلغ:
{amount}

المبلغ المتبقي: {remaining}

شكرًا لتعاملكم معنا."""

    const val REMINDER = """السلام عليكم ورحمة الله وبركاته
عزيزي {name}،

تذكير ودّي بالمبلغ المتبقي:
{remaining}

شكرًا لتعاملكم معنا."""
}

@Singleton
class SmsTemplateRepository @Inject constructor(
    private val dao: SmsTemplateDao
) {

    fun observeAll(): Flow<List<SmsTemplateEntity>> = dao.observeAll()

    /**
     * ⭐ يجيب القالب — وإذا لم يكن مخصصاً، يعيد الافتراضي.
     */
    suspend fun getTemplate(type: SmsTemplateType): String =
        withContext(Dispatchers.IO) {
            val saved = dao.getByType(type.name)
            saved?.body ?: defaultFor(type)
        }

    /**
     * ⭐ يحفظ قالباً مخصصاً.
     */
    suspend fun saveTemplate(type: SmsTemplateType, body: String) =
        withContext(Dispatchers.IO) {
            dao.upsert(
                SmsTemplateEntity(
                    type = type.name,
                    body = body.trim(),
                    isCustom = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

    /**
     * ⭐ يعيد القالب للافتراضي.
     */
    suspend fun resetToDefault(type: SmsTemplateType) =
        withContext(Dispatchers.IO) {
            dao.deleteByType(type.name)
        }

    /**
     * ⭐ يهيّئ القوالب الافتراضية إذا لم تكن موجودة.
     */
    suspend fun seedDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        SmsTemplateType.values().forEach { type ->
            if (dao.getByType(type.name) == null) {
                dao.upsert(
                    SmsTemplateEntity(
                        type = type.name,
                        body = defaultFor(type),
                        isCustom = false
                    )
                )
            }
        }
    }

    private fun defaultFor(type: SmsTemplateType): String = when (type) {
        SmsTemplateType.DEBT_REGISTERED -> DefaultSmsTemplates.DEBT_REGISTERED
        SmsTemplateType.PAYMENT_RECEIVED -> DefaultSmsTemplates.PAYMENT_RECEIVED
        SmsTemplateType.REMINDER -> DefaultSmsTemplates.REMINDER
    }
}