package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "used_licenses",
    indices = [Index("deviceFingerprint")]
)
data class UsedLicenseEntity(
    @PrimaryKey
    val licenseId: String,

    val deviceFingerprint: String,

    val usedAt: Long = System.currentTimeMillis()
)