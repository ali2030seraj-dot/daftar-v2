package com.daftar.v2.domain.usecase

import com.daftar.v2.data.repository.TransactionRepository
import com.daftar.v2.domain.audit.AuditAction
import com.daftar.v2.domain.audit.AuditLogger
import javax.inject.Inject

sealed class PaymentResult {
    data class Success(val transactionId: Long, val newBalanceCents: Long) : PaymentResult()
    data class Rejected(val reason: String) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
}

class AddPaymentUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val getPersonBalance: GetPersonBalanceUseCase,
    private val auditLogger: AuditLogger
) {

    suspend operator fun invoke(
        personId: Long,
        amountCents: Long,
        note: String = ""
    ): PaymentResult {
        if (personId <= 0) {
            return PaymentResult.Error("معرف الشخص غير صحيح")
        }
        if (amountCents <= 0) {
            return PaymentResult.Rejected("المبلغ يجب أن يكون أكبر من صفر")
        }

        val balance = getPersonBalance(personId)
        if (balance.balanceCents <= 0) {
            return PaymentResult.Rejected("لا يوجد رصيد مستحق على هذا الشخص")
        }
        if (amountCents > balance.balanceCents) {
            return PaymentResult.Rejected(
                "المبلغ أكبر من الرصيد المتبقي. الرصيد الحالي: ${balance.balanceCents}"
            )
        }

        return try {
            val id = transactionRepository.addPayment(personId, amountCents, note)
            val newBalance = balance.balanceCents - amountCents

            auditLogger.log(
                action = AuditAction.PAYMENT_ADDED,
                entityType = "transaction",
                entityId = id,
                details = "سداد بمبلغ $amountCents سنت للشخص رقم $personId"
            )

            PaymentResult.Success(id, newBalance)
        } catch (e: Exception) {
            PaymentResult.Error(e.message ?: "حدث خطأ غير متوقع")
        }
    }
}