package com.daftar.v2.ui.adaptive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DaftarWindowInfo(
    val windowSize: WindowSize,
    val orientation: WindowOrientation,
    val deviceType: DeviceType,
    val widthDp: Dp,
    val heightDp: Dp,
    val isLandscape: Boolean,
    val hasCutout: Boolean
)

@Composable
fun rememberDaftarWindowInfo(
    windowSizeClass: WindowSizeClass
): DaftarWindowInfo {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp

    val windowSize = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> WindowSize.COMPACT
        WindowWidthSizeClass.Medium -> WindowSize.MEDIUM
        WindowWidthSizeClass.Expanded -> WindowSize.EXPANDED
        else -> windowSizeForWidth(widthDp)
    }

    val orientation = if (widthDp > heightDp) WindowOrientation.LANDSCAPE
    else WindowOrientation.PORTRAIT

    val deviceType = when {
        widthDp < 600.dp -> DeviceType.PHONE
        widthDp < 840.dp -> DeviceType.FOLDABLE
        else -> DeviceType.TABLET
    }

    return remember(windowSizeClass, widthDp, heightDp) {
        DaftarWindowInfo(
            windowSize = windowSize,
            orientation = orientation,
            deviceType = deviceType,
            widthDp = widthDp,
            heightDp = heightDp,
            isLandscape = orientation == WindowOrientation.LANDSCAPE,
            hasCutout = configuration.screenHeightDp > configuration.screenWidthDp
        )
    }
}