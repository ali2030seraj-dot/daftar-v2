package com.daftar.v2.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.db.dao.ExpenseCategoryDao
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.entity.ExpenseCategoryEntity
import com.daftar.v2.data.entity.ExpenseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

data class MonthBar(
    val label: String,
    val amountCents: Long
)

data class CategorySlice(
    val name: String,
    val amountCents: Long,
    val colorArgb: Long
)

data class ChartsState(
    val loading: Boolean = true,
    val monthlyBars: List<MonthBar> = emptyList(),
    val categorySlices: List<CategorySlice> = emptyList(),
    val totalCurrentMonthCents: Long = 0L,
    val currentMonthLabel: String = "",
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: ExpenseCategoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(ChartsState())
    val state: StateFlow<ChartsState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                val expenses = expenseDao.getAll().filter { !it.isVoided }
                val categories = categoryDao.getAll()

                val monthlyBars = withContext(Dispatchers.Default) {
                    buildMonthlyBars(expenses)
                }
                val slicesResult = withContext(Dispatchers.Default) {
                    buildCategorySlices(expenses, categories)
                }

                val cal = Calendar.getInstance()
                val monthLabel = "${monthNameArabic(cal.get(Calendar.MONTH) + 1)} ${cal.get(Calendar.YEAR)}"

                _state.value = ChartsState(
                    loading = false,
                    monthlyBars = monthlyBars,
                    categorySlices = slicesResult.first,
                    totalCurrentMonthCents = slicesResult.second,
                    currentMonthLabel = monthLabel
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = "خطأ: ${e.message ?: "غير معروف"}",
                    isError = true
                )
            }
        }
    }

    private fun buildMonthlyBars(expenses: List<ExpenseEntity>): List<MonthBar> {
        val cal = Calendar.getInstance()
        val result = mutableListOf<MonthBar>()
        for (i in 5 downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1

            val start = startOfMonth(year, month)
            val end = startOfNextMonth(year, month)

            val amount = expenses
                .filter { it.timestamp in start until end }
                .sumOf { it.amountCents }

            result.add(MonthBar(label = shortMonthName(month), amountCents = amount))
        }
        return result
    }

    private fun buildCategorySlices(
        expenses: List<ExpenseEntity>,
        categories: List<ExpenseCategoryEntity>
    ): Pair<List<CategorySlice>, Long> {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        val start = startOfMonth(year, month)
        val end = startOfNextMonth(year, month)

        val currentMonthExpenses = expenses.filter { it.timestamp in start until end }
        val grouped = currentMonthExpenses.groupBy { it.categoryId }

        val palette = listOf(
            0xFF2563EB, 0xFF16A34A, 0xFFD97706, 0xFFDC2626, 0xFF7C3AED,
            0xFF0891B2, 0xFFCA8A04, 0xFFDB2777, 0xFF059669, 0xFF6B7280
        )

        var colorIdx = 0
        val slices = mutableListOf<CategorySlice>()
        var total = 0L

        grouped.forEach { (categoryId, list) ->
            val amount = list.sumOf { it.amountCents }
            val name = categories.firstOrNull { it.id == categoryId }?.name ?: "بدون تصنيف"
            slices.add(
                CategorySlice(
                    name = name,
                    amountCents = amount,
                    colorArgb = palette[colorIdx % palette.size]
                )
            )
            total += amount
            colorIdx++
        }

        return slices.sortedByDescending { it.amountCents } to total
    }

    private fun startOfMonth(year: Int, month: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun startOfNextMonth(year: Int, month: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
        }.timeInMillis

    private fun monthNameArabic(month: Int): String = when (month) {
        1 -> "يناير"; 2 -> "فبراير"; 3 -> "مارس"; 4 -> "أبريل"
        5 -> "مايو"; 6 -> "يونيو"; 7 -> "يوليو"; 8 -> "أغسطس"
        9 -> "سبتمبر"; 10 -> "أكتوبر"; 11 -> "نوفمبر"; 12 -> "ديسمبر"
        else -> ""
    }

    private fun shortMonthName(month: Int): String = when (month) {
        1 -> "ينا"; 2 -> "فبر"; 3 -> "مار"; 4 -> "أبر"
        5 -> "ماي"; 6 -> "يون"; 7 -> "يول"; 8 -> "أغس"
        9 -> "سبت"; 10 -> "أكت"; 11 -> "نوف"; 12 -> "ديس"
        else -> ""
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}