package com.daftar.v2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ⭐ ViewModel الرئيسي — يقرأ الوضع الليلي من الإعدادات.
 * يُستخدم في MainActivity لتطبيق الثيم فوراً عند التغيير.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    init {
        // ⭐ نراقب التغييرات فورياً من قاعدة البيانات
        viewModelScope.launch {
            settingsRepository.observe(SettingsRepository.KEY_DARK_MODE)
                .collect { value ->
                    _darkMode.value = value == "true"
                }
        }
    }
}