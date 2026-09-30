package com.daftar.v2.ui.util

import androidx.compose.ui.graphics.Color
import com.daftar.v2.ui.theme.SuccessGreen
import com.daftar.v2.ui.theme.TertiaryAmber

/**
 * حالة الرصيد المُبسّطة.
 */
enum class BalanceState(
    val label: String,
    val emoji: String,
    val color: Color
) {
    SETTLED("مسدَّد", "✓", SuccessGreen),
    ACTIVE("قيد التسوية", "●", TertiaryAmber);

    companion object {
        fun of(balanceCents: Long): BalanceState {
            return if (balanceCents == 0L) SETTLED else ACTIVE
        }
    }
}