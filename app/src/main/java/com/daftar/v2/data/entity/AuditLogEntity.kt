package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_logs",
    indices = [Index("timestamp"), Index("entityType")]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val entityType: String,
    val entityId: Long? = null,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)