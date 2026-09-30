package com.daftar.v2.util

import android.icu.util.Calendar
import android.icu.util.IslamicCalendar // <-- أضف هذا الاستيراد
import android.icu.util.TimeZone // <-- أضف هذا الاستيراد
import android.icu.util.ULocale

object HijriDateUtils {

    private val HIJRI_MONTHS = arrayOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    data class HijriDate(
        val year: Int,
        val month: Int,
        val day: Int
    ) {
        fun monthName(): String {
            val idx = (month - 1).coerceIn(0, 11)
            return HIJRI_MONTHS[idx]
        }
        override fun toString(): String = "$day ${monthName()} $year هـ"
    }

    // ⭐⭐⭐ هذا هو الحل ⭐⭐⭐
    // استخدام Calendar.getInstance مع ULocale يحتوي على وسم "أم القرى"
    private fun buildCalendar(): Calendar {
        val locale = ULocale("@calendar=islamic-umalqura")
        return Calendar.getInstance(locale)
    }

    fun fromMillis(timestamp: Long): HijriDate {
        return try {
            val cal = buildCalendar()
            cal.timeInMillis = timestamp
            HijriDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        } catch (e: Exception) {
            approximateFromMillis(timestamp)
        }
    }

    private fun approximateFromMillis(timestamp: Long): HijriDate {
        val daysSinceEpoch = timestamp / (1000L * 60L * 60L * 24L)
        val totalDays = daysSinceEpoch + 719162L
        val hijriDays = (totalDays * 33L) / 34L
        val year = (hijriDays / 354.367).toInt() + 1
        val remainder = (hijriDays % 354).toInt()
        val month = (remainder / 29.5).toInt() + 1
        val day = (remainder % 29).toInt() + 1
        return HijriDate(
            year = year.coerceAtLeast(1446),
            month = month.coerceIn(1, 12),
            day = day.coerceIn(1, 30)
        )
    }

    fun hijriLabel(timestamp: Long): String = fromMillis(timestamp).toString()

    fun dualLabel(timestamp: Long, gregorianLabel: String): String =
        "$gregorianLabel • ${hijriLabel(timestamp)}"

    fun hijriMonthName(gregorianYear: Int, gregorianMonth: Int): String {
        val cal = java.util.Calendar.getInstance().apply {
            set(gregorianYear, gregorianMonth - 1, 15, 12, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return fromMillis(cal.timeInMillis).monthName()
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = buildCalendar()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 30)
        val lastDay = cal.get(Calendar.DAY_OF_MONTH)
        return if (lastDay == 30) 30 else 29
    }

    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val cal = buildCalendar()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        return dow - 1
    }

    fun getMonthName(month: Int): String {
        val idx = (month - 1).coerceIn(0, 11)
        return HIJRI_MONTHS[idx]
    }
}