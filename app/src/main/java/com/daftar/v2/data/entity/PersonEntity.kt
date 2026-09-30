package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PersonType {
    CREDITOR,
    DEBTOR
}

@Entity(
    tableName = "people",
    indices = [Index("type"), Index("isArchived")]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val note: String = "",
    val type: PersonType,
    val currency: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)