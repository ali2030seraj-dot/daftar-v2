package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ⭐ نوع قالب الرسالة.
 */
enum class SmsTemplateType {
    DEBT_REGISTERED,    // عند تسجيل دين
    PAYMENT_RECEIVED,   // عند استلام دفعة
    REMINDER            // تذكير بالدين
}

/**
 * ⭐ قالب رسالة SMS قابل للتخصيص.
 */
@Entity(tableName = "sms_templates")
data class SmsTemplateEntity(
    @PrimaryKey val type: String,     // SmsTemplateType.name
    val body: String,
    val isCustom: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)