package com.daftar.v2.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.repository.SettingsRepository
import com.daftar.v2.util.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdvancedReportsUiState(
    val loading: Boolean = true,
    val currency: String = CurrencyUtils.DEFAULT_CODE,
    val aging: AgingReport? = null,
    val cashFlow: CashFlowReport? = null,
    val comparison: ComparisonReport? = null,
    val error: String? = null
)

@HiltViewModel
class AdvancedReportsViewModel @Inject constructor(
    private val repository: AdvancedReportRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdvancedReportsUiState())
    val state: StateFlow<AdvancedReportsUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            try {
                val currency = settingsRepository.getDefaultCurrency()

                val aging = repository.buildAgingReport(currency)
                val cashFlow = repository.buildCashFlowReport(currency)
                val comparison = repository.buildComparisonReport(currency)

                _state.value = _state.value.copy(
                    loading = false,
                    currency = currency,
                    aging = aging,
                    cashFlow = cashFlow,
                    comparison = comparison
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "فشل تحميل التقارير"
                )
            }
        }
    }

    fun refresh() = loadAll()
}