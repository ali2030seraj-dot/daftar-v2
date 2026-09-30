package com.daftar.v2.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daftar.v2.MainActivity
import com.daftar.v2.R
import com.daftar.v2.cloud.CloudBackupActionReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⭐ إشعارات النسخ الاحتياطي السحابي.
 */
@Singleton
class CloudBackupNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "cloud_backup_channel"
        const val PERMISSION_NOTIF_ID = 8001
        const val SUCCESS_NOTIF_ID = 8002
        const val ACTION_APPROVE = "com.daftar.v2.CLOUD_BACKUP_APPROVE"
        const val ACTION_REJECT = "com.daftar.v2.CLOUD_BACKUP_REJECT"
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "النسخ الاحتياطي السحابي",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات النسخ الاحتياطي إلى السحابة"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * ⭐ إشعار طلب الإذن قبل النسخ.
     */
    fun showPermissionNotification() {
        ensureChannel()

        // ⭐ فتح التطبيق عند الضغط على الإشعار
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ⭐ زر الموافقة
        val approveIntent = Intent(context, CloudBackupActionReceiver::class.java).apply {
            action = ACTION_APPROVE
        }
        val approvePending = PendingIntent.getBroadcast(
            context,
            1,
            approveIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ⭐ زر الرفض
        val rejectIntent = Intent(context, CloudBackupActionReceiver::class.java).apply {
            action = ACTION_REJECT
        }
        val rejectPending = PendingIntent.getBroadcast(
            context,
            2,
            rejectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("☁️ نسخة احتياطية سحابية")
            .setContentText("هل تريد إنشاء نسخة احتياطية الآن إلى السحابة؟")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "هل تريد إنشاء نسخة احتياطية الآن إلى السحابة؟\n" +
                        "سيتم تشفيرها بكلمة المرور التي اخترتها."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(
                R.mipmap.ic_launcher,
                "✅ نسخ الآن",
                approvePending
            )
            .addAction(
                R.mipmap.ic_launcher,
                "❌ لاحقاً",
                rejectPending
            )
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(PERMISSION_NOTIF_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * ⭐ إشعار النجاح.
     */
    fun showSuccessNotification() {
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("✅ تم النسخ الاحتياطي")
            .setContentText("تم إنشاء نسخة احتياطية سحابية بنجاح.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(SUCCESS_NOTIF_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelPermissionNotification() {
        NotificationManagerCompat.from(context).cancel(PERMISSION_NOTIF_ID)
    }
}