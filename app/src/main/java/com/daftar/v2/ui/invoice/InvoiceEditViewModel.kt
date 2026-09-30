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
import com.daftar.v2.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceEditUiState(
    val loading: Boolean = true,
    val isNew: Boolean = true,
    val invoiceId: Long = 0L,
    val number: String = "",
    val personId: Long = 0L,
    val personName: String = "",
    val currency: String = "YER",
    val note: String = "",
    val items: List<InvoiceItemEntity> = emptyList(),
    val taxPercent: Double = 0.0,
    val discountCents: Long = 0L,
    val subtotalCents: Long = 0L,
    val taxCents: Long = 0L,
    val totalCents: Long = 0L,
    val allPeople: List<PersonEntity> = emptyList(),   // ⭐ جديد
    val showPersonPicker: Boolean = false,              // ⭐ جديد
    val message: String? = null,
    val isError: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class InvoiceEditViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val personRepo: PersonRepository,
    private val settingsRepo: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceEditUiState())
    val state: StateFlow<InvoiceEditUiState> = _state.asStateFlow()

    init {
        val invoiceId: Long = savedStateHandle.get<String>("invoiceId")
            ?.toLongOrNull() ?: 0L
        val personIdArg: Long = savedStateHandle.get<String>("personId")
            ?.toLongOrNull() ?: 0L

        loadPeople()   // ⭐ حمّل قائمة العملاء أولاً

        if (invoiceId > 0L) {
            loadInvoice(invoiceId)
        } else {
            initNewInvoice(personIdArg)
        }
    }

    /**
     * ⭐ يحمّل قائمة العملاء.
     */
    private fun loadPeople() {
        viewModelScope.launch {
            try {
                val people = personRepo.getAll().filter { !it.isArchived }
                _state.value = _state.value.copy(allPeople = people)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadInvoice(id: Long) {
        viewModelScope.launch {
            val inv = invoiceRepo.getById(id)
            if (inv == null) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = "الفاتورة غير موجودة",
                    isError = true
                )
                return@launch
            }
            val items = invoiceRepo.getItems(id)
            val person = personRepo.getById(inv.personId)

            _state.value = _state.value.copy(
                loading = false,
                isNew = false,
                invoiceId = inv.id,
                number = inv.number,
                personId = inv.personId,
                personName = person?.name ?: "",
                currency = inv.currency,
                note = inv.note,
                items = items,
                taxPercent = inv.taxPercent,
                discountCents = inv.discountCents,
                subtotalCents = inv.subtotalCents,
                taxCents = inv.taxCents,
                totalCents = inv.totalCents
            )
        }
    }

    private fun initNewInvoice(personId: Long) {
        viewModelScope.launch {
            val currency = settingsRepo.getDefaultCurrency()
            val number = invoiceRepo.generateNumber()

            var personName = ""
            if (personId > 0L) {
                personName = personRepo.getById(personId)?.name ?: ""
            }

            _state.value = _state.value.copy(
                loading = false,
                isNew = true,
                number = number,
                personId = personId,
                personName = personName,
                currency = currency,
                items = listOf(
                    InvoiceItemEntity(
                        id = 0,
                        invoiceId = 0,
                        description = "",
                        quantity = 1.0,
                        unitPriceCents = 0L,
                        totalCents = 0L,
                        position = 0
                    )
                )
            )
        }
    }

    /**
     * ⭐ يفتح قائمة اختيار العميل.
     */
    fun openPersonPicker() {
        _state.value = _state.value.copy(showPersonPicker = true)
    }

    /**
     * ⭐ يغلق قائمة اختيار العميل.
     */
    fun closePersonPicker() {
        _state.value = _state.value.copy(showPersonPicker = false)
    }

    /**
     * ⭐ عند اختيار عميل.
     */
    fun selectPerson(person: PersonEntity) {
        _state.value = _state.value.copy(
            personId = person.id,
            personName = person.name,
            currency = person.currency,
            showPersonPicker = false
        )
    }

    fun setNote(text: String) {
        _state.value = _state.value.copy(note = text)
    }

    fun setTaxPercent(percent: Double) {
        _state.value = _state.value.copy(taxPercent = percent.coerceIn(0.0, 100.0))
        recalculate()
    }

    fun setDiscountCents(cents: Long) {
        _state.value = _state.value.copy(discountCents = cents.coerceAtLeast(0))
        recalculate()
    }

    fun addItem() {
        val newItem = InvoiceItemEntity(
            id = 0,
            invoiceId = 0,
            description = "",
            quantity = 1.0,
            unitPriceCents = 0L,
            totalCents = 0L,
            position = _state.value.items.size
        )
        _state.value = _state.value.copy(items = _state.value.items + newItem)
        recalculate()
    }

    fun updateItem(index: Int, item: InvoiceItemEntity) {
        val list = _state.value.items.toMutableList()
        if (index in list.indices) {
            list[index] = item
            _state.value = _state.value.copy(items = list)
            recalculate()
        }
    }

    fun removeItem(index: Int) {
        val list = _state.value.items.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _state.value = _state.value.copy(items = list)
            recalculate()
        }
    }

    private fun recalculate() {
        val s = _state.value
        val totals = invoiceRepo.computeTotals(
            items = s.items,
            taxPercent = s.taxPercent,
            discountCents = s.discountCents
        )
        _state.value = s.copy(
            subtotalCents = totals.subtotalCents,
            taxCents = totals.taxCents,
            totalCents = totals.totalCents
        )
    }

    fun save(onDone: (Long) -> Unit) {
        val s = _state.value
        if (s.personId <= 0L) {
            _state.value = s.copy(message = "يجب اختيار عميل", isError = true)
            return
        }
        val validItems = s.items.filter {
            it.description.isNotBlank() && it.totalCents > 0
        }
        if (validItems.isEmpty()) {
            _state.value = s.copy(message = "أضف بنداً واحداً على الأقل", isError = true)
            return
        }

        viewModelScope.launch {
            try {
                val totals = invoiceRepo.computeTotals(
                    validItems, s.taxPercent, s.discountCents
                )

                val invoice = InvoiceEntity(
                    id = if (s.isNew) 0L else s.invoiceId,
                    number = s.number,
                    personId = s.personId,
                    currency = s.currency,
                    status = InvoiceStatus.ISSUED,
                    note = s.note,
                    subtotalCents = totals.subtotalCents,
                    taxPercent = s.taxPercent,
                    taxCents = totals.taxCents,
                    discountCents = s.discountCents,
                    totalCents = totals.totalCents,
                    issuedAt = System.currentTimeMillis()
                )
                val id = invoiceRepo.saveWithItems(invoice, validItems)

                _state.value = s.copy(
                    message = "✅ تم حفظ الفاتورة",
                    isError = false,
                    savedSuccessfully = true,
                    invoiceId = id
                )
                onDone(id)
            } catch (e: Exception) {
                _state.value = s.copy(
                    message = "❌ فشل الحفظ: ${e.message}",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}