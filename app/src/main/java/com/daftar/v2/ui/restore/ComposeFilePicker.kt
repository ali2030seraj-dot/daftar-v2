package com.daftar.v2.ui.restore

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * دالة مساعدة تفتح منتقي الملفات (SAF) وتُرجع Uri المختار.
 *
 * الميزات:
 * - لا تحتاج أي صلاحيات
 * - المستخدم يختار من الجهاز أو Google Drive أو أي مزود آخر
 * - تقبل جميع أنواع الملفات (لاستقبال .daftar بأي mimeType)
 */
@Composable
fun rememberLauncherForActivityResultSafe(
    onResult: (Uri?) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        onResult(uri)
    }
    return {
        launcher.launch(
            arrayOf(
                "application/octet-stream",
                "application/json",
                "application/*",
                "*/*"
            )
        )
    }
}