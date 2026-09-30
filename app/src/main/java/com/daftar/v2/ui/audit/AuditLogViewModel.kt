package com.daftar.v2.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daftar.v2.data.entity.AuditLogEntity
import com.daftar.v2.domain.audit.AuditLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    auditLogger: AuditLogger
) : ViewModel() {

    private val _logs = MutableStateFlow<List<AuditLogEntity>>(emptyList())
    val logs: StateFlow<List<AuditLogEntity>> = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            auditLogger.observeRecent(500).collect {
                _logs.value = it
            }
        }
    }
}