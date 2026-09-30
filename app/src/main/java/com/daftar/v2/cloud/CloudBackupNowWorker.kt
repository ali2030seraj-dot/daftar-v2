package com.daftar.v2.cloud

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daftar.v2.notification.CloudBackupNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * ⭐ ينفذ النسخ الفوري بعد موافقة المستخدم.
 */
@HiltWorker
class CloudBackupNowWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cloudManager: CloudBackupManager,
    private val notificationHelper: CloudBackupNotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CloudBackupNow"
    }

    override suspend fun doWork(): Result {
        return try {
            val password = cloudManager.getPassword()
            if (password.isNullOrBlank() || password.length < 6) {
                return Result.failure()
            }

            val success = cloudManager.performBackup(password)
            if (success) {
                notificationHelper.showSuccessNotification()
                Log.d(TAG, "✅ نجح النسخ بعد الموافقة")
                Result.success()
            } else {
                Log.w(TAG, "⚠️ فشل النسخ")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ خطأ: ${e.message}", e)
            Result.failure()
        }
    }
}