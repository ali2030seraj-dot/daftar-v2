package com.daftar.v2.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.PersonType as EntityPersonType
import com.daftar.v2.data.repository.CreatePersonResult
import com.daftar.v2.data.repository.PersonRepository
import com.daftar.v2.data.repository.SettingsRepository
import com.daftar.v2.data.repository.TransactionRepository
import com.daftar.v2.util.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PersonWithBalance(
    val person: PersonEntity,
    val balanceCents: Long,
    val hasSameName: Boolean = false
)

data class PeopleUiState(
    val loading: Boolean = true,
    val searchQuery: String = "",
    val allPeople: List<PersonWithBalance> = emptyList(),   // ⭐ القائمة الكاملة
    val people: List<PersonWithBalance> = emptyList(),      // ⭐ المُفلترة (للعرض)
    val duplicatesForDialog: List<PersonEntity> = emptyList(),
    val defaultCurrency: String = CurrencyUtils.DEFAULT_CODE,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PeopleUiState())
    val state: StateFlow<PeopleUiState> = _state.asStateFlow()

    private var currentType: EntityPersonType = EntityPersonType.CREDITOR

    init {
        viewModelScope.launch {
            settingsRepository.observe(SettingsRepository.KEY_DEFAULT_CURRENCY)
                .collect { raw ->
                    _state.value = _state.value.copy(
                        defaultCurrency = CurrencyUtils.toCode(raw)
                    )
                }
        }
    }

    fun init(type: EntityPersonType) {
        currentType = type
        observe()
    }

    /**
     * ⭐ البحث — يعمل فوراً على القائمة المخزّنة
     */
    fun setSearchQuery(query: String) {
        val filtered = applySearch(_state.value.allPeople, query)
        _state.value = _state.value.copy(
            searchQuery = query,
            people = filtered
        )
    }

    /**
     * ⭐ دالة فلترة موحّدة
     */
    private fun applySearch(
        list: List<PersonWithBalance>,
        query: String
    ): List<PersonWithBalance> {
        if (query.isBlank()) return list
        val q = query.trim().lowercase()
        return list.filter { item ->
            item.person.name.lowercase().contains(q) ||
            item.person.phone.contains(q) ||
            item.person.note.lowercase().contains(q)
        }
    }

    private fun observe() {
        viewModelScope.launch(Dispatchers.IO) {
            val flow = when (currentType) {
                EntityPersonType.CREDITOR -> personRepository.observeCreditors()
                EntityPersonType.DEBTOR -> personRepository.observeDebtors()
            }
            flow.collect { people ->
                val ids = people.map { it.id }
                val balances = transactionRepository.balancesForPersons(ids)
                    .associate { it.personId to it.balance }

                val nameCounts = people.groupingBy { it.name.trim().lowercase() }.eachCount()
                val withBalances = people.map { person ->
                    PersonWithBalance(
                        person = person,
                        balanceCents = balances[person.id] ?: 0L,
                        hasSameName = (nameCounts[person.name.trim().lowercase()] ?: 0) > 1
                    )
                }

                // ⭐ نطبّق البحث الحالي على القائمة الجديدة
                val currentQuery = _state.value.searchQuery
                val filtered = applySearch(withBalances, currentQuery)

                _state.value = _state.value.copy(
                    loading = false,
                    allPeople = withBalances,
                    people = filtered
                )
            }
        }
    }

    fun checkDuplicates(name: String, currency: String) {
        if (name.length < 2) {
            _state.value = _state.value.copy(duplicatesForDialog = emptyList())
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dups = personRepository.findDuplicates(name, currentType, currency)
            _state.value = _state.value.copy(duplicatesForDialog = dups)
        }
    }

    fun createPerson(
        name: String,
        phone: String,
        note: String,
        @Suppress("UNUSED_PARAMETER") currency: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val savedCurrency = _state.value.defaultCurrency

            val result = withContext(Dispatchers.IO) {
                personRepository.createWithValidation(
                    name = name,
                    phone = phone,
                    note = note,
                    type = currentType,
                    currency = savedCurrency
                )
            }
            when (result) {
                is CreatePersonResult.Success -> {
                    _state.value = _state.value.copy(
                        message = "تم إنشاء الحساب",
                        isError = false,
                        duplicatesForDialog = emptyList()
                    )
                    onSuccess()
                }
                is CreatePersonResult.Rejected -> {
                    _state.value = _state.value.copy(
                        message = result.reason,
                        isError = true
                    )
                }
                is CreatePersonResult.Error -> {
                    _state.value = _state.value.copy(
                        message = result.message,
                        isError = true
                    )
                }
            }
        }
    }

    fun clearDuplicatesForDialog() {
        _state.value = _state.value.copy(duplicatesForDialog = emptyList())
    }

    fun updatePerson(id: Long, name: String, phone: String, note: String) {
        viewModelScope.launch {
            try {
                val existing = withContext(Dispatchers.IO) {
                    personRepository.getById(id)
                } ?: return@launch
                withContext(Dispatchers.IO) {
                    personRepository.update(
                        existing.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            note = note.trim()
                        )
                    )
                }
                _state.value = _state.value.copy(
                    message = "تم التعديل",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "فشل التعديل",
                    isError = true
                )
            }
        }
    }

    fun archive(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { personRepository.archive(id) }
                _state.value = _state.value.copy(
                    message = "تمت الأرشفة",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "فشل الأرشفة",
                    isError = true
                )
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { personRepository.hardDelete(id) }
                _state.value = _state.value.copy(
                    message = "تم الحذف",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "فشل الحذف",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}