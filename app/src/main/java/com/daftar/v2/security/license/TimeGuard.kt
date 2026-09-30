package com.daftar.v2.security.license

import android.os.SystemClock
import com.daftar.v2.data.db.dao.LicenseDao
import com.daftar.v2.data.entity.TimeGuardEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يحمي من تغيير تاريخ الجهاز.
 *
 * يعمل بطريقتين:
 * 1. يقارن الوقت الحالي بآخر وقت معروف.
 * 2. يستخدم elapsedRealtime (لا يتأثر بتغيير تاريخ الجهاز).
 */
@Singleton
class TimeGuard @Inject constructor(
    private val licenseDao: LicenseDao
) {

    companion object {
        // فارق مقبول = 5 دقائق
        private const val TOLERANCE_MS = 5L * 60L * 1000L
    }

    /**
     * يعيد true إذا كان الوقت سليمًا، false إذا اكتُشف تلاعب.
     */
    suspend fun checkTime(): Boolean {
        val now = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()

        val guard = licenseDao.getTimeGuard()

        if (guard == null) {
            // أول تشغيل
            licenseDao.insertTimeGuard(
                TimeGuardEntity(
                    id = 1,
                    lastKnownTime = now,
                    lastElapsedRealtime = nowElapsed,
                    lastWallClock = now
                )
            )
            return true
        }

        // الفحص 1: التاريخ الحالي أقل من آخر وقت معروف؟
        if (now < guard.lastKnownTime - TOLERANCE_MS) {
            return false
        }

        // الفحص 2: elapsedRealtime تقدم أكثر من wall clock؟
        val elapsedDiff = nowElapsed - guard.lastElapsedRealtime
        val wallDiff = now - guard.lastWallClock
        if (wallDiff < elapsedDiff - TOLERANCE_MS) {
            return false
        }

        // تحديث
        licenseDao.updateTimeGuard(
            guard.copy(
                lastKnownTime = now,
                lastElapsedRealtime = nowElapsed,
                lastWallClock = now
            )
        )

        return true
    }

    /**
     * يعيد الوقت الحالي للاستخدام في الفحوصات.
     */
    fun currentTimeSeconds(): Long = System.currentTimeMillis() / 1000L
}