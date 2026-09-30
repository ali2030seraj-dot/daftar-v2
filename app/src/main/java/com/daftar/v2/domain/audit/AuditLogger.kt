package com.daftar.v2.domain.audit

import com.daftar.v2.data.db.dao.AuditLogDao
import com.daftar.v2.data.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditLogger @Inject constructor(
    private val auditLogDao: AuditLogDao
) {

    suspend fun log(
        action: AuditAction,
        entityType: String,
        entityId: Long? = null,
        details: String = ""
    ) {
        val entry = AuditLogEntity(
            action = action.label,
            entityType = entityType,
            entityId = entityId,
            details = details
        )
        auditLogDao.insert(entry)
    }

    fun observeRecent(limit: Int = 500): Flow<List<AuditLogEntity>> =
        auditLogDao.observeRecent(limit)

    suspend fun clearOlderThan(timestamp: Long) {
        auditLogDao.clearOlderThan(timestamp)
    }
}