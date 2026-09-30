package com.daftar.v2

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.daftar.v2.backup.AutoBackupScheduler
import com.daftar.v2.cloud.CloudBackupScheduler
import com.daftar.v2.notification.CloudBackupNotificationHelper
import com.daftar.v2.notification.LicenseReminderScheduler
import com.daftar.v2.notification.NotificationHelper
import com.daftar.v2.notification.ReminderScheduler
import com.daftar.v2.security.SecurityChecks
import com.daftar.v2.work.RecurringTransactionScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DaftarApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject lateinit var autoBackupScheduler: AutoBackupScheduler
    @Inject lateinit var licenseReminderScheduler: LicenseReminderScheduler
    @Inject lateinit var recurringTransactionScheduler: RecurringTransactionScheduler
    @Inject lateinit var cloudBackupScheduler: CloudBackupScheduler
    @Inject lateinit var cloudBackupNotificationHelper: CloudBackupNotificationHelper

    /**
     * ⭐ حالة الجهاز (للاستخدام في الأمان).
     */
    var deviceSafetyChecked: Boolean = false
        private set
    var isDeviceRooted: Boolean = false
        private set
    var isDeviceEmulator: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()

        // ⭐ فحص أمني عند بدء التطبيق
        performSecurityChecks()

        // قنوات الإشعارات
        notificationHelper.ensureChannel()
        cloudBackupNotificationHelper.ensureChannel()

        // المهام المجدولة
        reminderScheduler.schedule()
        autoBackupScheduler.schedule()
        licenseReminderScheduler.schedule()
        recurringTransactionScheduler.schedule()
        cloudBackupScheduler.schedule()
    }

    /**
     * ⭐ فحص أمني.
     */
    private fun performSecurityChecks() {
        try {
            isDeviceRooted = SecurityChecks.isRooted()
            isDeviceEmulator = SecurityChecks.isEmulator()

            if (isDeviceRooted) {
                Log.w("DaftarApplication", "🔓 الجهاز مخترق (Root)")
            }
            if (isDeviceEmulator) {
                Log.w("DaftarApplication", "💻 تشغيل على محاكي")
            }
            if (SecurityChecks.isDebuggerAttached()) {
                Log.w("DaftarApplication", "🐛 Debugger مرفق")
            }

            deviceSafetyChecked = true
        } catch (e: Exception) {
            Log.e("DaftarApplication", "فشل الفحص الأمني: ${e.message}", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}