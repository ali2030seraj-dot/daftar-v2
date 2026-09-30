package com.daftar.v2.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val appName: String = "دفتر حساباتي",
    val currency: String = "ريال يمني",
    val darkMode: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observeAppName()
        observeCurrency()
        observeDarkMode()
    }

    private fun observeAppName() {
        viewModelScope.launch {
            settingsRepository.observeAppName().collect { name ->
                _state.value = _state.value.copy(appName = name)
            }
        }
    }

    fun setAppName(name: String) {
        viewModelScope.launch {
            settingsRepository.setAppName(name)
        }
    }

    private fun observeCurrency() {
        viewModelScope.launch {
            settingsRepository.observe(SettingsRepository.KEY_DEFAULT_CURRENCY)
                .collect { code ->
                    val value = code ?: "ريال يمني"
                    _state.value = _state.value.copy(currency = value)
                }
        }
    }

    private fun observeDarkMode() {
        viewModelScope.launch {
            settingsRepository.observe(SettingsRepository.KEY_DARK_MODE)
                .collect { value ->
                    _state.value = _state.value.copy(
                        darkMode = value == "true"
                    )
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
}