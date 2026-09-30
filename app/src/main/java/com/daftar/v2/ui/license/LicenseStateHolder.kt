package com.daftar.v2.ui.license

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.daftar.v2.security.license.LicenseStatus
import kotlinx.coroutines.launch

/**
 * حالة عامة للترخيص تُستخدم في كل التطبيق.
 */
@Composable
fun rememberLicenseState(
    viewModel: ActivationViewModel = hiltViewModel()
): LicenseUiState {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    return LicenseUiState(
        status = state.status,
        isLoading = state.loading,
        refresh = { viewModel.refresh() }
    )
}

data class LicenseUiState(
    val status: LicenseStatus?,
    val isLoading: Boolean,
    val refresh: () -> Unit
)