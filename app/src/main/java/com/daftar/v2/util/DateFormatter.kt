package com.daftar.v2.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val gregorianFormat = SimpleDateFormat("d MMM yyyy", Locale("ar"))
    private val timeFormat = SimpleDateFormat("h:mm a", Locale("ar"))
    private val dayNameFormat = SimpleDateFormat("EEEE", Locale("ar"))
    private val dayNameEnFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)

    /**
     * ✅ تاريخ ميلادي فقط (يستخدمها BackupExporter)
     * "16 سبتمبر 2026"
     */
    fun date(timestamp: Long): String {
        return gregorianFormat.format(Date(timestamp))
    }

    /**
     * تاريخ ميلادي فقط: "16 سبتمبر 2026"
     */
    fun gregorianLabel(timestamp: Long): String {
        return gregorianFormat.format(Date(timestamp))
    }

    /**
     * تاريخ ميلادي + وقت: "16 سبتمبر 2026 - 8:35 م"
     */
    fun fullLabel(timestamp: Long): String {
        val date = gregorianFormat.format(Date(timestamp))
        val time = timeFormat.format(Date(timestamp))
        return "$date - $time"
    }

    /**
     * تاريخ ميلادي + هجري: "16 سبتمبر 2026 م • 3 ربيع الأول 1448 هـ"
     */
    fun dualLabel(timestamp: Long): String {
        val greg = gregorianFormat.format(Date(timestamp))
        return try {
            val hijri = HijriDateUtils.hijriLabel(timestamp)
            "$greg م • $hijri"
        } catch (e: Exception) {
            greg
        }
    }

    /**
     * تاريخ ووقت + هجري: "16 سبتمبر 2026 - 8:35 م • 3 ربيع الأول 1448 هـ"
     */
    fun fullDualLabel(timestamp: Long): String {
        val greg = fullLabel(timestamp)
        return try {
            val hijri = HijriDateUtils.hijriLabel(timestamp)
            "$greg • $hijri"
        } catch (e: Exception) {
            greg
        }
    }

    /**
     * ✅ اسم اليوم بالعربية (يستخدمها BackupExporter)
     */
    fun dayName(timestamp: Long): String {
        return try {
            dayNameFormat.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * اسم اليوم بالإنجليزية
     */
    fun dayNameEn(timestamp: Long): String {
        return try {
            dayNameEnFormat.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * تاريخ بصيغة اسم الملف: "2026-09-17"
     */
    fun fileNameDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(timestamp))
    }
}