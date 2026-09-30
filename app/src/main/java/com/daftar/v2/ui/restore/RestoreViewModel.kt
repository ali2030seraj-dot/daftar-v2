package com.daftar.v2.ui.restore

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.backup.BackupImporter
import com.daftar.v2.backup.DataRestorer
import com.daftar.v2.backup.ImportMode
import com.daftar.v2.backup.ImportResult
import com.daftar.v2.backup.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestoreUiState(
    val password: String = "",
    val isLoading: Boolean = false,
    val status: String = "",
    val isSuccess: Boolean = false,
    val selectedFile: Uri? = null,
    val requestFilePicker: Boolean = false
)

@HiltViewModel
class RestoreViewModel @Inject constructor(
    private val backupImporter: BackupImporter,
    private val dataRestorer: DataRestorer
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestoreUiState())
    val uiState: StateFlow<RestoreUiState> = _uiState.asStateFlow()

    // ⭐ Job لإلغاء العملية السابقة
    private var restoreJob: Job? = null

    fun setIncomingFile(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedFile = uri,
                status = "📥 تم استلام الملف: ${uri.lastPathSegment ?: "غير معروف"}"
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun openFilePicker() {
        _uiState.update { it.copy(requestFilePicker = true) }
    }

    fun onFilePickerLaunched() {
        _uiState.update { it.copy(requestFilePicker = false) }
    }

    fun restore(context: Context) {
        val uri = _uiState.value.selectedFile ?: return
        val password = _uiState.value.password

        // ⭐ إلغاء أي عملية سابقة
        restoreJob?.cancel()

        restoreJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, status = "", isSuccess = false)
            }

            when (val readResult = backupImporter.importFromUri(context, uri, password)) {
                is ImportResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            status = readResult.message,
                            isSuccess = false
                        )
                    }
                }
                is ImportResult.Success -> {
                    // ⭐ استخدام MERGE افتراضياً (أكثر أماناً)
                    when (val restoreResult = dataRestorer.restoreFromJson(
                        readResult.plainJson,
                        ImportMode.MERGE
                    )) {
                        is RestoreResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    status = "✅ تم الاسترداد بنجاح",
                                    isSuccess = true
                                )
                            }
                        }
                        is RestoreResult.Failure -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    status = "❌ فشل الاسترداد: ${restoreResult.message}",
                                    isSuccess = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        restoreJob?.cancel()
    }
}