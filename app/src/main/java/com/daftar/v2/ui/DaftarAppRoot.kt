package com.daftar.v2.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daftar.v2.DevConfig
import com.daftar.v2.data.repository.SettingsRepository
import com.daftar.v2.notification.RequestNotificationPermission
import com.daftar.v2.onboarding.OnboardingPrefs
import com.daftar.v2.onboarding.OnboardingScreen
import com.daftar.v2.security.AppLockManager
import com.daftar.v2.ui.adaptive.ContentContainer
import com.daftar.v2.ui.adaptive.DeviceType
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.adaptive.WindowOrientation
import com.daftar.v2.ui.adaptive.WindowSize
import com.daftar.v2.ui.adaptive.adaptiveTokensFor
import com.daftar.v2.ui.license.ActivationViewModel
import com.daftar.v2.ui.license.ReinstallBanner
import com.daftar.v2.ui.license.StoragePermissionCard
import com.daftar.v2.ui.license.SubscriptionBanner
import com.daftar.v2.ui.license.SubscriptionLockOverlay
import com.daftar.v2.ui.license.TrialCheckViewModel
import com.daftar.v2.ui.navigation.DaftarNavHost
import com.daftar.v2.ui.navigation.Destinations
import com.daftar.v2.ui.navigation.FloatingBottomNav
import com.daftar.v2.update.UpdateCheckerHost
import com.daftar.v2.util.RequestStoragePermission
import com.daftar.v2.util.StoragePermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════
//              AppLockViewModel
// ═══════════════════════════════════════════════════════════

@HiltViewModel
class AppLockViewModel @Inject constructor(
    val appLockManager: AppLockManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private val _appName = MutableStateFlow("دفتر حساباتي")
    val appName: StateFlow<String> = _appName.asStateFlow()

    init {
        viewModelScope.launch {
            _locked.value = appLockManager.isLockEnabled()
        }
        viewModelScope.launch {
            settingsRepository.observeAppName().collect {
                _appName.value = it
            }
        }
    }

    fun unlock() {
        _locked.value = false
    }
}

// ═══════════════════════════════════════════════════════════
//              shouldHideBottomNav
// ═══════════════════════════════════════════════════════════

private fun shouldHideBottomNav(route: String?): Boolean {
    if (route.isNullOrBlank()) return false

    return when {
        route.startsWith(Destinations.STATEMENT) -> true
        route.startsWith(Destinations.RECEIPT) -> true
        route.startsWith(Destinations.RESTORE) -> true
        route == Destinations.ADMIN_LOGIN -> true
        route == Destinations.ADMIN_PANEL -> true
        else -> false
    }
}

// ═══════════════════════════════════════════════════════════
//              DaftarAppRoot
// ═══════════════════════════════════════════════════════════

@Composable
fun DaftarAppRoot(
    incomingFileUri: Uri? = null,
    onFileConsumed: () -> Unit = {},
    lockViewModel: AppLockViewModel = hiltViewModel(),
    licenseViewModel: ActivationViewModel = hiltViewModel(),
    trialCheckViewModel: TrialCheckViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp

    val windowSize = when {
        widthDp < 600.dp -> WindowSize.COMPACT
        widthDp < 840.dp -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
    val orientation = if (widthDp > heightDp) WindowOrientation.LANDSCAPE
    else WindowOrientation.PORTRAIT
    val deviceType = when {
        widthDp < 600.dp -> DeviceType.PHONE
        widthDp < 840.dp -> DeviceType.FOLDABLE
        else -> DeviceType.TABLET
    }

    val tokens = remember(windowSize, orientation, deviceType) {
        adaptiveTokensFor(windowSize, orientation, deviceType)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalAdaptiveTokens provides tokens
    ) {

        RequestNotificationPermission()
        RequestStoragePermission()

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // ⭐ صلاحية التخزين
        var needsStoragePermission by remember {
            mutableStateOf(StoragePermissionManager.needsManagePermission())
        }

        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(
                Lifecycle.State.RESUMED
            ) {
                needsStoragePermission =
                    StoragePermissionManager.needsManagePermission()
            }
        }

        var showOnboarding by remember {
            mutableStateOf(!OnboardingPrefs.isCompleted(context))
        }

        if (showOnboarding) {
            OnboardingScreen(
                onFinish = {
                    OnboardingPrefs.setCompleted(context, true)
                    showOnboarding = false
                }
            )
            return@CompositionLocalProvider
        }

        val locked by lockViewModel.locked.collectAsState()

        if (locked && !DevConfig.BYPASS_APP_LOCK) {
            LockScreen(
                appLockManager = lockViewModel.appLockManager,
                onUnlock = { lockViewModel.unlock() }
            )
            return@CompositionLocalProvider
        }

        val navController = rememberNavController()

        // ⭐ حالة الترخيص
        val licenseState by licenseViewModel.state.collectAsState()

        // ⭐ حالة إعادة التثبيت
        val wasReinstalled by trialCheckViewModel.wasReinstalled.collectAsState()
        val remainingDays by trialCheckViewModel.remainingDays.collectAsState()

        // ═══════════════════════════════════════════════════════
        //  ⭐⭐⭐ بانر إعادة التثبيت — مع rememberSaveable
        // ═══════════════════════════════════════════════════════
        var showReinstallBanner by rememberSaveable { mutableStateOf(false) }
        var bannerHandled by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(wasReinstalled) {
            if (wasReinstalled && !bannerHandled && !DevConfig.BYPASS_LICENSE) {
                delay(500)
                showReinstallBanner = true
                bannerHandled = true
            }
        }

        // ═══════════════════════════════════════════════════════
        //  ⭐ فحص دوري
        // ═══════════════════════════════════════════════════════
        if (!DevConfig.BYPASS_LICENSE) {
            LaunchedEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.repeatOnLifecycle(
                    Lifecycle.State.RESUMED
                ) {
                    delay(300)
                    licenseViewModel.refresh()
                    trialCheckViewModel.refresh()
                }
            }
        }

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val hideNav = shouldHideBottomNav(currentRoute)

        LaunchedEffect(incomingFileUri) {
            incomingFileUri?.let { uri ->
                navController.navigate(Destinations.restore(uri.toString())) {
                    launchSingleTop = true
                }
                onFileConsumed()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                if (!DevConfig.BYPASS_LICENSE) {
                    SubscriptionBanner(
                        status = licenseState.status,
                        onActivateClick = {
                            navController.navigate(Destinations.ACTIVATION)
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(bottom = if (hideNav) 0.dp else 120.dp)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    val safeContentWidth: Dp = if (
                        tokens.maxContentWidth != Dp.Unspecified &&
                        !tokens.maxContentWidth.value.isNaN() &&
                        tokens.maxContentWidth.value > 0f
                    ) tokens.maxContentWidth else 720.dp

                    ContentContainer(maxWidth = safeContentWidth) {
                        DaftarNavHost(navController = navController)
                    }
                }
            }

            AnimatedVisibility(
                visible = !hideNav,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingBottomNav(navController = navController)
            }

            // ⭐⭐⭐ بطاقة الصلاحية
            if (!DevConfig.BYPASS_LICENSE && needsStoragePermission) {
                StoragePermissionCard(
                    onRequestPermission = {
                        StoragePermissionManager.requestManagePermission(context)
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .zIndex(95f)
                )
            }

            // ⭐⭐⭐ بانر إعادة التثبيت
            if (!DevConfig.BYPASS_LICENSE && showReinstallBanner) {
                ReinstallBanner(
                    visible = showReinstallBanner,
                    remainingDays = remainingDays,
                    onDismiss = {
                        showReinstallBanner = false
                        trialCheckViewModel.clearResetFlag()
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .zIndex(100f)
                )
            }

            // ⭐⭐⭐ Subscription Lock Overlay
            val isOnActivationScreen = currentRoute == Destinations.ACTIVATION

            if (!DevConfig.BYPASS_LICENSE && !isOnActivationScreen) {
                SubscriptionLockOverlay(
                    status = licenseState.status,
                    deviceFingerprint = licenseState.deviceFingerprint,
                    onActivateClick = {
                        navController.navigate(Destinations.ACTIVATION)
                    },
                    wasReinstalled = wasReinstalled
                )
            }

            UpdateCheckerHost()

            // ⭐⭐⭐ لا يوجد Salam — تم الحذف نهائياً
        }
    }
}