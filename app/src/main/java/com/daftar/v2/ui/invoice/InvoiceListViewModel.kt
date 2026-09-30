package com.daftar.v2.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.entity.InvoiceEntity
import com.daftar.v2.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceListUiState(
    val loading: Boolean = true,
    val invoices: List<InvoiceEntity> = emptyList(),
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val repository: InvoiceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceListUiState())
    val state: StateFlow<InvoiceListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAll().collect { invoices ->
                _state.value = _state.value.copy(
                    loading = false,
                    invoices = invoices
                )
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            try {
                repository.delete(id)
                _state.value = _state.value.copy(
                    message = "تم حذف الفاتورة",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل الحذف",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}