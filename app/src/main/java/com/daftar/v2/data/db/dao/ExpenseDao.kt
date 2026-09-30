package com.daftar.v2.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.daftar.v2.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE isVoided = 0 ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :from AND :to AND isVoided = 0")
    fun observeBetween(from: Long, to: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE isVoided = 0")
    suspend fun getAllActive(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("UPDATE expenses SET isVoided = 1, updatedAt = :now WHERE id = :id")
    suspend fun voidById(id: Long, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()

    // ═══════════════════════════════════════════════════════
    //              ⭐ للتحقق من التكرار (في الدمج)
    // ═══════════════════════════════════════════════════════

    /**
     * ⭐ يبحث عن مصروف مطابق تماماً (لتفادي التكرار عند الدمج).
     * المعايير: statement + amountCents + currency + timestamp
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE statement = :statement 
          AND amountCents = :amountCents 
          AND currency = :currency 
          AND timestamp = :timestamp
          AND isVoided = 0
        LIMIT 1
    """)
    suspend fun findDuplicate(
        statement: String,
        amountCents: Long,
        currency: String,
        timestamp: Long
    ): ExpenseEntity?
}