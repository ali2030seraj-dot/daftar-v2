package com.daftar.v2.ui.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.entity.SmsTemplateEntity
import com.daftar.v2.data.entity.SmsTemplateType
import com.daftar.v2.data.repository.SmsTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsTemplatesUiState(
    val templates: List<SmsTemplateEntity> = emptyList(),
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class SmsTemplatesViewModel @Inject constructor(
    private val repository: SmsTemplateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SmsTemplatesUiState())
    val state: StateFlow<SmsTemplatesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfNeeded()
            repository.observeAll().collect { templates ->
                _state.value = _state.value.copy(templates = templates)
            }
        }
    }

    fun saveTemplate(typeName: String, body: String) {
        viewModelScope.launch {
            try {
                val type = SmsTemplateType.valueOf(typeName)
                repository.saveTemplate(type, body)
                _state.value = _state.value.copy(
                    message = "✅ تم حفظ القالب",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "❌ فشل الحفظ",
                    isError = true
                )
            }
        }
    }

    fun resetToDefault(typeName: String) {
        viewModelScope.launch {
            try {
                val type = SmsTemplateType.valueOf(typeName)
                repository.resetToDefault(type)
                repository.seedDefaultsIfNeeded()
                _state.value = _state.value.copy(
                    message = "تم استرجاع الافتراضي",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = "فشل",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}