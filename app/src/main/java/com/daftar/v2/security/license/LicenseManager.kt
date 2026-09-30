package com.daftar.v2.security.license

import android.util.Log
import com.daftar.v2.data.entity.LicenseEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * العقل المدبر لنظام الترخيص.
 *
 * ⭐ الترتيب الصحيح للفحص:
 *   1. ترخيص نشط؟       → نعم: يعمل (يتجاوز كل شيء)
 *   2. تلاعب بالساعة؟    → نعم: Tampered
 *   3. تجربة نشطة؟      → نعم: Trial
 *   4. تجربة منتهية؟    → TrialExpired
 *
 * ⚠️ البيانات محفوظة دائماً — الحماية لا تحذف شيئاً.
 */
@Singleton
class LicenseManager @Inject constructor(
    private val repository: LicenseRepository,
    private val codec: LicenseCodec,
    private val validator: LicenseValidator,
    private val deviceIdProvider: DeviceIdProvider,
    private val trialManager: TrialManager,
    private val timeGuard: TimeGuard
) {

    companion object {
        private const val TAG = "LicenseManager"
        private const val DAY_SECONDS = 24L * 60L * 60L
        private const val GRACE_PERIOD_DAYS = 3L
        private const val WARNING_DAYS = 7L
    }

    // ═══════════════════════════════════════════════════════
    //              التفعيل بالكود
    // ═══════════════════════════════════════════════════════

    sealed class ActivationResult {
        data class Success(val license: LicenseEntity) : ActivationResult()
        data class Rejected(val reason: String) : ActivationResult()
        data class Error(val message: String) : ActivationResult()
    }

    suspend fun activate(rawCode: String): ActivationResult {
        Log.d(TAG, "🔑 بدء التفعيل")

        // 1. فك الكود
        val payload = try {
            codec.decode(rawCode)
        } catch (e: Exception) {
            Log.e(TAG, "❌ فك الكود فشل: ${e.message}")
            return ActivationResult.Rejected("الكود غير صالح: ${e.message}")
        }

        // 2. التحقق من التوقيع (Ed25519)
        when (val outcome = validator.verify(payload)) {
            is ValidationOutcome.Valid -> { /* تابع */ }
            is ValidationOutcome.InvalidVersion -> {
                return ActivationResult.Rejected("إصدار الكود غير مدعوم")
            }
            is ValidationOutcome.InvalidSignature -> {
                return ActivationResult.Rejected("توقيع الكود غير صحيح")
            }
        }

        // 3. التحقق من بصمة الجهاز
        val currentFp = deviceIdProvider.getDeviceFingerprint()
        if (!payload.deviceFingerprint.equals(currentFp, ignoreCase = true)) {
            Log.w(TAG, "❌ البصمة غير مطابقة")
            return ActivationResult.Rejected(
                "هذا الكود مخصص لجهاز آخر.\n" +
                "بصمة هذا الجهاز: ${deviceIdProvider.getFormattedFingerprint()}"
            )
        }

        // 4. هل الكود مستخدم من قبل؟
        val usedKey = payload.deviceFingerprint + ":" + payload.serial
        if (repository.isLicenseUsed(usedKey)) {
            Log.w(TAG, "❌ الكود مستخدم مسبقاً")
            return ActivationResult.Rejected("هذا الكود مُستخدم مسبقًا على هذا الجهاز")
        }

        // 5. حفظ الترخيص
        return try {
            repository.getActiveLicense()?.let {
                repository.deactivateLicense(it.id)
            }

            val entity = LicenseEntity(
                licenseId = "L-${payload.serial}",
                licenseCode = rawCode.trim(),
                plan = payload.plan.name,
                issuedAt = payload.issuedAt,
                expiresAt = payload.expiresAt,
                deviceFingerprint = payload.deviceFingerprint,
                signature = payload.signature,
                payload = payload.rawPayload
            )

            val newId = repository.saveLicense(entity)

            repository.markLicenseUsed(
                licenseId = usedKey,
                deviceFingerprint = payload.deviceFingerprint
            )

            // ⭐⭐⭐ الإصلاح 1: مسح علامة إعادة التثبيت
            try {
                trialManager.clearResetFlag()
                Log.d(TAG, "✅ تم مسح علامة إعادة التثبيت")
            } catch (e: Exception) {
                Log.w(TAG, "تعذّر مسح العلامة: ${e.message}")
            }

            Log.d(TAG, "✅ تم التفعيل بنجاح: ${entity.licenseId} (${entity.plan})")
            ActivationResult.Success(entity.copy(id = newId))

        } catch (e: Exception) {
            Log.e(TAG, "❌ فشل حفظ الترخيص: ${e.message}", e)
            ActivationResult.Error(e.message ?: "فشل حفظ الترخيص")
        }
    }

    // ═══════════════════════════════════════════════════════
    //              ⭐⭐⭐ حساب الحالة (الترتيب الجديد)
    // ═══════════════════════════════════════════════════════

    suspend fun getCurrentStatus(): LicenseStatus {

        // ⭐⭐⭐ 1. أولاً: هل يوجد ترخيص نشط؟
        val license = repository.getActiveLicense()
        if (license != null) {
            Log.d(TAG, "✅ ترخيص نشط (${license.plan}) — تجاوز فحص الوقت")
            return computeLicenseStatus(license)
        }

        // ⭐ 2. لا ترخيص → فحص الوقت
        if (!timeGuard.checkTime()) {
            Log.w(TAG, "⚠️ كشف تلاعب بالساعة — لا يوجد ترخيص نشط")
            return LicenseStatus.Tampered
        }

        // ⭐ 3. فحص التجربة
        trialManager.startTrialIfNeeded()
        val trialEnd = trialManager.getTrialEnd()
            ?: return LicenseStatus.Error("لم تبدأ الفترة التجريبية")

        val now = System.currentTimeMillis() / 1000L
        val trialEndSec = trialEnd / 1000L

        return if (now < trialEndSec) {
            val daysRemaining = (trialEndSec - now) / DAY_SECONDS
            Log.d(TAG, "🎁 تجربة نشطة: $daysRemaining يوم متبقي")
            LicenseStatus.Trial(
                daysRemaining = daysRemaining,
                expiresAt = trialEndSec
            )
        } else {
            Log.d(TAG, "⏰ التجربة منتهية")
            LicenseStatus.TrialExpired
        }
    }

    // ═══════════════════════════════════════════════════════
    //              حساب حالة الترخيص
    // ═══════════════════════════════════════════════════════

    private fun computeLicenseStatus(license: LicenseEntity): LicenseStatus {
        val plan = try {
            LicensePlan.valueOf(license.plan)
        } catch (e: Exception) {
            LicensePlan.M30
        }

        // ⭐ ترخيص دائم (LIFETIME)
        if (plan == LicensePlan.LIFETIME || license.expiresAt == 0L) {
            return LicenseStatus.Permanent(plan)
        }

        val now = System.currentTimeMillis() / 1000L
        val remainingSeconds = license.expiresAt - now

        return when {
            // منتهي
            remainingSeconds <= 0L -> {
                val daysSinceExpiry = (-remainingSeconds) / DAY_SECONDS
                if (daysSinceExpiry < GRACE_PERIOD_DAYS) {
                    LicenseStatus.GracePeriod(
                        plan = plan,
                        daysRemaining = GRACE_PERIOD_DAYS - daysSinceExpiry,
                        expiresAt = license.expiresAt
                    )
                } else {
                    LicenseStatus.Expired(
                        plan = plan,
                        expiredAt = license.expiresAt
                    )
                }
            }

            // قريب الانتهاء (7 أيام)
            remainingSeconds / DAY_SECONDS <= WARNING_DAYS -> {
                LicenseStatus.ExpiringSoon(
                    plan = plan,
                    daysRemaining = remainingSeconds / DAY_SECONDS,
                    expiresAt = license.expiresAt
                )
            }

            // نشط (M30 / M90 / M365)
            else -> {
                LicenseStatus.Active(
                    plan = plan,
                    daysRemaining = remainingSeconds / DAY_SECONDS,
                    expiresAt = license.expiresAt
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //              أدوات مساعدة
    // ═══════════════════════════════════════════════════════

    fun getCurrentDeviceFingerprint(): String =
        deviceIdProvider.getFormattedFingerprint()

    suspend fun resetAll() {
        repository.deleteAll()
    }
}