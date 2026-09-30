package com.daftar.v2.pdf

import android.graphics.Bitmap

/**
 * بيانات العلامة التجارية لـ PDF.
 */
data class PdfBranding(
    val logoBitmap: Bitmap? = null,
    val storeName: String = "دفتر حساباتي",
    val storePhone: String = "",
    val storeAddress: String = ""
)