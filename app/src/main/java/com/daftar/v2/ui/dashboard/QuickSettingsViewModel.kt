package com.daftar.v2.ui.dashboard

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

/**
 * ⭐ ViewModel صغير لإعدادات سريعة (الوضع الليلي + العملة).
 */
@HiltViewModel
class QuickSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _currency = MutableStateFlow(CurrencyUtils.DEFAULT_CODE)
    val currency: StateFlow<String> = _currency.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observe(SettingsRepository.KEY_DARK_MODE).collect { raw ->
                _darkMode.value = raw?.toBooleanStrictOrNull() ?: false
            }
        }
        viewModelScope.launch {
            settingsRepository.observe(SettingsRepository.KEY_DEFAULT_CURRENCY).collect { raw ->
                _currency.value = CurrencyUtils.toCode(raw)
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.put(
                SettingsRepository.KEY_DARK_MODE,
                enabled.toString()
            )
        }
    }

    fun setCurrency(code: String) {
        viewModelScope.launch {
            settingsRepository.put(
                SettingsRepository.KEY_DEFAULT_CURRENCY,
                CurrencyUtils.toCode(code)
            )
        }
    }
}