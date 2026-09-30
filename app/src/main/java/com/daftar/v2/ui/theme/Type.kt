package com.daftar.v2.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.daftar.v2.R

/**
 * ⭐ Cairo Font Family — النسخة الصحيحة.
 *
 * - Normal → cairo_regular (كما كان)
 * - Medium → cairo_semibold
 * - SemiBold → cairo_semibold
 * - Bold → cairo_bold
 * - ExtraBold / Black → cairo_bold
 *
 * ⚠️ السبب: cairo_semibold أعرض من cairo_regular،
 * مما يجعل النص يخرج عن مكانه في الواجهات والـ PDF.
 */
val CairoFontFamily = FontFamily(
    // ⭐ Normal → Regular (كما كان)
    Font(R.font.cairo_regular, FontWeight.Normal),

    // ⭐ Medium → SemiBold (للتحسين دون كسر)
    Font(R.font.cairo_semibold, FontWeight.Medium),

    // ⭐ SemiBold → SemiBold
    Font(R.font.cairo_semibold, FontWeight.SemiBold),

    // ⭐ Bold → Bold
    Font(R.font.cairo_bold, FontWeight.Bold),

    // ⭐ ExtraBold → Bold (بديل)
    Font(R.font.cairo_bold, FontWeight.ExtraBold),

    // ⭐ Black → Bold (بديل)
    Font(R.font.cairo_bold, FontWeight.Black)
)