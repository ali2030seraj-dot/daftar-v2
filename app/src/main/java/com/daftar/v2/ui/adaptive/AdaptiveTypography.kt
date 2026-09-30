package com.daftar.v2.ui.adaptive

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.daftar.v2.ui.theme.CairoFontFamily

fun adaptiveTypography(tokens: AdaptiveTokens): Typography {
    val display = tokens.displaySize.value
    val headline = tokens.headlineSize.value
    val title = tokens.titleSize.value
    val body = tokens.bodySize.value
    val label = tokens.labelSize.value

    return Typography(
        // ⭐ عائلة Display — للعناوين الضخمة
        displayLarge = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (display + 6).sp,
            lineHeight = (display + 14).sp
        ),
        displayMedium = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = display.sp,
            lineHeight = (display + 8).sp
        ),

        // ⭐ عائلة Headline — عناوين رئيسية
        headlineLarge = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = headline.sp,
            lineHeight = (headline + 8).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (headline - 2).sp,
            lineHeight = (headline + 6).sp
        ),

        // ⭐ عائلة Title — عناوين فرعية
        titleLarge = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,        // ← من SemiBold
            fontSize = (title + 2).sp,
            lineHeight = (title + 10).sp
        ),
        titleMedium = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,    // ← من SemiBold (يبقى)
            fontSize = title.sp,
            lineHeight = (title + 8).sp
        ),

        // ⭐ عائلة Body — النصوص العادية
        bodyLarge = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = body.sp,
            lineHeight = (body + 7).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (body - 1).sp,
            lineHeight = (body + 6).sp
        ),
        bodySmall = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Medium,      // ← من Normal → Medium (أوضح)
            fontSize = label.sp,
            lineHeight = (label + 6).sp
        ),

        // ⭐ عائلة Label — التسميات الصغيرة
        labelLarge = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (label + 1).sp,
            lineHeight = (label + 6).sp
        ),
        labelMedium = TextStyle(
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,    // ← من Medium → SemiBold
            fontSize = label.sp,
            lineHeight = (label + 4).sp
        )
    )
}