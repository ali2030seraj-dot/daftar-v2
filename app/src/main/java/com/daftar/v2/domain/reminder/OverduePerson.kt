package com.daftar.v2.domain.reminder

/**
 * ⭐ يمثل شخصاً متأخراً في السداد.
 */
data class OverduePerson(
    val id: Long,
    val name: String,
    val balanceCents: Long,
    val currency: String,
    val daysOverdue: Long
)