package com.daftar.v2.ui.license

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * ⭐ بانر ودّي يظهر عند إعادة تثبيت التطبيق.
 *
 * ✅ تنسيق نظيف:
 *  - أيقونة دائرية بخلفية ناعمة
 *  - نص محاذى لليمين (RTL)
 *  - زر إغلاق واضح
 *  - ظل خفيف
 */
@Composable
fun ReinstallBanner(
    visible: Boolean,
    remainingDays: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ⭐ يختفي تلقائياً بعد 8 ثوانٍ
    LaunchedEffect(visible) {
        if (visible) {
            delay(8_000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFFF8E1),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ⭐ أيقونة دائرية
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE082)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // ⭐ النصوص
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "مرحباً بعودتك",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBF360C),
                        fontFamily = FontFamily.Default,
                        textAlign = TextAlign.Right,
                        style = TextStyle(
                            textDirection = TextDirection.Rtl
                        )
                    )
                    Text(
                        text = "الفترة التجريبية مستمرة — متبقي $remainingDays يوم",
                        fontSize = 13.sp,
                        color = Color(0xFF6D4C41),
                        fontFamily = FontFamily.Default,
                        textAlign = TextAlign.Right,
                        lineHeight = 18.sp,
                        style = TextStyle(
                            textDirection = TextDirection.Rtl
                        )
                    )
                }

                Spacer(Modifier.width(4.dp))

                // ⭐ زر إغلاق
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = Color(0xFF6D4C41),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}