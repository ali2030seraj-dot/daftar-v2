package com.daftar.v2.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

sealed class ImportResult {
    data class Success(val plainJson: String) : ImportResult()
    data class Failure(val message: String) : ImportResult()
}

@Singleton
class BackupImporter @Inject constructor() {

    companion object {
        private const val TAG = "BackupImporter"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * ⭐ يستورد نسخة احتياطية من Uri (ملف محلي).
     */
    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        password: String
    ): ImportResult {
        return try {
            Log.d(TAG, "📥 بدء الاسترداد من: $uri")

            val content = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
            } ?: return ImportResult.Failure("❌ فشل فتح الملف")

            Log.d(TAG, "📄 حجم الملف: ${content.length} حرف")

            importFromContent(content, password)

        } catch (e: Exception) {
            Log.e(TAG, "❌ استثناء: ${e.message}", e)
            ImportResult.Failure("❌ خطأ غير متوقع: ${e.message}")
        }
    }

    /**
     * ⭐⭐⭐ يستورد من محتوى نصي مباشرة (للاسترداد من السحابة).
     *
     * يُستخدم عند الاسترداد من ملف سحابي (Google Drive / Dropbox).
     */
    suspend fun importFromContent(
        content: String,
        password: String
    ): ImportResult {
        return try {
            // ⭐ 1) تحليل JSON
            val encrypted: BackupCrypto.EncryptedData = try {
                json.decodeFromString(content)
            } catch (e: Exception) {
                Log.e(TAG, "❌ فشل تحليل JSON: ${e.message}", e)
                return ImportResult.Failure("❌ الملف غير صالح: ${e.message}")
            }

            // ⭐ 2) التحقق من الصيغة
            if (encrypted.format != "daftar-backup-encrypted-v1") {
                return ImportResult.Failure("❌ صيغة غير مدعومة: ${encrypted.format}")
            }

            // ⭐ 3) فك التشفير
            val plaintext = try {
                BackupCrypto.decrypt(encrypted, password)
            } catch (e: javax.crypto.AEADBadTagException) {
                return ImportResult.Failure("❌ كلمة المرور غير صحيحة")
            } catch (e: Exception) {
                return ImportResult.Failure("❌ فشل فك التشفير: ${e.message}")
            }

            Log.d(TAG, "✅ تم فك التشفير (${plaintext.length} حرف)")
            ImportResult.Success(plaintext)

        } catch (e: Exception) {
            Log.e(TAG, "❌ استثناء: ${e.message}", e)
            ImportResult.Failure("❌ خطأ غير متوقع: ${e.message}")
        }
    }
}