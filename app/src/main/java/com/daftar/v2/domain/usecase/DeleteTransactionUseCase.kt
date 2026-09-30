package com.daftar.v2.domain.usecase

import com.daftar.v2.data.repository.TransactionRepository
import com.daftar.v2.domain.audit.AuditAction
import com.daftar.v2.domain.audit.AuditLogger
import javax.inject.Inject

sealed class DeleteTransactionResult {
    data class Success(val newBalanceCents: Long) : DeleteTransactionResult()
    data class Error(val message: String) : DeleteTransactionResult()
}

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val auditLogger: AuditLogger
) {

    suspend operator fun invoke(transactionId: Long): DeleteTransactionResult {
        if (transactionId <= 0) {
            return DeleteTransactionResult.Error("معرف العملية غير صحيح")
        }

        val existing = transactionRepository.getById(transactionId)
            ?: return DeleteTransactionResult.Error("العملية غير موجودة")

        return try {
            transactionRepository.hardDelete(transactionId)

            try {
                auditLogger.log(
                    action = AuditAction.TRANSACTION_DELETED,
                    entityType = "transaction",
                    entityId = transactionId,
                    details = existing.statement
                )
            } catch (_: Exception) {
            }

            // ❌ لا نحسب الرصيد هنا — Flow سيتولى ذلك
            DeleteTransactionResult.Success(0L)
        } catch (e: Exception) {
            DeleteTransactionResult.Error(e.message ?: "فشل الحذف")
        }
    }
}