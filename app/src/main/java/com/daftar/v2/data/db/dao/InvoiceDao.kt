package com.daftar.v2.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.daftar.v2.data.entity.InvoiceEntity
import com.daftar.v2.data.entity.InvoiceItemEntity
import com.daftar.v2.data.entity.InvoiceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    // ═════ الفواتير ═════

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE personId = :personId ORDER BY createdAt DESC")
    fun observeByPerson(personId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: InvoiceStatus): Flow<List<InvoiceEntity>>

    @Query("SELECT COUNT(*) FROM invoices WHERE number LIKE :prefix || '%'")
    suspend fun countByPrefix(prefix: String): Int

    @Insert
    suspend fun insert(invoice: InvoiceEntity): Long

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Delete
    suspend fun delete(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM invoices")
    suspend fun deleteAll()

    // ═════ البنود ═════

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY position ASC")
    suspend fun getItems(invoiceId: Long): List<InvoiceItemEntity>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY position ASC")
    fun observeItems(invoiceId: Long): Flow<List<InvoiceItemEntity>>

    @Insert
    suspend fun insertItem(item: InvoiceItemEntity): Long

    @Insert
    suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItems(invoiceId: Long)

    @Update
    suspend fun updateItem(item: InvoiceItemEntity)
}