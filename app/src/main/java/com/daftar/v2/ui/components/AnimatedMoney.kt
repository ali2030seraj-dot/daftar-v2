package com.daftar.v2.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.daftar.v2.util.MoneyFormatter

@Composable
fun AnimatedMoney(
    cents: Long,
    currency: String,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold,
    durationMillis: Int = 800
) {
    var target by remember { mutableFloatStateOf(0f) }
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis),
        label = "moneyAnim"
    )
    LaunchedEffect(cents) { target = cents.toFloat() }

    Text(
        text = MoneyFormatter.format(animated.toLong(), currency),
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}