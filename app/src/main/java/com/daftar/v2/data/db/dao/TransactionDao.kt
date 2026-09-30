package com.daftar.v2.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.daftar.v2.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE personId = :personId AND isVoided = 0 ORDER BY timestamp ASC")
    fun observeByPerson(personId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isVoided = 0")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Insert
    suspend fun insert(tx: TransactionEntity): Long

    @Update
    suspend fun update(tx: TransactionEntity)

    @Query("UPDATE transactions SET isVoided = 1, updatedAt = :now WHERE id = :id")
    suspend fun voidById(id: Long, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    // ===== ✅ الملاحظة الصوتية =====
    @Query("UPDATE transactions SET voicePath = :path, updatedAt = :now WHERE id = :id")
    suspend fun updateVoicePath(
        id: Long,
        path: String?,
        now: Long = System.currentTimeMillis()
    )

    // ===== استعلامات الرصيد =====
    @Query("""
        SELECT personId,
               COALESCE(SUM(CASE WHEN type = 'DEBT' THEN amountCents ELSE 0 END), 0)
               - COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amountCents ELSE 0 END), 0) AS balance
        FROM transactions
        WHERE isVoided = 0 AND personId IN (:personIds)
        GROUP BY personId
    """)
    suspend fun balancesForPersons(personIds: List<Long>): List<PersonBalanceRow>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE personId = :personId AND type = 'DEBT' AND isVoided = 0")
    suspend fun totalDebtCents(personId: Long): Long

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE personId = :personId AND type = 'PAYMENT' AND isVoided = 0")
    suspend fun totalPaymentCents(personId: Long): Long

    // ═══════════════════════════════════════════════════════
    //              ⭐ للتحقق من التكرار (في الدمج)
    // ═══════════════════════════════════════════════════════

    /**
     * ⭐ يبحث عن معاملة مطابقة تماماً (لتفادي التكرار عند الدمج).
     * المعايير: personId + type + amountCents + statement + timestamp
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE personId = :personId 
          AND type = :type 
          AND amountCents = :amountCents 
          AND statement = :statement 
          AND timestamp = :timestamp
          AND isVoided = 0
        LIMIT 1
    """)
    suspend fun findDuplicate(
        personId: Long,
        type: String,
        amountCents: Long,
        statement: String,
        timestamp: Long
    ): TransactionEntity?
}

data class PersonBalanceRow(
    val personId: Long,
    val balance: Long
)