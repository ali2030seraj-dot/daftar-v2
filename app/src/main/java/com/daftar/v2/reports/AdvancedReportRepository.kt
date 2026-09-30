package com.daftar.v2.reports

import com.daftar.v2.data.db.dao.ExpenseDao
import com.daftar.v2.data.db.dao.PersonDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdvancedReportRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val personDao: PersonDao,
    private val expenseDao: ExpenseDao
) {

    suspend fun buildAgingReport(currency: String): AgingReport =
        withContext(Dispatchers.IO) {
            val persons = personDao.getAll()
                .filter { !it.isArchived && it.currency == currency }

            if (persons.isEmpty()) {
                return@withContext AgingReport(
                    buckets = emptyBuckets(),
                    totalCents = 0L,
                    currency = currency
                )
            }

            val balances = transactionDao.balancesForPersons(persons.map { it.id })
                .associate { it.personId to it.balance }

            val buckets = emptyBuckets().toMutableList()
            val allTx = transactionDao.getAll().filter { !it.isVoided }
            val now = System.currentTimeMillis()

            for (person in persons) {
                val balance = balances[person.id] ?: 0L
                if (balance <= 0) continue

                val lastTx = allTx
                    .filter { it.personId == person.id }
                    .maxByOrNull { it.timestamp }
                    ?: continue

                val daysSince = TimeUnit.MILLISECONDS
                    .toDays(now - lastTx.timestamp)
                    .toInt()

                val index = buckets.indexOfFirst {
                    daysSince in it.minDays..it.maxDays
                }
                if (index >= 0) {
                    val b = buckets[index]
                    buckets[index] = b.copy(
                        totalCents = b.totalCents + balance,
                        count = b.count + 1
                    )
                }
            }

            AgingReport(
                buckets = buckets,
                totalCents = buckets.sumOf { it.totalCents },
                currency = currency
            )
        }

    suspend fun buildCashFlowReport(
        currency: String,
        monthsBack: Int = 6
    ): CashFlowReport = withContext(Dispatchers.IO) {
        val allTx = transactionDao.getAll().filter { !it.isVoided }
        val allExpenses = expenseDao.getAllActive()
            .filter { it.currency == currency }

        val months = mutableListOf<CashFlowMonth>()

        for (i in monthsBack - 1 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)

            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1

            val (start, end) = monthRange(year, month)

            val income = allTx
                .filter { it.type == TransactionType.PAYMENT }
                .filter { it.timestamp in start until end }
                .sumOf { it.amountCents }

            val expenses = allExpenses
                .filter { it.timestamp in start until end }
                .sumOf { it.amountCents }

            months.add(
                CashFlowMonth(
                    year = year,
                    month = month,
                    label = monthLabel(year, month),
                    incomeCents = income,
                    expenseCents = expenses,
                    netCents = income - expenses
                )
            )
        }

        CashFlowReport(
            months = months,
            totalIncomeCents = months.sumOf { it.incomeCents },
            totalExpenseCents = months.sumOf { it.expenseCents },
            netCents = months.sumOf { it.netCents },
            currency = currency
        )
    }

    suspend fun buildComparisonReport(currency: String): ComparisonReport =
        withContext(Dispatchers.IO) {
            val cashFlow = buildCashFlowReport(currency, monthsBack = 2)
            val current = cashFlow.months.getOrNull(1) ?: emptyMonth()
            val previous = cashFlow.months.getOrNull(0) ?: emptyMonth()

            ComparisonReport(
                currentMonth = current,
                previousMonth = previous,
                incomeChangePercent = percentChange(previous.incomeCents, current.incomeCents),
                expenseChangePercent = percentChange(previous.expenseCents, current.expenseCents),
                netChangePercent = percentChange(previous.netCents, current.netCents)
            )
        }

    private fun emptyBuckets(): List<AgingBucket> = listOf(
        AgingBucket("0-30 يوم", 0, 30, 0L, 0, 0xFF43A047),
        AgingBucket("31-60 يوم", 31, 60, 0L, 0, 0xFFFFA726),
        AgingBucket("61-90 يوم", 61, 90, 0L, 0, 0xFFFB8C00),
        AgingBucket("+90 يوم", 91, Int.MAX_VALUE, 0L, 0, 0xFFE53935)
    )

    private fun percentChange(old: Long, new: Long): Double {
        if (old == 0L) return if (new == 0L) 0.0 else 100.0
        return ((new - old).toDouble() / old.toDouble()) * 100.0
    }

    private fun emptyMonth(): CashFlowMonth {
        val cal = Calendar.getInstance()
        return CashFlowMonth(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            label = "",
            incomeCents = 0L,
            expenseCents = 0L,
            netCents = 0L
        )
    }

    private fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = startCal.clone() as Calendar
        endCal.add(Calendar.MONTH, 1)
        return startCal.timeInMillis to endCal.timeInMillis
    }

    private fun monthLabel(year: Int, month: Int): String {
        val names = listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        return "${names.getOrNull(month - 1) ?: month} $year"
    }
}