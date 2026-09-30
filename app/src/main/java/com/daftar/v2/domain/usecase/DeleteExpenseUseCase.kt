package com.daftar.v2.domain.usecase

import com.daftar.v2.data.repository.ExpenseRepository
import javax.inject.Inject

sealed class DeleteExpenseResult {
    object Success : DeleteExpenseResult()
    data class Error(val message: String) : DeleteExpenseResult()
}

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    suspend operator fun invoke(expenseId: Long): DeleteExpenseResult {
        if (expenseId <= 0) {
            return DeleteExpenseResult.Error("معرف المصروف غير صحيح")
        }

        return try {
            expenseRepository.hardDelete(expenseId)
            DeleteExpenseResult.Success
        } catch (e: Exception) {
            DeleteExpenseResult.Error(e.message ?: "فشل الحذف")
        }
    }
}