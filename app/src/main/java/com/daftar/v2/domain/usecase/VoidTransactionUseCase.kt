package com.daftar.v2.domain.usecase

import com.daftar.v2.data.repository.TransactionRepository
import com.daftar.v2.domain.audit.AuditAction
import com.daftar.v2.domain.audit.AuditLogger
import javax.inject.Inject

sealed class VoidTransactionResult {
    data class Success(val newBalanceCents: Long) : VoidTransactionResult()
    data class Rejected(val reason: String) : VoidTransactionResult()
    data class Error(val message: String) : VoidTransactionResult()
}

class VoidTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val getPersonBalance: GetPersonBalanceUseCase,
    private val auditLogger: AuditLogger
) {

    suspend operator fun invoke(transactionId: Long): VoidTransactionResult {
        if (transactionId <= 0) {
            return VoidTransactionResult.Error("معرف العملية غير صحيح")
        }

        val existing = transactionRepository.getById(transactionId)
            ?: return VoidTransactionResult.Error("العملية غير موجودة")

        if (existing.isVoided) {
            return VoidTransactionResult.Rejected("العملية ملغاة مسبقًا")
        }

        return try {
            transactionRepository.void(transactionId)

            val balance = getPersonBalance(existing.personId)

            auditLogger.log(
                action = AuditAction.TRANSACTION_DELETED,
                entityType = "transaction",
                entityId = transactionId,
                details = "إلغاء عملية بمبلغ ${existing.amountCents} سنت"
            )

            // ⭐ التصحيح: balanceCents بدلاً من currentBalanceCents
            VoidTransactionResult.Success(balance.balanceCents)
        } catch (e: Exception) {
            VoidTransactionResult.Error(e.message ?: "فشل إلغاء العملية")
        }
    }
}