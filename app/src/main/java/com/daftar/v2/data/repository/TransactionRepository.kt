package com.daftar.v2.data.repository

import com.daftar.v2.data.db.dao.PersonBalanceRow
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    // ═════ قراءة ═════

    fun observeByPerson(personId: Long): Flow<List<TransactionEntity>> =
        transactionDao.observeByPerson(personId)

    fun observeAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.observeAllTransactions()

    suspend fun getById(id: Long): TransactionEntity? =
        transactionDao.getById(id)

    suspend fun getAll(): List<TransactionEntity> =
        transactionDao.getAll()

    // ⭐ الأرصدة لعدة أشخاص (يستخدمها PeopleViewModel)
    suspend fun balancesForPersons(personIds: List<Long>): List<PersonBalanceRow> =
        transactionDao.balancesForPersons(personIds)

    // ═════ كتابة ═════

    suspend fun insert(tx: TransactionEntity): Long =
        transactionDao.insert(tx)

    suspend fun update(tx: TransactionEntity) =
        transactionDao.update(tx)

    suspend fun void(id: Long) {
        transactionDao.voidById(id)
    }

    suspend fun voidById(id: Long) =
        transactionDao.voidById(id)

    suspend fun hardDelete(id: Long) =
        transactionDao.hardDelete(id)

    // ═════ إضافة دين ═════

    suspend fun addDebt(
        personId: Long,
        statement: String,
        amountCents: Long,
        note: String = ""
    ): Long {
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            personId = personId,
            type = TransactionType.DEBT,
            amountCents = amountCents,
            statement = statement.trim(),
            note = note.trim(),
            timestamp = now,
            isVoided = false,
            createdAt = now,
            updatedAt = now
        )
        return transactionDao.insert(tx)
    }

    // ═════ إضافة سداد ═════

    suspend fun addPayment(
        personId: Long,
        amountCents: Long,
        note: String = ""
    ): Long {
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            personId = personId,
            type = TransactionType.PAYMENT,
            amountCents = amountCents,
            statement = "سداد",
            note = note.trim(),
            timestamp = now,
            isVoided = false,
            createdAt = now,
            updatedAt = now
        )
        return transactionDao.insert(tx)
    }

    // ═════ الرصيد ═════

    suspend fun totalDebtCents(personId: Long): Long =
        transactionDao.totalDebtCents(personId)

    suspend fun totalPaymentCents(personId: Long): Long =
        transactionDao.totalPaymentCents(personId)

    // ═════ الملاحظة الصوتية ═════

    suspend fun updateVoicePath(id: Long, path: String?) =
        transactionDao.updateVoicePath(id, path)

    // ═════ حذف الكل ═════

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAll()
    }
}