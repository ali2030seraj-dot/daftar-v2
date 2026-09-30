package com.daftar.v2.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "shimmerValue"
    )

    val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(shimmer * 800f - 400f, 0f),
                    end = Offset(shimmer * 800f, 0f)
                )
            )
    )
}

@Composable
fun DashboardSkeleton() {
    val tokens = LocalAdaptiveTokens.current
    Column(
        Modifier.padding(
            horizontal = tokens.screenPaddingHorizontal,
            vertical = tokens.screenPaddingVertical
        )
    ) {
        SkeletonBox(height = 24.dp, modifier = Modifier.fillMaxWidth(0.5f))
        Spacer(Modifier.height(tokens.cardSpacing))
        SkeletonBox(height = 100.dp)
        Spacer(Modifier.height(tokens.cardSpacing))
        SkeletonBox(height = 80.dp)
        Spacer(Modifier.height(tokens.cardSpacing))
        SkeletonBox(height = 80.dp)
    }
}