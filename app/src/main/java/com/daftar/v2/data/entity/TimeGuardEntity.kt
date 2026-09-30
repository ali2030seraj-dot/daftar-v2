package com.daftar.v2.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_guard")
data class TimeGuardEntity(
    @PrimaryKey
    val id: Int = 1,

    // آخر وقت معروف (Unix milliseconds)
    val lastKnownTime: Long,

    // elapsedRealtime عند آخر تحديث
    val lastElapsedRealtime: Long,

    // wallClock عند آخر تحديث
    val lastWallClock: Long
)