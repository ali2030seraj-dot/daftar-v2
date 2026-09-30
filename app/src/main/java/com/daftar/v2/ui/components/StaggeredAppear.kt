package com.daftar.v2.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import com.daftar.v2.ui.theme.GlassTokens

@Composable
fun StaggeredAppear(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(GlassTokens.AnimationNormal),
        label = "staggerAlpha"
    )
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(GlassTokens.AnimationNormal),
        label = "staggerOffset"
    )

    LaunchedEffect(Unit) {
        delay(index * GlassTokens.StaggerDelay.toLong())
        visible = true
    }

    Box(
        modifier = modifier
            .alpha(alpha)
            .graphicsLayer { translationY = offset }
    ) {
        content()
    }
}