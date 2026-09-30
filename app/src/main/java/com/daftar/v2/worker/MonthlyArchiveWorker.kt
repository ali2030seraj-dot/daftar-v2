package com.daftar.v2.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daftar.v2.R
import com.daftar.v2.data.repository.PersonRepository
import com.daftar.v2.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

@HiltWorker
class MonthlyArchiveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val personRepository: PersonRepository,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            when {
                // ⭐ قبل 3 أيام من نهاية الشهر → إرسال تنبيه
                dayOfMonth == lastDayOfMonth - 3 -> {
                    sendReminderNotification(
                        daysRemaining = 3
                    )
                    Result.success()
                }
                // ⭐ قبل يوم واحد من نهاية الشهر → تنبيه أخير
                dayOfMonth == lastDayOfMonth -> {
                    sendReminderNotification(
                        daysRemaining = 1
                    )
                    Result.success()
                }
                // ⭐ أول يوم من الشهر الجديد → أرشفة الحسابات المنتهية
                dayOfMonth == 1 -> {
                    archiveCompletedAccounts()
                    Result.success()
                }
                // ⭐ باقي الأيام → لا شيء
                else -> Result.success()
            }
        } catch (e: Exception) {
            android.util.Log.e("ARCHIVE_WORKER", "فشل التنفيذ", e)
            Result.retry()
        }
    }

    /**
     * ⭐ أرشفة الأشخاص الذين انتهى رصيدهم (يساوي صفر)
     *     مع الحفاظ على كل عملياتهم
     */
    private suspend fun archiveCompletedAccounts() {
        val activePeople = personRepository.getActivePeople()

        var archivedCount = 0
        activePeople.forEach { person ->
            // حساب رصيد الشخص
            val totalDebt = transactionRepository.totalDebtCents(person.id)
            val totalPayment = transactionRepository.totalPaymentCents(person.id)
            val balance = totalDebt - totalPayment

            // ⭐ إذا كان الرصيد صفر → أرشفة
            if (balance == 0L && (totalDebt > 0 || totalPayment > 0)) {
                personRepository.archive(person.id)
                archivedCount++
            }
        }

        // إرسال إشعار بنتيجة الأرشفة
        if (archivedCount > 0) {
            sendArchiveNotification(archivedCount)
        }
    }

    // ═══════════════════════════════════════════════════════════
    //              إشعارات
    // ═══════════════════════════════════════════════════════════

    private fun sendReminderNotification(daysRemaining: Int) {
        ensureNotificationChannel()

        val title = "📅 نهاية الشهر تقترب"
        val message = if (daysRemaining == 1) {
            "غداً يبدأ شهر جديد — راجع عملياتك قبل الأرشفة التلقائية"
        } else {
            "باقي $daysRemaining أيام على نهاية الشهر — راجع عملياتك"
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        manager.notify(NOTIF_REMINDER_ID, notification)
    }

    private fun sendArchiveNotification(count: Int) {
        ensureNotificationChannel()

        val title = "✅ تمت الأرشفة التلقائية"
        val message = "تم أرشفة $count حساب منتهي الرصيد"

        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        manager.notify(NOTIF_ARCHIVE_ID, notification)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "أرشفة شهرية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات نهاية الشهر والأرشفة التلقائية"
            }

            val manager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "monthly_archive_channel"
        const val NOTIF_REMINDER_ID = 1001
        const val NOTIF_ARCHIVE_ID = 1002

        const val WORK_NAME = "monthly_archive_work"
    }
}