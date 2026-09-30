package com.daftar.v2.cloud

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daftar.v2.notification.CloudBackupNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CloudBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cloudManager: CloudBackupManager,
    private val notificationHelper: CloudBackupNotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daftar_cloud_backup"
        private const val TAG = "CloudBackupWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val mode = cloudManager.getMode()

            // ⭐ معطل
            if (mode == BackupMode.OFF) {
                Log.d(TAG, "⏸️ معطل")
                return Result.success()
            }

            // ⭐ لا يوجد إذن مسبق
            if (!cloudManager.hasConsent()) {
                Log.d(TAG, "⏸️ لم يمنح المستخدم الإذن")
                return Result.success()
            }

            val password = cloudManager.getPassword()
            if (password.isNullOrBlank() || password.length < 6) {
                Log.w(TAG, "⚠️ لا توجد كلمة مرور صالحة")
                return Result.success()
            }

            if (cloudManager.getFolderUri() == null) {
                Log.w(TAG, "⚠️ لا يوجد مجلد")
                return Result.success()
            }

            when (mode) {
                BackupMode.AUTO -> {
                    // ⭐ نسخ مباشر
                    Log.d(TAG, "🟢 نسخ تلقائي")
                    val success = cloudManager.performBackup(password)
                    if (success) {
                        notificationHelper.showSuccessNotification()
                        Result.success()
                    } else {
                        Result.retry()
                    }
                }

                BackupMode.ASK_EACH_TIME -> {
                    // ⭐ اطلب الإذن
                    Log.d(TAG, "🟡 طلب إذن قبل النسخ")
                    notificationHelper.showPermissionNotification()
                    Result.success()
                }

                BackupMode.OFF -> Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ خطأ: ${e.message}", e)
            Result.retry()
        }
    }
}