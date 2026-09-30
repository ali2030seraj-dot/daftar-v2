package com.daftar.v2.ui.statement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.data.entity.RecurringInterval
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.domain.usecase.AddDebtUseCase
import com.daftar.v2.domain.usecase.AddPaymentUseCase
import com.daftar.v2.domain.usecase.DebtResult
import com.daftar.v2.domain.usecase.DeleteTransactionResult
import com.daftar.v2.domain.usecase.DeleteTransactionUseCase
import com.daftar.v2.domain.usecase.EditTransactionResult
import com.daftar.v2.domain.usecase.EditTransactionUseCase
import com.daftar.v2.domain.usecase.GetStatementUseCase
import com.daftar.v2.domain.usecase.PaymentResult
import com.daftar.v2.domain.usecase.Statement
import com.daftar.v2.domain.usecase.StatementRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatementUiState(
    val loading: Boolean = true,
    val statement: Statement? = null,
    val message: String? = null,
    val isError: Boolean = false,
    val isAllMode: Boolean = false
)

@HiltViewModel
class StatementViewModel @Inject constructor(
    private val getStatement: GetStatementUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val addPaymentUseCase: AddPaymentUseCase,
    private val editTransactionUseCase: EditTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val transactionDao: TransactionDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _state = MutableStateFlow(StatementUiState())
    val state: StateFlow<StatementUiState> = _state.asStateFlow()

    private var currentPersonId: Long = 0L
    private var observing = false

    fun init(personId: Long) {
        if (observing && currentPersonId == personId) return
        currentPersonId = personId
        observing = true

        if (personId == 0L) {
            observeAllTransactions()
        } else {
            observeStatement(personId)
        }
    }

    private fun observeStatement(personId: Long) {
        viewModelScope.launch {
            getStatement.observe(personId).collect { stmt ->
                _state.value = _state.value.copy(
                    loading = false,
                    statement = stmt,
                    isAllMode = false
                )
            }
        }
    }

    private fun observeAllTransactions() {
        viewModelScope.launch {
            transactionDao.observeAllTransactions().collect { transactions ->
                val allExpenses = expenseDao.getAllActive()
                val stmt = buildAllStatement(transactions, allExpenses)
                _state.value = _state.value.copy(
                    loading = false,
                    statement = stmt,
                    isAllMode = true
                )
            }
        }
    }

    private fun buildAllStatement(
        transactions: List<TransactionEntity>,
        expenses: List<ExpenseEntity>
    ): Statement {
        val sorted = transactions.sortedByDescending { it.timestamp }

        val totalDebt = sorted
            .filter { it.type == TransactionType.DEBT }
            .sumOf { it.amountCents }

        val totalPayment = sorted
            .filter { it.type == TransactionType.PAYMENT }
            .sumOf { it.amountCents }

        val totalExpenses = expenses
            .filter { !it.isVoided }
            .sumOf { it.amountCents }

        val rows = sorted.map { tx ->
            StatementRow(
                transaction = tx,
                runningBalanceCents = 0L
            )
        }

        return Statement(
            person = null,
            rows = rows,
            totalDebtCents = totalDebt,
            totalPaymentCents = totalPayment,
            finalBalanceCents = totalDebt - totalPayment - totalExpenses,
            isAllMode = true,
            displayCurrency = "YER"
        )
    }

    /**
     * ⭐ تسجيل دين — مع دعم التكرار التلقائي
     */
    fun addDebtTransaction(
        statement: String,
        amountCents: Long,
        note: String,
        isRecurring: Boolean = false,
        recurringInterval: RecurringInterval = RecurringInterval.MONTHLY
    ) {
        if (_state.value.isAllMode) {
            _state.value = _state.value.copy(
                message = "لا يمكن الإضافة من شاشة عرض الكل",
                isError = true
            )
            return
        }
        viewModelScope.launch {
            val result = addDebtUseCase(
                personId = currentPersonId,
                statement = statement,
                amountCents = amountCents,
                note = note,
                isRecurring = isRecurring,
                recurringInterval = recurringInterval
            )
            when (result) {
                is DebtResult.Success -> {
                    val msg = if (isRecurring) {
                        "تم تسجيل المبلغ + تفعيل التكرار"
                    } else {
                        "تم تسجيل المبلغ"
                    }
                    _state.value = _state.value.copy(message = msg, isError = false)
                }
                is DebtResult.Rejected -> _state.value = _state.value.copy(
                    message = result.reason, isError = true
                )
                is DebtResult.Error -> _state.value = _state.value.copy(
                    message = result.message, isError = true
                )
            }
        }
    }

    fun addPaymentTransaction(amountCents: Long, note: String) {
        if (_state.value.isAllMode) return
        viewModelScope.launch {
            when (val result = addPaymentUseCase(currentPersonId, amountCents, note)) {
                is PaymentResult.Success -> _state.value = _state.value.copy(
                    message = "تم تسجيل السداد", isError = false
                )
                is PaymentResult.Rejected -> _state.value = _state.value.copy(
                    message = result.reason, isError = true
                )
                is PaymentResult.Error -> _state.value = _state.value.copy(
                    message = result.message, isError = true
                )
            }
        }
    }

    fun editTransaction(
        txId: Long,
        statement: String,
        amountCents: Long,
        note: String
    ) {
        viewModelScope.launch {
            when (val result = editTransactionUseCase(txId, statement, amountCents, note)) {
                is EditTransactionResult.Success -> _state.value = _state.value.copy(
                    message = "تم التعديل", isError = false
                )
                is EditTransactionResult.Rejected -> _state.value = _state.value.copy(
                    message = result.reason, isError = true
                )
                is EditTransactionResult.Error -> _state.value = _state.value.copy(
                    message = result.message, isError = true
                )
            }
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            when (val result = deleteTransactionUseCase(txId)) {
                is DeleteTransactionResult.Success -> _state.value = _state.value.copy(
                    message = "تم حذف العملية", isError = false
                )
                is DeleteTransactionResult.Error -> _state.value = _state.value.copy(
                    message = result.message, isError = true
                )
            }
        }
    }

    fun attachVoice(txId: Long, path: String) {
        viewModelScope.launch {
            try {
                transactionDao.updateVoicePath(txId, path)
                _state.value = _state.value.copy(
                    message = "🎤 تم إرفاق التسجيل",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل حفظ التسجيل: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun removeVoice(txId: Long) {
        viewModelScope.launch {
            try {
                transactionDao.updateVoicePath(txId, null)
                _state.value = _state.value.copy(
                    message = "تم حذف التسجيل",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل الحذف: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}