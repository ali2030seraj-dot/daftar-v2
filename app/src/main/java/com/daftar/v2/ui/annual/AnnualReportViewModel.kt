package com.daftar.v2.ui.annual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.util.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

data class MonthSummary(
    val monthIndex: Int,
    val monthName: String,
    val debtsCents: Long,
    val paymentsCents: Long,
    val expensesCents: Long
)

/**
 * ⭐ إحصائيات عملة واحدة في السنة
 */
data class CurrencyYearStats(
    val currencyCode: String,
    val currencyName: String,
    val totalDebtsCents: Long,
    val totalPaymentsCents: Long,
    val totalExpensesCents: Long,
    val months: List<MonthSummary>
) {
    val netCents: Long
        get() = totalDebtsCents - totalPaymentsCents - totalExpensesCents
}

data class AnnualReportState(
    val loading: Boolean = true,
    val year: Int = 0,

    // ⭐ كل عملة في بطاقة منفصلة
    val currencies: List<CurrencyYearStats> = emptyList(),

    val activePersonsCount: Int = 0,
    val archivedPersonsCount: Int = 0,
    val canGoNext: Boolean = true,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class AnnualReportViewModel @Inject constructor(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    private val _state = MutableStateFlow(AnnualReportState())
    val state: StateFlow<AnnualReportState> = _state.asStateFlow()

    private var allPeople = emptyList<PersonEntity>()
    private var allTransactions = emptyList<TransactionEntity>()
    private var allExpenses = emptyList<ExpenseEntity>()
    private var currentYear: Int = 0

    init {
        val now = Calendar.getInstance().get(Calendar.YEAR)
        currentYear = now
        load(now)
    }

    private fun load(year: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                allPeople = personDao.getAll()
                allTransactions = transactionDao.getAll().filter { !it.isVoided }
                allExpenses = expenseDao.getAll().filter { !it.isVoided }

                val result = withContext(Dispatchers.Default) {
                    computeReport(year)
                }
                _state.value = result
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = "خطأ: ${e.message ?: "غير معروف"}",
                    isError = true
                )
            }
        }
    }

    private fun computeReport(year: Int): AnnualReportState {
        val personCurrencyMap = allPeople.associate {
            it.id to CurrencyUtils.toCode(it.currency)
        }

        // ⭐ أولاً: اكتشف كل العملات المستخدمة
        val usedCurrencies = mutableSetOf<String>()

        allTransactions.forEach { tx ->
            personCurrencyMap[tx.personId]?.let { usedCurrencies.add(it) }
        }
        allExpenses.forEach { exp ->
            usedCurrencies.add(CurrencyUtils.toCode(exp.currency))
        }

        // ⭐ ثانياً: لكل عملة — احسب إحصائياتها
        val currencyStatsList = usedCurrencies
            .sortedBy { CurrencyUtils.ALL_CODES.indexOf(it) }
            .map { code ->
                val matchingPersonIds = allPeople
                    .filter { CurrencyUtils.toCode(it.currency) == code }
                    .map { it.id }
                    .toSet()

                val filteredTx = allTransactions.filter { it.personId in matchingPersonIds }
                val filteredExpenses = allExpenses.filter {
                    CurrencyUtils.toCode(it.currency) == code
                }

                val months = mutableListOf<MonthSummary>()
                var totalDebts = 0L
                var totalPayments = 0L
                var totalExpenses = 0L

                for (m in 1..12) {
                    val start = startOfMonth(year, m)
                    val end = startOfMonth(year, m + 1)

                    val debts = filteredTx
                        .filter { it.type == TransactionType.DEBT && it.timestamp in start until end }
                        .sumOf { it.amountCents }
                    val payments = filteredTx
                        .filter { it.type == TransactionType.PAYMENT && it.timestamp in start until end }
                        .sumOf { it.amountCents }
                    val expenses = filteredExpenses
                        .filter { it.timestamp in start until end }
                        .sumOf { it.amountCents }

                    months.add(
                        MonthSummary(
                            monthIndex = m,
                            monthName = monthNameArabic(m),
                            debtsCents = debts,
                            paymentsCents = payments,
                            expensesCents = expenses
                        )
                    )

                    totalDebts += debts
                    totalPayments += payments
                    totalExpenses += expenses
                }

                CurrencyYearStats(
                    currencyCode = code,
                    currencyName = CurrencyUtils.arabicFullName(code),
                    totalDebtsCents = totalDebts,
                    totalPaymentsCents = totalPayments,
                    totalExpensesCents = totalExpenses,
                    months = months
                )
            }
            // نحتفظ فقط بالعملات اللي فيها حركة فعلية
            .filter {
                it.totalDebtsCents != 0L ||
                it.totalPaymentsCents != 0L ||
                it.totalExpensesCents != 0L
            }

        val activePersons = allPeople.count { !it.isArchived }
        val archivedPersons = allPeople.count { it.isArchived }

        val cy = Calendar.getInstance().get(Calendar.YEAR)
        val canGoNext = year < cy

        return AnnualReportState(
            loading = false,
            year = year,
            currencies = currencyStatsList,
            activePersonsCount = activePersons,
            archivedPersonsCount = archivedPersons,
            canGoNext = canGoNext
        )
    }

    fun previousYear() {
        currentYear = _state.value.year - 1
        load(currentYear)
    }

    fun nextYear() {
        if (!_state.value.canGoNext) return
        currentYear = _state.value.year + 1
        load(currentYear)
    }

    private fun startOfMonth(year: Int, month: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun monthNameArabic(month: Int): String = when (month) {
        1 -> "يناير"; 2 -> "فبراير"; 3 -> "مارس"; 4 -> "أبريل"
        5 -> "مايو"; 6 -> "يونيو"; 7 -> "يوليو"; 8 -> "أغسطس"
        9 -> "سبتمبر"; 10 -> "أكتوبر"; 11 -> "نوفمبر"; 12 -> "ديسمبر"
        else -> ""
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}