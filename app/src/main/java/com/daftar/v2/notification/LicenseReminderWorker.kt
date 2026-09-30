package com.daftar.v2.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daftar.v2.security.license.LicenseManager
import com.daftar.v2.security.license.LicenseStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * ⭐ Worker يعمل يومياً للتحقق من حالة الترخيص.
 * - إذا كانت الفترة التجريبية تنتهي خلال 5 أيام → إشعار
 * - إذا انتهت → إشعار عاجل
 */
@HiltWorker
class LicenseReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val licenseManager: LicenseManager,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        private const val WARNING_DAYS = 5L   // ⭐ يبدأ التنبيه قبل 5 أيام
    }

    override suspend fun doWork(): Result {
        return try {
            val status = licenseManager.getCurrentStatus()

            when (status) {
                // ⭐ فترة تجريبية — تنبيه قبل الانتهاء
                is LicenseStatus.Trial -> {
                    if (status.daysRemaining <= WARNING_DAYS) {
                        notificationHelper.showLicenseReminder(
                            daysRemaining = status.daysRemaining,
                            isExpired = false
                        )
                    }
                }

                // ⭐ ترخيص مدفوع — تنبيه قبل الانتهاء
                is LicenseStatus.ExpiringSoon -> {
                    if (status.daysRemaining <= WARNING_DAYS) {
                        notificationHelper.showLicenseReminder(
                            daysRemaining = status.daysRemaining,
                            isExpired = false
                        )
                    }
                }

                // ⭐ فترة السماح — تنبيه عاجل
                is LicenseStatus.GracePeriod -> {
                    notificationHelper.showLicenseReminder(
                        daysRemaining = 0,
                        isExpired = true,
                        isGracePeriod = true
                    )
                }

                // ⭐ انتهى — تنبيه أخير
                is LicenseStatus.TrialExpired -> {
                    notificationHelper.showLicenseReminder(
                        daysRemaining = 0,
                        isExpired = true
                    )
                }

                is LicenseStatus.Expired -> {
                    notificationHelper.showLicenseReminder(
                        daysRemaining = 0,
                        isExpired = true
                    )
                }

                // لا إشعار
                else -> Unit
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}