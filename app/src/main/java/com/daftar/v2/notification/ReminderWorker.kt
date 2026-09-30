package com.daftar.v2.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daftar.v2.data.ReminderPreferences
import com.daftar.v2.domain.reminder.ReminderChecker
import com.daftar.v2.util.MoneyFormatter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * ⭐ Worker يعمل يومياً — يفحص المدينين المتأخرين.
 * يستخدم ReminderChecker للفحص.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderPreferences: ReminderPreferences,
    private val checker: ReminderChecker,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. هل التنبيهات مفعّلة؟
            val enabled = reminderPreferences.enabled.first()
            if (!enabled) return Result.success()

            // 2. افحص المدينين
            val overdue = checker.findOverdueDebtors()
            if (overdue.isEmpty()) return Result.success()

            // 3. ابنِ الإشعار
            val count = overdue.size
            val title = if (count == 1) "💰 دين متأخر" else "💰 $count ديون متأخرة"

            // ⭐ عرض الأسماء + المبالغ (حتى 3)
            val details = overdue.take(3).joinToString("\n") { p ->
                "• ${p.name}: ${MoneyFormatter.formatShort(p.balanceCents)} " +
                        "(${p.daysOverdue} يوم)"
            }
            val moreText = if (count > 3) "\n+${count - 3} آخرون" else ""
            val message = "$details$moreText"

            // 4. أرسل الإشعار
            notificationHelper.showReminder(
                title = title,
                message = message,
                count = count
            )

            // 5. حدّث آخر إرسال
            reminderPreferences.setLastSent(System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}