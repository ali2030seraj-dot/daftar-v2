package com.daftar.v2.domain.usecase

import com.daftar.v2.data.repository.PersonRepository
import com.daftar.v2.domain.audit.AuditAction
import com.daftar.v2.domain.audit.AuditLogger
import javax.inject.Inject

sealed class ArchiveResult {
    data class Archived(val personId: Long) : ArchiveResult()
    data class Restored(val personId: Long) : ArchiveResult()
    data class Deleted(val personId: Long) : ArchiveResult()
    data class Rejected(val reason: String) : ArchiveResult()
    data class Error(val message: String) : ArchiveResult()
}

class ArchivePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository,
    private val getPersonBalance: GetPersonBalanceUseCase,
    private val auditLogger: AuditLogger
) {

    suspend fun archive(personId: Long): ArchiveResult {
        if (personId <= 0) return ArchiveResult.Error("معرف الشخص غير صحيح")

        return try {
            personRepository.archive(personId)
            auditLogger.log(
                action = AuditAction.PERSON_ARCHIVED,
                entityType = "person",
                entityId = personId
            )
            ArchiveResult.Archived(personId)
        } catch (e: Exception) {
            ArchiveResult.Error(e.message ?: "فشل الأرشفة")
        }
    }

    suspend fun restore(personId: Long): ArchiveResult {
        if (personId <= 0) return ArchiveResult.Error("معرف الشخص غير صحيح")

        return try {
            personRepository.restore(personId)
            auditLogger.log(
                action = AuditAction.PERSON_RESTORED,
                entityType = "person",
                entityId = personId
            )
            ArchiveResult.Restored(personId)
        } catch (e: Exception) {
            ArchiveResult.Error(e.message ?: "فشل الاستعادة")
        }
    }

    suspend fun hardDelete(personId: Long, forceDelete: Boolean = false): ArchiveResult {
        if (personId <= 0) return ArchiveResult.Error("معرف الشخص غير صحيح")

        return try {
            if (!forceDelete) {
                val balance = getPersonBalance(personId)
                if (balance.balanceCents != 0L) {
                    return ArchiveResult.Rejected(
                        "لا يمكن حذف حساب برصيد غير صفري. " +
                        "الرصيد الحالي: ${balance.balanceCents}. " +
                        "استخدم الأرشفة بدلًا من الحذف."
                    )
                }
            }

            personRepository.hardDelete(personId)
            auditLogger.log(
                action = AuditAction.PERSON_DELETED,
                entityType = "person",
                entityId = personId
            )
            ArchiveResult.Deleted(personId)
        } catch (e: Exception) {
            ArchiveResult.Error(e.message ?: "فشل الحذف النهائي")
        }
    }
}