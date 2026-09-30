package com.daftar.v2.ui.currency

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

data class CurrencyPickerState(
    val currentCurrency: String = CurrencyUtils.DEFAULT_CODE
)

@HiltViewModel
class CurrencyPickerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencyPickerState())
    val state: StateFlow<CurrencyPickerState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observe(SettingsRepository.KEY_DEFAULT_CURRENCY)
                .collect { raw ->
                    _state.value = _state.value.copy(
                        currentCurrency = CurrencyUtils.toCode(raw)
                    )
                }
        }
    }

    fun selectCurrency(code: String) {
        viewModelScope.launch {
            settingsRepository.put(
                SettingsRepository.KEY_DEFAULT_CURRENCY,
                CurrencyUtils.toCode(code)
            )
        }
    }
}