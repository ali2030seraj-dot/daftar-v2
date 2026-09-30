package com.daftar.v2.ui.zakat

import androidx.lifecycle.ViewModel
import com.daftar.v2.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * نصاب الذهب: 85 جرام.
 * نصاب الفضة: 595 جرام.
 */
data class ZakatUiState(
    val cashInput: String = "",
    val goldGramsInput: String = "",
    val goldPriceInput: String = "",     // سعر الجرام
    val silverGramsInput: String = "",
    val silverPriceInput: String = "",
    val debtsOwedToYouInput: String = "", // ديون لك عند الناس
    val debtsYouOweInput: String = "",    // ديون عليك
    val currency: String = "ريال يمني",

    // النتائج
    val totalAssetsCents: Long = 0L,
    val totalDebtsCents: Long = 0L,
    val netWealthCents: Long = 0L,
    val nisabGoldCents: Long = 0L,
    val nisabSilverCents: Long = 0L,
    val nisabCents: Long = 0L,           // الأقل بينهما
    val zakatCents: Long = 0L,
    val isEligible: Boolean = false      // هل بلغ النصاب؟
)

@HiltViewModel
class ZakatViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ZakatUiState())
    val state: StateFlow<ZakatUiState> = _state.asStateFlow()

    // ============ المدخلات ============

    fun setCash(value: String) { update { it.copy(cashInput = value) } }
    fun setGoldGrams(value: String) { update { it.copy(goldGramsInput = value) } }
    fun setGoldPrice(value: String) { update { it.copy(goldPriceInput = value) } }
    fun setSilverGrams(value: String) { update { it.copy(silverGramsInput = value) } }
    fun setSilverPrice(value: String) { update { it.copy(silverPriceInput = value) } }
    fun setDebtsOwedToYou(value: String) { update { it.copy(debtsOwedToYouInput = value) } }
    fun setDebtsYouOwe(value: String) { update { it.copy(debtsYouOweInput = value) } }
    fun setCurrency(value: String) { update { it.copy(currency = value) } }

    /**
     * تحديث الحالة + إعادة الحساب.
     */
    private fun update(transform: (ZakatUiState) -> ZakatUiState) {
        val newState = transform(_state.value)
        _state.value = calculate(newState)
    }

    private fun calculate(s: ZakatUiState): ZakatUiState {
        // 1. الأصول
        val cash = MoneyFormatter.parseToCents(s.cashInput) ?: 0L
        val goldGrams = s.goldGramsInput.toDoubleOrNull() ?: 0.0
        val goldPrice = MoneyFormatter.parseToCents(s.goldPriceInput) ?: 0L
        val goldValue = (goldGrams * goldPrice).toLong()

        val silverGrams = s.silverGramsInput.toDoubleOrNull() ?: 0.0
        val silverPrice = MoneyFormatter.parseToCents(s.silverPriceInput) ?: 0L
        val silverValue = (silverGrams * silverPrice).toLong()

        val debtsToYou = MoneyFormatter.parseToCents(s.debtsOwedToYouInput) ?: 0L

        val totalAssets = cash + goldValue + silverValue + debtsToYou

        // 2. الديون عليك
        val debtsYouOwe = MoneyFormatter.parseToCents(s.debtsYouOweInput) ?: 0L

        val netWealth = (totalAssets - debtsYouOwe).coerceAtLeast(0L)

        // 3. النصاب
        // نصاب الذهب = 85 جرام × سعر الجرام
        // نصاب الفضة = 595 جرام × سعر الجرام
        val nisabGold = (85.0 * goldPrice).toLong()
        val nisabSilver = (595.0 * silverPrice).toLong()

        // النصاب = الأقل (لأنه أكثر تحفظًا للفقراء)
        val nisab = when {
            nisabGold > 0 && nisabSilver > 0 -> minOf(nisabGold, nisabSilver)
            nisabGold > 0 -> nisabGold
            nisabSilver > 0 -> nisabSilver
            else -> 0L
        }

        // 4. هل بلغ النصاب؟
        val isEligible = nisab > 0 && netWealth >= nisab

        // 5. الزكاة = 2.5% من صافي الثروة (إذا بلغ النصاب)
        val zakat = if (isEligible) (netWealth * 25L / 1000L) else 0L

        return s.copy(
            totalAssetsCents = totalAssets,
            totalDebtsCents = debtsYouOwe,
            netWealthCents = netWealth,
            nisabGoldCents = nisabGold,
            nisabSilverCents = nisabSilver,
            nisabCents = nisab,
            zakatCents = zakat,
            isEligible = isEligible
        )
    }

    fun reset() {
        _state.value = ZakatUiState()
    }
}