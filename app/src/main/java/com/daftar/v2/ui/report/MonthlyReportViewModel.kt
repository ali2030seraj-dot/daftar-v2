package com.daftar.v2.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.util.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ⭐ إحصائيات عملة واحدة في الشهر
 */
data class CurrencyMonthStats(
    val currencyCode: String,
    val currencyName: String,
    val totalDebtCents: Long,
    val totalPaymentCents: Long,
    val totalExpensesCents: Long,
    val transactionCount: Int,
    val expenseCount: Int
) {
    val netCents: Long
        get() = totalDebtCents - totalPaymentCents - totalExpensesCents
}

data class MonthlyReportState(
    val loading: Boolean = true,
    val year: Int = 0,
    val month: Int = 0,
    val monthLabel: String = "",
    val hijriLabel: String = "",

    // ⭐ كل عملة في بطاقة منفصلة
    val currencies: List<CurrencyMonthStats> = emptyList(),

    // إجمالي عدد العمليات (بدون دمج العملات)
    val totalTransactionCount: Int = 0,
    val totalExpenseCount: Int = 0,

    val canGoNext: Boolean = true
)

@HiltViewModel
class MonthlyReportViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val expenseDao: ExpenseDao,
    private val personDao: PersonDao
) : ViewModel() {

    private val _state = MutableStateFlow(MonthlyReportState())
    val state: StateFlow<MonthlyReportState> = _state.asStateFlow()

    private var allTransactions = emptyList<TransactionEntity>()
    private var allExpenses = emptyList<com.daftar.v2.data.entity.ExpenseEntity>()
    private var allPeople = emptyList<PersonEntity>()

    private var currentYear: Int = 0
    private var currentMonth: Int = 0

    init {
        val now = Calendar.getInstance()
        currentYear = now.get(Calendar.YEAR)
        currentMonth = now.get(Calendar.MONTH) + 1
        loadAll(currentYear, currentMonth)
    }

    private fun loadAll(year: Int, month: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                allTransactions = transactionDao.getAll().filter { !it.isVoided }
                allExpenses = expenseDao.getAll().filter { !it.isVoided }
                allPeople = personDao.getAll()
                currentYear = year
                currentMonth = month
                compute(year, month)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    private fun compute(year: Int, month: Int) {
        val currentStart = startOfMonth(year, month)
        val currentEnd = endOfMonth(year, month)

        // ⭐ فلترة العمليات حسب الوقت فقط (كل العملات)
        val currentTx = allTransactions.filter {
            it.timestamp in currentStart until currentEnd
        }
        val currentExpenses = allExpenses.filter {
            it.timestamp in currentStart until currentEnd
        }

        // ⭐ الأشخاص ← خريطة: personId → currency code
        val personCurrencyMap = allPeople.associate {
            it.id to CurrencyUtils.toCode(it.currency)
        }

        // ⭐ تجميع حسب العملة
        val perCurrency = mutableMapOf<String, CurrencyMonthStats>()

        // 1) عمليات الديون والسداد
        currentTx.forEach { tx ->
            val code = personCurrencyMap[tx.personId] ?: CurrencyUtils.DEFAULT_CODE
            val existing = perCurrency[code] ?: CurrencyMonthStats(
                currencyCode = code,
                currencyName = CurrencyUtils.arabicFullName(code),
                totalDebtCents = 0L,
                totalPaymentCents = 0L,
                totalExpensesCents = 0L,
                transactionCount = 0,
                expenseCount = 0
            )

            val newDebt = if (tx.type == TransactionType.DEBT)
                existing.totalDebtCents + tx.amountCents
            else existing.totalDebtCents

            val newPayment = if (tx.type == TransactionType.PAYMENT)
                existing.totalPaymentCents + tx.amountCents
            else existing.totalPaymentCents

            perCurrency[code] = existing.copy(
                totalDebtCents = newDebt,
                totalPaymentCents = newPayment,
                transactionCount = existing.transactionCount + 1
            )
        }

        // 2) المصروفات
        currentExpenses.forEach { expense ->
            val code = CurrencyUtils.toCode(expense.currency)
            val existing = perCurrency[code] ?: CurrencyMonthStats(
                currencyCode = code,
                currencyName = CurrencyUtils.arabicFullName(code),
                totalDebtCents = 0L,
                totalPaymentCents = 0L,
                totalExpensesCents = 0L,
                transactionCount = 0,
                expenseCount = 0
            )
            perCurrency[code] = existing.copy(
                totalExpensesCents = existing.totalExpensesCents + expense.amountCents,
                expenseCount = existing.expenseCount + 1
            )
        }

        // ⭐ ترتيب: العملات اللي فيها حركة أولاً (بترتيب أبجدي ثابت)
        val currencies = perCurrency.values
            .filter {
                it.totalDebtCents != 0L ||
                it.totalPaymentCents != 0L ||
                it.totalExpensesCents != 0L
            }
            .sortedBy { CurrencyUtils.ALL_CODES.indexOf(it.currencyCode) }

        val now = Calendar.getInstance()
        val canGoNext = !(year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH) + 1)

        _state.value = MonthlyReportState(
            loading = false,
            year = year,
            month = month,
            monthLabel = monthNameArabic(month) + " $year",
            hijriLabel = com.daftar.v2.util.HijriDateUtils.hijriMonthName(year, month),
            currencies = currencies,
            totalTransactionCount = currentTx.size,
            totalExpenseCount = currentExpenses.size,
            canGoNext = canGoNext
        )
    }

    fun previousMonth() {
        val s = _state.value
        val newMonth = if (s.month == 1) 12 else s.month - 1
        val newYear = if (s.month == 1) s.year - 1 else s.year
        currentYear = newYear
        currentMonth = newMonth
        compute(newYear, newMonth)
    }

    fun nextMonth() {
        if (!_state.value.canGoNext) return
        val s = _state.value
        val newMonth = if (s.month == 12) 1 else s.month + 1
        val newYear = if (s.month == 12) s.year + 1 else s.year
        currentYear = newYear
        currentMonth = newMonth
        compute(newYear, newMonth)
    }

    private fun startOfMonth(year: Int, month: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun endOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MONTH, 1)
        return cal.timeInMillis
    }

    private fun monthNameArabic(month: Int): String = when (month) {
        1 -> "يناير"; 2 -> "فبراير"; 3 -> "مارس"; 4 -> "أبريل"
        5 -> "مايو"; 6 -> "يونيو"; 7 -> "يوليو"; 8 -> "أغسطس"
        9 -> "سبتمبر"; 10 -> "أكتوبر"; 11 -> "نوفمبر"; 12 -> "ديسمبر"
        else -> ""
    }
}