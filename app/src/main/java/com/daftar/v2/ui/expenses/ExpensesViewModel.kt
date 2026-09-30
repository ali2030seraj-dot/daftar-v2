package com.daftar.v2.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.data.repository.ExpenseRepository
import com.daftar.v2.domain.usecase.AddExpenseUseCase
import com.daftar.v2.domain.usecase.DeleteExpenseUseCase
import com.daftar.v2.domain.usecase.ExpenseResult
import com.daftar.v2.util.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ExpensesUiState(
    val loading: Boolean = true,
    val expenses: List<ExpenseEntity> = emptyList(),
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val addExpense: AddExpenseUseCase,
    private val deleteExpense: DeleteExpenseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExpensesUiState())
    val state: StateFlow<ExpensesUiState> = _state.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.observeAll().collect { list ->
                _state.value = _state.value.copy(loading = false, expenses = list)
            }
        }
    }

    fun add(statement: String, amountCents: Long, currency: String, note: String) {
        viewModelScope.launch {
            // ⭐ نطبّع العملة قبل الحفظ
            val code = CurrencyUtils.toCode(currency)
            val result = withContext(Dispatchers.IO) {
                addExpense(statement, null, amountCents, code, note)
            }
            when (result) {
                is ExpenseResult.Success -> {
                    _state.value = _state.value.copy(
                        message = "تم تسجيل المصروف",
                        isError = false
                    )
                }
                is ExpenseResult.Rejected -> {
                    _state.value = _state.value.copy(
                        message = result.reason,
                        isError = true
                    )
                }
                is ExpenseResult.Error -> {
                    _state.value = _state.value.copy(
                        message = result.message,
                        isError = true
                    )
                }
            }
        }
    }

    fun update(
        existing: ExpenseEntity,
        statement: String,
        amountCents: Long,
        currency: String,
        note: String
    ) {
        viewModelScope.launch {
            try {
                // ⭐ نطبّع العملة قبل التحديث
                val code = CurrencyUtils.toCode(currency)
                withContext(Dispatchers.IO) {
                    expenseRepository.update(
                        existing.copy(
                            statement = statement.trim(),
                            amountCents = amountCents,
                            currency = code,
                            note = note.trim()
                        )
                    )
                }
                _state.value = _state.value.copy(
                    message = "تم تحديث المصروف",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "فشل التحديث",
                    isError = true
                )
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { deleteExpense(id) }
            _state.value = _state.value.copy(
                message = "تم الحذف",
                isError = false
            )
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}