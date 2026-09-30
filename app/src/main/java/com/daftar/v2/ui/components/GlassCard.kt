package com.daftar.v2.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens
import com.daftar.v2.ui.theme.GlassTokens

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalAdaptiveTokens.current
    val finalShape = shape ?: RoundedCornerShape(tokens.cardRadius)
    val finalPadding = contentPadding ?: PaddingValues(tokens.cardPadding)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) GlassTokens.PressScale else 1f,
        animationSpec = tween(GlassTokens.AnimationFast),
        label = "glassScale"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.3f &&
            MaterialTheme.colorScheme.background.green < 0.3f

    val surfaceColor = MaterialTheme.colorScheme.surface.copy(
        alpha = if (isDark) GlassTokens.GlassAlphaDark else GlassTokens.GlassAlphaLight
    )
    val borderColor = Color.White.copy(
        alpha = if (isDark) GlassTokens.BorderAlphaDark else GlassTokens.BorderAlphaLight
    )

    // ⭐ الحل: clickable مع indication افتراضي من Material3
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,   // ⭐ نُلغي الـ ripple المخصص
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = tokens.cardElevation + 6.dp,
                shape = finalShape,
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.28f)
            )
            .clip(finalShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor,
                        surfaceColor.copy(alpha = surfaceColor.alpha * 0.88f)
                    )
                )
            )
            .border(BorderStroke(1.2.dp, borderColor), finalShape)
            .then(clickModifier)
            .padding(finalPadding),
        content = content
    )
}