package com.daftar.v2.cloud

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CloudBackupUiState(
    val mode: BackupMode = BackupMode.OFF,
    val hasConsent: Boolean = false,
    val folderUri: Uri? = null,
    val folderName: String = "لم يتم الاختيار",
    val password: String = "",
    val lastBackupLabel: String = "لم يتم النسخ بعد",
    val busy: Boolean = false,
    val showConsentDialog: Boolean = false,      // ⭐ جديد
    val pendingMode: BackupMode? = null,          // ⭐ جديد
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class CloudBackupViewModel @Inject constructor(
    private val manager: CloudBackupManager,
    private val scheduler: CloudBackupScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(CloudBackupUiState())
    val state: StateFlow<CloudBackupUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val last = manager.getLastBackupAt()
        val label = if (last > 0L) {
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(last))
        } else {
            "لم يتم النسخ بعد"
        }

        _state.value = _state.value.copy(
            mode = manager.getMode(),
            hasConsent = manager.hasConsent(),
            folderUri = manager.getFolderUri(),
            folderName = manager.getFolderName(),
            password = manager.getPassword() ?: "",
            lastBackupLabel = label
        )
    }

    fun onFolderPicked(uri: Uri) {
        manager.saveFolderUri(uri)
        refresh()
        _state.value = _state.value.copy(
            message = "✅ تم حفظ المجلد",
            isError = false
        )
    }

    fun setPassword(password: String) {
        manager.setPassword(password)
        _state.value = _state.value.copy(password = password)
    }

    /**
     * ⭐ طلب تغيير الوضع — يفتح حوار الموافقة أولاً.
     */
    fun requestModeChange(newMode: BackupMode) {
        // إذا كان OFF → لا حاجة لحوار
        if (newMode == BackupMode.OFF) {
            applyMode(newMode)
            return
        }

        // ⭐ تحقق من الشروط
        if (manager.getFolderUri() == null) {
            _state.value = _state.value.copy(
                message = "⚠️ اختر مجلداً أولاً",
                isError = true
            )
            return
        }
        if ((manager.getPassword() ?: "").length < 6) {
            _state.value = _state.value.copy(
                message = "⚠️ كلمة المرور 6 أحرف على الأقل",
                isError = true
            )
            return
        }

        // ⭐ عرض حوار الموافقة
        _state.value = _state.value.copy(
            showConsentDialog = true,
            pendingMode = newMode
        )
    }

    /**
     * ⭐ عند الموافقة في الحوار.
     */
    fun confirmConsent() {
        val pending = _state.value.pendingMode ?: return
        manager.giveConsent()
        applyMode(pending)
        _state.value = _state.value.copy(
            showConsentDialog = false,
            pendingMode = null
        )
    }

    /**
     * ⭐ عند رفض الحوار.
     */
    fun dismissConsent() {
        _state.value = _state.value.copy(
            showConsentDialog = false,
            pendingMode = null
        )
    }

    private fun applyMode(mode: BackupMode) {
        manager.setMode(mode)
        if (mode != BackupMode.OFF) {
            scheduler.schedule()
        } else {
            scheduler.cancel()
            manager.revokeConsent()
        }
        refresh()

        val msg = when (mode) {
            BackupMode.AUTO -> "🟢 تم تفعيل النسخ التلقائي"
            BackupMode.ASK_EACH_TIME -> "🟡 سيُطلب إذنك قبل كل نسخة"
            BackupMode.OFF -> "🔴 تم إيقاف النسخ السحابي"
        }
        _state.value = _state.value.copy(message = msg, isError = false)
    }

    fun runBackupNow() {
        val password = manager.getPassword()
        if (password.isNullOrBlank() || password.length < 6) {
            _state.value = _state.value.copy(
                message = "⚠️ كلمة المرور 6 أحرف على الأقل",
                isError = true
            )
            return
        }
        if (manager.getFolderUri() == null) {
            _state.value = _state.value.copy(
                message = "⚠️ اختر مجلداً أولاً",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            val success = manager.performBackup(password)
            refresh()
            _state.value = _state.value.copy(
                busy = false,
                message = if (success)
                    "✅ تم النسخ الاحتياطي بنجاح"
                else
                    "❌ فشل النسخ الاحتياطي",
                isError = !success
            )
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}