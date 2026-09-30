package com.daftar.v2.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.security.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityUiState(
    val lockEnabled: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val appLockManager: AppLockManager
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityUiState())
    val state: StateFlow<SecurityUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                lockEnabled = appLockManager.isLockEnabled()
            )
        }
    }

    fun setPassword(password: String, confirm: String) {
        viewModelScope.launch {
            if (password != confirm) {
                _state.value = _state.value.copy(
                    message = "كلمتا المرور غير متطابقتين",
                    isError = true
                )
                return@launch
            }
            if (password.length < 4) {
                _state.value = _state.value.copy(
                    message = "كلمة المرور قصيرة جدًا",
                    isError = true
                )
                return@launch
            }
            appLockManager.setPassword(password)
            _state.value = _state.value.copy(
                message = "تم تعيين كلمة المرور",
                isError = false
            )
        }
    }

    fun changePassword(current: String, newPass: String, confirm: String) {
        viewModelScope.launch {
            if (newPass != confirm) {
                _state.value = _state.value.copy(
                    message = "كلمتا المرور غير متطابقتين",
                    isError = true
                )
                return@launch
            }
            try {
                appLockManager.changePassword(current, newPass)
                _state.value = _state.value.copy(
                    message = "تم تغيير كلمة المرور",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "فشل التغيير",
                    isError = true
                )
            }
        }
    }

    fun toggleLock(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                try {
                    appLockManager.enableLock()
                    _state.value = _state.value.copy(
                        lockEnabled = true,
                        message = "تم تفعيل القفل",
                        isError = false
                    )
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        message = "يجب تعيين كلمة مرور أولًا",
                        isError = true
                    )
                }
            } else {
                appLockManager.disableLock()
                _state.value = _state.value.copy(
                    lockEnabled = false,
                    message = "تم تعطيل القفل",
                    isError = false
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}