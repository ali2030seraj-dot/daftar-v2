package com.daftar.v2.util

/**
 * ⭐ المرجع المركزي لكل تعاملات العملة.
 */
object CurrencyUtils {

    // ⭐ الأكواد المدعومة
    val ALL_CODES = listOf("YER", "SAR", "OMR", "AED", "USD", "EGP")
    const val DEFAULT_CODE = "YER"

    fun arabicFullName(raw: String?): String = when (toCode(raw)) {
        "YER" -> "ريال يمني"
        "SAR" -> "ريال سعودي"
        "OMR" -> "ريال عماني"
        "AED" -> "درهم إماراتي"
        "USD" -> "دولار أمريكي"
        "EGP" -> "جنيه مصري"
        else  -> raw?.trim()?.ifBlank { "غير محدد" } ?: "غير محدد"
    }

    fun arabicShortName(raw: String?): String = when (toCode(raw)) {
        "YER" -> "يمني"
        "SAR" -> "سعودي"
        "OMR" -> "عماني"
        "AED" -> "إماراتي"
        "USD" -> "دولار"
        "EGP" -> "مصري"
        else  -> raw?.trim() ?: ""
    }

    fun arabicSymbol(raw: String?): String = when (toCode(raw)) {
        "YER" -> "ر.ي"
        "SAR" -> "ر.س"
        "OMR" -> "ر.ع"
        "AED" -> "د.إ"
        "USD" -> "$"
        "EGP" -> "ج.م"
        else  -> ""
    }

    fun toCode(raw: String?): String {
        if (raw.isNullOrBlank()) return DEFAULT_CODE
        val t = raw.trim()
        return when {
            t.equals("YER", ignoreCase = true) -> "YER"
            t.equals("SAR", ignoreCase = true) -> "SAR"
            t.equals("OMR", ignoreCase = true) -> "OMR"
            t.equals("AED", ignoreCase = true) -> "AED"
            t.equals("USD", ignoreCase = true) -> "USD"
            t.equals("EGP", ignoreCase = true) -> "EGP"

            t.contains("يمني") || t.contains("اليمن") -> "YER"
            t.contains("سعودي") || t.contains("السعودية") -> "SAR"
            t.contains("عماني") || t.contains("عُمان") || t.contains("عمان") -> "OMR"
            t.contains("إماراتي") || t.contains("اماراتي") || t.contains("درهم") -> "AED"
            t.contains("دولار") || t.contains("أمريكي") || t.contains("امريكي") -> "USD"
            t.contains("مصري") || t.contains("مصر") || t.contains("جنيه") -> "EGP"

            else -> t.uppercase()
        }
    }
}