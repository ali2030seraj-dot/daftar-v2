package com.daftar.v2.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.db.dao.ExpenseCategoryDao
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.entity.ExpenseCategoryEntity
import com.daftar.v2.data.entity.ExpenseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategoryStat(
    val categoryId: Long?,
    val name: String,
    val totalCents: Long,
    val count: Int,
    val percent: Float
)

data class MonthlyPoint(
    val label: String,
    val totalCents: Long,
    val year: Int,
    val month: Int
)

data class TopExpense(
    val id: Long,
    val statement: String,
    val amountCents: Long,
    val currency: String,
    val categoryName: String?,
    val timestamp: Long
)

data class ExpenseReportState(
    val loading: Boolean = true,
    val year: Int = 0,
    val month: Int = 0,
    val monthLabel: String = "",
    val totalCents: Long = 0L,
    val expenseCount: Int = 0,
    val averageCents: Long = 0L,
    val byCategory: List<CategoryStat> = emptyList(),
    val monthly: List<MonthlyPoint> = emptyList(),
    val topExpenses: List<TopExpense> = emptyList(),
    val prevTotalCents: Long = 0L,
    val canGoNext: Boolean = true
) {
    val changePercent: Int
        get() {
            if (prevTotalCents == 0L) return if (totalCents > 0) 100 else 0
            return (((totalCents - prevTotalCents).toDouble() / prevTotalCents) * 100).toInt()
        }
}

@HiltViewModel
class ExpenseReportViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: ExpenseCategoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseReportState())
    val state: StateFlow<ExpenseReportState> = _state.asStateFlow()

    private var allExpenses: List<ExpenseEntity> = emptyList()
    private var allCategories: List<ExpenseCategoryEntity> = emptyList()

    init {
        val now = Calendar.getInstance()
        loadAll(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
    }

    private fun loadAll(year: Int, month: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                allExpenses = expenseDao.getAll().filter { !it.isVoided }
                allCategories = categoryDao.getAll()
                compute(year, month)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    private fun compute(year: Int, month: Int) {
        val currentStart = startOfMonth(year, month)
        val currentEnd = startOfMonth(year, month + 1)

        val prevYear = if (month == 1) year - 1 else year
        val prevMonth = if (month == 1) 12 else month - 1
        val prevStart = startOfMonth(prevYear, prevMonth)
        val prevEnd = startOfMonth(prevYear, prevMonth + 1)

        val current = allExpenses.filter { it.timestamp in currentStart until currentEnd }
        val previous = allExpenses.filter { it.timestamp in prevStart until prevEnd }

        val total = current.sumOf { it.amountCents }
        val count = current.size
        val average = if (count > 0) total / count else 0L

        // تصنيف
        val categoryMap = allCategories.associateBy { it.id }
        val byCategory = current
            .groupBy { it.categoryId }
            .map { (catId, list) ->
                val catTotal = list.sumOf { it.amountCents }
                CategoryStat(
                    categoryId = catId,
                    name = catId?.let { categoryMap[it]?.name } ?: "بدون تصنيف",
                    totalCents = catTotal,
                    count = list.size,
                    percent = if (total > 0) catTotal.toFloat() / total else 0f
                )
            }
            .sortedByDescending { it.totalCents }

        // آخر 6 أشهر
        val monthly = mutableListOf<MonthlyPoint>()
        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance().apply {
                set(year, month - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MONTH, -i)
            }
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val s = startOfMonth(y, m)
            val e = startOfMonth(y, m + 1)
            val tot = allExpenses
                .filter { it.timestamp in s until e }
                .sumOf { it.amountCents }
            monthly.add(
                MonthlyPoint(
                    label = monthShort(m),
                    totalCents = tot,
                    year = y,
                    month = m
                )
            )
        }

        // أعلى 5
        val top = current
            .sortedByDescending { it.amountCents }
            .take(5)
            .map {
                TopExpense(
                    id = it.id,
                    statement = it.statement,
                    amountCents = it.amountCents,
                    currency = it.currency,
                    categoryName = it.categoryId?.let { cid -> categoryMap[cid]?.name },
                    timestamp = it.timestamp
                )
            }

        val now = Calendar.getInstance()
        val canGoNext = !(year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH) + 1)

        _state.value = ExpenseReportState(
            loading = false,
            year = year,
            month = month,
            monthLabel = monthNameArabic(month) + " $year",
            totalCents = total,
            expenseCount = count,
            averageCents = average,
            byCategory = byCategory,
            monthly = monthly,
            topExpenses = top,
            prevTotalCents = previous.sumOf { it.amountCents },
            canGoNext = canGoNext
        )
    }

    fun previousMonth() {
        val s = _state.value
        val newMonth = if (s.month == 1) 12 else s.month - 1
        val newYear = if (s.month == 1) s.year - 1 else s.year
        compute(newYear, newMonth)
    }

    fun nextMonth() {
        if (!_state.value.canGoNext) return
        val s = _state.value
        val newMonth = if (s.month == 12) 1 else s.month + 1
        val newYear = if (s.month == 12) s.year + 1 else s.year
        compute(newYear, newMonth)
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

    private fun monthShort(month: Int): String = when (month) {
        1 -> "ينا"; 2 -> "فبر"; 3 -> "مار"; 4 -> "أبر"
        5 -> "ماي"; 6 -> "يون"; 7 -> "يول"; 8 -> "أغس"
        9 -> "سبت"; 10 -> "أكت"; 11 -> "نوف"; 12 -> "ديس"
        else -> ""
    }
}