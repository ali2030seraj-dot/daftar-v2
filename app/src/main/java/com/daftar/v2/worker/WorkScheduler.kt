package com.daftar.v2.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkScheduler {

    /**
     * ⭐ جدولة فحص يومي — يتحقق من اليوم الحالي:
     *    - قبل 3 أيام من نهاية الشهر → تنبيه
     *    - قبل يوم من نهاية الشهر → تنبيه
     *    - أول يوم من الشهر → أرشفة
     */
    fun scheduleMonthlyArchive(context: Context) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 9)   // 9 صباحاً
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val delay = next.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<MonthlyArchiveWorker>(
            1, TimeUnit.DAYS,
            15, TimeUnit.MINUTES
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MonthlyArchiveWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}