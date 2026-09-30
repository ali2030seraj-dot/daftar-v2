package com.daftar.v2.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.daftar.v2.data.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 500): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insert(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs WHERE timestamp < :before")
    suspend fun clearOlderThan(before: Long)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
}