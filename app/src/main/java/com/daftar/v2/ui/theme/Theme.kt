package com.daftar.v2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.daftar.v2.ui.adaptive.DeviceType
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.adaptive.WindowOrientation
import com.daftar.v2.ui.adaptive.WindowSize
import com.daftar.v2.ui.adaptive.adaptiveTokensFor
import com.daftar.v2.ui.adaptive.adaptiveTypography

// ═══════════════════════════════════════════════════════════
//              ⭐ الوضع الفاتح — تحسين بسيط
// ═══════════════════════════════════════════════════════════

private val LightColors = lightColorScheme(
    primary = Color(0xFF172554),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFFD4A84B),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFFF4D97A),
    onSecondaryContainer = Color(0xFF0F172A),

    tertiary = Color(0xFFD4A84B),
    onTertiary = Color(0xFF0F172A),

    background = Color(0xFFF6F7F9),
    onBackground = Color(0xFF0F172A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),

    surfaceVariant = Color(0xFFF1F3F5),
    // ⭐ تحسين بسيط: أغمق قليلاً (كان 0xFF334155)
    onSurfaceVariant = Color(0xFF2A3548),

    outline = Color(0xFFB8C2CF),              // ⭐ أغمق قليلاً
    outlineVariant = Color(0xFFD5DBE3),

    error = Color(0xFFDC2626),
    onError = Color.White
)

// ═══════════════════════════════════════════════════════════
//              🌙 الوضع الداكن — محفوظ 100%
// ═══════════════════════════════════════════════════════════

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD4A84B),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFFF4D97A),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFD4A84B),
    onTertiary = Color(0xFF0F172A),

    background = Color(0xFF0B1120),
    onBackground = Color(0xFFF1F5F9),

    surface = Color(0xFF151E2E),
    onSurface = Color(0xFFF1F5F9),

    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFB4BCC8),

    outline = Color(0xFF2D3748),
    outlineVariant = Color(0xFF1E293B),

    error = Color(0xFFF87171),
    onError = Color(0xFF0F172A)
)

// ═══════════════════════════════════════════════════════════
//              ⭐ DaftarTheme
// ═══════════════════════════════════════════════════════════

@Composable
fun DaftarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
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
    val typography = remember(tokens) { adaptiveTypography(tokens) }

    CompositionLocalProvider(LocalAdaptiveTokens provides tokens) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = typography,
            content = content
        )
    }
}