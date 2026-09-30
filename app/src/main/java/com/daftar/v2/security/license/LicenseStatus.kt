package com.daftar.v2.security.license

/**
 * حالة الترخيص الحالية.
 */
sealed class LicenseStatus {

    /** لم يُفعَّل بعد، لكن الفترة التجريبية متاحة */
    data class Trial(
        val daysRemaining: Long,
        val expiresAt: Long
    ) : LicenseStatus()

    /** لم يُفعَّل بعد، والفترة التجريبية انتهت */
    object TrialExpired : LicenseStatus()

    /** ترخيص نشط، أيام متبقية أكثر من 7 */
    data class Active(
        val plan: LicensePlan,
        val daysRemaining: Long,
        val expiresAt: Long
    ) : LicenseStatus()

    /** ترخيص نشط، أيام متبقية 7 أو أقل */
    data class ExpiringSoon(
        val plan: LicensePlan,
        val daysRemaining: Long,
        val expiresAt: Long
    ) : LicenseStatus()

    /** ترخيص منتهٍ، لكن فترة السماح 3 أيام */
    data class GracePeriod(
        val plan: LicensePlan,
        val daysRemaining: Long,
        val expiresAt: Long
    ) : LicenseStatus()

    /** ترخيص منتهٍ، انتهت فترة السماح */
    data class Expired(
        val plan: LicensePlan,
        val expiredAt: Long
    ) : LicenseStatus()

    /** ترخيص دائم */
    data class Permanent(
        val plan: LicensePlan
    ) : LicenseStatus()

    /** تم اكتشاف تلاعب في الوقت */
    object Tampered : LicenseStatus()

    /** خطأ في قراءة الترخيص */
    data class Error(val message: String) : LicenseStatus()

    /** هل التطبيق في وضع القراءة فقط؟ */
    val isReadOnly: Boolean
        get() = when (this) {
            is Expired, is TrialExpired, is Tampered -> true
            else -> false
        }

    /** هل التطبيق يعمل بكامل الميزات؟ */
    val isFullyFunctional: Boolean
        get() = !isReadOnly

    /** عدد الأيام المتبقية (للعرض) */
    val daysLeft: Long
        get() = when (this) {
            is Trial -> daysRemaining
            is Active -> daysRemaining
            is ExpiringSoon -> daysRemaining
            is GracePeriod -> daysRemaining
            is Expired -> 0L
            is Permanent -> -1L
            else -> 0L
        }
}