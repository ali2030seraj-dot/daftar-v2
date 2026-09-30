package com.daftar.v2.domain.usecase

import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.data.repository.TransactionRepository
import com.daftar.v2.domain.audit.AuditAction
import com.daftar.v2.domain.audit.AuditLogger
import javax.inject.Inject

sealed class EditTransactionResult {
    data class Success(val newBalanceCents: Long) : EditTransactionResult()
    data class Rejected(val reason: String) : EditTransactionResult()
    data class Error(val message: String) : EditTransactionResult()
}

class EditTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val auditLogger: AuditLogger
) {

    suspend operator fun invoke(
        transactionId: Long,
        newStatement: String,
        newAmountCents: Long,
        newNote: String
    ): EditTransactionResult {
        if (transactionId <= 0) {
            return EditTransactionResult.Error("معرف العملية غير صحيح")
        }
        if (newAmountCents <= 0) {
            return EditTransactionResult.Rejected("المبلغ يجب أن يكون أكبر من صفر")
        }

        val existing = transactionRepository.getById(transactionId)
            ?: return EditTransactionResult.Error("العملية غير موجودة")

        // للتحقق من السداد الزائد
        if (existing.type == TransactionType.PAYMENT) {
            val totalDebt = transactionRepository.totalDebtCents(existing.personId)
            val totalPaid = transactionRepository.totalPaymentCents(existing.personId)
            val balanceWithoutThis = totalDebt - (totalPaid - existing.amountCents)

            if (newAmountCents > balanceWithoutThis) {
                return EditTransactionResult.Rejected(
                    "المبلغ المسدد أكبر من الرصيد المتبقي"
                )
            }
        }

        return try {
            val updated = existing.copy(
                statement = newStatement.trim(),
                amountCents = newAmountCents,
                note = newNote.trim()
            )
            transactionRepository.update(updated)

            // سجل التغيير — بسيط، بدون تعقيد
            try {
                auditLogger.log(
                    action = AuditAction.TRANSACTION_UPDATED,
                    entityType = "transaction",
                    entityId = transactionId,
                    details = "$newStatement - $newAmountCents سنت"
                )
            } catch (_: Exception) {
            }

            // ❌ لا نحسب الرصيد هنا — Flow في ViewModel سيجلبه تلقائيًا
            EditTransactionResult.Success(0L)
        } catch (e: Exception) {
            EditTransactionResult.Error(e.message ?: "فشل التعديل")
        }
    }
}