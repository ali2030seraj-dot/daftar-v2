package com.daftar.v2.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.ReminderPreferences
import com.daftar.v2.domain.reminder.ReminderChecker
import com.daftar.v2.notification.NotificationHelper
import com.daftar.v2.notification.ReminderScheduler
import com.daftar.v2.util.MoneyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderSettingsState(
    val loading: Boolean = true,
    val enabled: Boolean = true,
    val thresholdDays: Int = 30,
    val lastSent: Long = 0L,
    val checking: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class ReminderSettingsViewModel @Inject constructor(
    private val prefs: ReminderPreferences,
    private val scheduler: ReminderScheduler,
    private val notificationHelper: NotificationHelper,
    private val checker: ReminderChecker
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderSettingsState())
    val state: StateFlow<ReminderSettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.enabled.collect { enabled ->
                _state.value = _state.value.copy(enabled = enabled, loading = false)
            }
        }
        viewModelScope.launch {
            prefs.thresholdDays.collect { days ->
                _state.value = _state.value.copy(thresholdDays = days)
            }
        }
        viewModelScope.launch {
            prefs.lastSent.collect { ts ->
                _state.value = _state.value.copy(lastSent = ts)
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setEnabled(enabled)
            if (enabled) {
                scheduler.schedule()
                _state.value = _state.value.copy(
                    message = "تم تفعيل التنبيهات",
                    isError = false
                )
            } else {
                scheduler.cancel()
                _state.value = _state.value.copy(
                    message = "تم إيقاف التنبيهات",
                    isError = false
                )
            }
        }
    }

    fun setThresholdDays(days: Int) {
        viewModelScope.launch {
            prefs.setThresholdDays(days)
        }
    }

    /**
     * ⭐⭐⭐ فحص المدينين الفعليين الآن.
     *
     * - يقرأ المدينين من قاعدة البيانات
     * - يحسب المتأخرين حسب thresholdDays
     * - يعرض الإشعار فوراً
     */
    fun checkRealDebtorsNow() {
        viewModelScope.launch {
            _state.value = _state.value.copy(checking = true, message = null)

            try {
                val overdue = checker.findOverdueDebtors()

                if (overdue.isEmpty()) {
                    _state.value = _state.value.copy(
                        checking = false,
                        message = "✅ لا يوجد مدينون متأخرون حالياً",
                        isError = false
                    )
                    return@launch
                }

                // ⭐ ابنِ الإشعار بنفس منطق Worker
                val count = overdue.size
                val title = if (count == 1) "💰 دين متأخر" else "💰 $count ديون متأخرة"

                val details = overdue.take(3).joinToString("\n") { p ->
                    "• ${p.name}: ${MoneyFormatter.formatShort(p.balanceCents)} " +
                            "(${p.daysOverdue} يوم)"
                }
                val moreText = if (count > 3) "\n+${count - 3} آخرون" else ""
                val message = "$details$moreText"

                // ⭐ أرسل الإشعار
                notificationHelper.showReminder(
                    title = title,
                    message = message,
                    count = count
                )

                _state.value = _state.value.copy(
                    checking = false,
                    message = "🔔 تم إرسال إشعار بـ $count مدين متأخر — افتح شريط الإشعارات",
                    isError = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    checking = false,
                    message = "فشل الفحص: ${e.message ?: "غير معروف"}",
                    isError = true
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}