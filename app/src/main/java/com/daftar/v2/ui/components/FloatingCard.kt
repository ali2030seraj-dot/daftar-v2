package com.daftar.v2.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.theme.GlassTokens
import kotlin.math.sin

@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    floating: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalAdaptiveTokens.current
    val finalShape = shape ?: RoundedCornerShape(tokens.cardRadius)
    val finalPadding = contentPadding ?: PaddingValues(tokens.cardPadding)

    val floatOffset = if (floating) {
        val transition = rememberInfiniteTransition(label = "float")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(6000),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )
        sin(phase.toDouble()).toFloat() * GlassTokens.FloatOffset
    } else 0f

    Box(
        modifier = modifier.graphicsLayer { translationY = floatOffset }
    ) {
        GlassCard(
            shape = finalShape,
            onClick = onClick,
            contentPadding = finalPadding,
            content = content
        )
    }
}