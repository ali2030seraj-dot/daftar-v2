package com.daftar.v2.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daftar.v2.data.db.dao.RecurringTransactionDao
import com.daftar.v2.data.db.dao.TransactionDao
import com.daftar.v2.data.entity.RecurringInterval
import com.daftar.v2.data.entity.TransactionEntity
import com.daftar.v2.data.entity.TransactionType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recurringDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daftar_recurring_transactions"
        private const val TAG = "RecurringWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            val due = recurringDao.getDue(now)
            Log.d(TAG, "🔄 معاملات مستحقة: ${due.size}")

            for (item in due) {
                try {
                    // ⭐ سجّل المعاملة
                    transactionDao.insert(
                        TransactionEntity(
                            personId = item.personId,
                            type = item.type,
                            amountCents = item.amountCents,
                            statement = item.statement,
                            note = "[تلقائي] ${item.note}",
                            timestamp = now,
                            isVoided = false,
                            voicePath = null
                        )
                    )

                    // ⭐ احسب وقت التشغيل التالي
                    val next = calculateNextRun(item.interval, item.dayOfMonth)

                    recurringDao.update(
                        item.copy(
                            nextRunAt = next,
                            lastRunAt = now
                        )
                    )

                    Log.d(TAG, "✅ تم تنفيذ: ${item.statement}")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ فشل تنفيذ ${item.id}: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل Worker", e)
            Result.retry()
        }
    }

    private fun calculateNextRun(
        interval: RecurringInterval,
        dayOfMonth: Int
    ): Long {
        val cal = Calendar.getInstance()
        when (interval) {
            RecurringInterval.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
            RecurringInterval.MONTHLY -> {
                cal.add(Calendar.MONTH, 1)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(28))
            }
            RecurringInterval.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return cal.timeInMillis
    }
}