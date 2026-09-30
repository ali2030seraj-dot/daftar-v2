package com.daftar.v2.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daftar.v2.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // ═══════════════════════════════════════════════════════════
        //              قنوات الإشعارات
        // ═══════════════════════════════════════════════════════════

        // قناة الديون المتأخرة
        const val CHANNEL_ID = "daftar_reminders"
        const val CHANNEL_NAME = "تنبيهات الديون"
        const val NOTIFICATION_ID = 1001

        // قناة الترخيص
        const val LICENSE_CHANNEL_ID = "daftar_license"
        const val LICENSE_CHANNEL_NAME = "تنبيهات الترخيص"
        const val LICENSE_NOTIFICATION_ID = 1002

        // رقم واتساب المطور
        private const val DEVELOPER_WHATSAPP = "967775670090"
    }

    /**
     * ⭐ ينشئ كل قنوات الإشعارات عند الإقلاع.
     * - قناة الديون المتأخرة (DEFAULT)
     * - قناة الترخيص (HIGH)
     */
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            // ─── قناة الديون المتأخرة ───
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val debtChannel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "تنبيهات بالأشخاص الذين تأخر سدادهم"
                }
                manager.createNotificationChannel(debtChannel)
            }

            // ─── قناة الترخيص ───
            if (manager.getNotificationChannel(LICENSE_CHANNEL_ID) == null) {
                val licenseChannel = NotificationChannel(
                    LICENSE_CHANNEL_ID,
                    LICENSE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات قرب انتهاء الترخيص أو الفترة التجريبية"
                }
                manager.createNotificationChannel(licenseChannel)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //              ⭐ إشعارات الديون المتأخرة (مُحسَّنة)
    // ═══════════════════════════════════════════════════════════

    /**
     * يعرض إشعار الديون المتأخرة.
     *
     * ⭐ التحديثات الجديدة:
     * - يعرض قائمة بالأسماء + المبالغ
     * - يدعم أسطر متعددة (BigTextStyle)
     * - العنوان يعرض عدد المتأخرين
     *
     * @param title   العنوان (مثل: "💰 3 ديون متأخرة")
     * @param message الرسالة الكاملة (مع أسماء ومبالغ)
     * @param count   عدد المتأخرين (للعرض في الرقم)
     */
    fun showReminder(
        title: String,
        message: String,
        count: Int
    ) {
        ensureChannel()

        // ─── النقر على الإشعار → يفتح التطبيق ───
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ─── السطر الأول المختصر ───
        val shortText = message.lineSequence().firstOrNull()?.trim() ?: message

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(shortText)
            // ⭐⭐⭐ BigTextStyle يعرض كل الأسطر عند توسيع الإشعار
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setNumber(count)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // ═══════════════════════════════════════════════════════════
    //              ⭐ إشعارات الترخيص (موجودة سابقاً)
    // ═══════════════════════════════════════════════════════════

    /**
     * يعرض إشعار انتهاء الترخيص أو قرب انتهائه.
     *
     * @param daysRemaining الأيام المتبقية
     * @param isExpired هل انتهى فعلاً؟
     * @param isGracePeriod هل في فترة السماح؟
     */
    fun showLicenseReminder(
        daysRemaining: Long,
        isExpired: Boolean,
        isGracePeriod: Boolean = false
    ) {
        ensureChannel()

        // ─── بناء الرسالة ───
        val title: String
        val message: String

        when {
            isGracePeriod -> {
                title = "⚠️ انتهى الترخيص — فترة السماح"
                message = "انتهى ترخيصك. أنت في فترة السماح (3 أيام).\n" +
                        "تواصل مع المطور لتفعيل كود جديد."
            }
            isExpired -> {
                title = "🔒 انتهى الترخيص"
                message = "انتهت فترة الترخيص.\n" +
                        "التطبيق الآن في وضع القراءة فقط.\n" +
                        "تواصل مع المطور للتفعيل."
            }
            daysRemaining == 1L -> {
                title = "⏰ آخر يوم!"
                message = "ينتهي ترخيصك غداً.\n" +
                        "تواصل مع المطور الآن لتفعيل كود جديد."
            }
            else -> {
                title = "⏰ ترخيصك ينتهي قريباً"
                message = "متبقي $daysRemaining أيام فقط.\n" +
                        "تواصل مع المطور لتفعيل كود جديد."
            }
        }

        // ─── النقر على الإشعار → يفتح التطبيق ───
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_activation", true)
        }
        val pending = PendingIntent.getActivity(
            context,
            LICENSE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ─── زر "تواصل مع المطور" (واتساب) ───
        val whatsappUrl = "https://wa.me/$DEVELOPER_WHATSAPP?text=" +
                Uri.encode("مرحباً، أريد تفعيل تطبيق دفتر حساباتي.")
        val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val whatsappPending = PendingIntent.getActivity(
            context,
            LICENSE_NOTIFICATION_ID + 1,
            whatsappIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, LICENSE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(title)
            .setContentText(message.lineSequence().firstOrNull() ?: message)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            )
            .setPriority(
                if (isExpired || daysRemaining <= 2)
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .addAction(
                android.R.drawable.ic_menu_call,
                "تواصل مع المطور",
                whatsappPending
            )
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(LICENSE_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // ═══════════════════════════════════════════════════════════
    //              إلغاء الإشعارات
    // ═══════════════════════════════════════════════════════════

    fun cancelAll() {
        NotificationManagerCompat.from(context).apply {
            cancel(NOTIFICATION_ID)
            cancel(LICENSE_NOTIFICATION_ID)
        }
    }
}