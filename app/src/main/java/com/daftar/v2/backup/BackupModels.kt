package com.daftar.v2.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupFile(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val appName: String = "دفتر حساباتي",
    val source: String = "DaftarV2",
    val people: List<BackupPerson> = emptyList(),
    val transactions: List<BackupTransaction> = emptyList(),
    val expenses: List<BackupExpense> = emptyList(),
    val categories: List<BackupCategory> = emptyList(),
    val settings: List<BackupSetting> = emptyList(),
    // ✅ جديد — العلامة التجارية
    val branding: BackupBranding? = null
)

@Serializable
data class BackupBranding(
    val storeName: String = "",
    val storePhone: String = "",
    val storeAddress: String = "",
    // ✅ الشعار بصيغة base64
    val logoBase64: String? = null
)

@Serializable
data class BackupPerson(
    val id: Long,
    val name: String,
    val phone: String = "",
    val type: String,
    val currency: String,
    @SerialName("isArchived") val isArchived: Boolean = false,
    @SerialName("createdAt") val createdAt: Long = 0L,
    @SerialName("updatedAt") val updatedAt: Long = 0L
)

@Serializable
data class BackupTransaction(
    val id: Long,
    val personId: Long,
    val type: String,
    val amountCents: Long = 0L,
    val paymentCents: Long = 0L,
    val statement: String = "",
    val note: String = "",
    val timestamp: Long = 0L,
    val dayName: String = "",
    val date: String = "",
    @SerialName("isVoided") val isVoided: Boolean = false,
    // ✅ جديد — الملاحظة الصوتية
    val voiceFileName: String? = null,
    val voiceBase64: String? = null
)

@Serializable
data class BackupExpense(
    val id: Long,
    val statement: String,
    val categoryId: Long? = null,
    val amountCents: Long,
    val currency: String,
    val note: String = "",
    val timestamp: Long = 0L,
    val dayName: String = "",
    val date: String = "",
    @SerialName("isVoided") val isVoided: Boolean = false
)

@Serializable
data class BackupCategory(
    val id: Long,
    val name: String,
    @SerialName("createdAt") val createdAt: Long = 0L
)

@Serializable
data class BackupSetting(
    val key: String,
    val value: String
)