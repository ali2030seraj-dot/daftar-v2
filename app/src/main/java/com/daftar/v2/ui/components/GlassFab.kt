package com.daftar.v2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftar.v2.ui.adaptive.LocalAdaptiveTokens

// ═══════════════════════════════════════════════════════════
//              نموذج الحدث
// ═══════════════════════════════════════════════════════════

data class FabAction(
    val label: String,
    val icon: String,
    val onClick: () -> Unit
)

// ═══════════════════════════════════════════════════════════
//              ⭐ الزر العائم
// ═══════════════════════════════════════════════════════════

/**
 * ⭐ زر عائم مع أزرار فرعية.
 *
 * @param actions       قائمة الأزرار الفرعية
 * @param modifier      مُعدّل من الخارج
 * @param bottomPadding ⭐ المساحة السفلية فوق الشريط السفلي (افتراضي 100dp)
 *                      - ترفع الزر فوق الشريط السفلي تلقائياً
 *                      - يمكن تعديلها حسب الشاشة
 */
@Composable
fun GlassFab(
    actions: List<FabAction>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 100.dp   // ⭐ الإصلاح الجديد — يرفع الزر فوق الشريط
) {
    val tokens = LocalAdaptiveTokens.current
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(300),
        label = "fabRotate"
    )

    // ⭐ الألوان المتكيّفة مع الوضع الفاتح/الداكن
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = bottomPadding)   // ⭐ رفع الزر فوق الشريط السفلي
    ) {
        Column(horizontalAlignment = Alignment.End) {

            // ═══════ الأزرار الفرعية (تظهر عند الضغط) ═══════
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + scaleIn(
                    tween(250),
                    initialScale = 0.7f
                ),
                exit = fadeOut(tween(150)) + scaleOut(
                    tween(150),
                    targetScale = 0.7f
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    actions.forEach { action ->
                        Row(
                            modifier = Modifier
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.15f),
                                    spotColor = Color.Black.copy(alpha = 0.20f)
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .background(surfaceColor)
                                .border(
                                    width = 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    expanded = false
                                    action.onClick()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ⭐ النص بلون واضح في كل الأوضاع
                            Text(
                                text = action.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default,
                                color = onSurfaceColor
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = action.icon,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ═══════ الزر الرئيسي (FAB) ═══════
            Box(
                modifier = Modifier
                    .size(tokens.fabSize)
                    .shadow(
                        elevation = tokens.floatingElevation,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .clickable { expanded = !expanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "إضافة",
                    tint = Color.White,
                    modifier = Modifier
                        .size(tokens.iconSize + 4.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}