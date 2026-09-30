package com.daftar.v2.update

import com.google.gson.annotations.SerializedName

/**
 * ⭐ معلومات التحديث — تُقرأ من version.json على GitHub.
 */
data class UpdateInfo(
    @SerializedName("latest_version_code")
    val latestVersionCode: Int,

    @SerializedName("latest_version_name")
    val latestVersionName: String,

    @SerializedName("min_supported_version_code")
    val minSupportedVersionCode: Int = 1,

    @SerializedName("download_url")
    val downloadUrl: String,

    @SerializedName("release_notes")
    val releaseNotes: List<String> = emptyList(),

    @SerializedName("mandatory")
    val mandatory: Boolean = false,

    @SerializedName("released_at")
    val releasedAt: String = "",

    @SerializedName("apk_size_mb")
    val apkSizeMb: Double = 0.0
)