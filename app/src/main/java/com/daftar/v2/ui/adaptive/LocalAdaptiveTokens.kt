package com.daftar.v2.ui.adaptive

import androidx.compose.runtime.compositionLocalOf

val LocalAdaptiveTokens = compositionLocalOf<AdaptiveTokens> {
    error("AdaptiveTokens not provided")
}