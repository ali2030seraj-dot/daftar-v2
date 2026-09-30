package com.daftar.v2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftar.v2.util.CurrencyUtils

/**
 * ⭐ شريط العملات — بدون أعلام (Chips فقط)
 */
@Composable
fun CurrencyBar(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // ⭐ الأكواد: ["YER", "SAR", "USD", "AED", "EGP"]
    val currencies = CurrencyUtils.ALL_CODES

    // ⭐ نطبّع المختار للتأكد من المقارنة الصحيحة
    val selectedCode = CurrencyUtils.toCode(selected)

    Box(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            currencies.forEach { code ->
                CurrencyChip(
                    code = code,
                    isSelected = code == selectedCode,
                    onClick = { onSelect(code) }
                )
            }
        }
    }
}

@Composable
private fun CurrencyChip(
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = currencyColor(code)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (isSelected) color.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            // ⭐ الاسم بالعربي عبر CurrencyUtils
            text = CurrencyUtils.arabicFullName(code),
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
            fontFamily = FontFamily.Default,
            color = if (isSelected) color
            else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════════════════
//                       Helpers
// ═══════════════════════════════════════════════════════════

private fun currencyColor(code: String): Color = when (CurrencyUtils.toCode(code)) {
    "YER" -> Color(0xFFDC2626)  // أحمر
    "SAR" -> Color(0xFF16A34A)  // أخضر
    "USD" -> Color(0xFF0891B2)  // أزرق
    "AED" -> Color(0xFF7C3AED)  // بنفسجي
    "EGP" -> Color(0xFFF59E0B)  // ذهبي
    else -> Color(0xFF64748B)
}