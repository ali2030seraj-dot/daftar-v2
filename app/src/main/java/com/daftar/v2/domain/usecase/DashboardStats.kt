package com.daftar.v2.domain.usecase

data class DashboardStats(
    val currency: String,
    val totalReceivableCents: Long,
    val totalPayableCents: Long,
    val currentMonthExpensesCents: Long,
    val netBalanceCents: Long
)