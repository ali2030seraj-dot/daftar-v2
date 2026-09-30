package com.daftar.v2.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ⭐ أيقونة "طائرة" احترافية بنمط Financial App.
 *
 * الميزات:
 * - ظل ملوّن قوي (12dp) يجعل الزر يطفو
 * - دائرة داخلية بـ gradient بلون الأيقونة
 * - أيقونة بيضاء نقية على الدائرة
 * - بدون حدود (نظيف)
 * - تأثير ضغط (scale + elevation)
 * - ⭐ النص Bold واضح (FontWeight.Black + 12sp)
 */
@Composable
fun JiibIconTile(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // ⭐ الحاوية البيضاء
    val containerColor = if (isDark) {
        Color(0xFF1A2332)
    } else {
        Color(0xFFFFFFFF)
    }

    // ⭐ لون الأيقونة الفعلي (للدائرة الداخلية)
    val effectiveColor = if (isDark) lighten(accentColor, 0.15f) else accentColor

    // ⭐ لون النص — أغمق للوضوح
    val labelColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)

    // ⭐ تأثير الضغط
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(120),
        label = "scale"
    )
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 4f else 12f,
        animationSpec = tween(120),
        label = "elevation"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ⭐ الحاوية الطائرة
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = elevation.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = effectiveColor.copy(alpha = 0.40f),
                    spotColor = effectiveColor.copy(alpha = 0.50f),
                    clip = false
                )
                .clip(RoundedCornerShape(24.dp))
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            // ⭐ الدائرة الداخلية بـ gradient
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                effectiveColor,
                                effectiveColor.copy(alpha = 0.82f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ⭐⭐⭐ النص Bold واضح
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            letterSpacing = 0.2.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════
//              Helpers
// ═══════════════════════════════════════════════════════════

private fun Color.luminance(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue

private fun lighten(color: Color, factor: Float): Color {
    val r = (color.red + (1f - color.red) * factor).coerceIn(0f, 1f)
    val g = (color.green + (1f - color.green) * factor).coerceIn(0f, 1f)
    val b = (color.blue + (1f - color.blue) * factor).coerceIn(0f, 1f)
    return Color(r, g, b, color.alpha)
}