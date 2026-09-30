package com.daftar.v2.cloud

import android.net.Uri

/**
 * ⭐ معلومات ملف نسخة احتياطية سحابي.
 */
data class CloudFileInfo(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long
) {
    fun sizeLabel(): String {
        return when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> String.format("%.1f MB", sizeBytes / 1024.0 / 1024.0)
        }
    }
}