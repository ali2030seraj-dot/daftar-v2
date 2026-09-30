package com.daftar.v2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daftar.v2.R

@Composable
fun CurrencyFlag(
    code: String,
    size: Dp = 44.dp,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val flagRes = flagResourceFor(code)

    val borderColor = if (isSelected) Color(0xFF1E40AF)
    else Color.Black.copy(alpha = 0.10f)
    val borderWidth = if (isSelected) 3.dp else 1.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = flagRes),
            contentDescription = code,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

private fun flagResourceFor(code: String): Int = when (code.uppercase()) {
    "YER" -> R.drawable.flag_yer
    "SAR" -> R.drawable.flag_sar
    "USD" -> R.drawable.flag_usd
    "AED" -> R.drawable.flag_aed
    "EGP" -> R.drawable.flag_egp
    else -> R.drawable.flag_yer
}