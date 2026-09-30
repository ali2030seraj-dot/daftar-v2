package com.daftar.v2.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.cloud.BackupMode
import com.daftar.v2.cloud.CloudBackupManager
import com.daftar.v2.cloud.CloudFileInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class BackupUiState(
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val cloudEnabled: Boolean = false,
    val cloudFolderName: String = "لم يتم الاختيار",
    val cloudHasFolder: Boolean = false,
    val cloudLastBackupLabel: String = "لم يتم النسخ بعد",
    val cloudMode: BackupMode = BackupMode.OFF,
    val cloudFiles: List<CloudFileInfo> = emptyList(),
    val showCloudFilesDialog: Boolean = false,
    val showExportCloudDialog: Boolean = false,
    val selectedCloudFile: CloudFileInfo? = null,
    val cloudPassword: String = ""
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exporter: BackupExporter,
    private val importer: BackupImporter,
    private val restorer: DataRestorer,
    private val cloudManager: CloudBackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    init {
        refreshCloudState()
    }

    // ═══════════════════════════════════════════════════════
    //              ⭐ اسم الملف التسلسلي
    // ═══════════════════════════════════════════════════════

    suspend fun getNextSequentialFileName(): String {
        return exporter.nextSequentialFileName()
    }

    fun getDefaultFileName(): String {
        return exporter.suggestedFileName()
    }

    // ═══════════════════════════════════════════════════════
    //              ⭐ السحابة
    // ═══════════════════════════════════════════════════════

    fun refreshCloudState() {
        val last = cloudManager.getLastBackupAt()
        val label = if (last > 0L) {
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(last))
        } else "لم يتم النسخ بعد"

        // ⭐ نستخدم copy — لا ننشئ كائن جديد
        _state.value = _state.value.copy(
            cloudEnabled = cloudManager.getMode() != BackupMode.OFF,
            cloudFolderName = cloudManager.getFolderName(),
            cloudHasFolder = cloudManager.getFolderUri() != null,
            cloudLastBackupLabel = label,
            cloudMode = cloudManager.getMode(),
            cloudPassword = cloudManager.getPassword() ?: ""
        )
    }

    fun onCloudFolderPicked(uri: Uri) {
        cloudManager.saveFolderUri(uri)
        refreshCloudState()
        showSuccess("✅ تم حفظ المجلد السحابي")
    }

    fun setCloudPassword(password: String) {
        cloudManager.setPassword(password)
        _state.value = _state.value.copy(cloudPassword = password)
    }

    fun setCloudMode(mode: BackupMode) {
        if (mode != BackupMode.OFF) {
            if (cloudManager.getFolderUri() == null) {
                showError("⚠️ اختر مجلداً أولاً")
                return
            }
            if ((cloudManager.getPassword() ?: "").length < 6) {
                showError("⚠️ كلمة المرور 6 أحرف على الأقل")
                return
            }
            cloudManager.giveConsent()
        } else {
            cloudManager.revokeConsent()
        }
        cloudManager.setMode(mode)
        refreshCloudState()

        val msg = when (mode) {
            BackupMode.AUTO -> "🟢 تم تفعيل النسخ التلقائي"
            BackupMode.ASK_EACH_TIME -> "🟡 سيُطلب إذنك قبل كل نسخة"
            BackupMode.OFF -> "🔴 تم الإيقاف"
        }
        showSuccess(msg)
    }

    fun backupToCloudNow() {
        val password = cloudManager.getPassword()
        if (password.isNullOrBlank() || password.length < 6) {
            showError("⚠️ كلمة المرور 6 أحرف على الأقل")
            return
        }
        if (cloudManager.getFolderUri() == null) {
            showError("⚠️ اختر مجلداً أولاً")
            return
        }

        viewModelScope.launch {
            // ⭐ copy بدل new
            _state.value = _state.value.copy(busy = true, message = null)
            val success = cloudManager.performBackup(password)
            refreshCloudState()
            _state.value = _state.value.copy(
                busy = false,
                message = if (success)
                    "✅ تم النسخ السحابي بنجاح"
                else
                    "❌ فشل النسخ السحابي",
                isError = !success
            )
        }
    }

    fun openCloudFilesDialog() {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true)
            val files = cloudManager.listCloudFiles()
            _state.value = _state.value.copy(
                busy = false,
                cloudFiles = files.toList(),   // ⭐ نسخة آمنة
                showCloudFilesDialog = true
            )
        }
    }

    fun closeCloudFilesDialog() {
        _state.value = _state.value.copy(
            showCloudFilesDialog = false,
            selectedCloudFile = null,
            showExportCloudDialog = false
        )
    }

    fun selectCloudFileForRestore(file: CloudFileInfo) {
        _state.value = _state.value.copy(
            selectedCloudFile = file,
            showExportCloudDialog = true
        )
    }

    fun dismissExportCloudDialog() {
        _state.value = _state.value.copy(
            showExportCloudDialog = false,
            selectedCloudFile = null
        )
    }

    fun restoreFromCloudFile(
        file: CloudFileInfo,
        password: String,
        mode: ImportMode = ImportMode.MERGE
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)

            val content = cloudManager.readCloudFile(file.uri)
            if (content == null) {
                _state.value = _state.value.copy(
                    busy = false,
                    message = "❌ فشل قراءة الملف السحابي",
                    isError = true
                )
                return@launch
            }

            val readResult = importer.importFromContent(content, password)
            when (readResult) {
                is ImportResult.Failure -> {
                    _state.value = _state.value.copy(
                        busy = false,
                        message = readResult.message,
                        isError = true
                    )
                }
                is ImportResult.Success -> {
                    when (val restoreResult = restorer.restoreFromJson(readResult.plainJson, mode)) {
                        is RestoreResult.Success -> {
                            _state.value = _state.value.copy(
                                busy = false,
                                message = "✅ تم الاسترداد من السحابة",
                                isError = false,
                                showCloudFilesDialog = false,
                                selectedCloudFile = null,
                                showExportCloudDialog = false
                            )
                        }
                        is RestoreResult.Failure -> {
                            _state.value = _state.value.copy(
                                busy = false,
                                message = "❌ فشل: ${restoreResult.message}",
                                isError = true
                            )
                        }
                    }
                }
            }
        }
    }

    fun exportCloudFileToDevice(
        file: CloudFileInfo,
        destinationUri: Uri
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, message = null)
            val success = cloudManager.exportCloudFileToDevice(file.uri, destinationUri)
            _state.value = _state.value.copy(
                busy = false,
                message = if (success)
                    "✅ تم التصدير إلى الجهاز"
                else
                    "❌ فشل التصدير",
                isError = !success,
                showExportCloudDialog = false,
                selectedCloudFile = null
            )
        }
    }

    // ═══════════════════════════════════════════════════════
    //              النسخ الاحتياطي المحلي
    // ═══════════════════════════════════════════════════════

    suspend fun buildEncryptedContent(password: String): String? {
        return try {
            exporter.exportEncryptedJson(password)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importFromUri(
        uri: Uri,
        password: String,
        mode: ImportMode = ImportMode.MERGE
    ) {
        viewModelScope.launch {
            // ⭐⭐⭐ الإصلاح: copy بدل new (يحافظ على cloudFiles وغيرها)
            _state.value = _state.value.copy(
                busy = true,
                message = null,
                isError = false
            )

            when (val readResult = importer.importFromUri(context, uri, password)) {
                is ImportResult.Failure -> {
                    _state.value = _state.value.copy(
                        busy = false,
                        message = readResult.message,
                        isError = true
                    )
                    refreshCloudState()
                }
                is ImportResult.Success -> {
                    when (val restoreResult = restorer.restoreFromJson(readResult.plainJson, mode)) {
                        is RestoreResult.Success -> {
                            val modeLabel = when (mode) {
                                ImportMode.MERGE -> "دمج"
                                ImportMode.REPLACE -> "استبدال"
                            }
                            _state.value = _state.value.copy(
                                busy = false,
                                message = "✅ تم الاستيراد بنجاح (وضع $modeLabel)",
                                isError = false
                            )
                            refreshCloudState()
                        }
                        is RestoreResult.Failure -> {
                            _state.value = _state.value.copy(
                                busy = false,
                                message = "❌ فشل: ${restoreResult.message}",
                                isError = true
                            )
                            refreshCloudState()
                        }
                    }
                }
            }
        }
    }

    fun showSuccess(message: String) {
        _state.value = _state.value.copy(message = message, isError = false)
    }

    fun showError(message: String) {
        _state.value = _state.value.copy(message = message, isError = true)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}