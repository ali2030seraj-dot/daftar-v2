package com.daftar.v2.ui.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.entity.InvoiceEntity
import com.daftar.v2.data.entity.InvoiceItemEntity
import com.daftar.v2.data.entity.InvoiceStatus
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.repository.InvoiceRepository
import com.daftar.v2.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceViewUiState(
    val loading: Boolean = true,
    val invoice: InvoiceEntity? = null,
    val items: List<InvoiceItemEntity> = emptyList(),
    val person: PersonEntity? = null,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class InvoiceViewViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val personRepo: PersonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceViewUiState())
    val state: StateFlow<InvoiceViewUiState> = _state.asStateFlow()

    private val invoiceId: Long = savedStateHandle.get<String>("invoiceId")
        ?.toLongOrNull() ?: 0L

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val inv = invoiceRepo.getById(invoiceId)
            if (inv == null) {
                _state.value = InvoiceViewUiState(
                    loading = false,
                    message = "الفاتورة غير موجودة",
                    isError = true
                )
                return@launch
            }
            val items = invoiceRepo.getItems(inv.id)
            val person = personRepo.getById(inv.personId)

            _state.value = InvoiceViewUiState(
                loading = false,
                invoice = inv,
                items = items,
                person = person
            )
        }
    }

    fun markAsPaid() {
        viewModelScope.launch {
            try {
                invoiceRepo.updateStatus(invoiceId, InvoiceStatus.PAID)
                load()
                _state.value = _state.value.copy(
                    message = "✅ تم تعليمها كمدفوعة",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "❌ فشل",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}