package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ⭐ حالة الفاتورة.
 */
enum class InvoiceStatus {
    DRAFT,      // مسودة
    ISSUED,     // صادرة
    PAID,       // مدفوعة
    CANCELLED   // ملغاة
}

/**
 * ⭐ فاتورة.
 */
@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,              // INV-2026-0001
    val personId: Long,              // العميل
    val currency: String,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val note: String = "",
    val subtotalCents: Long = 0L,
    val taxPercent: Double = 0.0,
    val taxCents: Long = 0L,
    val discountCents: Long = 0L,
    val totalCents: Long = 0L,
    val issuedAt: Long = System.currentTimeMillis(),
    val dueAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * ⭐ بند في الفاتورة.
 */
@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val description: String,
    val quantity: Double = 1.0,
    val unitPriceCents: Long,
    val totalCents: Long,
    val position: Int = 0
)