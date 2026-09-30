package com.daftar.v2.pdf

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object PdfStorageHelper {

    private const val TAG = "PdfStorageHelper"
    private const val FOLDER_NAME = "دفتر حساباتي"

    data class SaveResult(
        val success: Boolean,
        val location: String?,
        val uri: Uri? = null,
        val error: String? = null
    )

    fun savePdfToDownloads(
        context: Context,
        sourceFile: File,
        displayName: String
    ): SaveResult {

        // 1) تحقق من الملف المصدر
        if (!sourceFile.exists()) {
            Log.e(TAG, "الملف المؤقت غير موجود: ${sourceFile.absolutePath}")
            return SaveResult(false, null, null, "الملف المؤقت غير موجود")
        }
        if (sourceFile.length() == 0L) {
            Log.e(TAG, "الملف المؤقت فارغ: ${sourceFile.absolutePath}")
            return SaveResult(false, null, null, "الملف المؤقت فارغ")
        }
        Log.d(TAG, "بدء الحفظ: ${sourceFile.absolutePath} (${sourceFile.length()} bytes)")

        // 2) تأكد من الامتداد
        val name = if (displayName.endsWith(".pdf", ignoreCase = true))
            displayName else "$displayName.pdf"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(context, sourceFile, name)
                    .let { if (it.success) it else saveWithMediaStoreFiles(context, sourceFile, name) }
            } else {
                saveWithLegacy(context, sourceFile, name)
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل الحفظ", e)
            SaveResult(false, null, null, e.message ?: "فشل الحفظ")
        }
    }

    // ====== MediaStore.Downloads (المسار الأساسي على Q+) ======
    private fun saveWithMediaStore(
        context: Context,
        sourceFile: File,
        displayName: String
    ): SaveResult {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER_NAME"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        val uri: Uri = try {
            resolver.insert(collection, values)
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore.Downloads insert فشل", e)
            null
        } ?: return SaveResult(false, null, null, "لم نستطع إنشاء الملف في التنزيلات")

        Log.d(TAG, "تم إنشاء URI: $uri")

        return try {
            resolver.openOutputStream(uri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: run {
                resolver.delete(uri, null, null)
                return SaveResult(false, null, null, "فشل فتح الملف للكتابة")
            }

            // إزالة pending
            val done = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(uri, done, null, null)

            Log.d(TAG, "تم الحفظ بنجاح: $uri")
            SaveResult(
                success = true,
                location = "التنزيلات/$FOLDER_NAME/$displayName",
                uri = uri
            )
        } catch (e: Exception) {
            Log.e(TAG, "فشل الكتابة، حذف الـ URI المعلّق", e)
            try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            SaveResult(false, null, null, e.message ?: "فشل الكتابة")
        }
    }

    // ====== Fallback: MediaStore.Files (لبعض أجهزة MIUI) ======
    private fun saveWithMediaStoreFiles(
        context: Context,
        sourceFile: File,
        displayName: String
    ): SaveResult {
        Log.w(TAG, "محاولة fallback عبر MediaStore.Files")
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER_NAME"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val uri = try {
            resolver.insert(collection, values)
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore.Files insert فشل", e)
            null
        } ?: return SaveResult(false, null, null, "فشل fallback أيضًا")

        return try {
            resolver.openOutputStream(uri)?.use { output ->
                sourceFile.inputStream().use { input -> input.copyTo(output) }
            }
            val done = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            resolver.update(uri, done, null, null)
            SaveResult(true, "التنزيلات/$FOLDER_NAME/$displayName", uri)
        } catch (e: Exception) {
            try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            SaveResult(false, null, null, e.message)
        }
    }

    // ====== Android 9 وأقل ======
    @Suppress("DEPRECATION")
    private fun saveWithLegacy(
        context: Context,
        sourceFile: File,
        displayName: String
    ): SaveResult {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val targetDir = File(downloadsDir, FOLDER_NAME)
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return SaveResult(false, null, null, "لم نستطع إنشاء المجلد")
        }
        val targetFile = File(targetDir, displayName)
        FileOutputStream(targetFile).use { output ->
            sourceFile.inputStream().use { input -> input.copyTo(output) }
        }
        return SaveResult(
            success = true,
            location = targetFile.absolutePath,
            uri = Uri.fromFile(targetFile)
        )
    }

    fun buildFileName(personName: String): String {
        val safeName = personName
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), "")
            .trim()
            .replace(" ", "_")
            .ifBlank { "unknown" }
        val timestamp = System.currentTimeMillis()
        return "statement_${safeName}_$timestamp.pdf"
    }
}