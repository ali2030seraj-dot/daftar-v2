package com.daftar.v2.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.util.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrencySummary(
    val currency: String,
    val forYouCents: Long,
    val onYouCents: Long,
    val expensesCents: Long,
    val peopleCount: Int
) {
    // ⭐ الصافي = ما لك − ما عليك − المصروفات
    val netCents: Long get() = forYouCents - onYouCents - expensesCents

    // ⭐ هل هذه العملة تحتوي بيانات؟
    val hasData: Boolean get() =
        forYouCents != 0L || onYouCents != 0L || expensesCents != 0L || peopleCount > 0

    // ⭐ متوافق مع الكود القديم
    val isActive: Boolean get() = hasData

    val arabicName: String get() = CurrencyUtils.arabicFullName(currency)
}

data class CurrencySummaryState(
    val loading: Boolean = true,
    val summaries: List<CurrencySummary> = emptyList(),
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class CurrencySummaryViewModel @Inject constructor(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencySummaryState())
    val state: StateFlow<CurrencySummaryState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, message = null)
            try {
                val people = personDao.getAll().filter { !it.isArchived }
                val allTxs = transactionDao.getAll().filter { !it.isVoided }
                val allExpenses = expenseDao.getAllActive()

                // ⭐⭐⭐ نبني قائمة كاملة بكل العملات المدعومة
                val summaries = CurrencyUtils.ALL_CODES.map { currencyCode ->

                    // ─── من الأشخاص ───
                    val personList = people.filter {
                        CurrencyUtils.toCode(it.currency) == currencyCode
                    }

                    var forYou = 0L
                    var onYou = 0L

                    personList.forEach { person ->
                        val personTxs = allTxs.filter { it.personId == person.id }
                        val balance = personTxs.sumOf { tx ->
                            if (tx.type == TransactionType.DEBT) tx.amountCents
                            else -tx.amountCents
                        }

                        when (person.type) {
                            PersonType.CREDITOR -> forYou += balance
                            PersonType.DEBTOR -> onYou += balance
                        }
                    }

                    // ─── المصروفات ───
                    val expenses = allExpenses
                        .filter { CurrencyUtils.toCode(it.currency) == currencyCode }
                        .sumOf { it.amountCents }

                    CurrencySummary(
                        currency = currencyCode,
                        forYouCents = forYou,
                        onYouCents = onYou,
                        expensesCents = expenses,
                        peopleCount = personList.size
                    )
                }
                    // ⭐⭐⭐ الترتيب:
                    //    1) الأكثر استخداماً أولاً (hasData = true)
                    //    2) ثم الأقل عدد أشخاص
                    //    3) ثم الأقل رصيد
                    .sortedWith(
                        compareByDescending<CurrencySummary> { it.hasData }
                            .thenByDescending { it.peopleCount }
                            .thenByDescending { kotlin.math.abs(it.netCents) }
                    )

                _state.value = _state.value.copy(
                    loading = false,
                    summaries = summaries,
                    message = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = e.message ?: "خطأ في تحميل البيانات",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}