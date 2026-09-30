package com.daftar.v2.data.repository

import com.daftar.v2.data.db.dao.InvoiceDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.entity.InvoiceEntity
import com.daftar.v2.data.entity.InvoiceItemEntity
import com.daftar.v2.data.entity.InvoiceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val personDao: PersonDao
) {

    fun observeAll(): Flow<List<InvoiceEntity>> = invoiceDao.observeAll()

    fun observeByPerson(personId: Long): Flow<List<InvoiceEntity>> =
        invoiceDao.observeByPerson(personId)

    fun observeItems(invoiceId: Long): Flow<List<InvoiceItemEntity>> =
        invoiceDao.observeItems(invoiceId)

    suspend fun getById(id: Long): InvoiceEntity? = invoiceDao.getById(id)

    suspend fun getItems(invoiceId: Long): List<InvoiceItemEntity> =
        invoiceDao.getItems(invoiceId)

    /**
     * ⭐ يولّد رقم فاتورة تسلسلي.
     * INV-2026-0001
     */
    suspend fun generateNumber(): String = withContext(Dispatchers.IO) {
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
        val prefix = "INV-$year-"
        val count = invoiceDao.countByPrefix(prefix)
        val next = (count + 1).toString().padStart(4, '0')
        "$prefix$next"
    }

    /**
     * ⭐ يحفظ الفاتورة + بنودها.
     */
    suspend fun saveWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long = withContext(Dispatchers.IO) {
        val invoiceId: Long
        if (invoice.id == 0L) {
            invoiceId = invoiceDao.insert(invoice)
        } else {
            invoiceId = invoice.id
            invoiceDao.update(invoice)
            invoiceDao.deleteItems(invoiceId)
        }

        val itemsWithId = items.mapIndexed { index, item ->
            item.copy(id = 0, invoiceId = invoiceId, position = index)
        }
        invoiceDao.insertItems(itemsWithId)
        invoiceId
    }

    suspend fun updateStatus(id: Long, status: InvoiceStatus) =
        withContext(Dispatchers.IO) {
            val existing = invoiceDao.getById(id) ?: return@withContext
            invoiceDao.update(
                existing.copy(
                    status = status,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        invoiceDao.deleteItems(id)
        invoiceDao.deleteById(id)
    }

    /**
     * ⭐ يحسب الإجماليات.
     */
    fun computeTotals(
        items: List<InvoiceItemEntity>,
        taxPercent: Double,
        discountCents: Long
    ): Totals {
        val subtotal = items.sumOf { it.totalCents }
        val afterDiscount = (subtotal - discountCents).coerceAtLeast(0)
        val tax = (afterDiscount * taxPercent / 100.0).toLong()
        val total = afterDiscount + tax
        return Totals(subtotal, tax, afterDiscount, total)
    }

    data class Totals(
        val subtotalCents: Long,
        val taxCents: Long,
        val afterDiscountCents: Long,
        val totalCents: Long
    )
}