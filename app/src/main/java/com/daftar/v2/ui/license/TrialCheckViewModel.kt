package com.daftar.v2.ui.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.security.license.TrialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ⭐ ViewModel للتحقق من إعادة تثبيت التطبيق.
 */
@HiltViewModel
class TrialCheckViewModel @Inject constructor(
    private val trialManager: TrialManager
) : ViewModel() {

    private val _wasReinstalled = MutableStateFlow(false)
    val wasReinstalled: StateFlow<Boolean> = _wasReinstalled.asStateFlow()

    private val _remainingDays = MutableStateFlow(0L)
    val remainingDays: StateFlow<Long> = _remainingDays.asStateFlow()

    init {
        viewModelScope.launch {
            // ⭐ فحوصات متعددة لضمان قراءة flag بعد كتابته
            refresh()
            delay(1_000)
            refresh()
            delay(2_000)
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                // ⭐ wasReinstalled تأخذ في الحسبان "تم الإغلاق مؤخراً"
                _wasReinstalled.value = trialManager.wasReinstalled()
                _remainingDays.value = trialManager.trialDaysRemaining()
            } catch (e: Exception) { }
        }
    }

    /**
     * ⭐ يُستدعى عند إغلاق البانر.
     * يعلّم البانر كمُغلق → لا يظهر لمدة 7 أيام.
     */
    fun clearResetFlag() {
        viewModelScope.launch {
            try {
                trialManager.markBannerDismissed()
                _wasReinstalled.value = false
            } catch (e: Exception) { }
        }
    }
}