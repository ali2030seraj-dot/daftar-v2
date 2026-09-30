package com.daftar.v2.domain.usecase

import com.daftar.v2.data.db.dao.RecurringTransactionDao
import com.daftar.v2.data.entity.RecurringInterval
import com.daftar.v2.data.entity.RecurringTransactionEntity
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.data.repository.TransactionRepository
import com.daftar.v2.domain.audit.AuditAction
import com.daftar.v2.domain.audit.AuditLogger
import java.util.Calendar
import javax.inject.Inject

sealed class DebtResult {
    data class Success(val transactionId: Long, val newBalanceCents: Long) : DebtResult()
    data class Rejected(val reason: String) : DebtResult()
    data class Error(val message: String) : DebtResult()
}

class AddDebtUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val getPersonBalance: GetPersonBalanceUseCase,
    private val auditLogger: AuditLogger,
    private val recurringDao: RecurringTransactionDao   // ⭐ جديد
) {

    suspend operator fun invoke(
        personId: Long,
        statement: String,
        amountCents: Long,
        note: String = "",
        isRecurring: Boolean = false,                              // ⭐ جديد
        recurringInterval: RecurringInterval = RecurringInterval.MONTHLY  // ⭐ جديد
    ): DebtResult {
        if (personId <= 0) {
            return DebtResult.Error("معرف الشخص غير صحيح")
        }
        if (amountCents <= 0) {
            return DebtResult.Rejected("المبلغ يجب أن يكون أكبر من صفر")
        }
        if (statement.isBlank()) {
            return DebtResult.Rejected("بيان العملية مطلوب")
        }

        return try {
            // 1) سجّل المعاملة في transactions
            val id = transactionRepository.addDebt(
                personId = personId,
                statement = statement,
                amountCents = amountCents,
                note = note
            )

            // 2) إذا كان تكرار تلقائي — سجّل في recurring_transactions
            if (isRecurring) {
                try {
                    val cal = Calendar.getInstance()
                    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                    val nextRun = calculateNextRun(recurringInterval, dayOfMonth)

                    recurringDao.insert(
                        RecurringTransactionEntity(
                            personId = personId,
                            type = TransactionType.DEBT,
                            statement = statement,
                            amountCents = amountCents,
                            note = note,
                            interval = recurringInterval,
                            dayOfMonth = dayOfMonth,
                            nextRunAt = nextRun,
                            isActive = true
                        )
                    )
                } catch (e: Exception) {
                    // لا نُفشل العملية الأصلية إذا فشل التكرار
                    e.printStackTrace()
                }
            }

            val balance = getPersonBalance(personId)

            auditLogger.log(
                action = AuditAction.DEBT_ADDED,
                entityType = "transaction",
                entityId = id,
                details = "مبلغ $amountCents سنت - $statement" +
                        if (isRecurring) " (متكرر: $recurringInterval)" else ""
            )

            DebtResult.Success(id, balance.balanceCents)
        } catch (e: Exception) {
            DebtResult.Error(e.message ?: "حدث خطأ غير متوقع")
        }
    }

    /**
     * ⭐ حساب وقت التشغيل التالي.
     */
    private fun calculateNextRun(
        interval: RecurringInterval,
        dayOfMonth: Int
    ): Long {
        val cal = Calendar.getInstance()
        when (interval) {
            RecurringInterval.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
            RecurringInterval.MONTHLY -> {
                cal.add(Calendar.MONTH, 1)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(28))
            }
            RecurringInterval.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}