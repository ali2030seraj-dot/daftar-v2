package com.daftar.v2.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.daftar.v2.BuildConfig

/**
 * ⭐ مكوّن وسيط:
 * - يفحص التحديثات عند الإقلاع
 * - يعرض الحوار المناسب
 */
@Composable
fun UpdateCheckerHost(
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(silent = true)
    }

    UpdateDialog(
        state = state,
        currentVersionName = BuildConfig.VERSION_NAME,
        onUpdate = { viewModel.startDownload() },
        onDismiss = { viewModel.reset() }
    )
}