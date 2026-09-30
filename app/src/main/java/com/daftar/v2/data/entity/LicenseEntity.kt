package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "licenses",
    indices = [Index("licenseId"), Index("isActive")]
)
data class LicenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // معرف الترخيص الفريد (مثل L-2026-0001)
    val licenseId: String,

    // الكود الكامل المُدخل
    val licenseCode: String,

    // الباقة: TRIAL, M30, M90, M365, LIFETIME
    val plan: String,

    // تاريخ الإصدار (Unix seconds)
    val issuedAt: Long,

    // تاريخ الانتهاء (Unix seconds، صفر للدائم)
    val expiresAt: Long,

    // بصمة الجهاز
    val deviceFingerprint: String,

    // توقيع Ed25519 (64 بايت)
    val signature: ByteArray,

    // الـ Payload الأصلي (22 بايت)
    val payload: ByteArray,

    // تاريخ التفعيل على هذا الجهاز
    val activatedAt: Long = System.currentTimeMillis(),

    // هل الترخيص نشط؟
    val isActive: Boolean = true,

    // آخر فحص للترخيص
    val lastCheck: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LicenseEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}