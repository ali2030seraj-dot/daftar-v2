package com.daftar.v2.ui.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.security.license.LicenseManager
import com.daftar.v2.security.license.LicenseStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivationUiState(
    val loading: Boolean = true,
    val status: LicenseStatus? = null,
    val deviceFingerprint: String = "",
    val codeInput: String = "",
    val activating: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val activationSuccess: Boolean = false
)

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val licenseManager: LicenseManager
) : ViewModel() {

    private val _state = MutableStateFlow(ActivationUiState())
    val state: StateFlow<ActivationUiState> = _state.asStateFlow()

    // ⭐ Job لإلغاء أي refresh سابق
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    /**
     * ⭐ إعادة قراءة الحالة.
     * يلغي أي refresh سابق لمنع ConcurrentModificationException.
     */
    fun refresh() {
        // ⭐ إلغاء أي refresh سابق
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, isError = false)
            try {
                val status = licenseManager.getCurrentStatus()
                val fingerprint = licenseManager.getCurrentDeviceFingerprint()
                _state.value = _state.value.copy(
                    loading = false,
                    status = status,
                    deviceFingerprint = fingerprint
                )
            } catch (e: CancellationException) {
                // ⭐ لا نعالج الإلغاء
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    message = e.message ?: "خطأ في قراءة الحالة",
                    isError = true
                )
            }
        }
    }

    fun updateCodeInput(value: String) {
        _state.value = _state.value.copy(codeInput = value, message = null)
    }

    fun activate() {
        val code = _state.value.codeInput.trim()
        if (code.isEmpty()) {
            _state.value = _state.value.copy(
                message = "أدخل الكود أولًا",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                activating = true,
                message = null,
                isError = false
            )

            when (val result = licenseManager.activate(code)) {
                is LicenseManager.ActivationResult.Success -> {
                    _state.value = _state.value.copy(
                        activating = false,
                        message = "تم التفعيل بنجاح",
                        isError = false,
                        codeInput = "",
                        activationSuccess = true
                    )
                    refresh()
                }
                is LicenseManager.ActivationResult.Rejected -> {
                    _state.value = _state.value.copy(
                        activating = false,
                        message = result.reason,
                        isError = true
                    )
                }
                is LicenseManager.ActivationResult.Error -> {
                    _state.value = _state.value.copy(
                        activating = false,
                        message = result.message,
                        isError = true
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    override fun onCleared() {
        super.onCleared()
        // ⭐ إلغاء أي job عند تدمير ViewModel
        refreshJob?.cancel()
    }
}