package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ⭐ نوع التكرار.
 */
enum class RecurringInterval {
    WEEKLY,     // كل أسبوع
    MONTHLY,    // كل شهر
    YEARLY      // كل سنة
}

/**
 * ⭐ معاملة متكررة تلقائياً.
 */
@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // ⭐ بيانات المعاملة الأصلية
    val personId: Long,
    val type: TransactionType,      // DEBT أو PAYMENT
    val statement: String,
    val amountCents: Long,
    val note: String = "",

    // ⭐ إعدادات التكرار
    val interval: RecurringInterval,
    val dayOfMonth: Int = 1,        // 1-31 (للتكرار الشهري)
    val nextRunAt: Long,            // وقت التشغيل التالي (millis)

    // ⭐ الحالة
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastRunAt: Long = 0L
)