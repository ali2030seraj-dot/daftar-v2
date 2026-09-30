package com.daftar.v2.domain.usecase

import com.daftar.v2.data.repository.ExpenseRepository
import javax.inject.Inject

sealed class ExpenseResult {
    data class Success(val expenseId: Long) : ExpenseResult()
    data class Rejected(val reason: String) : ExpenseResult()
    data class Error(val message: String) : ExpenseResult()
}

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    suspend operator fun invoke(
        statement: String,
        categoryId: Long?,
        amountCents: Long,
        currency: String,
        note: String = ""
    ): ExpenseResult {
        if (statement.isBlank()) {
            return ExpenseResult.Rejected("بيان المصروف مطلوب")
        }
        if (amountCents <= 0) {
            return ExpenseResult.Rejected("المبلغ يجب أن يكون أكبر من صفر")
        }
        if (currency.isBlank()) {
            return ExpenseResult.Rejected("العملة مطلوبة")
        }

        return try {
            val id = expenseRepository.create(
                statement = statement,
                categoryId = categoryId,
                amountCents = amountCents,
                currency = currency,
                note = note
            )
            ExpenseResult.Success(id)
        } catch (e: Exception) {
            ExpenseResult.Error(e.message ?: "فشل تسجيل المصروف")
        }
    }
}