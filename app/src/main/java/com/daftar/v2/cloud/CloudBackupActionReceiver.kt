package com.daftar.v2.cloud

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.daftar.v2.notification.CloudBackupNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ⭐ يستقبل موافقة/رفض المستخدم من الإشعار.
 */
@AndroidEntryPoint
class CloudBackupActionReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: CloudBackupNotificationHelper

    companion object {
        private const val TAG = "CloudBackupAction"
    }

    override fun onReceive(context: Context, intent: Intent) {
        notificationHelper.cancelPermissionNotification()

        when (intent.action) {
            CloudBackupNotificationHelper.ACTION_APPROVE -> {
                Log.d(TAG, "✅ المستخدم وافق — بدء النسخ")
                // ⭐ نفّذ النسخ الآن
                val request = OneTimeWorkRequestBuilder<CloudBackupNowWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                            .build()
                    )
                    .build()
                WorkManager.getInstance(context).enqueue(request)
            }

            CloudBackupNotificationHelper.ACTION_REJECT -> {
                Log.d(TAG, "❌ المستخدم رفض")
                // لا نفعل شيئاً
            }
        }
    }
}