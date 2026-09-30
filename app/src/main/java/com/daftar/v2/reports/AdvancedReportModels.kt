package com.daftar.v2.reports

import com.daftar.v2.data.entity.PersonEntity

data class AgingReport(
    val buckets: List<AgingBucket>,
    val totalCents: Long,
    val currency: String
)

data class AgingBucket(
    val label: String,
    val minDays: Int,
    val maxDays: Int,
    val totalCents: Long,
    val count: Int,
    val color: Long
)

data class CashFlowReport(
    val months: List<CashFlowMonth>,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long,
    val netCents: Long,
    val currency: String
)

data class CashFlowMonth(
    val year: Int,
    val month: Int,
    val label: String,
    val incomeCents: Long,
    val expenseCents: Long,
    val netCents: Long
)

data class ComparisonReport(
    val currentMonth: CashFlowMonth,
    val previousMonth: CashFlowMonth,
    val incomeChangePercent: Double,
    val expenseChangePercent: Double,
    val netChangePercent: Double
)

data class OverduePerson(
    val person: PersonEntity,
    val balanceCents: Long,
    val daysSinceLastPayment: Int,
    val lastPaymentDate: Long
)