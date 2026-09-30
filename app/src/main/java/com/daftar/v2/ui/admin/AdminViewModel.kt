package com.daftar.v2.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.security.license.AdminAuthManager
import com.daftar.v2.security.license.DeviceIdProvider
import com.daftar.v2.security.license.LicenseManager
import com.daftar.v2.security.license.LicenseRepository
import com.daftar.v2.security.license.LicenseStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isAuthenticated: Boolean = false,
    val loading: Boolean = false,
    val passwordInput: String = "",
    val message: String? = null,
    val isError: Boolean = false,
    val currentStatus: LicenseStatus? = null,
    val deviceFingerprint: String = "",
    val usingDefaultPassword: Boolean = false,
    val showChangePassword: Boolean = false,
    val oldPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = ""
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val authManager: AdminAuthManager,
    private val licenseManager: LicenseManager,
    private val licenseRepository: LicenseRepository,
    private val deviceIdProvider: DeviceIdProvider
) : ViewModel() {

    companion object {
        private const val TAG = "AdminViewModel"
    }

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        Log.d(TAG, "═══════════════════════════")
        Log.d(TAG, "AdminViewModel init")
        Log.d(TAG, "usingDefaultPassword = ${authManager.isUsingDefaultPassword()}")
        Log.d(TAG, "isSessionActive = ${authManager.isSessionActive()}")
        Log.d(TAG, "═══════════════════════════")
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val active = authManager.isSessionActive()
            Log.d(TAG, "checkSession: active = $active")
            _state.value = _state.value.copy(
                isAuthenticated = active,
                usingDefaultPassword = authManager.isUsingDefaultPassword()
            )
            if (active) {
                loadData()
            }
        }
    }

    fun updatePasswordInput(value: String) {
        Log.d(TAG, "updatePasswordInput: '$value' (len=${value.length})")
        _state.value = _state.value.copy(passwordInput = value, message = null)
    }

    fun login() {
        viewModelScope.launch {
            val inputValue = _state.value.passwordInput

            Log.d(TAG, "═══════════════════════════")
            Log.d(TAG, "LOGIN CALLED")
            Log.d(TAG, "passwordInput = '$inputValue'")
            Log.d(TAG, "length = ${inputValue.length}")
            Log.d(TAG, "isEmpty = ${inputValue.isEmpty()}")
            Log.d(TAG, "═══════════════════════════")

            _state.value = _state.value.copy(loading = true, message = null)

            val ok = authManager.verifyPassword(inputValue)
            Log.d(TAG, "verifyPassword => $ok")

            if (ok) {
                Log.d(TAG, "→ LOGIN SUCCESS")
                authManager.startSession()
                _state.value = _state.value.copy(
                    isAuthenticated = true,
                    loading = false,
                    passwordInput = "",
                    message = null,
                    isError = false
                )
                loadData()
            } else {
                Log.d(TAG, "→ LOGIN FAILED")
                _state.value = _state.value.copy(
                    loading = false,
                    message = "كلمة المرور غير صحيحة",
                    isError = true
                )
            }
        }
    }

    fun logout() {
        Log.d(TAG, "logout()")
        authManager.endSession()
        _state.value = AdminUiState(
            isAuthenticated = false,
            usingDefaultPassword = authManager.isUsingDefaultPassword()
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val status = licenseManager.getCurrentStatus()
                val fp = licenseManager.getCurrentDeviceFingerprint()
                Log.d(TAG, "loadData: status=$status, fp=$fp")
                _state.value = _state.value.copy(
                    currentStatus = status,
                    deviceFingerprint = fp
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadData error: ${e.message}", e)
                _state.value = _state.value.copy(
                    message = e.message ?: "خطأ في تحميل البيانات",
                    isError = true
                )
            }
        }
    }

    fun refresh() {
        Log.d(TAG, "refresh()")
        loadData()
    }

    fun toggleChangePassword() {
        _state.value = _state.value.copy(
            showChangePassword = !_state.value.showChangePassword,
            message = null
        )
    }

    fun updateOldPassword(value: String) {
        _state.value = _state.value.copy(oldPasswordInput = value)
    }

    fun updateNewPassword(value: String) {
        _state.value = _state.value.copy(newPasswordInput = value)
    }

    fun updateConfirmPassword(value: String) {
        _state.value = _state.value.copy(confirmPasswordInput = value)
    }

    fun changePassword() {
        val s = _state.value
        if (s.newPasswordInput != s.confirmPasswordInput) {
            _state.value = s.copy(
                message = "كلمتا المرور غير متطابقتين",
                isError = true
            )
            return
        }
        if (s.newPasswordInput.length < 4) {
            _state.value = s.copy(
                message = "كلمة المرور يجب أن تكون 4 أحرف على الأقل",
                isError = true
            )
            return
        }

        val ok = authManager.changePassword(s.oldPasswordInput, s.newPasswordInput)
        if (ok) {
            _state.value = s.copy(
                showChangePassword = false,
                oldPasswordInput = "",
                newPasswordInput = "",
                confirmPasswordInput = "",
                message = "تم تغيير كلمة المرور بنجاح",
                isError = false,
                usingDefaultPassword = false
            )
        } else {
            _state.value = s.copy(
                message = "كلمة المرور القديمة غير صحيحة",
                isError = true
            )
        }
    }

    fun resetAllLicenses() {
        viewModelScope.launch {
            Log.d(TAG, "resetAllLicenses()")
            licenseManager.resetAll()
            _state.value = _state.value.copy(
                message = "تم حذف جميع التراخيص",
                isError = false
            )
            loadData()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}