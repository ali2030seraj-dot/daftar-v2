package com.daftar.v2.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.daftar.v2.util.MoneyFormatter

@Composable
fun MoneyText(
    cents: Long,
    currency: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Text(
        text = MoneyFormatter.format(cents, currency),
        color = color,
        fontWeight = fontWeight,
        style = style
    )
}