package com.daftar.v2.ui.adaptive

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSize(val label: String) {
    COMPACT("هاتف صغير"),
    MEDIUM("هاتف كبير"),
    EXPANDED("جهاز لوحي")
}

object WindowSizeDefaults {
    val CompactMaxWidth: Dp = 600.dp
    val MediumMaxWidth: Dp = 840.dp
}

fun windowSizeForWidth(width: Dp): WindowSize = when {
    width < WindowSizeDefaults.CompactMaxWidth -> WindowSize.COMPACT
    width < WindowSizeDefaults.MediumMaxWidth -> WindowSize.MEDIUM
    else -> WindowSize.EXPANDED
}

enum class WindowOrientation {
    PORTRAIT,
    LANDSCAPE
}

enum class DeviceType {
    PHONE,
    TABLET,
    FOLDABLE
}