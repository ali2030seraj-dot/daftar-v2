package com.daftar.v2.util

import java.text.NumberFormat
import java.util.Locale

object MoneyFormatter {

    private val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    /**
     * ⭐ التنسيق الأساسي: الرقم + اسم العملة بالعربي
     * مثال: "1,250.50 ريال يمني"
     */
    fun format(cents: Long, currency: String = ""): String {
        val value = cents / 100.0
        val formatted = formatter.format(value)
        if (currency.isBlank()) return formatted
        val name = CurrencyUtils.arabicFullName(currency)
        return "$formatted $name"
    }

    /** الرقم فقط */
    fun formatShort(cents: Long): String {
        val value = cents / 100.0
        return formatter.format(value)
    }

    /** الرقم + الرمز: "1,250 ر.ي" */
    fun formatWithSymbol(cents: Long, currency: String): String {
        val value = cents / 100.0
        val formatted = formatter.format(value)
        val symbol = CurrencyUtils.arabicSymbol(currency)
        return if (symbol.isBlank()) formatted else "$formatted $symbol"
    }

    fun parseToCents(input: String): Long? {
        val cleaned = input.trim().replace(",", "")
        if (cleaned.isEmpty()) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        if (value < 0) return null
        return Math.round(value * 100)
    }
}