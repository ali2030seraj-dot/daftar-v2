package com.daftar.v2.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.ExpenseEntity
import com.daftar.v2.data.entity.PersonEntity
import com.daftar.v2.data.entity.PersonType
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import com.daftar.v2.data.repository.SettingsRepository
import com.daftar.v2.util.CurrencyUtils
import com.daftar.v2.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class RecentActivity(
    val id: Long,
    val title: String,
    val subtitle: String,
    val amountCents: Long,
    val isIncoming: Boolean,
    val timestamp: Long
)

data class DashboardStats(
    val netBalanceCents: Long,
    val totalReceivableCents: Long,
    val totalPayableCents: Long,
    val currentMonthExpensesCents: Long,
    val totalExpensesCents: Long = 0L
)

data class DashboardUiState(
    val loading: Boolean = true,
    val currency: String = CurrencyUtils.DEFAULT_CODE,
    val stats: DashboardStats? = null,
    val recentActivities: List<RecentActivity> = emptyList(),
    val dailyExpenses: List<Long> = List(7) { 0L }
)

private data class DashboardData(
    val people: List<PersonEntity>,
    val transactions: List<TransactionEntity>,
    val expenses: List<ExpenseEntity>,
    val currencyCode: String
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val personDao: PersonDao,
    private val transactionDao: TransactionDao,
    private val expenseDao: ExpenseDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    private val recentLimit = 5

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                personDao.observeActive(),
                transactionDao.observeAllTransactions(),
                expenseDao.observeAll(),
                settingsRepository.observe(SettingsRepository.KEY_DEFAULT_CURRENCY)
            ) { people, transactions, expenses, rawCurrency ->
                DashboardData(
                    people = people,
                    transactions = transactions,
                    expenses = expenses,
                    currencyCode = CurrencyUtils.toCode(rawCurrency)
                )
            }.collect { data ->

                // ⭐ فلترة الأشخاص حسب العملة (مع تطبيع)
                val matchingPeople = data.people.filter {
                    CurrencyUtils.toCode(it.currency) == data.currencyCode
                }

                // ⭐ فلترة المصروفات حسب العملة
                val matchingExpenses = data.expenses.filter {
                    !it.isVoided && CurrencyUtils.toCode(it.currency) == data.currencyCode
                }

                val stats = calculateStats(
                    matchingPeople = matchingPeople,
                    allTransactions = data.transactions,
                    matchingExpenses = matchingExpenses
                )

                // ⭐ آخر العمليات — بدون فلترة عملة (آخر 5 من الكل)
                val recent = data.transactions
                    .asSequence()
                    .filter { !it.isVoided }
                    .sortedByDescending { it.timestamp }
                    .take(recentLimit)
                    .map { toRecentActivity(it, data.people) }
                    .toList()

                val daily = calculateDailyExpenses(matchingExpenses)

                Log.d(
                    "DashboardDebug",
                    "code=${data.currencyCode}, " +
                    "matchingPeople=${matchingPeople.size}, " +
                    "matchingExpenses=${matchingExpenses.size}, " +
                    "tx=${data.transactions.size}, " +
                    "exp_total=${stats.totalExpensesCents}, " +
                    "net=${stats.netBalanceCents}"
                )

                _state.value = _state.value.copy(
                    loading = false,
                    currency = data.currencyCode,
                    stats = stats,
                    recentActivities = recent,
                    dailyExpenses = daily
                )
            }
        }
    }

    private fun calculateStats(
        matchingPeople: List<PersonEntity>,
        allTransactions: List<TransactionEntity>,
        matchingExpenses: List<ExpenseEntity>
    ): DashboardStats {
        var totalReceivable = 0L
        var totalPayable = 0L

        matchingPeople.forEach { person ->
            val personTx = allTransactions.filter {
                it.personId == person.id && !it.isVoided
            }
            val debt = personTx
                .filter { it.type == TransactionType.DEBT }
                .sumOf { it.amountCents }
            val payment = personTx
                .filter { it.type == TransactionType.PAYMENT }
                .sumOf { it.amountCents }
            val balance = debt - payment

            when (person.type) {
                PersonType.CREDITOR -> totalReceivable += balance
                PersonType.DEBTOR -> totalPayable += balance
                else -> Unit
            }
        }

        val totalExpenses = matchingExpenses.sumOf { it.amountCents }

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val currentMonthExpenses = matchingExpenses
            .filter { it.timestamp >= startOfMonth }
            .sumOf { it.amountCents }

        return DashboardStats(
            netBalanceCents = totalReceivable - totalPayable - totalExpenses,
            totalReceivableCents = totalReceivable,
            totalPayableCents = totalPayable,
            currentMonthExpensesCents = currentMonthExpenses,
            totalExpensesCents = totalExpenses
        )
    }

    private fun calculateDailyExpenses(expenses: List<ExpenseEntity>): List<Long> {
        val result = mutableListOf<Long>()
        val baseCalendar = Calendar.getInstance()

        for (dayOffset in 6 downTo 0) {
            val cal = (baseCalendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, -dayOffset)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + 86_400_000L
            val dayTotal = expenses
                .filter { it.timestamp in dayStart until dayEnd }
                .sumOf { it.amountCents }
            result.add(dayTotal)
        }
        return result
    }

    private fun toRecentActivity(
        tx: TransactionEntity,
        people: List<PersonEntity>
    ): RecentActivity {
        val person = people.find { it.id == tx.personId }
        val isIncoming = tx.type == TransactionType.DEBT
        return RecentActivity(
            id = tx.id,
            title = person?.name ?: tx.statement.ifBlank { "بدون بيان" },
            subtitle = tx.statement.ifBlank {
                tx.note.ifBlank { DateFormatter.date(tx.timestamp) }
            },
            amountCents = if (isIncoming) tx.amountCents else -tx.amountCents,
            isIncoming = isIncoming,
            timestamp = tx.timestamp
        )
    }

    fun setCurrency(currencyCode: String) {
        viewModelScope.launch {
            settingsRepository.put(
                SettingsRepository.KEY_DEFAULT_CURRENCY,
                CurrencyUtils.toCode(currencyCode)
            )
        }
    }
}