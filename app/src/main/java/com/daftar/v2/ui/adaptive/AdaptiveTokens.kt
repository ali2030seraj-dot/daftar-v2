package com.daftar.v2.ui.adaptive

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AdaptiveTokens(
    val windowSize: WindowSize,
    val orientation: WindowOrientation,
    val deviceType: DeviceType,

    // الحشو
    val screenPaddingHorizontal: Dp,
    val screenPaddingVertical: Dp,
    val cardPadding: Dp,
    val cardSpacing: Dp,
    val sectionSpacing: Dp,

    // الحواف
    val cardRadius: Dp,
    val smallRadius: Dp,
    val pillRadius: Dp,

    // أحجام البطاقات
    val maxContentWidth: Dp,
    val mainBalanceCardMinHeight: Dp,
    val compactCardMinHeight: Dp,

    // الشبكة
    val gridColumns: Int,

    // الخطوط
    val displaySize: TextUnit,
    val headlineSize: TextUnit,
    val titleSize: TextUnit,
    val bodySize: TextUnit,
    val labelSize: TextUnit,

    // الأزرار
    val fabSize: Dp,
    val bottomNavHeight: Dp,
    val bottomNavMaxWidth: Dp,
    val iconSize: Dp,

    // الارتفاع
    val cardElevation: Dp,
    val floatingElevation: Dp
)

fun adaptiveTokensFor(
    windowSize: WindowSize,
    orientation: WindowOrientation,
    deviceType: DeviceType
): AdaptiveTokens {
    return when (windowSize) {
        WindowSize.COMPACT -> AdaptiveTokens(
            windowSize = windowSize,
            orientation = orientation,
            deviceType = deviceType,
            screenPaddingHorizontal = 12.dp,
            screenPaddingVertical = 12.dp,
            cardPadding = 14.dp,
            cardSpacing = 10.dp,
            sectionSpacing = 16.dp,
            cardRadius = 20.dp,
            smallRadius = 12.dp,
            pillRadius = 50.dp,
            maxContentWidth = Dp.Unspecified,
            mainBalanceCardMinHeight = 120.dp,
            compactCardMinHeight = 100.dp,
            gridColumns = 1,
            displaySize = 24.sp,
            headlineSize = 20.sp,
            titleSize = 16.sp,
            bodySize = 14.sp,
            labelSize = 12.sp,
            fabSize = 56.dp,
            bottomNavHeight = 60.dp,
            bottomNavMaxWidth = 480.dp,      // ⭐ كان Dp.Unspecified
            iconSize = 22.dp,
            cardElevation = 3.dp,
            floatingElevation = 10.dp
        )

        WindowSize.MEDIUM -> AdaptiveTokens(
            windowSize = windowSize,
            orientation = orientation,
            deviceType = deviceType,
            screenPaddingHorizontal = 16.dp,
            screenPaddingVertical = 16.dp,
            cardPadding = 16.dp,
            cardSpacing = 12.dp,
            sectionSpacing = 20.dp,
            cardRadius = 22.dp,
            smallRadius = 14.dp,
            pillRadius = 50.dp,
            maxContentWidth = 720.dp,
            mainBalanceCardMinHeight = 140.dp,
            compactCardMinHeight = 120.dp,
            gridColumns = 2,
            displaySize = 30.sp,
            headlineSize = 22.sp,
            titleSize = 17.sp,
            bodySize = 15.sp,
            labelSize = 13.sp,
            fabSize = 60.dp,
            bottomNavHeight = 64.dp,
            bottomNavMaxWidth = 600.dp,
            iconSize = 24.dp,
            cardElevation = 4.dp,
            floatingElevation = 12.dp
        )

        WindowSize.EXPANDED -> AdaptiveTokens(
            windowSize = windowSize,
            orientation = orientation,
            deviceType = deviceType,
            screenPaddingHorizontal = 24.dp,
            screenPaddingVertical = 24.dp,
            cardPadding = 20.dp,
            cardSpacing = 16.dp,
            sectionSpacing = 28.dp,
            cardRadius = 24.dp,
            smallRadius = 16.dp,
            pillRadius = 50.dp,
            maxContentWidth = 900.dp,
            mainBalanceCardMinHeight = 160.dp,
            compactCardMinHeight = 140.dp,
            gridColumns = 3,
            displaySize = 34.sp,
            headlineSize = 26.sp,
            titleSize = 18.sp,
            bodySize = 15.sp,
            labelSize = 13.sp,
            fabSize = 64.dp,
            bottomNavHeight = 72.dp,
            bottomNavMaxWidth = 700.dp,
            iconSize = 26.dp,
            cardElevation = 6.dp,
            floatingElevation = 16.dp
        )
    }
}