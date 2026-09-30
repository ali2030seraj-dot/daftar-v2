package com.daftar.v2.update

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ يفحص وجود تحديث جديد من GitHub Releases.
 */
@Singleton
class UpdateChecker @Inject constructor() {

    companion object {
        private const val TAG = "UpdateChecker"

        // ⭐⭐⭐ استبدل YOUR_USERNAME باسم مستخدم GitHub
        // والصق هذا بدلاً منه
private const val VERSION_URL =
    "https://raw.githubusercontent.com/ali2030seraj-dot/daftar-v2/main/version.json"
    }

    private val gson = Gson()

    suspend fun fetchLatestVersion(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(VERSION_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Cache-Control", "no-cache")
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.w(TAG, "HTTP $responseCode — تعذّر جلب version.json")
                    return@withContext null
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val info = gson.fromJson(body, UpdateInfo::class.java)

                Log.d(
                    TAG,
                    "الإصدار الأخير: ${info?.latestVersionName} (${info?.latestVersionCode})"
                )
                info
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل جلب version.json", e)
            null
        }
    }
}