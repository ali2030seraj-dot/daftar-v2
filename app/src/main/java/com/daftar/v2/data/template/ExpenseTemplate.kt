package com.daftar.v2.data.template

import kotlinx.serialization.Serializable

/**
 * قالب مصروف محفوظ.
 */
@Serializable
data class ExpenseTemplate(
    val id: String,
    val name: String,               // اسم القالب: "إيجار"، "كهرباء"
    val defaultAmountCents: Long,   // المبلغ الافتراضي
    val currency: String,           // العملة
    val categoryId: Long? = null,   // تصنيف اختياري
    val note: String = ""           // ملاحظة اختيارية
)