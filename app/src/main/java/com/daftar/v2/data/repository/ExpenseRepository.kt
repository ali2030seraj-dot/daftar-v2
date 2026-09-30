package com.daftar.v2.data.repository

import com.daftar.v2.data.db.dao.ExpenseCategoryDao
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.entity.ExpenseCategoryEntity
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.domain.audit.AuditAction
import com.daftar.v2.domain.audit.AuditLogger
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: ExpenseCategoryDao,
    private val auditLogger: AuditLogger
) {

    fun observeAll(): Flow<List<ExpenseEntity>> =
        expenseDao.observeAll()

    fun observeCurrentMonth(): Flow<List<ExpenseEntity>> =
        expenseDao.observeBetween(startOfMonth(), endOfMonth())

    suspend fun create(
        statement: String,
        categoryId: Long?,
        amountCents: Long,
        currency: String,
        note: String = ""
    ): Long {
        require(statement.isNotBlank()) { "بيان المصروف مطلوب" }
        require(amountCents > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        require(currency.isNotBlank()) { "العملة مطلوبة" }

        val expense = ExpenseEntity(
            statement = statement.trim(),
            categoryId = categoryId,
            amountCents = amountCents,
            currency = currency,
            note = note.trim()
        )
        val id = expenseDao.insert(expense)

        auditLogger.log(
            action = AuditAction.EXPENSE_CREATED,
            entityType = "expense",
            entityId = id,
            details = "$statement - $amountCents سنت"
        )
        return id
    }

    suspend fun update(expense: ExpenseEntity) {
        require(expense.statement.isNotBlank()) { "بيان المصروف مطلوب" }
        require(expense.amountCents > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        expenseDao.update(expense.copy(updatedAt = System.currentTimeMillis()))

        auditLogger.log(
            action = AuditAction.EXPENSE_UPDATED,
            entityType = "expense",
            entityId = expense.id,
            details = expense.statement
        )
    }

    suspend fun void(id: Long) {
        expenseDao.voidById(id)
        auditLogger.log(
            action = AuditAction.EXPENSE_VOIDED,
            entityType = "expense",
            entityId = id
        )
    }

    suspend fun hardDelete(id: Long) {
        expenseDao.hardDelete(id)
        auditLogger.log(
            action = AuditAction.EXPENSE_DELETED,
            entityType = "expense",
            entityId = id
        )
    }

    fun observeCategories(): Flow<List<ExpenseCategoryEntity>> =
        categoryDao.observeAll()

    suspend fun createCategory(name: String): Long {
        require(name.isNotBlank()) { "اسم التصنيف مطلوب" }
        val id = categoryDao.insert(ExpenseCategoryEntity(name = name.trim()))
        auditLogger.log(
            action = AuditAction.CATEGORY_CREATED,
            entityType = "category",
            entityId = id,
            details = name
        )
        return id
    }

    suspend fun deleteCategory(category: ExpenseCategoryEntity) {
        categoryDao.delete(category)
        auditLogger.log(
            action = AuditAction.CATEGORY_DELETED,
            entityType = "category",
            entityId = category.id,
            details = category.name
        )
    }

    private fun startOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun endOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}