package com.daftar.v2.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ⭐ حالة فحص التحديثات.
 */
sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class UpdateAvailable(
        val info: UpdateInfo,
        val isMandatory: Boolean
    ) : UpdateState
    data class Downloading(val downloadId: Long) : UpdateState
    data class DownloadFailed(val reason: String) : UpdateState
    data class Error(val message: String) : UpdateState
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val checker: UpdateChecker,
    private val downloader: UpdateDownloader,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    /**
     * ⭐ فحص التحديثات.
     */
    fun checkForUpdates(silent: Boolean = true) {
        viewModelScope.launch {
            if (!silent) _state.value = UpdateState.Checking

            val info = checker.fetchLatestVersion()
            if (info == null) {
                if (!silent) {
                    _state.value = UpdateState.Error("تعذّر الاتصال بالخادم")
                } else {
                    _state.value = UpdateState.Idle
                }
                return@launch
            }

            val currentVersionCode = BuildConfig.VERSION_CODE

            when {
                // تحديث إجباري
                currentVersionCode < info.minSupportedVersionCode -> {
                    _state.value = UpdateState.UpdateAvailable(
                        info = info,
                        isMandatory = true
                    )
                }
                // تحديث اختياري
                currentVersionCode < info.latestVersionCode -> {
                    _state.value = UpdateState.UpdateAvailable(
                        info = info,
                        isMandatory = info.mandatory
                    )
                }
                // محدّث
                else -> {
                    _state.value = UpdateState.UpToDate
                }
            }
        }
    }

    /**
     * ⭐ بدء التحميل.
     */
    fun startDownload() {
        val current = _state.value
        if (current !is UpdateState.UpdateAvailable) return

        val downloadId = downloader.downloadApk(
            context = context,
            url = current.info.downloadUrl,
            versionName = current.info.latestVersionName
        )

        _state.value = if (downloadId != null && downloadId > 0) {
            UpdateState.Downloading(downloadId)
        } else {
            UpdateState.DownloadFailed("فشل بدء التحميل")
        }
    }

    /**
     * ⭐ المستخدم تجاهل التحديث.
     */
    fun dismiss() {
        val current = _state.value
        if (current is UpdateState.UpdateAvailable && !current.isMandatory) {
            _state.value = UpdateState.Idle
        }
    }

    fun reset() {
        _state.value = UpdateState.Idle
    }
}