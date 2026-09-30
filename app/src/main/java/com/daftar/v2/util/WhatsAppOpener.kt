package com.daftar.v2.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppOpener {
    fun openWhatsApp(context: Context, phone: String, message: String) {
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        val url = if (cleaned.isNotEmpty()) {
            "https://wa.me/$cleaned?text=${Uri.encode(message)}"
        } else {
            "https://wa.me/?text=${Uri.encode(message)}"
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}