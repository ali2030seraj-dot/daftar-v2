package com.daftar.v2.data.repository

import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.PersonType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════
//              نتائج إنشاء شخص
// ═══════════════════════════════════════════════════════════

sealed class CreatePersonResult {
    data class Success(val personId: Long) : CreatePersonResult()
    data class Rejected(val reason: String) : CreatePersonResult()
    data class Error(val message: String) : CreatePersonResult()
}

// ═══════════════════════════════════════════════════════════
//              PersonRepository
// ═══════════════════════════════════════════════════════════

@Singleton
class PersonRepository @Inject constructor(
    private val personDao: PersonDao
) {

    // ═══════════════════════════════════════════════════════
    //                      قراءة (Flow)
    // ═══════════════════════════════════════════════════════

    fun observeCreditors(): Flow<List<PersonEntity>> =
        personDao.observeByType(PersonType.CREDITOR)

    fun observeDebtors(): Flow<List<PersonEntity>> =
        personDao.observeByType(PersonType.DEBTOR)

    fun observeActive(): Flow<List<PersonEntity>> =
        personDao.observeActive()

    fun observeArchived(): Flow<List<PersonEntity>> =
        personDao.observeArchived()

    fun observeById(id: Long): Flow<PersonEntity?> =
        personDao.observeById(id)

    // ═══════════════════════════════════════════════════════
    //                      قراءة (suspend)
    // ═══════════════════════════════════════════════════════

    suspend fun getById(id: Long): PersonEntity? =
        personDao.getById(id)

    suspend fun getAll(): List<PersonEntity> =
        personDao.getAll()

    suspend fun getByType(type: PersonType): List<PersonEntity> =
        personDao.getByType(type)

    /**
     * ⭐ الدالة المطلوبة للأرشفة الشهرية التلقائية
     * تُرجع كل الأشخاص غير المؤرشفين
     */
    suspend fun getActivePeople(): List<PersonEntity> =
        personDao.getAll().filter { !it.isArchived }

    /**
     * ⭐ البحث عن أشخاص بنفس الاسم والنوع والعملة
     */
    suspend fun findDuplicates(
        name: String,
        type: PersonType,
        currency: String
    ): List<PersonEntity> =
        personDao.findByNameTypeCurrency(name, type, currency)

    // ═══════════════════════════════════════════════════════
    //                      كتابة
    // ═══════════════════════════════════════════════════════

    suspend fun insert(person: PersonEntity): Long =
        personDao.insert(person)

    suspend fun update(person: PersonEntity) =
        personDao.update(person)

    suspend fun delete(person: PersonEntity) =
        personDao.delete(person)

    // ═══════════════════════════════════════════════════════
    //              الأرشفة والاسترجاع
    // ═══════════════════════════════════════════════════════

    /**
     * أرشفة شخص
     */
    suspend fun archive(id: Long) =
        personDao.setArchived(id, true)

    /**
     * إلغاء الأرشفة
     */
    suspend fun unarchive(id: Long) =
        personDao.setArchived(id, false)

    /**
     * ⭐ alias لدالة unarchive
     * يستخدمها ArchivePersonUseCase
     */
    suspend fun restore(id: Long) =
        personDao.setArchived(id, false)

    // ═══════════════════════════════════════════════════════
    //              الحذف النهائي
    // ═══════════════════════════════════════════════════════

    suspend fun hardDelete(id: Long) =
        personDao.deleteById(id)

    suspend fun deleteAll() =
        personDao.deleteAll()

    // ═══════════════════════════════════════════════════════
    //              إنشاء مع التحقق
    // ═══════════════════════════════════════════════════════

    suspend fun createWithValidation(
        name: String,
        phone: String,
        note: String,
        type: PersonType,
        currency: String
    ): CreatePersonResult {
        if (name.isBlank()) {
            return CreatePersonResult.Rejected("الاسم مطلوب")
        }

        val now = System.currentTimeMillis()
        val person = PersonEntity(
            name = name.trim(),
            phone = phone.trim(),
            note = note.trim(),
            type = type,
            currency = currency,
            isArchived = false,
            createdAt = now,
            updatedAt = now
        )

        return try {
            val id = personDao.insert(person)
            CreatePersonResult.Success(id)
        } catch (e: Exception) {
            CreatePersonResult.Error(e.message ?: "فشل إنشاء الحساب")
        }
    }
}