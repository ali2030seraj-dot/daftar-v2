package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index("timestamp"), Index("categoryId"), Index("isVoided")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val statement: String,
    val categoryId: Long? = null,
    val amountCents: Long,
    val currency: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isVoided: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)